package it.hackcaffebabe.applicationutil;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test class for {@link Locker}
 */
public class LockerTest
{
    @Test
    public void testCorrectBehavior(){
        String lockID = "testingID";
        Locker l1 = createNewLocker(lockID);

        Assert.assertTrue(
            "Expected that application id remain the same",
            lockID.equals(l1.getLockID())
        );

        String TMP_DIR = System.getProperty("java.io.tmpdir");
        String SEP = System.getProperty("file.separator");
        boolean fileMustExists = Files.exists(
                Paths.get(TMP_DIR+SEP+l1.getLockID()+".lock")
        );
        Assert.assertTrue(
            "Expected that locker creates a lock file in <TMP>/getLockID()+\".lock\"",
            fileMustExists
        );

        long myPid = Util.getProcessID();
        try{
            long fromLocker = l1.checkLock();
            Assert.assertEquals(
                    "Expected the same pid from Util.getProcessID() and l1.checkLock()",
                    myPid,
                    fromLocker
            );
        }catch (IOException ioe){
            Assert.fail(ioe.getMessage());
        }

        Locker l2 = createNewLocker(lockID);
        try{
            long fromLocker = l2.checkLock();
            Assert.assertEquals(
                    "Expected the same pid from Util.getProcessID() and l2.checkLock()",
                    myPid,
                    fromLocker
            );
        }catch (IOException ioe){
            Assert.fail(ioe.getMessage());
        }
    }

    @Test
    public void testNotAllowedTestId(){
        // testing multiple lockID not allowed
        String[] idForFail = { "/this/is/a/path", "/this/is/.also/a/path",
            "thisisnota/file", "/this\\ is\\ not\\ a file", null, ""
        };
        for( String s: idForFail ){
            try{
                new Locker(s);
                Assert.fail("Expect to fail for id = "+s);
            }catch (IllegalArgumentException ignored){}
        }
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
