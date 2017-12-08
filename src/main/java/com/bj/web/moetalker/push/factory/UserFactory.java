package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.utils.Hib;
import com.bj.web.moetalker.push.utils.TextUtil;
import org.hibernate.Session;

/**
 *  Service 层  用于用户逻辑处理
 */

public class UserFactory {

    /**
     * 查询Phone是否重复
     * @param phone
     * @return
     */
    public static User findByPhone(String phone){
        //TODO lambda
        return Hib.query(new Hib.Query<User>() {
            @Override
            public User query(Session session) {
                return (User) session.createQuery("from User where phone=:inPhone")
                        .setParameter("inPhone",phone).uniqueResult();
            }
        });
    }

    /**
     * 查询Name是否重复
     * @param name
     * @return
     */
    public static User findByName(String name){
        //TODO lambda
        return Hib.query(new Hib.Query<User>() {
            @Override
            public User query(Session session) {
                return (User) session.createQuery("from User where name=:inName")
                        .setParameter("inName",name).uniqueResult();
            }
        });
    }

    /**
     * 用户注册 操作需要写入数据库并返回数据库中的User信息
     * @param account 账户
     * @param password 密码
     * @param name 用户名
     * @return User
     */
    public static User register(String account,String password,String name){
        //去除account的首尾空格
        account = account.trim();
        //处理密码--加密
        password = encodePassword(password);

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

    private static String encodePassword(String password){
        //密码去除首尾空格
        password = password.trim();
        //进行MD5加密--非对称
        password = TextUtil.getMD5(password);
        //再进行一次对称的Base64加密，当然可以采取加盐的方案
        return TextUtil.encodeBase64(password);
    }



}
