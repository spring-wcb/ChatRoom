/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: UserService
 * Author:   ITryagain
 * Date:     2019/5/15 18:34
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package server.model.service;

import common.model.entity.User;
import common.util.IOUtil;
import server.DataBuffer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author ITryagain
 * @create 2019/5/15
 * @since 1.0.0
 */

public class UserService {
    private static int idCount = 3; //id

    /** 检查账号是否存在 */
    public boolean isAccountExists(String account){
        List<User> users = loadAllUser();
        for (User user : users) {
            if(account.equals(user.getAccount())){
                return true;
            }
        }
        return false;
    }

    /** 新增用户 */
    public boolean addUser(User user){
        // Check if account already exists
        if(isAccountExists(user.getAccount())){
            return false;
        }
        user.setId(++idCount);
        List<User> users = loadAllUser();
        users.add(user);
        saveAllUser(users);
        return true;
    }

    /** 用户登录 (使用账号) */
    public User login(String account, String password){
        User result = null;
        List<User> users = loadAllUser();
        for (User user : users) {
            if(account.equals(user.getAccount()) && password.equals(user.getPassword())){
                result = user;
                break;
            }
        }
        return result;
    }
    
    /** 用户登录 (使用ID - 保留以兼容旧代码) */
    public User login(long id, String password){
        User result = null;
        List<User> users = loadAllUser();
        for (User user : users) {
            if(id == user.getId() && password.equals(user.getPassword())){
                result = user;
                break;
            }
        }
        return result;
    }

    /** 根据ID加载用户 */
    public User loadUser(long id){
        User result = null;
        List<User> users = loadAllUser();
        for (User user : users) {
            if(id == user.getId()){
                result = user;
                break;
            }
        }
        return result;
    }


    /** 加载所有用户 */
    @SuppressWarnings("unchecked")
    public List<User> loadAllUser() {
        List<User> list = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(
                    new FileInputStream(
                            DataBuffer.configProp.getProperty("dbpath")));

            list = (List<User>)ois.readObject();
        } catch (java.io.FileNotFoundException e) {
            // Database file doesn't exist yet, initialize with default users
            System.out.println("user.db not found, creating with default users...");
            initUser();
            // Try loading again after initialization
            ObjectInputStream ois2 = null;
            try {
                ois2 = new ObjectInputStream(
                        new FileInputStream(
                                DataBuffer.configProp.getProperty("dbpath")));
                list = (List<User>)ois2.readObject();
            } catch (Exception ex) {
                System.err.println("Failed to load users after initialization: " + ex.getMessage());
                list = new CopyOnWriteArrayList<>();
            } finally {
                IOUtil.close(ois2);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            list = new CopyOnWriteArrayList<>();
        }finally{
            IOUtil.close(ois);
        }
        // Ensure we never return null
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
        }
        return list;
    }

    private void saveAllUser(List<User> users) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(
                    new FileOutputStream(
                            DataBuffer.configProp.getProperty("dbpath")));
            //写回用户信息
            oos.writeObject(users);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            IOUtil.close(oos);
        }
    }



    /** 初始化几个测试用户 */
    public void initUser(){
        User user = new User("101", "123", "Admin", 'M', 0);
        user.setId(1);

        User user2 = new User("102", "123", "yong", 'M', 1);
        user2.setId(2);

        User user3 = new User("103", "123", "anni", 'F', 2);
        user3.setId(3);

        List<User> users = new CopyOnWriteArrayList<User>();
        users.add(user);
        users.add(user2);
        users.add(user3);

        this.saveAllUser(users);
    }

    public static void main(String[] args){
        new UserService().initUser();
        List<User> users = new UserService().loadAllUser();
        for (User user : users) {
            System.out.println(user);
        }
    }
}
