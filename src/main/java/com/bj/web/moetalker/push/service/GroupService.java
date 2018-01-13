package com.bj.web.moetalker.push.service;

import com.bj.web.moetalker.push.bean.api.base.ResponseModel;
import com.bj.web.moetalker.push.bean.api.group.GroupCreateModel;
import com.bj.web.moetalker.push.bean.api.group.GroupMemberAddModel;
import com.bj.web.moetalker.push.bean.api.group.GroupMemberUpdateModel;
import com.bj.web.moetalker.push.bean.card.ApplyCard;
import com.bj.web.moetalker.push.bean.card.GroupCard;
import com.bj.web.moetalker.push.bean.card.GroupMemberCard;
import com.bj.web.moetalker.push.bean.db.Group;
import com.bj.web.moetalker.push.bean.db.GroupMember;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.factory.GroupFactory;
import com.bj.web.moetalker.push.factory.PushFactory;
import com.bj.web.moetalker.push.factory.UserFactory;
import com.bj.web.moetalker.push.provider.LocalDateTimeConverter;
import com.google.common.base.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群组接口入口
 * Created by BJ on 2018/1/13.
 */
@Path("/group")
public class GroupService extends BaseService{

    /**
     * 创建群
     * @param model 基本参数
     * @return 群信息
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupCard> create(GroupCreateModel model){
        if (GroupCreateModel.check(model)){
            return ResponseModel.buildParameterError();
        }

        //创建者
        User creator = getSelf();
        //移除自己，保证创建者不在列表中
        model.getUsers().remove(creator.getId());
        //如果去除创建者之后，还有成员，就可以创建
        if (model.getUsers().size() == 0)
            return ResponseModel.buildParameterError();
        //检查名称是否已存在
        if (GroupFactory.findByName(model.getName()) != null)
            return ResponseModel.buildHaveNameError();

        List<User> users = new ArrayList<>();

        for (String s : model.getUsers()) {
            //fixme 可能有大量操作消耗，可优化
            User user = UserFactory.findById(s);
            if (user == null)
                continue;
            users.add(user);
        }
        //没有一个成员
        if (users.size() == 0)
            return ResponseModel.buildParameterError();

        Group group = GroupFactory.create(creator,model,users);
        if (group == null){
            //服务器异常
            return ResponseModel.buildServiceError();
        }

        //拿到管理员(自己)的信息
        GroupMember creatorMember = GroupFactory.getMember(creator.getId(),group.getId());
        if (creatorMember == null)
            //服务器异常
            return ResponseModel.buildServiceError();

        //拿到群的成员，给所有群成员发送信息：已经被添加到群的信息
        Set<GroupMember> members = GroupFactory.getMembers(group);
        if (members == null)
            return ResponseModel.buildServiceError();
        members = members.stream()
                .filter(groupMember -> {
                    //如果没有匹配的，就都是成立的
                    return !groupMember.getId().equalsIgnoreCase(creatorMember.getId());
                }).collect(Collectors.toSet());

        //开始发起推送
        PushFactory.pushJoinGroup(members);

        return ResponseModel.buildOk(new GroupCard(creatorMember));
    }

    /**
     * 查找群，没有传递参数就是搜索最近所有的群
     * @param name 搜索的参数
     * @return 群信息列表
     */
    @GET
    //正则：姓名可以为空，可以为任意
    @Path("/search/{name:(.*)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupCard>> search(@PathParam("name") @DefaultValue("") String name){
        User self = getSelf();
        List<Group> groups = GroupFactory.search(name);
        if (groups != null && groups.size() > 0){
            List<GroupCard> groupCards = groups.stream()
                    .map(group -> {
                        //使用方法：每一条进行查询
                        GroupMember member = GroupFactory.getMember(self.getId(),group.getId());
                        return new GroupCard(group, member);
                    }).collect(Collectors.toList());
            return ResponseModel.buildOk(groupCards);
        }
        return ResponseModel.buildOk();
    }

    /**
     * 拉取自己当前群的列表
     * @param dateStr 时间字段，不传递，则返回全部当前的群列表；有时间，则返回这个时间之后的加入的群
     * @return 群信息列表
     */
    @GET
    //拉取的是这个时间之后，有没有人新加入了群
    @Path("/list/{date:(.*)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupCard>> list(@DefaultValue("") @PathParam("date") String dateStr){
        User self = getSelf();
        //拿时间
        LocalDateTime dateTime = null;
        if (!Strings.isNullOrEmpty(dateStr)){
            try{
                dateTime = LocalDateTime.parse(dateStr, LocalDateTimeConverter.FORMATTER);
            }catch (Exception e){
                dateTime = null;
            }
        }

        Set<GroupMember> members = GroupFactory.getMembers(self);
        if (members == null||members.size() == 0)
            return ResponseModel.buildOk();
        //进行时间过滤
        final LocalDateTime finalDateTime = dateTime;
        List<GroupCard> groupCards = members.stream()
                .filter(groupMember -> finalDateTime == null||   //时间如果为null，则不做限制
                groupMember.getUpdateAt().isAfter(finalDateTime))  //时间不为null，需要在我的这个时间之后
                .map(GroupCard::new)    // 转换操作
                .collect(Collectors.toList());

        return ResponseModel.buildOk(groupCards);
    }

