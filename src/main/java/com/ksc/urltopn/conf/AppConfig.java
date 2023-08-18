package com.ksc.urltopn.conf;

public class AppConfig {

    public static String shuffleTempDir;

    static {
        shuffleTempDir = System.getProperty("java.io.tmpdir")+"/shuffle";
    }
}
