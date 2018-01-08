package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.api.message.MessageCreateModel;
import com.bj.web.moetalker.push.bean.card.MessageCard;
import com.bj.web.moetalker.push.bean.card.UserCard;
import com.bj.web.moetalker.push.bean.db.Group;
import com.bj.web.moetalker.push.bean.db.Message;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.MessageFactory;
import com.bj.web.moetalker.push.factory.PushFactory;
import com.bj.web.moetalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * 消息发送入口
 * Created by BJ on 2018/1/8.
 */
@Path("/msg")
public class MessageService extends BaseService{

    //发送一条消息到服务器
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<MessageCard> pushMessage(MessageCreateModel model){
        if (!MessageCreateModel.check(model)){
            //返回参数异常
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        //查询是否已经在数据库中有了
        Message message = MessageFactory.findById(model.getId());
        if (message != null){
            //正常返回
            return ResponseModel.buildOk(new MessageCard(message));
        }
        if (model.getReceiverType() == Message.RECEIVER_TYPE_GROUP){
            return pushToGroup(self, model);
        }else {
            return pushToUser(self, model);
        }
    }

    //发送到人
    private ResponseModel<MessageCard> pushToUser(User sender, MessageCreateModel model) {
        User receiver = UserFactory.findById(model.getReceiverId());
        if (receiver == null) {
            return ResponseModel.buildNotFoundUserError("未找到接收者");
        }
        if (receiver.getId().equals(sender.getId())){
            //发送者接收者是同一个人就返回创建失败
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }
        //存储数据库
        Message message = MessageFactory.add(sender,receiver,model);
        return buildAndPushResponse(sender,message);
    }

    //发送到群
    private ResponseModel<MessageCard> pushToGroup(User sender, MessageCreateModel model) {
        //Group group = GroupFactory.findById();
        //TODO Group
        return null;
    }

    //推送并构建一个返回信息
    private ResponseModel<MessageCard> buildAndPushResponse(User sender, Message message) {
        if (message == null){
            //存储数据库失败
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }
        //进行推送
        PushFactory.pushNewMessage(sender,message);

        //返回
        return ResponseModel.buildOk(new MessageCard(message));
    }
}