    /**
     * 获取一个群信息,你必须是这个群的成员
     * @param id 群Id
     * @return 群的信息
     */
    @GET
    @Path("/{groupId}") //http:.../api/group/0000-0000-0000
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupCard> getGroup(@PathParam("groupId") String id){
        if(Strings.isNullOrEmpty(id))
            return ResponseModel.buildParameterError();

        User self = getSelf();
        GroupMember member = GroupFactory.getMember(self.getId(),id);
        if (member == null)
            return ResponseModel.buildNotFoundGroupError(null);

        return ResponseModel.buildOk(new GroupCard(member));
    }


    /**
     * 获取一个群所有群成员信息，你必须是群成员之一才行
     * @param groupId 群Id
     * @return 成员列表
     */
    @GET
    @Path("/{groupId}/members") //http:.../api/group/0000-0000-0000/member
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupMemberCard>> members(@PathParam("groupId") String groupId){
        User self = getSelf();

        //如果没有这个群
        Group group = GroupFactory.findById(groupId);
        if (group == null)
            return ResponseModel.buildNotFoundGroupError(null);
        //检查权限
        GroupMember selfMember = GroupFactory.getMember(self.getId(),groupId);
        if (selfMember == null)
            return ResponseModel.buildNoPermissionError();
        //所有的成员
        Set<GroupMember> members = GroupFactory.getMembers(group);
        if (members == null)
            return ResponseModel.buildServiceError();
        //返回
        List<GroupMemberCard> memberCards = members.stream()
                //.map(groupMember -> new GroupMemberCard(groupMember))
                .map(GroupMemberCard::new)
                .collect(Collectors.toList());
        return ResponseModel.buildOk(memberCards);
    }

    /**
     * 给群添加成员的接口
     * @param groupId 群Id，你必须是这个群的管理者之一
     * @param model 添加成员Model
     * @return 成员列表
     */
    @POST
    @Path("/{groupId}/member")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<GroupMemberCard>> memberAdd(@PathParam("groupId") String groupId, GroupMemberAddModel model){
        if (Strings.isNullOrEmpty(groupId)||!GroupMemberAddModel.check(model))
            return ResponseModel.buildParameterError();
        //拿到自己信息
        User self = getSelf();
        //移除自己之后再进行判断数量
        model.getUsers().remove(self.getId());
        if (model.getUsers().size() == 0)
            return ResponseModel.buildParameterError();

        //如果没有这个群
        Group group = GroupFactory.findById(groupId);
        if (group == null)
            return ResponseModel.buildNotFoundGroupError(null);

        //权限检查:必须是成员，同时还是管理员及其以上级别
        GroupMember selfMember = GroupFactory.getMember(self.getId(),groupId);
        if (selfMember == null||selfMember.getPermissionType()==GroupMember.NOTIFY_LEVEL_NONE)
            return ResponseModel.buildNoPermissionError();

        //已有的成员
        Set<GroupMember> oldMembers = GroupFactory.getMembers(group);
        Set<String> oldMemberUserIds = oldMembers.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());

        List<User> insertUsers = new ArrayList<>();

        for (String s : model.getUsers()) {
            //找人
            User user = UserFactory.findById(s);
            if (user == null)
                continue;
            //已经在群里了
            if (oldMemberUserIds.contains(user.getId()))
                continue;
            insertUsers.add(user);
        }
        //没有一个新增的成员
        if (insertUsers.size() == 0)
            return ResponseModel.buildParameterError();
        //进行添加操作
        Set<GroupMember> insertMembers = GroupFactory.addMembers(group, insertUsers);
        if (insertMembers == null)
            return ResponseModel.buildServiceError();
        //转换成Card
        List<GroupMemberCard> insertCards = insertMembers.stream()
                .map(GroupMemberCard::new)
                .collect(Collectors.toList());
        //进行通知：
        // 1.通知新增的成员，你被加入了XX群
        PushFactory.pushJoinGroup(insertMembers);

        // 2.通知老成员，有XXX加入了群
        PushFactory.pushGroupMemberAdd(oldMembers, insertCards);

        return ResponseModel.buildOk(insertCards);
    }


    /**
     * 群成员信息修改，请求的人，要么是管理员，要么是成员本人
     * @param memberId 成员Id，可以查询对应的群和人
     * @param model 修改的Model
     * @return 当前成员的信息
     */
    @PUT
    @Path("/member/{memberId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<GroupMemberCard> modifyMember(@PathParam("memberId") String memberId, GroupMemberUpdateModel model){
        return null;

    }

    /**
     * 申请加入某一个群
     * 此时会创建一个加入的申请，并写入表；然后会给管理员发送消息
     * 管理员同意，其实就是调用添加成员的接口，把对应的用户添加进去
     * @param groupId 群Id
     * @return 申请的信息
     */
    @POST
    @Path("/applyJoin/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<ApplyCard> join(@PathParam("groupId") String groupId){
        return null;
    }


}
