package it.hackcaffebabe.applicationutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
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
        TO keep in mind ( from this article http://goo.gl/8OlXBD )
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
     * This method checks if another application is already running with the
     * same lockID (specified in constructor method), in this case return the
     * process id of the application that is already running. No matter how many
     * instance of the single application you can launch, it will be return the
     * process id of the first application that has been launched.
     * In the other case, where there is no other application with the same
     * lockID running, this method returns the process id of the current
     * application.
     * @return {@link java.lang.Long} the long representing the PID described
     *         previous.
     * @throws IOException if something went terribly wrong.
     */
    public long checkLock() throws IOException{
        try{
            this.applicationFileLock = this.applicationFileChannel.tryLock();
            // if tryLock() returns null then another application has already
            // locked the file. It is the same case if it throws exceptions.
            if( this.applicationFileLock == null ){
                // read pid from this.applicationFile.getAbsolutePath()
                // and return it
                return readPid();
            }else{
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

                // write my pid into this.applicationFile.getAbsolutePath()
                // and return it
                int nOfBytesWritten = writePid();
                if( nOfBytesWritten <= 0 ){
                    throw new IOException("Writing pid returned zero bytes " +
                            "written.");
                }else{
                    return Util.getProcessID();
                }
            }
        }catch (OverlappingFileLockException ignored) {
            // read pid from this.applicationFile.getAbsolutePath()
            // and return it
            return readPid();
        }
    }

    /* write pid into file lock */
    private int writePid() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong( Util.getProcessID() );
        byteBuffer.flip(); // necessary: https://goo.gl/vGQX5O
        int nOfBytesWritten = this.applicationFileChannel.write(byteBuffer);
        byteBuffer.clear();
        return nOfBytesWritten;
    }

    /* read pid from file lock */
    private long readPid() throws IOException {
        MappedByteBuffer mappedByteBuffer = this.applicationFileChannel.map(
                FileChannel.MapMode.READ_ONLY, 0,
                this.applicationFileChannel.size()
        );
        mappedByteBuffer.load();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        while( mappedByteBuffer.hasRemaining() ){
            byteBuffer.put(mappedByteBuffer.get());
        }
        byteBuffer.flip(); // necessary: https://goo.gl/vGQX5O
        return byteBuffer.getLong();
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
