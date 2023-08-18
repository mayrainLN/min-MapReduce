package com.ksc.urltopn;

/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/18 13:47
 * @description :
 */
/**
 * @author :MayRain
 * @version :1.0
 * @date :2023/8/18 13:46
 * @description :
 */
public enum AppStatusEnum {
    /**
     * 任务状态
     */
    ACCEPT(0, "初始化"),
    RUNNING(1, "运行中"),
    FINISHED(2, "完成"),
    FAILED(3, "失败");

    private int code;
    private String desc;

    AppStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
