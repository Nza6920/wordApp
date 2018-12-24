package example.loginsystem.my.config;

// 配置类
public class Config {
//    public final static String URL = "http://192.168.0.109:8088/wordApp/";    // 联网
    public final static String URL = "http://172.20.10.2:8088/wordApp/";        // wifi
//    public final static String URL = "http://192.168.56.1:8088/wordApp/";     // 断网


    // HTTP 返回码
    public final static int HTTP_UNAUTHORIZED = 401;
    public final static int HTTP_OK = 200;
    public final static int HTTP_CREATED = 201;

    // 消息循环常量
    public static final int MESSAGE_ERROR = 1;          // 用户导致的异常
    public static final int REQUEST_FAILED = 2;         // 服务器异常
    public static final int DISSMISS_POPUPWINDOW = 3;   // 关闭popupwindow
    public static final int REQUEST_OK = 6;             // 关闭请求成功
    public static final int REQUEST_OK2 = 7;            // 二次请求成功
    public static final int REVIEW_NULL = 8;           // 没有复习单词

    // ActivityResult 常量
    public static final int REGISTER_OK = 4;            // 注册成功
    public static final int RELANDING = 5;              // 重新登陆

    // 系统变量
    public static final int TEST_TIME = 300000;         // 测试时间

}
