package com.dlvs.monstereditor;


/**
 * desc：网络配置类
 * author：haojie
 * date：2017-05-05
 */
public class Network {
    /*接口地址*/
    public static String MAIN_URL;
    /*文件服务器公共地址*/
    public static String FILE_SERVER_COMMON_URL;
    /*true为本地测试环境false为知好乐环境*/
    public static boolean DEBUG = false;
    static {
        if(!DEBUG) {
            MAIN_URL = "http://58.132.209.234:8080";
            FILE_SERVER_COMMON_URL = "http://58.132.209.235:8099/down/nor/";
        } else {
            MAIN_URL = "http://192.168.201.20:8087";
            FILE_SERVER_COMMON_URL = "http://58.132.209.245/down/nor/";
        }
    }

}