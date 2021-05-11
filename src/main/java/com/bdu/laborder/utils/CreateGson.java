package com.bdu.laborder.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @Author Qi
 * @data 2021/4/2 10:55
 */
public class CreateGson {
    /**
     * 让gson序列化null值
     * @return
     */
    public static Gson createGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        return gsonBuilder.create();
    }
}
