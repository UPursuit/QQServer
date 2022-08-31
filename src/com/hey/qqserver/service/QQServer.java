package com.hey.qqserver.service;

import com.hey.qqcommon.Message;
import com.hey.qqcommon.MessageType;
import com.hey.qqcommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/*
    @author 何恩运
    服务器，监听9999端口，等待客户端的连接并保持通信
*/
@SuppressWarnings({"all"})
public class QQServer {

    private ServerSocket ss = null;
    //创建一个集合存放多个用户，如果是这些用户登录就认为是合法的
    //注意：这里也可以使用ConcurrentHashMap，可以处理并发的集合，没有线程安全问题
    //     HashMap没有处理线程安全，因此在多线程情况下不安全
    //     ConcurrentHashMap处理的线程安全，即线程同步处理，在多线程的情况下是安全的
    private static ConcurrentHashMap<String, User> validUsers = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, ArrayList<Message>> offlineDatabase = new ConcurrentHashMap<>();

    static {//在静态代码块初始化validUsers
        validUsers.put("100", new User("100", "123456"));
        validUsers.put("200", new User("200", "123456"));
        validUsers.put("300", new User("300", "123456"));
        validUsers.put("何恩运", new User("何恩运", "123456"));
        validUsers.put("吴永星", new User("吴永星", "123456"));
        validUsers.put("李霜娇", new User("李霜娇", "123456"));
    }

    //验证用户是否有效的方法
    private boolean checkUser(String userId, String passwd) {

        User user = validUsers.get(userId);
        if (user == null) {//说明userId没有存在validUsers的key中
            return false;
        }
        if (!user.getPasswd().equals(passwd)) {//userId正确但密码错误
            return false;
        }
        return true;
    }

    public QQServer() {
        //注意：端口可以写在配置文件
        try {
            System.out.println("服务端在9998端口监听...");
            //启动推送新闻的线程
            new Thread(new SendNewsToAllService()).start();
            ss = new ServerSocket(9998);

            while (true) {//当和某个客户端连接后会继续监听，因此使用while循环
                Socket socket = ss.accept();
                //得到socket关联的对象输入流
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //得到socket关联的对象输出流
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                User u = (User)ois.readObject();//读取客户端发送的User对象
                //创建一个Message对象，准备回复客户端
                Message message = new Message();
                //验证用户
                if (checkUser(u.getUserID(), u.getPasswd())) {//登录通过
                    message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);
                    //将message对象回复客户端
                    oos.writeObject(message);
                    //创建一个线程，和客户端保持通信，该线程需要持有socket对象
                    ServerConnectClientThread serverConnectClientThread =
                            new ServerConnectClientThread(socket, u.getUserID());
                    //启动该线程
                    serverConnectClientThread.start();
                    //把线程对象放入一个集合中进行管理
                    ManageClientThreads.addClientThread(u.getUserID(), serverConnectClientThread);

                } else {//登陆失败
                    System.out.println("用户id=" + u.getUserID() + " 密码=" + u.getPasswd() + " 验证失败");
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                    oos.writeObject(message);
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //如果服务器退出了while，说明服务器端不在监听，因此需要关闭ServerSocket
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
