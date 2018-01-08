package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.Group;
import com.bj.web.moetalker.push.bean.db.GroupMember;
import com.bj.web.moetalker.push.bean.db.User;

import java.util.Set;

/**
 * 群数据库的处理
 * Created by BJ on 2018/1/8.
 */
public class GroupFactory {

    //查询一个群
    public static Group findById(String groupId) {

        //TODO
        return null;
    }

    //查询一个群，同时该User必须为群的成员，否则返回null
    public static Group findById(User sender, String receiverId) {
        return null;
    }

    //查询一个群的成员
    public static Set<GroupMember> getMembers(Group group) {

        return null;
    }


}
