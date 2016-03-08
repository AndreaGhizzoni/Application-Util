package it.hackcaffebabe.applicationutil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for class {@link Util}
 */
public class UtilTest {

    @Test
    public void testGetProcessID(){
        Long pid = Util.getProcessID();
        Assert.assertNotNull("Expecting pid not null from getProcessID()", pid);
        Assert.assertTrue("Expecting that pid is > 0", pid > 0 );

        Long pid2 = Util.getProcessID();
        Assert.assertEquals(
                "Expected that multiple calls of getProcessID() returns the same value",
                pid, // expected
                pid2 // actual
        );
    }
}
