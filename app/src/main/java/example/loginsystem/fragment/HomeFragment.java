package example.loginsystem.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import example.loginsystem.R;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.HTTP_OK;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    View contentView;
    JSONObject req;
    JSONObject resp;
    TextView libraryName;
    TextView libraryCount;

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
//                    Log.d("测试", (String) msg.obj);
                    break;
                case REQUEST_FAILED:
//                    Log.d("测试", (String) msg.obj);
                    break;
                case REQUEST_OK:
                    try {
                        // 重置界面信息
                        resp = new JSONObject((String)msg.obj).getJSONObject("data");
                        libraryName.setText(resp.getString("name"));
                        libraryCount.setText(resp.getString("count"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("测试", "HomeFragment 创建");
        contentView = inflater.inflate(R.layout.fragment_home, container, false);
        libraryName = contentView.findViewById(R.id.id_textView_libraryName);
        libraryCount = contentView.findViewById(R.id.id_textView_wordCount);

        return contentView;

    }

    @Override
    public void onStart() {
        try {
            sendRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    // 发送请求
    public void sendRequest() throws Exception {
        req = new JSONObject(Global.i.request.toString());
        req.put("libraryId", Global.i.user.getLibraryId());
        Rest2.sendRequest(Config.URL + "GetLibraryById.api", req, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg2 = new Message();
                msg2.what = REQUEST_FAILED;
                msg2.obj = e.getMessage();
                msgHandler.sendMessage(msg2);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HTTP_OK) {
                    try {
                        JSONObject resp1 = new JSONObject(response.body().string());
                        Message msg1 = new Message();
                        msg1.what = REQUEST_OK;
                        msg1.obj = resp1.toString();
                        msgHandler.sendMessage(msg1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
