package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.account.AccountRspModel;
import com.bj.web.moetalker.push.bean.api.base.PushModel;
import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.api.user.UpdateInfoModel;
import com.bj.web.moetalker.push.bean.card.UserCard;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.bj.web.moetalker.push.utils.PushDispatcher;
import com.google.common.base.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 拉取联系人
     * @return
     */
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact(){
        User self = getSelf();

        //拿到我的联系人
        List<User> users = UserFactory.contacts(self);
        //map操作，相当于转置操作，User->UserCard
        List<UserCard> userCards = users.stream()
                .map(user -> new UserCard(user,true)).collect(Collectors.toList());

        //返回
        return ResponseModel.buildOk(userCards);
    }

    // 关注人，
    // 简化：双方同时关注
    //TODO： 关注人需要发送请求对方同意，才可以互为好友
    @PUT //修改类，使用PUT
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId){
        User self = getSelf();
        //自己不能关注自己
        if (self.getId().equalsIgnoreCase(followId)||Strings.isNullOrEmpty(followId)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        //找到我要关注的人
        User followUser = UserFactory.findById(followId);
        if (followUser == null){
            //未找到人
            return ResponseModel.buildNotFoundUserError(null);
        }
        //备注默认没有，后面可以扩展
        followUser = UserFactory.follow(self,followUser,null);
        if (followUser == null){
            //关注失败，返回服务器异常
            return ResponseModel.buildServiceError();
        }
        //TODO 通知我关注的人我关注了他

        //返回关注的人的信息
        return ResponseModel.buildOk(new UserCard(followUser,true));
    }

    //获取某人的信息
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id){
        if (Strings.isNullOrEmpty(id)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        User self = getSelf();
        if(self.getId().equalsIgnoreCase(id)){
            //返回自己不必查询数据库
            return ResponseModel.buildOk(new UserCard(self,true));
        }

        User user = UserFactory.findById(id);
        if (user == null){
            //没找到，返回没找到用户异常
            return ResponseModel.buildNotFoundUserError(null);
        }

        //如果我们直接有关注的记录，则我已关注需要查询信息的用户
        boolean isFollow = UserFactory.getUserFollow(self,user) != null;
        return ResponseModel.buildOk(new UserCard(user,isFollow));
    }

    /**
     * 搜索联系人
     * 数量限制简化分页：只返回20条数据
     */
    @GET //搜索人，不涉及数据更改，只是查询，则为GET
    @Path("/search/{name:(.*)?}") //名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name){
        User self = getSelf();
        //先查询数据
        List<User> searchUsers = UserFactory.search(name);
        //把查询的人封装为UserCard
        //判断这些人是否有我已经关注的人，
        //如果有，则返回的关注状态中应该已经设置好状态

        //拿出我的联系人
        final List<User> contacts = UserFactory.contacts(self);

        //User->UserCard
        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    //判断这个人是否是我自己，在我的联系人中
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            //进行联系人的任意匹配，匹配其中的Id字段
                            || contacts.stream().anyMatch(
                            contactUser -> contactUser.getId().equalsIgnoreCase(user.getId())
                    );
                    return new UserCard(user,isFollow);
                }).collect(Collectors.toList());
        //TODO 双重查询--低效，如果用户数量过多就不可以这么做。需要优化
        //返回
        return ResponseModel.buildOk(userCards);
    }
}
