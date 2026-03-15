package com.example.jdo;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import java.io.Serializable;

/**
 * JDO 实体类 - User
 */
@PersistenceCapable(table = "users", detachable = "true")
public class User implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Column(name = "id", allowsNull = "false")
    private Long id;

    @Persistent
    @Column(name = "username", length = 50, allowsNull = "false")
    private String username;

    @Persistent
    @Column(name = "email", length = 100)
    private String email;

    @Persistent
    @Column(name = "age")
    private Integer age;

    @Persistent
    @Column(name = "created_at", allowsNull = "false")
    private Long createdAt;

    // 默认构造函数
    public User() {
        this.createdAt = System.currentTimeMillis();
    }

    // 带参数的构造函数
    public User(String username, String email, Integer age) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", createdAt=" + createdAt +
                '}';
    }
}
