package com.bj.web.moetalker.push;


import com.bj.web.moetalker.push.service.AccountService;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

public class Application extends ResourceConfig {
    public Application(){
        //注册逻辑处理
        //两种写法
        //packages("com.bj.web.moetalker.push.service");
        packages(AccountService.class.getPackage().getName());

        //注册Json转换器
        register(JacksonJsonProvider.class);
        //注册日志打印输出
        register(Logger.class);


    }



}
