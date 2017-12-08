package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.AccountRspModel;
import com.bj.web.moetalker.push.bean.api.account.LoginModel;
import com.bj.web.moetalker.push.bean.api.account.RegisterModel;
import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
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

    /**
     * 登录
     * @param model
     * @return
     */
    @POST
    @Path("/login")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> login(LoginModel model){

        User user = UserFactory.login(model.getAccount(), model.getPassword());
        if(user != null){
            //返回当前账户rspModel
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else {
            //登录失败
            return ResponseModel.buildLoginError();
        }
    }


    /**
     * 注册
     * @param model
     * @return
     */
    @POST
    @Path("/register")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model){
        User user = UserFactory.findByPhone(model.getAccount().trim());
        if (user != null){
            //account(电话号码)已存在
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(model.getName().trim());
        if (user != null){
            //用户名已存在
            return ResponseModel.buildHaveNameError();
        }

        //注册逻辑部分
        user = UserFactory.register(
                model.getAccount(),model.getPassword(),model.getName());
        if (user != null){
            //返回当前账户rspModel
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else {
            //注册异常
            return ResponseModel.buildRegisterError();
        }
    }


}
