package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.utils.Hib;
import org.hibernate.Session;

/**
 *  Service 层  用于用户逻辑处理
 */

public class UserFactory {


    /**
     * 用户注册 操作需要写入数据库并返回数据库中的User信息
     * @param account 账户
     * @param password 密码
     * @param name 用户名
     * @return User
     */
    public static User register(String account,String password,String name){
        User user = new User();

        user.setName(name);
        user.setPassword(password);
        //account 为手机号码
        user.setPhone(account);

        //数据库操作，创建一个会话
        Session session = Hib.session();
        //开启一个事务
        session.beginTransaction();
        try{
            //保存操作
            session.save(user);
            //提交操作
            session.getTransaction().commit();
            return user;
        }catch (Exception e){
            //失败回滚
            session.getTransaction().rollback();
            return null;
        }


    }


}
