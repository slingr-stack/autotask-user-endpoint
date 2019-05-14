package io.slingr.endpoints.autotaskuser;

import io.slingr.endpoints.PerUserEndpoint;
import io.slingr.endpoints.autotaskuser.polling.LockService;
import io.slingr.endpoints.autotaskuser.polling.PollingService;
import io.slingr.endpoints.autotaskuser.ws.*;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.framework.annotations.ApplicationLogger;
import io.slingr.endpoints.framework.annotations.EndpointFunction;
import io.slingr.endpoints.framework.annotations.EndpointProperty;
import io.slingr.endpoints.framework.annotations.SlingrEndpoint;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.exchange.ReservedName;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.soap.SOAPException;
import java.util.ArrayList;
import java.util.List;

@SlingrEndpoint(name = "autotask-user")
public class AutotaskUserEndpoint extends PerUserEndpoint {
    private static final Logger logger = Logger.getLogger(AutotaskUserEndpoint.class);

    @ApplicationLogger
    private AppLogs appLogger;

    @EndpointProperty
    private String integrationCode;

    @EndpointProperty
    private String pollingEnabled;

    @EndpointProperty
    private String pollingFrequency;

    @EndpointProperty
    private String entitiesToPoll;

    private AutotaskApi autotaskApi;

    private PollingService pollingService;

    public AutotaskUserEndpoint() {
    }

    @Override
    public void endpointStarted() {
        try {
            autotaskApi = new AutotaskApi();
            for (EntityType entityType : EntityType.values()) {
                entityType.setAutotaskApi(autotaskApi);
            }
            logger.info(String.format("Polling is [%s]", pollingEnabled));
			if ("enable".equals(pollingEnabled)) {
			    logger.info(String.format("Polling frequency is [%s] minutes", pollingFrequency));
                logger.info(String.format("Entities to poll are [%s]", entitiesToPoll));
                List<EntityType> entityTypesToPoll = new ArrayList<>();
                for (String entityToPoll : StringUtils.split(entitiesToPoll, ",")) {
                    EntityType entityType = EntityType.getEntityTypeByName(entityToPoll.trim());
                    entityTypesToPoll.add(entityType);
                }
                pollingService = new PollingService(Integer.valueOf(pollingFrequency), entityTypesToPoll,
                        (eventName, data, id) -> this.events().send(eventName, data, null, id),
                        autotaskApi, userDataStore(), new LockService() {
                    @Override
                    public boolean lock(String key) {
                        return AutotaskUserEndpoint.this.locks().lock(key);
                    }

                    @Override
                    public boolean unlock(String key) {
                        return AutotaskUserEndpoint.this.locks().unlock(key);
                    }
                });
                pollingService.run();
            }
		} catch (SOAPException e) {
            appLogger.error("There was a problem configuring the Autotask API. Please check credentials.", e);
        }
    }

    @EndpointFunction(name = ReservedName.CONNECT_USER)
    public Json connectUser(FunctionRequest request) {
        final String userId = request.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            final Json jsonBody = request.getJsonParams();
            String username = jsonBody.string("username");
            String password = jsonBody.string("password");
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                logger.warn(String.format("Username and password a required. User [%s] won't be connected.", userId));
                appLogger.warn(String.format("Username and password a required. User [%s] won't be connected.", userId));
                defaultMethodDisconnectUsers(request);
                return Json.map();
            }
            ApiCredentials credentials = new ApiCredentials();
            credentials.setUsername(username);
            credentials.setPassword(password);
            credentials.setIntegrationCode(integrationCode);
            try {
                credentials.setZoneUrl(autotaskApi.getZoneUrl(credentials));
                credentials.setWebUrl(autotaskApi.getWebUrl(credentials));
            } catch (SOAPException e) {
                logger.warn(String.format("Error while getting default URLs for user Autotask [%s] for Slingr user [%s]", username, userId), e);
                appLogger.warn(String.format("Error while getting default URLs for user [%s]. Check it is a valid username.", username));
                defaultMethodDisconnectUsers(request);
                return Json.map();
            }
            // check credentials are valid
            try {
                QueryBuilder testQuery = new QueryBuilder(credentials, EntityType.COUNTRY);
                autotaskApi.query(credentials, testQuery);
            } catch (SOAPException e) {
                logger.warn(String.format("Error when doing a test query to validate credentials for Autotask user [%s] and Slingr user [%s]", username, userId), e);
                appLogger.warn(String.format("Error when doing a test query to validate credentials for user [%s]: %s", username, e.getMessage()));
                defaultMethodDisconnectUsers(request);
                return Json.map();
            }
            // saves the information on the users data store
            Json conf = users().save(userId, credentials.toJson());
            logger.info(String.format("User [%s] connected to Autotask account [%s]", userId, username));

            // sends connected user event
            users().sendUserConnectedEvent(request.getFunctionId(), userId, conf);

