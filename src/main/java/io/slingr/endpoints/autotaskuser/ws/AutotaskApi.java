package io.slingr.endpoints.autotaskuser.ws;

import io.slingr.endpoints.utils.Base64Utils;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.CDATASection;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AutotaskApi {
    private static final Logger logger = Logger.getLogger(AutotaskApi.class);

    private final static String AUTOTASK_NAMESPACE = "http://autotask.net/ATWS/v1_5/";
    private final static String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private final static String XSD_NAMESPACE= "http://www.w3.org/2001/XMLSchema";
    private final static String DEFAULT_ZONE_URL = "https://webservices.autotask.net/atservices/1.5/atws.asmx";
    private final static String DEFAULT_WEB_URL = "https://ww5.autotask.net/";
    private final static String SOAP_VERSION = "SOAP 1.2 Protocol";

    private MessageFactory messageFactory;
    private SOAPConnectionFactory soapConnectionFactory;
    private SOAPConnection soapConnection;

    public AutotaskApi() throws SOAPException {
        this.messageFactory = MessageFactory.newInstance(SOAP_VERSION);
        this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
        this.soapConnection = soapConnectionFactory.createConnection();
    }

    public String getZoneUrl(ApiCredentials credentials) throws SOAPException {
        try {
            String respXml = getZoneInfo(credentials);
			Pattern pattern = Pattern.compile("<URL>([^<]+)</URL>");
			Matcher matcher = pattern.matcher(respXml.toString());
			if(matcher.find()) {
				return matcher.group(1);
			}
		} catch (IOException e) {
            logger.error(String.format("Error getting zone URL for account [%s]", credentials.getUsername()));
        }
		return DEFAULT_ZONE_URL;
    }

    public String getWebUrl(ApiCredentials credentials) throws SOAPException {
        try {
            String respXml = getZoneInfo(credentials);
            Pattern pattern = Pattern.compile("<WebUrl>([^<]+)</WebUrl>");
            Matcher matcher = pattern.matcher(respXml.toString());
            if(matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException e) {
            logger.error(String.format("Error getting web URL for account [%s]", credentials.getUsername()));
        }
        return DEFAULT_WEB_URL;
    }

    public String getZoneInfo(ApiCredentials credentials) throws IOException {
        URLConnection connection = new URL(DEFAULT_ZONE_URL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", "http://autotask.net/ATWS/v1_5/getZoneInfo");
        connection.setRequestProperty("Accept", "text/xml");

        StringBuilder xmlOutput = new StringBuilder();
        xmlOutput.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xmlOutput.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://autotask.net/ATWS/v1_5/\"><SOAP-ENV:Body><ns1:getZoneInfo><ns1:UserName>");
        xmlOutput.append(credentials.getUsername());
        xmlOutput.append("</ns1:UserName></ns1:getZoneInfo></SOAP-ENV:Body></SOAP-ENV:Envelope>");

        OutputStream output = connection.getOutputStream();
        output.write(xmlOutput.toString().getBytes(Charset.forName("UTF-8")));

        InputStream response = connection.getInputStream();
        StringBuilder respXml = new StringBuilder();
        Reader in = new InputStreamReader(response, "UTF-8");
        char[] buffer = new char[4096];
        for(;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if(rsz < 0) break;
            respXml.append(buffer, 0, rsz);
        }

        return respXml.toString();
    }

    public List<EntityInfo> getEntityInfo(ApiCredentials credentials) throws SOAPException, AutotaskException {
        SOAPMessage soapMessage = createBasicMessage(credentials);
        SOAPBody soapBody = soapMessage.getSOAPBody();
        SOAPElement methodElement = soapBody.addChildElement("GetEntityInfo", "atns");
        logger.trace("Request [GetEntityInfo]:\n" + getSOAPMessageAsString(soapMessage));
        SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [GetEntityInfo]:\n" + getSOAPMessageAsString(soapResponse));
        checkResponseForErrors(soapResponse);

        NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("EntityInfo");
        List<EntityInfo> entityInfoList = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Node node = elements.item(i);
            EntityInfo entityInfo = new EntityInfo();
            entityInfo.fromXml(node);
            entityInfoList.add(entityInfo);
        }

        return entityInfoList;
    }

    public List<EntityFieldInfo> getFieldInfo(ApiCredentials credentials, EntityType entityType) throws SOAPException, AutotaskException {
        SOAPMessage soapMessage = createBasicMessage(credentials);
        SOAPBody soapBody = soapMessage.getSOAPBody();
        SOAPElement methodElement = soapBody.addChildElement("GetFieldInfo", "atns");
        SOAPElement nameElement = methodElement.addChildElement("psObjectType", "atns");
        nameElement.setTextContent(entityType.getName());
        logger.trace("Request [GetFieldInfo]:\n" + getSOAPMessageAsString(soapMessage));
        SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [GetFieldInfo]:\n" + getSOAPMessageAsString(soapResponse));
        checkResponseForErrors(soapResponse);

        NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("Field");
        List<EntityFieldInfo> entityFieldInfoList = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            try {
                org.w3c.dom.Node node = elements.item(i);
                EntityFieldInfo entityFieldInfo = new EntityFieldInfo();
                entityFieldInfo.setEntity(entityType);
                entityFieldInfo.fromXml(node);
                entityFieldInfoList.add(entityFieldInfo);
            } catch (Exception ex){
                logger.warn(String.format("Error when process field: %s", ex.getMessage()));
            }
        }
        return entityFieldInfoList;
    }

    public List<EntityFieldInfo> getUDFInfo(ApiCredentials credentials, EntityType entityType) throws SOAPException, AutotaskException {
        SOAPMessage soapMessage = createBasicMessage(credentials);
        SOAPBody soapBody = soapMessage.getSOAPBody();
        SOAPElement methodElement = soapBody.addChildElement("getUDFInfo", "atns");
        SOAPElement nameElement = methodElement.addChildElement("psTable", "atns");
        nameElement.setTextContent(entityType.getName());
        logger.trace("Request [getUDFInfo]:\n" + getSOAPMessageAsString(soapMessage));
        SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [getUDFInfo]:\n" + getSOAPMessageAsString(soapResponse));
        checkResponseForErrors(soapResponse);

        NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("Field");
        List<EntityFieldInfo> entityFieldInfoList = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            try {
                org.w3c.dom.Node node = elements.item(i);
                EntityFieldInfo entityFieldInfo = new EntityFieldInfo();
                entityFieldInfo.setEntity(entityType);
                entityFieldInfo.setUserDefinedField(true);
                entityFieldInfo.fromXml(node);
                entityFieldInfoList.add(entityFieldInfo);
            } catch (Exception ex){
                logger.warn(String.format("Error when process udf: %s", ex.getMessage()));
            }
        }
        return entityFieldInfoList;
    }

    public List<? extends Entity> query(ApiCredentials credentials, QueryBuilder queryBuilder) throws SOAPException {
        SOAPMessage soapMessage = createBasicMessage(credentials);
        SOAPBody soapBody = soapMessage.getSOAPBody();
        SOAPElement methodElement = soapBody.addChildElement("query", "atns");
        SOAPElement sxmlElement = methodElement.addChildElement("sXML", "atns");
        CDATASection query = soapMessage.getSOAPPart().createCDATASection(queryBuilder.getXML());
        sxmlElement.appendChild(query);
        logger.trace("Request [query]:\n" + getSOAPMessageAsString(soapMessage));
        SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [query]:\n" + getSOAPMessageAsString(soapResponse));
        NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("Entity");
        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Node node = elements.item(i);
            Entity entity = queryBuilder.getEntityType().newInstance(credentials, node);
            result.add(entity);
        }
        return result;
    }

    public Long create(ApiCredentials credentials, Entity toCreate) throws SOAPException, AutotaskException {
    	SOAPMessage soapMessage = createBasicMessage(credentials);
    	SOAPBody soapBody = soapMessage.getSOAPBody();
    	soapBody.addNamespaceDeclaration("xsi", XSI_NAMESPACE);
        soapBody.addNamespaceDeclaration("xsd", XSD_NAMESPACE);
    	SOAPElement methodElement = soapBody.addChildElement("create", "atns");
    	SOAPElement entitiesElement = methodElement.addChildElement("Entities", "atns");
    	toCreate.toXml(entitiesElement);
        logger.trace("Request [create]:\n" + getSOAPMessageAsString(soapMessage));
    	SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [create]:\n" + getSOAPMessageAsString(soapResponse));
    	checkResponseForErrors(soapResponse);
    	NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("id");
    	if (elements.getLength() > 0) {
    		org.w3c.dom.Node node = elements.item(0);
    		org.w3c.dom.Node txtNode = node.getFirstChild();
    		if (txtNode != null) {
    			return Long.parseLong(txtNode.getNodeValue());
			}
    	}
    	return null;
    }

    public Long update(ApiCredentials credentials, Entity toUpdate) throws SOAPException, AutotaskException {
    	SOAPMessage soapMessage = createBasicMessage(credentials);
    	SOAPBody soapBody = soapMessage.getSOAPBody();
    	soapBody.addNamespaceDeclaration("xsi", XSI_NAMESPACE);
        soapBody.addNamespaceDeclaration("xsd", XSD_NAMESPACE);
    	SOAPElement methodElement = soapBody.addChildElement("update", "atns");
    	SOAPElement entitiesElement = methodElement.addChildElement("Entities", "atns");
    	toUpdate.toXml(entitiesElement);
        logger.trace("Request [update]:\n" + getSOAPMessageAsString(soapMessage));
    	SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [update]:\n" + getSOAPMessageAsString(soapResponse));
    	checkResponseForErrors(soapResponse);
    	NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("id");
    	if (elements.getLength() > 0) {
    		org.w3c.dom.Node node = elements.item(0);
    		org.w3c.dom.Node txtNode = node.getFirstChild();
    		if (txtNode != null) {
    			return Long.parseLong(txtNode.getNodeValue());
			}
    	}
    	return null;
    }

    public Long delete(ApiCredentials credentials, Entity toCreate) throws SOAPException, AutotaskException {
        SOAPMessage soapMessage = createBasicMessage(credentials);
        SOAPBody soapBody = soapMessage.getSOAPBody();
        soapBody.addNamespaceDeclaration("xsi", XSI_NAMESPACE);
        soapBody.addNamespaceDeclaration("xsd", XSD_NAMESPACE);
        SOAPElement methodElement = soapBody.addChildElement("delete", "atns");
        SOAPElement entitiesElement = methodElement.addChildElement("Entities", "atns");
        toCreate.toXml(entitiesElement);
        logger.trace("Request [delete]:\n" + getSOAPMessageAsString(soapMessage));
        SOAPMessage soapResponse = soapConnection.call(soapMessage, credentials.getZoneUrl());
        logger.trace("Response [delete]:\n" + getSOAPMessageAsString(soapResponse));
        checkResponseForErrors(soapResponse);
        NodeList elements = soapResponse.getSOAPBody().getElementsByTagName("id");
        if (elements.getLength() > 0) {
            org.w3c.dom.Node node = elements.item(0);
            org.w3c.dom.Node txtNode = node.getFirstChild();
            if (txtNode != null) {
                return Long.parseLong(txtNode.getNodeValue());
            }
        }
        return null;
    }


    /**
     * This will throw an IllegalStateException with the error messages from the SOAPMessage if any exist
     * @param response
     */
    private void checkResponseForErrors(SOAPMessage response) throws SOAPException, AutotaskException {
    	NodeList errorNodes = response.getSOAPBody().getElementsByTagName("ATWSError");
    	if(errorNodes.getLength() > 0) {
    		Json errorList = Json.list();
    		for(int i = 0; i < errorNodes.getLength(); i++) {
    			org.w3c.dom.Node curNode = errorNodes.item(i).getFirstChild();
    			if("Message".equals(curNode.getNodeName())) {
    				errorList.push(curNode.getFirstChild().getNodeValue());
    			}
    		}
    		String errMsg = String.format("ATWS Error %s", errorList.toString());
    		System.err.println(errMsg);
    		throw new AutotaskException(errMsg);
    	}
    }

    private SOAPMessage createBasicMessage(ApiCredentials credentials) throws SOAPException {
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        soapEnvelope.addNamespaceDeclaration("atns", AUTOTASK_NAMESPACE);

        MimeHeaders soapHeaders = soapMessage.getMimeHeaders();
        String authorization = Base64Utils.encodeBasicAuthorization(credentials.getUsername(), credentials.getPassword());
        soapHeaders.addHeader("Authorization", "Basic " + authorization);
        if (!StringUtils.isBlank(credentials.getIntegrationCode())) {
            SOAPElement autotaskIntegrationsElement = soapMessage.getSOAPHeader().addChildElement("AutotaskIntegrations", "atns");
            SOAPElement integrationCodeElement = autotaskIntegrationsElement.addChildElement("IntegrationCode");
            integrationCodeElement.setTextContent(credentials.getIntegrationCode());
        }

        return soapMessage;
    }

    // this is a debug function that can be used to
    private String getSOAPMessageAsString(SOAPMessage message) {
        try {
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();

            // Set formatting
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source sc = message.getSOAPPart().getContent();
            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(streamOut);
            tf.transform(sc, result);
            String strMessage = streamOut.toString();
            return strMessage;
        } catch (Exception e) {
            logger.error("Exception converting SOAP message to string", e);
            return null;
        }
    }
}
