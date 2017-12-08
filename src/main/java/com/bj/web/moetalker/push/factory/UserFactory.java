package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.utils.Hib;
import com.bj.web.moetalker.push.utils.TextUtil;
import org.hibernate.Session;

import java.util.UUID;

/**
 *  Service 层  用于用户逻辑处理
 */

public class UserFactory {

    /**
     * 查询Phone是否重复--通过Phone找到User
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
     * 查询Name是否重复--通过name找到User
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
     * 使用账户和密码进行登录
     * @param account
     * @param password
     * @return
     */
    public static User login(String account,String password){
        final String accountStr = account.trim();
        //数据库中存储的是密文，所以需要加密后比较
        final String encodePassword = encodePassword(password);

        //寻找是否存在
        User user = Hib.query(session -> {
           return (User) session.createQuery("from User where phone=:inPhone and password=:inPassword")
                    .setParameter("inPhone",accountStr)
                    .setParameter("inPassword",encodePassword)
                    .uniqueResult();
        });

        if (user != null){
            //对User进行登录操作，更新Token
            user = login(user);
        }
        return user;
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

        User user = createUser(account, password, name);
        if (user != null){
            user = login(user);
        }
        return user;
    }

    /**
     * 密码加密
     * @param password 原密码
     * @return 加密后密码
     */
    private static String encodePassword(String password){
        //密码去除首尾空格
        password = password.trim();
        //进行MD5加密--非对称
        password = TextUtil.getMD5(password);
        //再进行一次对称的Base64加密，当然可以采取加盐的方案
        return TextUtil.encodeBase64(password);
    }

    /**
     * 创建用户方法--注册部分
     * @param account 账号--手机号码
     * @param password 密码--加密后的
     * @param name 姓名
     * @return User
     */
    private static User createUser(String account,String password,String name){
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        //account 为手机号码
        user.setPhone(account);

        //lambda 数据库存储
        return Hib.query(session -> (User)session.save(user));
    }

    /**
     * 登录验证功能
     * 本质上就是对Token进行操作
     * @param user User
     * @return User
     */
    private static User login(User user){
        //使用一个随机的UUID值充当Token
        String newToken = UUID.randomUUID().toString();
        //进行一次Base64格式化操作
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);
        return Hib.query(session -> {
           session.saveOrUpdate(user);
           return user;
        });
    }



}
