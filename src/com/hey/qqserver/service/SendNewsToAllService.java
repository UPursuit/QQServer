package com.hey.qqserver.service;

import com.hey.qqcommon.Message;
import com.hey.qqcommon.MessageType;
import com.hey.utils.Utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/*
    @author 何恩运
*/
@SuppressWarnings({"all"})
public class SendNewsToAllService implements Runnable {

    @Override
    public void run() {
        //多次推送新闻 => 使用while
        while (true) {
            System.out.println("请输入服务器要推送的新闻/消息(输入exit退出推送)：");
            String news = Utility.readString(100);
            if ("exit".equals(news)) {
                System.out.println("退出推送服务...");
                break;
            }
            //构建一个群发消息
            Message message = new Message();
            message.setSender("服务器");
            message.setContent(news);
            message.setMesType(MessageType.MESSAGE_TO_ALL_MES);
            message.setSendTime(new Date().toString());
            System.out.println("服务器推送以下消息给所有人：" + news);

            //遍历当前所有的通信线程，得到socket并发送message
            HashMap<String, ServerConnectClientThread> hm = ManageClientThreads.getHm();
            Iterator<String> iterator = hm.keySet().iterator();
            while (iterator.hasNext()) {
                String onlineUserId = iterator.next().toString();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(hm.get(onlineUserId).getSocket().getOutputStream());
                    oos.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
