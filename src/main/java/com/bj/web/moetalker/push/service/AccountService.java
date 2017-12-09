package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.AccountRspModel;
import com.bj.web.moetalker.push.bean.api.account.LoginModel;
import com.bj.web.moetalker.push.bean.api.account.RegisterModel;
import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.card.UserCard;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.google.common.base.Strings;


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
        if(!LoginModel.check(model)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }


        User user = UserFactory.login(model.getAccount(), model.getPassword());
        if(user != null){
            //如果有携带PushId
            if(!Strings.isNullOrEmpty(model.getPushId())){
                return bind(user,model.getPushId());
            }
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
        if(!RegisterModel.check(model)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

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
            //如果有携带PushId
            if(!Strings.isNullOrEmpty(model.getPushId())){
                return bind(user,model.getPushId());
            }
            //返回当前账户rspModel
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else {
            //注册异常
            return ResponseModel.buildRegisterError();
        }
    }

    /**
     * 绑定设备id
     */
    @POST
    @Path("/bind/{pushId}")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //从请求头中获取token字段
    //pushId从url地址中获取
    public ResponseModel<AccountRspModel> login(@HeaderParam("token") String token,
                                                @PathParam("pushId") String pushId){
        if(Strings.isNullOrEmpty(token)||Strings.isNullOrEmpty(pushId)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        //拿到自己的个人信息
        User user = UserFactory.findByToken(token);
        if(user != null){
            return bind(user,pushId);
        }else {
            //Token失效，所以无法进行绑定
            return ResponseModel.buildAccountError();
        }
    }

    /**
     * 进行pushId绑定操作
     * @param self 自己User_self
     * @param pushId PushId
     * @return User
     */
    private ResponseModel<AccountRspModel> bind(User self,String pushId){
        //进行设备Id绑定操作
        User user = UserFactory.bindPushId(self,pushId);

        if(user == null){
            //绑定失败则是服务器异常
            return ResponseModel.buildServiceError();
        }
        //返回当前账户rspModel,并且已经绑定了
        AccountRspModel rspModel = new AccountRspModel(user,true);
        return ResponseModel.buildOk(rspModel);
    }



}
