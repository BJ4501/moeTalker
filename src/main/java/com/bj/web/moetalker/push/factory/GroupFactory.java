package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.api.group.GroupCreateModel;
import com.bj.web.moetalker.push.bean.db.Group;
import com.bj.web.moetalker.push.bean.db.GroupMember;
import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.utils.Hib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 群数据库的处理
 * Created by BJ on 2018/1/8.
 */
public class GroupFactory {

    //通过Id拿群Model
    public static Group findById(String groupId) {
        return Hib.query(session -> session.get(Group.class,groupId));
    }

    //查询一个群，同时该User必须为群的成员，否则返回null
    public static Group findById(User user, String groupId) {
        GroupMember member = getMember(user.getId(), groupId);
        if (member != null){
            return member.getGroup();
        }
        return null;
    }

    //通过名字查找群
    public static Group findByName(String name) {
        return Hib.query(session -> (Group) session
                .createQuery("from Group where lower(name)=:name")
                .setParameter("name",name.toLowerCase())
                .uniqueResult());
    }

    //查询一个群的成员
    public static Set<GroupMember> getMembers(Group group) {
        return Hib.query(session -> {
            @SuppressWarnings("unchecked")
            List<GroupMember> members = session.createQuery("from GroupMember where group=:group")
                    .setParameter("group",group)
                    .list();
            return new HashSet<>(members);
        });
    }

    //创建群
    public static Group create(User creator, GroupCreateModel model, List<User> users) {
        return Hib.query(session -> {
            Group group = new Group(creator,model);
            session.save(group);

            GroupMember ownerMember = new GroupMember(creator,group);
            //设置超级权限->创建者
            ownerMember.setPermissionType(GroupMember.PERMISSION_TYPE_ADMIN_SU);
            //保存，并没有提交到数据库
            session.save(ownerMember);
            for (User user : users) {
                GroupMember member = new GroupMember(user,group);
                //保存，并没有提交到数据库
                session.save(member);
            }
            //可做的操作：立即刷新进数据库
            //session.flush();
            //session.load(group,group.getId());
            return group;
        });
    }

    //获取一个群的成员
    public static GroupMember getMember(String userId, String groupId) {
        return Hib.query(session -> (GroupMember) session
                .createQuery("from GroupMember where userId=:userId and groupId=:groupId")
                .setParameter("userId",userId)
                .setParameter("groupId",groupId)
                .setMaxResults(1)
                .uniqueResult());
    }
}
