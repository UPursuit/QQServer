package com.hey.qqcommon;

import java.io.Serializable;

/*
    @author 何恩运
    表示一个用户/客户信息
*/
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String userID;
    private String passwd;

    public User() { }

    public User(String userID, String passwd) {
        this.userID = userID;
        this.passwd = passwd;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
