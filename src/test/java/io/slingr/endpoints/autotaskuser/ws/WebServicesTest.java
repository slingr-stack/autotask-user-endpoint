package io.slingr.endpoints.autotaskuser.ws;

import io.slingr.endpoints.utils.Json;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.soap.SOAPException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore("For dev purposes only")
public class WebServicesTest {

    @Before
    public void init() {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
    }

    @Test
    public void getZoneUrl() throws SOAPException {
        AutotaskApi autotaskApi = getApi();
        String zoneInfo = autotaskApi.getZoneUrl(getCredentials());
        assertEquals("https://webservices15.autotask.net/ATServices/1.5/atws.asmx", zoneInfo);
    }

    @Test
    public void getWebUrl() throws SOAPException {
        AutotaskApi autotaskApi = getApi();
        String webUrl = autotaskApi.getWebUrl(getCredentials());
        assertEquals("https://ww15.autotask.net/", webUrl);
    }

    @Test
    public void getEntityInfo() throws SOAPException, AutotaskException {
        AutotaskApi autotaskApi = getApi();
        List<EntityInfo> entityInfoList = autotaskApi.getEntityInfo(getCredentials());
        assertTrue(entityInfoList.size() > 0);
    }

    @Test
    public void getFieldInfo() throws AutotaskException, SOAPException {
        AutotaskApi autotaskApi = getApi();
        List<EntityFieldInfo> entityFieldInfoList = autotaskApi.getFieldInfo(getCredentials(), EntityType.ACCOUNT);
        assertTrue(entityFieldInfoList.size() > 0);
    }

    @Test
    public void getUDFInfo() throws AutotaskException, SOAPException {
        AutotaskApi autotaskApi = getApi();
        List<EntityFieldInfo> entityFieldInfoList = autotaskApi.getUDFInfo(getCredentials(), EntityType.ACCOUNT);
        assertTrue(entityFieldInfoList.size() > 0);
    }

    @Test
    public void listAccounts() throws RemoteException, SOAPException {
        AutotaskApi autotaskApi = getApi();
        QueryBuilder queryBuilder = new QueryBuilder(getCredentials(), EntityType.ACCOUNT);
        queryBuilder.addFilter("id", false, "equals", "29760247");
        List<Entity> accounts = (List<Entity>) autotaskApi.query(getCredentials(), queryBuilder);
        assertEquals(1, accounts.size());
        assertEquals("Test Account", accounts.get(0).getValue("AccountName", false));
    }

    @Test
    public void entityToJson() throws RemoteException, SOAPException {
        AutotaskApi autotaskApi = getApi();
        QueryBuilder queryBuilder = new QueryBuilder(getCredentials(), EntityType.ACCOUNT);
        queryBuilder.addFilter("id", false, "equals", "29760247");
        List<Entity> accounts = (List<Entity>) autotaskApi.query(getCredentials(), queryBuilder);
        assertEquals(1, accounts.size());
        Json json = accounts.get(0).toJson();
        assertEquals(Long.valueOf(29760247), json.longInteger("id"));
        assertEquals("Test Account", json.string("AccountName"));
        assertEquals("Question", json.json("UserDefinedFields").string("ATOppPromotion"));
        assertTrue(json.object("ClientPortalActive") instanceof Boolean);
        assertTrue(json.object("LastActivityDate") instanceof Date);
    }

    @Test
    public void createTicket() throws SOAPException, AutotaskException {
        AutotaskApi autotaskApi = getApi();
        QueryBuilder queryBuilder = new QueryBuilder(getCredentials(), EntityType.ACCOUNT);
        queryBuilder.addFilter("id", false, "equals", "30053684");
        List<Entity> accounts = (List<Entity>) autotaskApi.query(getCredentials(), queryBuilder);
        assertEquals(1, accounts.size());
        Entity ticket = new Entity(getCredentials(), EntityType.TICKET);
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 60 * 60 * 24);
        ticket.setValue("AccountID", false, ""+accounts.get(0).getId());
        ticket.setValue("DueDateTime", false, date);
        ticket.setValue("Priority", false, "2");
        ticket.setValue("Status", false, "1");
        ticket.setValue("Title", false, "test ticket A");
        ticket.setValue("Description", false, "this is a test ticket");
        ticket.setValue("AssignedResourceID", false, "31053307");
        ticket.setValue("AssignedResourceRoleID", false, "29720942");
        Long ticketId = autotaskApi.create(getCredentials(), ticket);
        assertNotNull(ticketId);
        System.out.println("ticket id: " + ticketId);
    }

    private ApiCredentials getCredentials() {
        ApiCredentials credentials = new ApiCredentials();
        credentials.setUsername("test@test");
        credentials.setPassword("password");
        credentials.setIntegrationCode("autotask-integration-code");
        credentials.setZoneUrl("https://webservices15.autotask.net/ATServices/1.5/atws.asmx");
        return credentials;
    }

    private AutotaskApi getApi() throws SOAPException {
        AutotaskApi autotaskApi = new AutotaskApi();
        for (EntityType type : EntityType.values()) {
            type.setAutotaskApi(autotaskApi);
        }
        return autotaskApi;
    }
}
