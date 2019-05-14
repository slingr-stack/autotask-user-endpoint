package io.slingr.endpoints.autotaskuser.polling;

import io.slingr.endpoints.autotaskuser.ws.*;
import io.slingr.endpoints.services.datastores.DataStore;
import io.slingr.endpoints.services.datastores.DataStoreResponse;
import io.slingr.endpoints.utils.Json;
import org.apache.log4j.Logger;

import javax.xml.soap.SOAPException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PollingService {
    private static final Logger logger = Logger.getLogger(PollingService.class);

    private int frequencyInMinutes;
    private List<EntityType> entitiesToPoll;
    private EventSender eventSender;
    private AutotaskApi autotaskApi;
    private DataStore users;
    private LockService lockService;

    public PollingService(int frequencyInMinutes, List<EntityType> entitiesToPoll, EventSender eventSender, AutotaskApi autotaskApi, DataStore users, LockService lockService) {
        this.frequencyInMinutes = frequencyInMinutes;
        this.entitiesToPoll = entitiesToPoll;
        this.eventSender = eventSender;
        this.autotaskApi = autotaskApi;
        this.users = users;
        this.lockService = lockService;
    }

    public void run() {
        logger.info(String.format("Initializing polling service to run every [%s] minutes", frequencyInMinutes));

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable periodicTask = () -> {
            try {
                logger.info("Polling entities");
                Date newLastPolling = null;
                if (entitiesToPoll != null) {
                    // TODO we need to fix this to scale because we cannot get more than 1,000 records
                    DataStoreResponse responseConfigs = users.find();
                    List<Json> configs = responseConfigs.getItems();
                    logger.info(String.format("[%s] user configurations were found to poll entities", configs.size()));
                    ApiCredentials credentials = new ApiCredentials();
                    for (Json config : configs) {
                        String configId = config.string("_id");
                        if (lockService.lock("polling-"+configId)) {
                            try {
                                credentials.fromJson(config);
                                logger.info(String.format("Polling entities for account [%s - %s]", credentials.getUsername(), configId));
                                for (EntityType entityType : entitiesToPoll) {
                                    // first we check if the last time we poll is too close, which means another
                                    // instance of the endpoint already did the polling
                                    // if the time elapses is smaller than 50% of the interval, we skip it
                                    Date lastPollingDate = entityType.getLastPolling(configId);
                                    if (lastPollingDate != null &&
                                            (System.currentTimeMillis() - lastPollingDate.getTime()) < (long) (frequencyInMinutes * 60 * 1000 * 0.5)) {
                                        continue;
                                    }
                                    String queryField = null;
                                    if (entityType.getModifiedField() != null) {
                                        queryField = entityType.getModifiedField();
                                    } else if (entityType.getCreateField() != null) {
                                        queryField = entityType.getCreateField();
                                    } else {
                                        logger.warn(String.format("Changes cannot be detected on entity [%s]", entityType.getName()));
                                    }
                                    if (queryField != null) {
                                        boolean moreRecords;
                                        newLastPolling = new Date();
                                        Long lastId = entityType.getLastPollingId(configId);
                                        do {
                                            QueryBuilder queryBuilder = new QueryBuilder(credentials, entityType);
                                            queryBuilder.addFilter(queryField, false, "GreaterThan", DateHelper.convertToDateTime(entityType.getEffectiveLastPolling(configId)));
                                            if (lastId != null) {
                                                queryBuilder.addFilter("id", false, "GreaterThan", lastId.toString());
                                            }
                                            List<Entity> entities = (List<Entity>) autotaskApi.query(credentials, queryBuilder);
                                            for (Entity entity : entities) {
                                                Json event = Json.map();
                                                event.set("entityType", entityType.getName());
                                                event.set("record", entity.toJson());
                                                eventSender.sendEvent("recordChange", event, configId);
                                            }
                                            moreRecords = entities.size() >= 500;
                                            lastId = entities.isEmpty() ? lastId : entities.get(entities.size() - 1).getId();
                                        } while (moreRecords);
                                        entityType.setLastPolling(configId, newLastPolling);
                                        if (entityType.getModifiedField() == null && entityType.getCreateField() != null) {
                                            // we only have to set this when polling by create time due to limitations (create dates are truncated)
                                            entityType.setLastPollingId(configId, lastId);
                                        }
                                    }
                                }
                                logger.info(String.format("Done polling entities for account [%s]", credentials.getUsername()));
                            } catch (SOAPException e) {
                                logger.error("Error when querying Autotask to detect changes", e);
                            } catch (Exception e) {
                                logger.error("Error polling Autotaks to detect changes", e);
                            } finally {
                                lockService.unlock("polling-"+configId);
                            }
                        }
                    }
                }
                logger.info("Done polling entities");
            } catch (Exception e) {
                logger.error("Error polling Autotaks to detect changes", e);
            }
        };

        executor.scheduleAtFixedRate(periodicTask, 1, frequencyInMinutes, TimeUnit.MINUTES);
    }
}
