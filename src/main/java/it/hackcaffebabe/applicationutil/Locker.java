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
    private String fileID;

    /**
     * TODO add doc
     * @param fileID
     */
    public Locker(String fileID) throws IllegalArgumentException {
        this.setFileID(fileID);
        // create a file in <TEMP_DIR>/fileID.lock
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
        return TMP_DIR + SEP + this.getFileID() + ".lock";
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * TODO add doc
     * @param fileID
     * @throws IllegalArgumentException
     */
    public void setFileID(String fileID) throws IllegalArgumentException {
        if( fileID == null || fileID.isEmpty() )
            throw new IllegalArgumentException("FileID given can not be null or empty string");
        this.fileID = fileID;
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add doc
     * @return
     */
    public String getFileID(){ return this.fileID; }
}
