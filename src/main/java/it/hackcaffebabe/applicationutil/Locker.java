package it.hackcaffebabe.applicationutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Locker class provide a simple method to prevent multiple instances of a
 * single program.
 * <pre>{@code
 * String appId = "myApp";
 * Locker l = new Locker(appId);
 * if( l.isAlreadyRunning() ){
 *     // do something
 * }else{
 *     // do something else
 * }
 * }</pre>
 */
public class Locker
{
    // static constant that returns the platform dependent temporary directory.
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String SEP = System.getProperty("file.separator");

    private File applicationFile;
    private FileChannel applicationFileChannel;
    private FileLock applicationFileLock;
    private String lockID;

    /**
     * Create a new Locker with a specific lockID. Creating multiple Locker with
     * the same lockID is equivalent to run the same application multiple times;
     * this behavior is caused because Locker use a temporary file locked by
     * your application. When the application shuts down, the file will be
     * deleted.
     * @param lockID {@link java.lang.String} the application lock id.
     * @throws IllegalArgumentException if argument given is null or empty string.
     */
    public Locker(String lockID) throws IllegalArgumentException {
        this.setLockID(lockID);
        // create a file in <TEMP_DIR>/lockID.lock
        this.applicationFile = new File( this.makeFileName() );
        // create channel to file
        try {
            this.applicationFileChannel = new RandomAccessFile(
                    this.applicationFile, "rw"
            ).getChannel();
            // ignored because is sure that file exists
        } catch (FileNotFoundException ignored) {
            ignored.printStackTrace();
        }
    }

//==============================================================================
//  METHODS
//==============================================================================
    /**
     * Check if current application is already running or not.
     * @return true if application is already running, false otherwise.
     */
    public boolean isAlreadyRunning(){
        //try to lock
        try {
            this.applicationFileLock = this.applicationFileChannel.tryLock();
            // if tryLock() returns null then another application has already
            // locked the file. It is the same case if it throws exceptions.
            if( this.applicationFileLock == null ){
                return true;
            }

            // add hook only if it can get lock on file
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        closeLock();
                        deleteFile();
                    } catch (IOException ignored) {}
                }
            }));
        } catch (OverlappingFileLockException ignored) {
            // if lock fail, other application is running
            return true;
        } catch (IOException e) {
            return true; // not sure
        }
        // if reached, application is not already running
        return false;
    }

    /* close the current lock and channel */
    private void closeLock() throws IOException {
        if( this.applicationFileLock != null )
            this.applicationFileLock.release();
        if( this.applicationFileChannel != null )
            this.applicationFileChannel.close();
    }

    /* delete the current file */
    private void deleteFile() throws IOException{
        //this.applicationFile.delete();
        if(this.applicationFile != null )
            Files.delete(Paths.get(this.applicationFile.getAbsolutePath()));
    }

    /* build the path of lock file */
    private String makeFileName(){
        return TMP_DIR + SEP + this.getLockID() + ".lock";
    }

//==============================================================================
//  SETTER
//==============================================================================
    /* Set the lock identifier for current application. */
    private void setLockID(String LockID) throws IllegalArgumentException {
        if( LockID == null || LockID.isEmpty() )
            throw new IllegalArgumentException(
                    "LockID given can not be null or empty string"
            );
        this.lockID = LockID;
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * Return the current application lock ID.
     * @return {@link java.lang.String} the current application lock ID.
     */
    public String getLockID(){ return this.lockID; }
}
