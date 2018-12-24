package example.loginsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import example.loginsystem.R;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Popup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;

public class UpdatePasswordActivity extends AppCompatActivity {

    EditText pswEditText;       // 密码框
    EditText vfEditText;        // 确认密码
    Button button;              // 提交按钮
    Popup popup;                // popupWindow

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(UpdatePasswordActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(UpdatePasswordActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:
                    ((Popup)msg.obj).dismiss();
                    break;
                case REQUEST_OK:
                    Intent intent = new Intent(UpdatePasswordActivity.this, OptionActivity.class);
                    intent.putExtra("result", "重置成功, 请重新登陆.");
                    setResult(RESULT_OK, intent);
                    finish();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        pswEditText = (EditText) findViewById(R.id.id_updatePassword_password);
        vfEditText = (EditText) findViewById(R.id.id_updatePassword_verify);
        button = (Button) findViewById(R.id.id_updatePassword_button);
        popup = new Popup(this);

    }

    // 重置密码
    public void resetPassword(View view) {
        // 取得用户的输入
        String password = pswEditText.getText().toString();
        String verify = vfEditText.getText().toString();

        // 验证输入合法性
        if(password.trim().isEmpty()) {
            Toast.makeText(this, "密码不能为空.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(verify)){
            Toast.makeText(this, "两次输入的密码不一致.", Toast.LENGTH_SHORT).show();
            return;
        }

        popup.show(view, 300, -200);

        try {
            JSONObject jreq = new JSONObject(Global.i.request.toString());
            jreq.put("password", password);

            // 请求 api
            Rest2.sendRequest(Config.URL + "SetPassword.api", jreq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {     // 请求失败
                    // 关闭popupwindow
                    Message msg1 = new Message();    // 新建一个消息
                    msg1.what = DISSMISS_POPUPWINDOW;
                    msg1.obj = popup;
                    msgHandler.sendMessage(msg1);

                    // 显示异常信息
                    Message msg2 = new Message();
                    msg2.what = REQUEST_FAILED;
                    msg2.obj = "服务器异常请重试.";
                    msgHandler.sendMessage(msg2);
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == Config.HTTP_CREATED) {
                        Message msg3 = new Message();
                        msg3.what = DISSMISS_POPUPWINDOW;
                        msg3.obj = popup;
                        msgHandler.sendMessage(msg3);

                        Message msg4 = new Message();
                        msg4.what = REQUEST_OK;
                        msgHandler.sendMessage(msg4);
                    } else {
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
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回键
    public void back(View view) {
        finish();
    }
}
