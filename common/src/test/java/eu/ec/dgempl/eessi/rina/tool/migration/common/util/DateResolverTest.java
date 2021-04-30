package eu.ec.dgempl.eessi.rina.tool.migration.common.util;

import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;

public class DateResolverTest {

    @Test
    public void testParser() {
        String reference = "2020-06-25T22:00:00.000Z";
        ZonedDateTime zdt = ZonedDateTime.parse(reference);

        String date = reference;
        Assert.assertEquals(zdt, DateResolver.parse(date));

        date = "2020-06-25T22:00:00.000";
        Assert.assertEquals(zdt, DateResolver.parse(date));

        date = "1593122400";
        Assert.assertEquals(zdt, DateResolver.parse(date));

        date = "1593122400000";
        Assert.assertEquals(zdt, DateResolver.parse(date));

        reference = "1919-07-09T02:00:00.000Z";
        zdt = ZonedDateTime.parse(reference);

        date = "-1593122400";
        Assert.assertEquals(zdt, DateResolver.parse(date));

        date = "-1593122400000";
        Assert.assertEquals(zdt, DateResolver.parse(date));
    }
}
