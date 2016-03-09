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
 * TODO add doc and examples
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
     * TODO add doc
     * @param lockID
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
        } catch (FileNotFoundException ignored) {}

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    closeLock();
                    deleteFile();
                } catch (IOException ignored) {}
            }
        }));
    }

//==============================================================================
//  METHODS
//==============================================================================
    /**
     * TODO add doc
     * @return
     */
    public boolean isAlreadyRunning(){
        //try to lock
        try {
            this.applicationFileLock = this.applicationFileChannel.lock();
        } catch (OverlappingFileLockException ignored) {
            // if lock fail, other application is running
            return true;
        } catch (IOException e) {
            return true; // not sure
        }
        // if reached, application is not already running
        return false;
    }

    /* TODO add comment */
    private void closeLock() throws IOException {
        if( this.applicationFileLock != null )
            this.applicationFileLock.release();
        if( this.applicationFileChannel != null )
            this.applicationFileChannel.close();
    }

    /* TODO add comment */
    private void deleteFile() throws IOException{
        //this.applicationFile.delete();
        if(this.applicationFile != null )
            Files.delete(Paths.get(this.applicationFile.getAbsolutePath()));
    }

    /* TODO add comment */
    private String makeFileName(){
        return TMP_DIR + SEP + this.getLockID() + ".lock";
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * TODO add doc
     * @param LockID
     * @throws IllegalArgumentException
     */
    public void setLockID(String LockID) throws IllegalArgumentException {
        if( LockID == null || LockID.isEmpty() )
            throw new IllegalArgumentException("FileID given can not be null or empty string");
        this.lockID = LockID;
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add doc
     * @return
     */
    public String getLockID(){ return this.lockID; }
}
