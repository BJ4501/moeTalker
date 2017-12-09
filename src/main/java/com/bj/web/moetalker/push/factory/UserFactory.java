package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.utils.Hib;
import com.bj.web.moetalker.push.utils.TextUtil;
import com.google.common.base.Strings;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

/**
 *  Service 层  用于用户逻辑处理
 */

public class UserFactory {

    /**
     * 通过Token找到User用户信息
     * 只能自己使用，查询的信息是个人信息，不能是他人信息
     * @param token Token
     * @return User
     */
    public static User findByToken(String token){
        //TODO lambda
        return Hib.query(new Hib.Query<User>() {
            @Override
            public User query(Session session) {
                return (User) session.createQuery("from User where token=:inToken")
                        .setParameter("inToken",token).uniqueResult();
            }
        });
    }

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
     * 给当前的账户绑定PushId
     * @param user 自己的User
     * @param pushId 自己设备的PushId
     * @return User
     */
    public static User bindPushId(User user,String pushId){
        //1.查询是否有其他账户绑定了这个设备
        // 取消绑定，避免推送混乱
        // lower忽略大小写
        Hib.queryOnly(session -> {
            @SuppressWarnings("unchecked")
            List<User> userList = (List<User>) session
                    .createQuery("from User where lower(pushId)=:inpushId and id !=:userId")
                    .setParameter("inpushId",pushId.toLowerCase())
                    .setParameter("userId",user.getId())
                    .list();

            for (User u : userList) {
                //更新为null
                u.setPushId(null);
                session.saveOrUpdate(u);
            }
        });

        //2.

        if (pushId.equalsIgnoreCase(user.getPushId())){
            //如果当前需要绑定的设备Id，之前已经绑定过了
            //那么不需要额外绑定
            return user;
        }else {
            //如果我当前账户之前的设备Id，和需要绑定的不同
            //那么需要单点登录，让之前的设备退出账户，给之前的设备推送一条退出消息
            if (Strings.isNullOrEmpty(user.getPushId())){
                //TODO 推送一个退出消息
            }
            //更新新的设备Id
            user.setPushId(pushId);
            return Hib.query(session -> {
                session.saveOrUpdate(user);
                return user;
            });
        }
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
