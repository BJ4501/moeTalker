package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.AccountRspModel;
import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.api.user.UpdateInfoModel;
import com.bj.web.moetalker.push.bean.card.UserCard;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.google.common.base.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * 用户信息处理 Controller
 */
//真实访问路径---> localhost/api/user
@Path("/user")
public class UserService extends BaseService{

    /**
     * 用户信息修改接口
     * @param model
     * @return 自己的个人信息
     */
    @PUT
    //@Path("/")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel model){
        if(!UpdateInfoModel.check(model)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        User self = getSelf();
        //更新用户信息
        self = model.updateToUser(self);
        self = UserFactory.update(self);
        //构架自己的用户信息
        UserCard card = new UserCard(self,true);
        //返回
        return ResponseModel.buildOk(card);
    }

}
