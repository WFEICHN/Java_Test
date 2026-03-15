package com.example.jdo;

import java.util.List;

/**
 * JDO DataNucleus 测试主类
 */
public class JDOTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("JDO DataNucleus + MySQL 测试开始");
        System.out.println("========================================\n");

        UserDAO userDAO = new UserDAO();

        try {
            // 1. 清空数据
            System.out.println("【步骤1】清空现有数据");
            System.out.println("----------------------------------------");
            userDAO.deleteAllUsers();
            System.out.println();

            // 2. 插入测试数据
            System.out.println("【步骤2】插入测试数据");
            System.out.println("----------------------------------------");
            User user1 = new User("张三", "zhangsan@example.com", 25);
            User user2 = new User("李四", "lisi@example.com", 30);
            User user3 = new User("王五", "wangwu@example.com", 28);

            userDAO.insertUser(user1);
            userDAO.insertUser(user2);
            userDAO.insertUser(user3);
            System.out.println();

            // 3. 查询所有用户
            System.out.println("【步骤3】查询所有用户");
            System.out.println("----------------------------------------");
            List<User> allUsers = userDAO.findAllUsers();
            for (User user : allUsers) {
                System.out.println("  → " + user);
            }
            System.out.println();

            // 4. 根据ID查询用户
            System.out.println("【步骤4】根据ID查询用户");
            System.out.println("----------------------------------------");
            if (!allUsers.isEmpty()) {
                Long firstUserId = allUsers.get(0).getId();
                User foundUser = userDAO.findUserById(firstUserId);
                if (foundUser != null) {
                    System.out.println("  → " + foundUser);
                }
            }
            System.out.println();

            // 5. 根据用户名查询
            System.out.println("【步骤5】根据用户名查询");
            System.out.println("----------------------------------------");
            List<User> zhangUsers = userDAO.findUsersByUsername("张三");
            for (User user : zhangUsers) {
                System.out.println("  → " + user);
            }
            System.out.println();

            // 6. 更新用户
            System.out.println("【步骤6】更新用户信息");
            System.out.println("----------------------------------------");
            if (!allUsers.isEmpty()) {
                User userToUpdate = allUsers.get(0);
                userToUpdate.setAge(26);
                userToUpdate.setEmail("zhangsan_new@example.com");
                userDAO.updateUser(userToUpdate);

                // 验证更新
                User updatedUser = userDAO.findUserById(userToUpdate.getId());
                System.out.println("  → 更新后: " + updatedUser);
            }
            System.out.println();

            // 7. 删除指定用户
            System.out.println("【步骤7】删除指定用户");
            System.out.println("----------------------------------------");
            if (allUsers.size() > 1) {
                Long userIdToDelete = allUsers.get(1).getId();
                userDAO.deleteUser(userIdToDelete);
            }
            System.out.println();

            // 8. 查询剩余用户
            System.out.println("【步骤8】查询剩余用户");
            System.out.println("----------------------------------------");
            List<User> remainingUsers = userDAO.findAllUsers();
            for (User user : remainingUsers) {
                System.out.println("  → " + user);
            }
            System.out.println();

            System.out.println("========================================");
            System.out.println("所有测试完成！");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("测试过程中发生错误:");
            e.printStackTrace();
        } finally {
            userDAO.close();
        }
    }
}
