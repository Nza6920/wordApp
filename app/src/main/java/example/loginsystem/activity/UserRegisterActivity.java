package example.loginsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import example.loginsystem.untils.Popup;
import example.loginsystem.R;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.http.Rest2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;

public class UserRegisterActivity extends AppCompatActivity {

    Popup popup;                  // popUpwindow
    ImageView logo;

    EditText unEditText;
    EditText pswEditText;
    EditText vfEditText;

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:    // 用户导致错误
                    Toast.makeText(UserRegisterActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:   // 服务器异常
                    Toast.makeText(UserRegisterActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:     // 关闭popupWindow
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
        setContentView(R.layout.activity_user_register);

        logo = (ImageView)findViewById(R.id.id_register_logo);
        unEditText = (EditText) findViewById(R.id.id_register_username);
        pswEditText = (EditText) findViewById(R.id.id_register_password);
        vfEditText = (EditText) findViewById(R.id.id_register_verify);

        popup = new Popup(this);
        setLogoVisible(true);
    }

    // username 控件监听器
    public void UserNameClicked(View view) {
        setLogoVisible(false);
    }

    // password 控件监听器
    public void passwordCicked(View view) {
        setLogoVisible(false);
    }

    // verify 控件监听器
    public void verifyClicked(View view) {
        setLogoVisible(false);
    }


    // 隐藏或显示 logo
    public void setLogoVisible(boolean isVisible) {
        if (isVisible)
            logo.setVisibility(View.VISIBLE);
        else
            logo.setVisibility(View.GONE);
    }

    // 注册按钮事件
    public void doRegister(View view) {
        // 取得用户的输入
        String username = unEditText.getText().toString();
        String password = pswEditText.getText().toString();
        String verify = vfEditText.getText().toString();

        // 验证输入合法性
        if(password.trim().isEmpty() || username.trim().isEmpty()) {
            Toast.makeText(this, "用户名或密码不能为空.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(verify)){
            Toast.makeText(this, "两次输入的密码不一致.", Toast.LENGTH_SHORT).show();
            return;
        }


        // 显示popupwindow
        popup.show(view);

        JSONObject request = new JSONObject();

        try {
            request.put("username", username);
            request.put("password", password);

            // 发送请求
            Rest2.sendRequest(Config.URL + "Register.api", request, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message msg1 = new Message();
                    msg1.what = DISSMISS_POPUPWINDOW;
                    msg1.obj = popup;
                    msgHandler.sendMessage(msg1);
                    e.printStackTrace();

                    Message msg2 = new Message();
                    msg2.what = REQUEST_FAILED;
                    msg2.obj = "服务器异常请重试.";
                    msgHandler.sendMessage(msg2);
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jresp = new JSONObject(response.body().string());

                        switch (response.code()) {
                            case 403:
                                // 关闭popupwindow
                                Message msg1 = new Message();
                                msg1.what = DISSMISS_POPUPWINDOW;
                                msg1.obj = popup;
                                msgHandler.sendMessage(msg1);

                                Message msg2 = new Message();
                                msg2.what = MESSAGE_ERROR;
                                msg2.obj = jresp.getJSONObject("data").getString("message");
                                msgHandler.sendMessage(msg2);
                                break;
                            case 201:
                                // 关闭popupwindow
                                Message msg3 = new Message();
                                msg3.what = DISSMISS_POPUPWINDOW;
                                msg3.obj = popup;
                                msgHandler.sendMessage(msg3);

                                // 跳转页面
                                Intent intent = new Intent(UserRegisterActivity.this, UserLoginActivity.class);
                                intent.putExtra("result", "注册成功, 请登陆.");
                                setResult(RESULT_OK, intent);
                                // 销毁当前页面
                                finish();
                                break;
                            default:
                                // 关闭popUpwindow
                                Message msg4 = new Message();
                                msg4.what = DISSMISS_POPUPWINDOW;
                                msg4.obj = popup;
                                msgHandler.sendMessage(msg4);

                                Message msg5 = new Message();
                                msg5.what = MESSAGE_ERROR;
                                msg5.obj = jresp.getString("reason");
                                msgHandler.sendMessage(msg5);
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
