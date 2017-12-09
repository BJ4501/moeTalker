package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.AccountRspModel;
import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.api.user.UpdateInfoModel;
import com.bj.web.moetalker.push.bean.card.UserCard;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.google.common.base.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 用户信息处理 Controller
 */
//真实访问路径---> localhost/api/user
@Path("/user")
public class UserService {

    /**
     * 用户信息修改接口
     * @param token
     * @param model
     * @return 自己的个人信息
     */
    @PUT
    //@Path("/")
    //指定请求与返回的响应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(@HeaderParam("token") String token,
                                          UpdateInfoModel model){
        if(Strings.isNullOrEmpty(token)||!UpdateInfoModel.check(model)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        //拿到自己的个人信息
        User user = UserFactory.findByToken(token);
        if(user != null){
            //更新用户信息
            user = model.updateToUser(user);
            user = UserFactory.update(user);
            //构架自己的用户信息
            UserCard card = new UserCard(user,true);
            //返回
            return ResponseModel.buildOk(card);
        }else {
            //Token失效，所以无法进行绑定
            return ResponseModel.buildAccountError();
        }



    }

}
