package it.hackcaffebabe.applicationutil;

import java.lang.management.ManagementFactory;

/**
 * TODO add doc and examples
 */
public class Util
{
    /**
     * TODO add doc
     * @return
     */
    public static Long getProcessID(){
        String pName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(pName.split("@")[0]);
    }
}
