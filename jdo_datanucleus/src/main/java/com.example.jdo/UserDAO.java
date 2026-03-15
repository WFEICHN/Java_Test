package com.example.jdo;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jdo.Query;
import javax.jdo.JDOHelper;
import java.util.List;

/**
 * User 数据访问对象
 */
public class UserDAO {

    private PersistenceManagerFactory pmf;

    public UserDAO() {
        // 创建 PersistenceManagerFactory
        this.pmf = JDOHelper.getPersistenceManagerFactory("MyPersistenceUnit");
    }

    /**
     * 插入用户
     */
    public User insertUser(User user) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(user);
            tx.commit();
            System.out.println("✓ 插入用户成功: " + user);
            return user;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("插入用户失败", e);
        } finally {
            pm.close();
        }
    }

    /**
     * 根据ID查询用户
     */
    public User findUserById(Long id) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query<User> query = pm.newQuery(User.class, "id == :id");
            query.setParameters(id);
            List<User> results = query.executeList();
            if (results.isEmpty()) {
                System.out.println("✗ 未找到ID为 " + id + " 的用户");
                return null;
            }
            User user = pm.detachCopy(results.get(0));
            System.out.println("✓ 查询用户成功: " + user);
            return user;
        } finally {
            pm.close();
        }
    }

    /**
     * 查询所有用户
     */
    public List<User> findAllUsers() {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query<User> query = pm.newQuery(User.class);
            List<User> results = query.executeList();
            List<User> detachedResults = (List<User>) pm.detachCopyAll(results);
            System.out.println("✓ 查询到 " + detachedResults.size() + " 个用户");
            return detachedResults;
        } finally {
            pm.close();
        }
    }

    /**
     * 根据用户名查询用户
     */
    public List<User> findUsersByUsername(String username) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query<User> query = pm.newQuery(User.class, "username == :username");
            query.setParameters(username);
            List<User> results = query.executeList();
            List<User> detachedResults = (List<User>) pm.detachCopyAll(results);
            System.out.println("✓ 找到 " + detachedResults.size() + " 个用户名为 '" + username + "' 的用户");
            return detachedResults;
        } finally {
            pm.close();
        }
    }

    /**
     * 更新用户
     */
    public void updateUser(User user) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            User persistentUser = pm.getObjectById(User.class, user.getId());
            persistentUser.setUsername(user.getUsername());
            persistentUser.setEmail(user.getEmail());
            persistentUser.setAge(user.getAge());
            tx.commit();
            System.out.println("✓ 更新用户成功: " + user);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("更新用户失败", e);
        } finally {
            pm.close();
        }
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            Query<User> query = pm.newQuery(User.class, "id == :id");
            query.setParameters(id);
            List<User> results = query.executeList();
            if (!results.isEmpty()) {
                pm.deletePersistent(results.get(0));
                System.out.println("✓ 删除用户成功，ID: " + id);
            } else {
                System.out.println("✗ 未找到ID为 " + id + " 的用户");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("删除用户失败", e);
        } finally {
            pm.close();
        }
    }

    /**
     * 删除所有用户
     */
    public void deleteAllUsers() {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            Query<User> query = pm.newQuery(User.class);
            long deletedCount = query.deletePersistentAll();
            tx.commit();
            System.out.println("✓ 删除了 " + deletedCount + " 个用户");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("删除所有用户失败", e);
        } finally {
            pm.close();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (pmf != null && !pmf.isClosed()) {
            pmf.close();
        }
    }
}
