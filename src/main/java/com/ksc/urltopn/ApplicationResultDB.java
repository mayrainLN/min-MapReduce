package com.ksc.urltopn;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/18 11:36
 * @description :
 */
public class ApplicationResultDB {
    private static Map<String, AppStatusEnum> statusMap = new ConcurrentHashMap<>();

    private static Map<String, String> outputPathMap = new ConcurrentHashMap<>();

    private static Map<String, List<UrlTopNResult>> resultMap = new ConcurrentHashMap<>();

    public static void setStatus(String applicationId, AppStatusEnum status) {
        statusMap.put(applicationId, status);
    }

    public static AppStatusEnum getStatus(String applicationId) {
        return statusMap.get(applicationId);
    }

    public static void setResult(String applicationId, List<UrlTopNResult> result) {
        resultMap.put(applicationId, result);
    }

    public static List<UrlTopNResult> getResult(String applicationId) {
        return resultMap.get(applicationId);
    }

    public static void addOutputPath(String applicationId, String outputPath) {
        outputPathMap.put(applicationId, outputPath);
    }

    public static String getOutputPath(String applicationId) {
        return outputPathMap.get(applicationId);
    }
}
