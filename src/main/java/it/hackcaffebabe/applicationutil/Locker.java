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
import java.util.regex.Pattern;

/**
 * Locker class provide a simple method to prevent multiple instances of a
 * java program.
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
    /*
        TODO:
        using lockID.pid as file locked, so a second run application can
        detect what process (pid) is already running.

        TODO: keep in mind ( from this article http://goo.gl/8OlXBD )
        Shutdown Hook: is NOT guarantee that is execute always; it is executed
        only when JVM is closed normally, not via SIGKILL.
        If multiple Shutdown Hook has been added, their execution order is not
        guarantee
    */
    private File applicationFile;
    private FileChannel applicationFileChannel;
    private FileLock applicationFileLock;
    private String lockID;

    /**
     * Create a new Locker with a specific lockID. Creating multiple Locker with
     * the same lockID is equivalent to run the same application multiple times;
     * this behavior is caused because Locker use a temporary file to lock your
     * application. When the application shuts down, the file will be deleted.
     * @param lockID {@link java.lang.String} the application lock id.
     * @throws IllegalArgumentException if argument given is null or empty string.
     * @throws SecurityException if Security Manager block creation of files.
     */
    public Locker(String lockID) throws IllegalArgumentException, SecurityException {
        this.setLockID(lockID);
        // create a file in <TEMP_DIR>/lockID.lock
        this.applicationFile = new File( this.makeFileName() );
        // create channel to file
        try {
            this.applicationFileChannel = new RandomAccessFile(
                    this.applicationFile, "rw"
            ).getChannel();
        } catch (FileNotFoundException ignored) {
            // ignored because applicationFile is set to a fixed path.
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

            // add shutdown hook only if it can get lock on file
            Runtime.getRuntime().addShutdownHook(new Thread( new Runnable() {
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

        // if reached, no other application is already running with the same
        // lockID given to Locker
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
        if(this.applicationFile != null )
            Files.delete(Paths.get(this.applicationFile.getAbsolutePath()));
    }

    /* build the path of lock file */
    private String makeFileName(){
        String TMP_DIR = System.getProperty("java.io.tmpdir");
        String SEP = System.getProperty("file.separator");
        return TMP_DIR + SEP + this.getLockID() + ".lock";
    }

//==============================================================================
//  SETTER
//==============================================================================
    /* Set the lock identifier for current application. */
    private void setLockID(String lid) throws IllegalArgumentException {
        if( lid == null || lid.isEmpty() )
            throw new IllegalArgumentException(
                    "Argument lid given can not be null or empty string");
        if(Pattern.matches(".*[/\\x5c].*", lid))
            throw new IllegalArgumentException(
                    "Argument lid given is not a regular file name.");
        this.lockID = lid;
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
