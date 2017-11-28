package com.bj.web.moetalker.push.bean.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户关系Model，用于用户之间进行好友关系的实现
 */

@Entity
@Table(name = "MT_USERFOLLOW")
public class UserFollow {

    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid" , strategy = "uuid2")
    @Column(updatable = false,nullable = false)
    private String id;

    //定义一个发起人，你关注某人, origin-->你
    //多对一--->你可以关注很多人，你的每一次关注都是一条记录
    //你可以创建很多个关注的信息，所以是多对一
    //这里的多对一是：User对应多个UserFollow
    //optional 不可选，必须存储，一条关注记录一定要有一个“你”
    @ManyToOne(optional = false)
    private User origin;

    //定义关注的目标，你关注的人
    //也是多对一，你可以被很多人关注吗，每次一关注都是一条记录
    //所有就是 多个UserFollow 对应一个 User的情况
    @ManyToOne(optional = false)
    private User target;

    //别名，也就是对target的备注名
    @Column
    private String alias;

    // 定义为创建时间戳，在创建时就已经写入
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    // 定义为更新时间戳，在创建时就已经写入
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOrigin() {
        return origin;
    }

    public void setOrigin(User origin) {
        this.origin = origin;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
