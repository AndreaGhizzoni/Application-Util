package it.hackcaffebabe.applicationutil;

import java.lang.management.ManagementFactory;

/**
 * TODO add doc and examples
 */
public class Util
{
    // this attribute is set once getProcessID() is called
    private static Long pid = null;

    /**
     * TODO add doc
     * @return
     */
    public static Long getProcessID(){
        if( pid == null ) {
            String pName = ManagementFactory.getRuntimeMXBean().getName();
            pid = Long.parseLong(pName.split("@")[0]);
        }
        return pid;
    }
}
