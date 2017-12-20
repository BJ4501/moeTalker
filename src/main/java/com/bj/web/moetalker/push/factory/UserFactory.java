package com.bj.web.moetalker.push.factory;

import com.bj.web.moetalker.push.bean.db.User;
import com.bj.web.moetalker.push.bean.db.UserFollow;
import com.bj.web.moetalker.push.utils.Hib;
import com.bj.web.moetalker.push.utils.TextUtil;
import com.google.common.base.Strings;
import org.hibernate.Session;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
      /*  return Hib.query(new Hib.Query<User>() {
            @Override
            public User query(Session session) {
                return (User) session.createQuery("from User where phone=:inPhone")
                        .setParameter("inPhone",phone).uniqueResult();
            }
        });*/
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

    //通过name找到User
    public static User findById(String id){
        //通过Id查询 更方便
        return Hib.query(session -> session.get(User.class,id));
    }

    /**
     * 更新用户信息到数据库
     * @param user User
     * @return User
     */
    public static User update(User user){
        return Hib.query(session -> {
           session.saveOrUpdate(user);
           return user;
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
            return update(user);
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
        return Hib.query(session -> {
            session.save(user);
            return user;
        });
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
        return update(user);
    }

    /**
     * 获取self的联系人列表
     * @param self User
     * @return List<User>
     */
    public static List<User> contacts(User self){
        return Hib.query(session -> {
            //重新加载一次信息到session中，和当前session绑定
            session.load(self,self.getId());
            //获取关注的人
            Set<UserFollow> flows = self.getFollowing();
            //JAVA8用法 转换为List
            return flows.stream().map(UserFollow::getTarget).collect(Collectors.toList());
        });
    }

    /**
     * 关注人的操作
     * @param origin 发起者
     * @param target 被关注的人
     * @param alias 备注名
     * @return 被关注人的信息
     */
    public static User follow(final User origin,final User target,final String alias){
        UserFollow follow = getUserFollow(origin, target);
        if(follow != null){
            //已关注，直接返回
            return follow.getTarget();
        }
        return Hib.query(session -> {
            //想要操作懒加载的数据，需要重新load一次
            session.load(origin,origin.getId());
            session.load(target,target.getId());
            // 我关注人的时候，同时他也关注我，
            // 所以需要添加两条UserFollow数据
            UserFollow originFollow = new UserFollow();
            originFollow.setOrigin(origin);
            originFollow.setTarget(target);
            originFollow.setAlias(alias);//备注是我对他的备注，他对我默认没有备注

            //发起者是target，origin是被关注者
            UserFollow targetFollow = new UserFollow();
            targetFollow.setOrigin(target);
            targetFollow.setTarget(origin);
            //保存数据库
            session.save(originFollow);
            session.save(targetFollow);
            return target;
        });
    }

    /**
     * 查询两个人是否已经关注
     * @param origin 发起者
     * @param target 被关注的人
     * @return 返回中间类UserFollow
     */
    public static UserFollow getUserFollow(final User origin,final User target){
        return Hib.query(session -> (UserFollow) session
                .createQuery("from UserFollow where originId = :IoriginId and targetId = :ItargetId")
                .setParameter("IoriginId",origin.getId())
                .setParameter("ItargetId",target.getId())
                .setMaxResults(1)
                //查询一条数据
                .uniqueResult());
    }

    /**
     * 搜索联系人的实现
     * @param name 查询的名字，可为空
     * @return 查询到的用户集合，如果name为空，则返回最近的用户
     */
    @SuppressWarnings("unchecked")
    public static List<User> search(String name) {
        if (Strings.isNullOrEmpty(name))
            name = ""; //保证不能为null的情况,减少后面的判断和额外的错误
        final String searchName = "%"+name+"%"; //模糊匹配

        return Hib.query(session -> {
            //查询的条件：name忽略大小写，并且使用like(模糊查询)，头像和描述必须完善才能查询到
            return (List<User>) session.createQuery("from User where lower(name) like :name and portrait is not null and description is not null")
                    .setParameter("name",searchName)
                    .setMaxResults(20)//至多20条
                    .list();
        });

    }
}