            return conf;
        }
        defaultMethodDisconnectUsers(request);
        return Json.map();
    }

    @EndpointFunction(name = "_query")
    public Json query(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _query, params: " + params.toString());
        ApiCredentials credentials = getUserCredentials(request);
        EntityType entityType = EntityType.getEntityTypeByName(params.string("entity"));
        QueryBuilder queryBuilder = new QueryBuilder(credentials, entityType);
        try {
            if (params.contains("filters") && !params.isEmpty("filters")) {
                for (Json filter : params.jsons("filters")) {
                    queryBuilder.addFilter(filter.string("field"), filter.bool("udf"), filter.string("op"), filter.string("value"));
                }
            }
        } catch (Exception e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("Error parsing query [%s]", params.toString()), e);
        }
        List<Entity> entities;
        try {
            entities = (List<Entity>) autotaskApi.query(credentials, queryBuilder);
        } catch (SOAPException e) {
            throw EndpointException.permanent(ErrorCode.API, String.format("Error executing query [%s]", queryBuilder.getXML()), e);
        }
        Json result = Json.list();
        entities.stream().forEach(entity -> result.push(entity.toJson()));
        return result;
    }

    @EndpointFunction(name = "_create")
    public Json create(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _create, params: " + params.toString());
    	Long createdId = null;
        ApiCredentials credentials = getUserCredentials(request);
        EntityType entityType;
        Entity instance;
    	try {
            entityType = EntityType.getEntityTypeByName(params.string("entity"));
            instance = entityType.newInstance(credentials);
            instance.fromJson(params);
        } catch (IllegalArgumentException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error parsing the entity", e);
        }
        try {
            createdId = autotaskApi.create(credentials, instance);
        } catch (SOAPException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error creating the entity", e);
        } catch (AutotaskException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error creating the entity", e);
        }
    	Json result = Json.map().set("id", createdId);
    	return result;
    }

    @EndpointFunction(name = "_update")
    public Json update(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _update, params: " + params.toString());
    	Long updatedId = null;
        ApiCredentials credentials = getUserCredentials(request);
        EntityType entityType;
        Entity instance;
    	try {
            entityType = EntityType.getEntityTypeByName(params.string("entity"));
            instance = entityType.newInstance(credentials);
            instance.fromJson(params);
        } catch (IllegalArgumentException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error parsing the entity", e);
        }
    	try {
	    	updatedId = autotaskApi.update(credentials, instance);
    	} catch (SOAPException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error updating the entity", e);
    	} catch (AutotaskException e) {
    		throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error updating the entity", e);
    	}
		Json result = Json.map().set("id", updatedId);
		return result;
    }

	@EndpointFunction(name = "_delete")
	public Json delete(FunctionRequest request) {
        Json params = request.getJsonParams();
		logger.info("Calling _delete, params: " + params.toString());
        ApiCredentials credentials = getUserCredentials(request);
        EntityType entityType;
        Entity instance;
        try {
            entityType = EntityType.getEntityTypeByName(params.string("entity"));
            instance = entityType.newInstance(credentials);
            instance.fromJson(params);
        } catch (IllegalArgumentException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error parsing the entity", e);
        }
        try {
            autotaskApi.delete(credentials, instance);
        } catch (SOAPException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error deleting the entity", e);
        } catch (AutotaskException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "There was an error deleting the entity", e);
        }
        Json result = Json.map().set("id", instance.getId());
        return result;
	}

	@EndpointFunction(name = "_getEntity")
    public Json getEntity(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _getEntityFields, params: " + params.toString());
        EntityType entityType;
        ApiCredentials credentials = getUserCredentials(request);
        try {
            entityType = EntityType.getEntityTypeByName(params.string("entity"));
        } catch (IllegalArgumentException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Invalid entity name", e);
        }
        return entityType.getCleanInfo(credentials).toJson();
    }

    @EndpointFunction(name = "_getEntityFields")
    public Json getEntityFields(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _getEntityFields, params: " + params.toString());
        EntityType entityType;
        ApiCredentials credentials = getUserCredentials(request);
        try {
            entityType = EntityType.getEntityTypeByName(params.string("entity"));
        } catch (IllegalArgumentException e) {
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Invalid entity name", e);
        }
        List<EntityFieldInfo> fieldInfoList = entityType.getCleanEntityFieldsInfo(credentials);
        return Json.list(fieldInfoList, fieldInfo -> fieldInfo.toJson());
    }

    @EndpointFunction(name = "_getWebUrl")
    public Json getWebUrl(FunctionRequest request) {
        Json params = request.getJsonParams();
        logger.info("Calling _getWebUrl, params: " + params.toString());
        ApiCredentials credentials = getUserCredentials(request);
        return Json.map().set("webUrl", credentials.getWebUrl());
    }

    private ApiCredentials getUserCredentials(FunctionRequest request) {
        Json userConfig = users().findById(request.getUserId());
        if (userConfig == null) {
            throw EndpointException.permanent(ErrorCode.CLIENT, String.format("User [%s] is not connected", request.getUserEmail()));
        }
        ApiCredentials credentials = new ApiCredentials();
        credentials.fromJson(userConfig);
        if (StringUtils.isBlank(credentials.getZoneUrl()) || StringUtils.isBlank(credentials.getWebUrl()) ||
                !StringUtils.equals(credentials.getIntegrationCode(), integrationCode)) {
            // the integration code might have changed
            credentials.setIntegrationCode(integrationCode);
            // this might happen when credentials has been set externally
            try {
                credentials.setZoneUrl(autotaskApi.getZoneUrl(credentials));
                credentials.setWebUrl(autotaskApi.getWebUrl(credentials));
            } catch (SOAPException e) {
                throw EndpointException.permanent(ErrorCode.ARGUMENT, "Problem validating credentials");
            }
            users().save(request.getUserId(), credentials.toJson());
        }
        return credentials;
    }
}
