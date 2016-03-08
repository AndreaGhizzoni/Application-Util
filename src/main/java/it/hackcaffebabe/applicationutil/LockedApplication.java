package it.hackcaffebabe.applicationutil;

/**
 * TODO add doc and examples
 */
public class LockedApplication
{
    private static LockedApplication instance;

    /**
     * TODO add doc
     * @return
     */
    public static LockedApplication getInstance(){
        if( instance == null )
            instance = new LockedApplication();
        return instance;
    }

    /*
     *
     */
    private LockedApplication(){

    }


}
