package com.bj.web.moetalker.push.bean.api.group;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by BJ on 2018/1/13.
 */
public class GroupMemberAddModel {

    @Expose
    private Set<String> users = new HashSet<>();

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public static boolean check(GroupMemberAddModel model){
        return !(model.users == null || model.users.size() == 0);
    }
}
