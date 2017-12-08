package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.RegisterModel;
import com.bj.web.moetalker.push.bean.card.UserCard;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Controller 层
 */

//真实访问路径---> localhost/api/account
@Path("/account")
public class AccountService {

    @POST
    @Path("/register")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserCard register(RegisterModel model){
        User user = UserFactory.register(
                model.getAccount(),model.getPassword(),model.getName());

        if (user != null){
            UserCard card = new UserCard();
            card.setName(user.getName());
            card.setPhone(user.getPhone());
            card.setSex(user.getSex());
            card.setIsFollow(true);
            card.setModifyAt(user.getUpdateAt());
            return card;
        }
        return null;
    }


}
