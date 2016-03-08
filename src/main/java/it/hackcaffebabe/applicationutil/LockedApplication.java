package it.hackcaffebabe.applicationutil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * TODO add doc and examples
 */
public class LockedApplication
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
    public LockedApplication(String fileID) {
        this.setFileID(fileID);
    }

    public boolean isAlreadyRunning(){
        try{
            // get the current process id
            long pid = Util.getProcessID();
            // create a file in <TEMP_DIR>/<pid>.lock
            this.applicationFile = new File( TMP_DIR+SEP+pid+".lock" );
            this.applicationFileChannel = new RandomAccessFile(
                    this.applicationFile,
                    "rw"
            ).getChannel();

            try {
                this.applicationFileLock = this.applicationFileChannel.lock();
            }catch (OverlappingFileLockException ignored){
                //close lock
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    // closelock
                    // delete file
                }
            }));


        }catch (Exception e){
            //close lock
            return true;
        }

        return false;
    }

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
}
