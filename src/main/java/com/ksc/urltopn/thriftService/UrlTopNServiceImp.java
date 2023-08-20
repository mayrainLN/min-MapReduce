package com.ksc.urltopn.thriftService;

import com.ksc.urltopn.*;
import org.apache.thrift.TException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/16 15:06
 * @description :
 */
public class UrlTopNServiceImp implements UrlTopNService.Iface, Serializable {
    public UrlTopNServiceImp() {
    }

    @Override
    public List<UrlTopNResult> getTopNAppResult(String applicationId) throws TException {
        System.out.println(ApplicationResultDB.isSubmit(applicationId));
        System.out.println("applicationId结果: " + ApplicationResultDB.getResult(applicationId).toString());
        if (ApplicationResultDB.getResult(applicationId) == null) {
            List<UrlTopNResult> urlTopNResults = new ArrayList<>();
            String outputPath = ApplicationResultDB.getOutputPath(applicationId);
            System.out.println("outputPath = " + outputPath);
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(outputPath))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] split = line.split("\\s+");
                    if (split.length == 2) {
                        urlTopNResults.add(new UrlTopNResult(split[0], Integer.valueOf(split[1])));
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ApplicationResultDB.setResult(applicationId, urlTopNResults);
            return urlTopNResults;
        }
        return ApplicationResultDB.getResult(applicationId);
    }

    @Override
    public UrlTopNAppResponse submitApp(UrlTopNAppRequest urlTopNAppRequest) throws TException {
        String inputPath = urlTopNAppRequest.getInputPath();
        String outputPath = urlTopNAppRequest.getOuputPath();
        String applicationId = urlTopNAppRequest.getApplicationId();
        int reduceTaskNum = urlTopNAppRequest.getNumReduceTasks();
        int splitSize = urlTopNAppRequest.getSplitSize();
        int topN = urlTopNAppRequest.getTopN();

        ApplicationResultDB.setStatus(applicationId, AppStatusEnum.ACCEPT);
        ApplicationResultDB.addOutputPath(applicationId, outputPath + "/" + applicationId + ".txt");
        AppManager.submitApplication(inputPath, outputPath, applicationId, reduceTaskNum, splitSize, topN);
        //submitApp 直接返回，返回的是App的状态。
        return new UrlTopNAppResponse(applicationId, ApplicationResultDB.getStatus(applicationId).getCode());
    }

    @Override
    public UrlTopNAppResponse getAppStatus(String applicationId) throws TException {
        System.out.println("获取statsu:applicationId = " + applicationId);
        return new UrlTopNAppResponse(applicationId, ApplicationResultDB.getStatus(applicationId).getCode());
    }
}
