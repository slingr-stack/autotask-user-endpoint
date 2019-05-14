package io.slingr.endpoints.autotaskuser.ws;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class DateHelperTest {
    @Test
    public void testUtcFormat() {
        String str = "2017-07-17T03:18:32.634Z";
        Date date = DateHelper.convertFromUtcDateTime(str);
        assertNotNull(date);
        System.out.println("Date: " + date);
    }

    @Test
    public void testConvertToUtc() {
        Date date = new Date(new Date().getTime() - 1000*60*60);
        String str = DateHelper.convertToDateTime(date);
        assertNotNull(str);
        System.out.println("Date: " + str);
    }
}
