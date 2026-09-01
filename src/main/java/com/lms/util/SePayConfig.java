package com.lms.util;

import java.io.InputStream;
import java.util.Properties;

public class SePayConfig {
    private static Properties prop = new Properties();

    static {
        try{
            InputStream is = SePayConfig.class.getClassLoader().getResourceAsStream("sepay.properties");
            if(is != null){
                prop.load(is);
            }
            else{
                System.out.println("Không tìm thấy sepay.properties");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBankName(){
        return prop.getProperty("sepay.bank.name", "");
    }

    public static String getBankAccount(){
        return prop.getProperty("sepay.bank.account", "");
    }

    public static String getApiToken(){
        return prop.getProperty("sepay.api.token", "");
    }
}
