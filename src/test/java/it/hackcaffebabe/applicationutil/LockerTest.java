package it.hackcaffebabe.applicationutil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link Locker}
 */
public class LockerTest
{
    @Test
    public void testLocker(){
        String lockID = "testingID";

        Locker l1 = createNewLocker(lockID);

        String appIDFromLocker = l1.getLockID();
        Assert.assertTrue("Expected that application id remain the same",
                lockID.equals(appIDFromLocker));

        boolean mustBeFalse = l1.isAlreadyRunning();
        Assert.assertFalse("Expected false for the first check of isAlreadyRunning",
                mustBeFalse);

        Locker l2 = createNewLocker(lockID);
        boolean mustBeTrue = l2.isAlreadyRunning();
        Assert.assertTrue("Expected true for the second check of isAlreadyRunning",
                mustBeTrue);
    }

//==============================================================================
//  METHODS
//==============================================================================
    /* create a locker and check if throws some exceptions */
    public Locker createNewLocker( String id ) {
        Locker l=null;
        try {
            l = new Locker(id);
        }catch (Exception e) {
            Assert.fail("Expected not to fail while creating new Locker: "+id);
        }
        return l;
    }
}
