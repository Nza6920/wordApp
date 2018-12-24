package example.loginsystem.my.config;

import org.json.JSONObject;

import example.loginsystem.my.bean.User;

// 全局对象
public class Global {
    public static Global i = new Global();

    public User user;                               // 当前登陆用户
    public JSONObject request = new JSONObject();   // 请求对象

    public Integer wordId;       // 当前练习的单词ID

    public Integer reviewCountCn;  // Cn 复习进度

    public Integer reviewCountEn;  // En 复习进度

    public Integer currentTest;      // 当前测试进度


}
