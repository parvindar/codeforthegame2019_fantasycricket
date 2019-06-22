package com.test.fantasycricket;

import java.util.ArrayList;

public class UserInfo {

    static String INR = "\u20B9 ";
    static Boolean logined=false;
    static String usertype;
    static String name;
    static String username;
    static String email;
    static ArrayList<String> contests;
    static Double cash;
    static Integer winnings;
    static Integer xp;

    public static void  login(String _usertype,String _username,String _name,String _email,Double _cash,Integer _winnings,Integer _xp)
    {
        logined=true;
        usertype=_usertype;
        username=_username;
        name= _name;
        email=_email;
        cash=_cash;
        winnings=_winnings;
        xp = _xp;


    }

    public static void logout()
    {
        logined=false;
        username=null;
        usertype=null;
        name=null;
        email=null;
        contests=null;
        cash=null;
        winnings=null;

    }

    private static final UserInfo ourInstance = new UserInfo();

    public static UserInfo getInstance() {
        return ourInstance;
    }

    private UserInfo() {
    }
}
