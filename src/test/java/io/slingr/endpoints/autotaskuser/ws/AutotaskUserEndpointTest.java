package io.slingr.endpoints.autotaskuser.ws;

import io.slingr.endpoints.autotaskuser.AutotaskUserEndpoint;
import io.slingr.endpoints.autotaskuser.Runner;
import io.slingr.endpoints.services.datastores.DataStore;
import io.slingr.endpoints.services.exchange.Parameter;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.tests.EndpointTests;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

@Ignore("For dev purposes only")
public class AutotaskUserEndpointTest {

    private static EndpointTests test;
    private static AutotaskUserEndpoint endpoint;

    @BeforeClass
    public static void init() throws Exception {
        test = EndpointTests.start(new io.slingr.endpoints.autotaskuser.Runner(), "test.properties");
        endpoint = (AutotaskUserEndpoint) test.getEndpoint();

        DataStore users = endpoint.userDataStore();
        users.save(Json.map()
                .set("_id", "5506fc44c2eee3b1a702696e")
                .set("username", "test@test")
                .set("password", "password")
        );
    }

    @Test
    public void testReadAccount(){
        Json params = Json.map()
                .set("entity", "Account")
                .set("filters", Json.list()
                        .push(Json.map()
                                .set("field", "id")
                                .set("udf", false)
                                .set("op", "equals")
                                .set("value", 29760247l)
                        )
                );
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json accounts = endpoint.query(functionRequest);
        Json account = (Json) accounts.object(0);
        assertNotNull(account);
        assertEquals(29760247l, (long) account.longInteger("id"));
        assertEquals("Test Account", account.string("AccountName"));
    }

    @Test
    public void testCreateAccount() {
        String accountName = "automated-test-account-7";

        // create account
        Json params = Json.map()
                .set("entity", "Account")
                .set("data", Json.map()
                        .set("AccountName", accountName)
                        .set("AccountNumber", "123")
                        .set("AccountType", 2)
                        .set("Active", true)
                        .set("Address1", "76 test")
                        .set("AssetValue", 77234.23)
                        .set("CountryID", 12)
                        .set("Phone", "867-5309")
                        .set("OwnerResourceID", 30759325)
                        .set("UserDefinedFields", Json.map()
                                .set("LFNOTE", "Test note!")
                        )
                );
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json res = endpoint.create(functionRequest);
        assertNotNull(res);
        assertNotNull(res.longInteger("id"));
        long accountId = res.longInteger("id");
        System.out.println("account ID: "+accountId);

        // read and check data is OK
        params = Json.map()
                .set("entity", "Account")
                .set("filters", Json.list()
                        .push(Json.map()
                                .set("field", "id")
                                .set("op", "equals")
                                .set("value", accountId)
                        )
                );
        functionRequest = getFunctionRequest(params);
        Json accounts = endpoint.query(null);
        Json account = (Json) accounts.object(0);
        assertNotNull(account);
        assertEquals(accountId, (long) account.longInteger("id"));
        assertEquals(accountName, account.string("AccountName"));
        assertEquals("123", account.string("AccountNumber"));
        assertEquals(2, (int) account.integer("AccountType"));
        assertEquals(true, account.bool("Active"));
        assertEquals("76 test", account.string("Address1"));
        assertEquals(77234.23, (double) account.decimal("AssetValue"), 0.1);
        assertEquals(12, (long) account.longInteger("CountryID"));
        assertEquals("Test note!", account.json("UserDefinedFields").string("LFNOTE"));
        assertTrue(account.object("CreateDate") instanceof Date);
    }

    @Test
    public void readCountries() {
        Json params = Json.map()
                .set("entity", "Country")
                .set("filters", Json.list()
                        .push(Json.map()
                                .set("field", "CountryCode")
                                .set("op", "IsNotNull")
                        )
                );
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json countries = endpoint.query(functionRequest);
        for (Object obj : countries.objects()) {
            Json country = (Json) obj;
            System.out.println("Country: "+country.string("Name")+", "+country.string("CountryCode")+", "+country.longInteger("id"));
        }
    }

    @Test
    public void testDelete() {
        Json params = Json.map()
                .set("entity", "AccountToDo")
                .set("data", Json.map()
                        .set("id", 30072703l)
                );
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json res = endpoint.delete(functionRequest);
        assertNotNull(res);
        assertNotNull(res.longInteger("id"));
    }

    @Test
    public void testGetEntityInfo() {
        Json params = Json.map()
                .set("entity", "Account");
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json res = endpoint.getEntity(functionRequest);
        assertNotNull(res);
        assertEquals("Account", res.string("name"));
        System.out.println(res.toString());
    }

    @Test
    public void testGetEntityFieldsInfo() {
        Json params = Json.map()
                .set("entity", "Account");
        FunctionRequest functionRequest = getFunctionRequest(params);
        Json res = endpoint.getEntityFields(functionRequest);
        assertNotNull(res);
        assertTrue(res.size() > 0);
        System.out.println(res.toString());
    }

    private FunctionRequest getFunctionRequest(Json params) {
        return new FunctionRequest(Json.map()
                .set(Parameter.PARAMS, params)
                .set(Parameter.USER_ID, "5506fc44c2eee3b1a702696e")
                .set(Parameter.USER_EMAIL, "user@test.com")
        );
    }

    @Test
    public void testReadRole() {
        Json params = Json.map()
                .set("entity", "Role")
                .set("filters", Json.list()
                        .push(Json.map()
                                .set("field", "id")
                                .set("op", "isnotnull")
                        )
                );

        final FunctionRequest functionRequest = getFunctionRequest(params);
        final Json list = endpoint.query(functionRequest);
        assertNotNull(list);
        assertTrue(list.isList());
        assertFalse(list.isEmpty());
        final Json response = (Json) list.object(0);
        assertNotNull(response);
    }

    @Test
    public void testReadProject() {
        Json params = Json.map()
                .set("entity", "Project")
                .set("filters", Json.list()
                        .push(Json.map()
                                .set("field", "id")
                                .set("op", "isnotnull")
                        )
                        .push(Json.map()
                                .set("field", "Status")
                                .set("op", "equals")
                                .set("value", 1)
                        )
                );

        final FunctionRequest functionRequest = getFunctionRequest(params);
        final Json list = endpoint.query(functionRequest);
        assertNotNull(list);
        assertTrue(list.isList());
        assertFalse(list.isEmpty());
        final Json response = (Json) list.object(0);
        assertNotNull(response);
    }
}
