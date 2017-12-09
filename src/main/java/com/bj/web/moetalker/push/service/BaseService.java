package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.db.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 *
 */
public class BaseService {

    //添加一个上下文注解，该注解会给securityContext赋值，
    //具体值为拦截器中所返回的上下文SecurityContext
    @Context
    protected SecurityContext securityContext;

    /**
     * 从上下文中直接获取自己信息
     * 必须继承(protected)
     * @return
     */
    protected User getSelf(){
        /*if (securityContext == null)
            return null;*/
        return (User) securityContext.getUserPrincipal();
    }
}
