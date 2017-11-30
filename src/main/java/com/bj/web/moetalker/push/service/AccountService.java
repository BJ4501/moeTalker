package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.db.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

//真实访问路径---> localhost/api/account
@Path("/account")
public class AccountService {

    @GET
    @Path("/login")
    public String get(){
        return "you --> /login";
    }

    @POST
    @Path("/login")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User post(){

        User u = new User();
        u.setName("Aaa");
        u.setSex(2);
        return u;
    }


}
