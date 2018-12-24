package example.loginsystem.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import example.loginsystem.R;
import example.loginsystem.my.bean.User;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Popup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REGISTER_OK;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;

public class UserLoginActivity extends AppCompatActivity {

    Popup popup;            // popUpwindow
    EditText unEditText;    // 用户名
    EditText psEditText;    // 密码
    ImageView logo;

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(UserLoginActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(UserLoginActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:
                    ((Popup)msg.obj).dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        popup = new Popup(this);

        unEditText = (EditText) findViewById(R.id.id_login_username);
        psEditText = (EditText) findViewById(R.id.id_login_password);
        logo = (ImageView) findViewById(R.id.id_login_logo);

        // 从配置里加载
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");
        String password = sharedPref.getString("password", "");
        if (username.length() > 0 && password.length() > 0)
        {
            // 自动填写用户名和密码
            unEditText.setText(username);
            psEditText.setText(password);
        }

        // 重新登陆
        try {
            Intent intent =  getIntent();
            if (intent != null) {
                if (! intent.getStringExtra("result").isEmpty()) {
                    Toast.makeText(this, getIntent().getStringExtra("result"), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }

    }

    // 延时登陆
    private void autoLogin() {
        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doLogin(null);
            }
        }, 1500);   // 延时 1500 ms 后执行一个 Runable 线程
    }

    // 点击登陆按钮
    public void doLogin(View view) {
        // 取得用户界面输入
        String username = unEditText.getText().toString();
        String password = psEditText.getText().toString();

        // 判断用户输入
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(this, "用户名或密码不能为空.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存当前用户信息, 以便下一次开机启动时加载
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        // 显示popupwindow
        popup.show(view);

        // 请求数据
        JSONObject request = new JSONObject();

        try {
            request.put("username", username);
            request.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 验证用户信息
        try {
            Rest2.sendRequest(Config.URL + "Login.api", request, new Callback() {
                // 请求失败
                @Override
                public void onFailure(Call call, IOException e) {

                    // 关闭popupwindow
                    Message msg5 = new Message();    // 新建一个消息
                    msg5.what = DISSMISS_POPUPWINDOW;
                    msg5.obj = popup;
                    msgHandler.sendMessage(msg5);

                    // 显示异常信息
                    Message msg6 = new Message();
                    msg6.what = REQUEST_FAILED;
                    msg6.obj = "服务器异常请重试.";
                    msgHandler.sendMessage(msg6);
                    e.printStackTrace();
                }

                // 请求成功
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject resp = new JSONObject(response.body().string());

                        switch (response.code()) {
                            case Config.HTTP_UNAUTHORIZED:
                                // 登陆失败
                                String reason1 = resp.getJSONObject("data").getString("message");   // 失败原因

                                // 关闭popupwindow
                                Message msg1 = new Message();    // 新建一个消息
                                msg1.what = DISSMISS_POPUPWINDOW;
                                msg1.obj = popup;
                                msgHandler.sendMessage(msg1);

                                Message msg2 = new Message();    // 新建一个消息
                                msg2.what = MESSAGE_ERROR;       // 消息类型
                                msg2.obj = reason1;              // 消息内容
                                msgHandler.sendMessage(msg2);    // 把消息发送出去
                                break;
                            case Config.HTTP_OK:
                                // 登陆成功
                                loginSuccess(resp);
                                break;
                            default:
                                String reason2 = resp.getString("reason");

                                // 关闭popupwindow
                                Message msg3 = new Message();    // 新建一个消息
                                msg3.what = DISSMISS_POPUPWINDOW;
                                msg3.obj = popup;
                                msgHandler.sendMessage(msg3);

                                // 服务器出错
                                Message msg4 = new Message();    // 新建一个消息
                                msg4.what = REQUEST_FAILED;
                                msg4.obj = reason2;
                                msgHandler.sendMessage(msg4);
                                break;
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    // 登陆成功具体逻辑
    public void loginSuccess(JSONObject resp) throws Exception {
        JSONObject success = resp.getJSONObject("data");
        User user = new User(success.getInt("userId"),
                success.getString("username"),
                null,
                null,
                null,
                success.getString("updated_at"));

        Global.i.user = user;
        Global.i.request.put("updated_at", user.getUpdated_at());
        Global.i.request.put("userId", user.getId());
        Global.i.request.put("username", user.getName());
        Global.i.request.put("apiKey", success.getString("apiKey"));

        // 请求当前用户设置
        Rest2.sendRequest(Config.URL + "GetOption.api", Global.i.request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject optionResp = new JSONObject(response.body().string()).getJSONObject("data");

//                    Log.d("测试",  String.valueOf(optionResp.has("libraryId1")));

                    Global.i.user.setErrorInterval(optionResp.getInt("errorInterval"));
                    Global.i.user.setLibraryId((optionResp.has("libraryId") ? optionResp.getInt("libraryId") : null));
                    Global.i.user.setRandInterval(optionResp.getInt("randInterval"));
                    Global.i.wordId = optionResp.has("currentWord") ? optionResp.getInt("currentWord") : null;
                    Global.i.reviewCountCn = optionResp.has("currentReview") ? optionResp.getInt("currentReview") : 1;
                    Global.i.reviewCountEn = optionResp.has("currentReview2") ? optionResp.getInt("currentReview2") : 1;
                    Global.i.currentTest = optionResp.has("currentTest") ? optionResp.getInt("currentTest") : 1;
                    Log.d("当前登陆用户", Global.i.user.toString() + "\n" + Global.i.wordId + "\n" + Global.i.reviewCountCn + "  " + Global.i.reviewCountEn + " " + Global.i.currentTest);
                    // 进入主界面

                    // 关闭popupwindow
                    Message msg5 = new Message();
                    msg5.what = DISSMISS_POPUPWINDOW;
                    msg5.obj = popup;
                    msgHandler.sendMessage(msg5);

                    Intent intent2 = new Intent(UserLoginActivity.this, MainActivity.class);
                    startActivity(intent2);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 点击 "点我注册"
    public void doRegister(View view)
    {
        Intent intent = new Intent(this, UserRegisterActivity.class);
        startActivityForResult(intent, REGISTER_OK);
    }

    // username 控件点击事件
    public void usernameClieked(View view)
    {
        setLogoVisible(false);
    }

    // password 控件点击事件
    public void passwordCicked(View view)
    {
        setLogoVisible(false);
    }

    // 隐藏或显示 logo
    public void setLogoVisible(boolean isVisible)
    {
        if (isVisible)
            logo.setVisibility(View.VISIBLE);
        else
            logo.setVisibility(View.GONE);
    }

    // 接收返回消息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_OK:
                if (resultCode == RESULT_OK) {
                    // 清空输入框
                    unEditText.setText("");
                    psEditText.setText("");
                    String result = data.getStringExtra("result");
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                    break;
                }
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "onStart被调用");
        setLogoVisible(true);
    }
}
