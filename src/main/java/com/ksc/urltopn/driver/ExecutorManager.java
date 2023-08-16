package com.ksc.urltopn.driver;

import com.ksc.urltopn.rpc.ExecutorRegister;

import java.util.HashMap;
import java.util.Map;

public class ExecutorManager {

    /**
     * ExecutorUrl和ExecutorRegister的映射
     */
    private Map<String, ExecutorRegister> executorRegisterMap = new HashMap<>();

    /**
     * ExecutorUrl和Core数的映射
     */
    private Map<String, Integer> executorAvailableCoresMap = new HashMap<>();

    public void updateExecutorRegister(ExecutorRegister executorRegister) {
        executorRegisterMap.put(executorRegister.getExecutorUrl(),executorRegister);
        //建立ExecutorUrl和Core数的映射
        executorAvailableCoresMap.put(executorRegister.getExecutorUrl(),executorRegister.getCores());
    }

    public Map<String, Integer> getExecutorAvailableCoresMap() {
        return executorAvailableCoresMap;
    }

    public Map<String, ExecutorRegister> getExecutorRegisterMap() {
        return executorRegisterMap;
    }

    public int getExecutorMaxCore(String executorUrl) {
        return executorRegisterMap.get(executorUrl).getCores();
    }

    public int getExecutorAvalibeCore(String executorUrl) {
        return executorAvailableCoresMap.get(executorUrl);
    }

    public synchronized void  updateExecutorAvailableCores(String executorUrl,int addCores){
        int oldCore = executorAvailableCoresMap.get(executorUrl)==null?0:executorAvailableCoresMap.get(executorUrl);
        executorAvailableCoresMap.put(executorUrl,oldCore+addCores);
    }






}
