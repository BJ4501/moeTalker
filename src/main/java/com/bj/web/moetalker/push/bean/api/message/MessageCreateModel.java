package com.bj.web.moetalker.push.bean.api.message;

import com.bj.web.moetalker.push.bean.api.user.UpdateInfoModel;
import com.bj.web.moetalker.push.bean.db.Message;
import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;

/**
 * API请求Model的格式
 * Created by BJ on 2018/1/8.
 */
public class MessageCreateModel {

    //ID从客户端，UUID
    @Expose
    private String id;

    //内容
    @Expose
    private String content;

    //附件
    @Expose
    private String attach;

    //消息类型
    @Expose
    private int type = Message.TYPE_STR;

    //接收者，可为空
    @Expose
    private String receiverId;

    //接收者类型，群，人
    @Expose
    private int receiverType = Message.RECEIVER_TYPE_NONE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }

    public static boolean check(MessageCreateModel model) {
        // Model 不允许为null，
        // 并且只需要具有一个及其以上的参数即可
        return model != null
                && !(Strings.isNullOrEmpty(model.id)
                ||Strings.isNullOrEmpty(model.content)
                ||Strings.isNullOrEmpty(model.receiverId))
                //需要满足只能是这两种接收者类型
                && (model.receiverType == Message.RECEIVER_TYPE_NONE
                ||model.receiverType == Message.RECEIVER_TYPE_GROUP)
                //需要满足只能是这四种消息类型
                && (model.type == Message.TYPE_STR
                ||model.type == Message.TYPE_AUDIO
                ||model.type == Message.TYPE_FILE
                ||model.type == Message.TYPE_PIC);
    }
}
