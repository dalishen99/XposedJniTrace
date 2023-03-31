package com.zhenxi.jnitrace.config;

/**
 * Created by Zhenxi on 2019/4/3.
 */

public class ConfigKey {


    public static final String CONFIG_JSON="CONFIG_JSON";
    /**
     * 选中的包名
     */
    public static final String PACKAGE_NAME="PACKAGE_NAME";

    /**
     * 注入模块So的Path
     */
    public static final String MOUDLE_SO_PATH="MOUDLE_SO_PATH";

    /**
     * 选择Apk的时间,十分钟有效
     */
    public static final String SAVE_TIME="SAVE_TIME";

    /**
     * 是否开启内存序列化
     */
    public static final String IS_SERIALIZATION="IS_SERIALIZATION";

    /**
     * 是否监听全部的SO文件
     */
    public static final String IS_LISTEN_TO_ALL="IS_LISTEN_TO_ALL";


    /**
     * 过滤的集合
     */
    public static final String FILTER_LIST="FILTER_LIST";

}
