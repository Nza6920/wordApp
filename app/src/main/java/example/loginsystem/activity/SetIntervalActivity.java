package example.loginsystem.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import example.loginsystem.R;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Popup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.HTTP_CREATED;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;

public class SetIntervalActivity extends AppCompatActivity {

    Spinner testSpinner;
    Spinner errorSpinner;
    private ArrayAdapter<Integer> spinnerAdapter;
    private List<Integer> spinner_List;
    JSONObject jreq;
    Popup popup;                // popupWindow
    int error;
    int test;

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(SetIntervalActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(SetIntervalActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:
                    ((Popup)msg.obj).dismiss();
                    break;
                case REQUEST_OK:
                    try {
                        JSONObject result = new JSONObject((String) msg.obj);
                        Global.i.user.setErrorInterval(result.getInt("errorInterval"));
                        Global.i.user.setRandInterval(result.getInt("randInterval"));
                        finish();
                        Toast.makeText(SetIntervalActivity.this, "更新成功.", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_interval);

        testSpinner = (Spinner) findViewById(R.id.id_spinner_testInterval);
        errorSpinner = (Spinner) findViewById(R.id.id_spinner_errorInterval);
        popup = new Popup(this);

        try {
            jreq = new JSONObject(Global.i.request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 初始化spinner
        initSpinner();

        testSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                test = spinnerAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        errorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    error = spinnerAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // 返回
    public void back(View view) {
        finish();
    }

    // 初始化spinner
    public void initSpinner()
    {
        spinner_List = new ArrayList<>();
        spinner_List.add(5);
        spinner_List.add(10);
        spinner_List.add(20);
        spinner_List.add(30);
        spinner_List.add(40);
        spinner_List.add(50);

        spinnerAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, spinner_List);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testSpinner.setAdapter(spinnerAdapter);
        errorSpinner.setAdapter(spinnerAdapter);

        // 设置当前所选值
        for (int i = 0; i < spinnerAdapter.getCount(); i++)
        {
            if (Global.i.user.getErrorInterval().equals(spinnerAdapter.getItem(i))) {
                errorSpinner.setSelection(i);
            }
            if (Global.i.user.getRandInterval().equals(spinnerAdapter.getItem(i))) {
                testSpinner.setSelection(i);
            }
        }

    }

    public void submit(View view) {

        if (test <= error) {
            Toast.makeText(this, "间隔数不能大于等于测试数.", Toast.LENGTH_SHORT).show();
        }else{
            try {
                jreq.put("randInterval", test);
                jreq.put("errorInterval", error);

                Rest2.sendRequest(Config.URL + "SetRandandError.api", jreq, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
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
                        if (response.code() == HTTP_CREATED) {
                            Message msg3 = new Message();
                            msg3.what = DISSMISS_POPUPWINDOW;
                            msg3.obj = popup;
                            msgHandler.sendMessage(msg3);

                            Message msg4 = new Message();
                            msg4.what = REQUEST_OK;
                            msg4.obj = jreq.toString();
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
    }
}
