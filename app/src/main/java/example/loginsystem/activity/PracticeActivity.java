package example.loginsystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import example.loginsystem.R;
import example.loginsystem.my.bean.Word;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;

public class PracticeActivity extends AppCompatActivity {

    TextView currentCount;     // 当前单词数
    TextView totalCount;       // 单词总数
    TextView wordContent;      // 单词内容
    TextView wordTranslation;  // 单词翻译
    JSONObject resp;           // 请求返回数据
    List<Word> words;          // 单词列表
    JSONArray reviews;         // 复习列表
    Integer count;             // 当前位置

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(PracticeActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(PracticeActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_OK:
                    try {
                        resp = new JSONObject((String) msg.obj);
                        // 初始化界面数据
                        initWordList(resp);
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
        setContentView(R.layout.activity_practice);

        currentCount = (TextView) findViewById(R.id.id_textView_currentCount);
        totalCount = (TextView) findViewById(R.id.id_textView_totalCount);
        wordContent = (TextView) findViewById(R.id.id_textView_wordContent);
        wordTranslation = (TextView) findViewById(R.id.id_textView_wordTranslation);
        reviews = new JSONArray();

        try {
            // 发送请求
            sendPost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 认识, 点击事件
    public void know(View view) {
        // 判断是否有单词
        if (words == null || words.size()<=0) {
            Toast.makeText(this, "服务器异常", Toast.LENGTH_SHORT).show();
        }

        // 判断个数是否越界
        if (count >= words.size()) {
            Toast.makeText(this, "已全部背完, 快去复习一下吧!", Toast.LENGTH_SHORT).show();
            return;
        }

        wordContent.setText(words.get(count).getContent());
        wordTranslation.setText(words.get(count).getTranslation());
        Global.i.wordId = words.get(count).getId();

        try {
            Word temp = words.get(count-1);
            JSONObject word = new JSONObject();
            word.put("id", temp.getId());
            word.put("content", temp.getContent());
            word.put("translation", temp.getTranslation());
            word.put("library_id", temp.getLibrary_id());
            reviews.put(word);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        count++;

        currentCount.setText(String.valueOf(count));   // 设置标题
        wordTranslation.setVisibility(View.GONE);
    }

    // 不认识, 点击事件
    public void notKnow(View view) {
        wordTranslation.setVisibility(View.VISIBLE);
    }

    // 返回主页, 点击事件
    public void backToHome(View view) {
        finish();
    }

    // 发送请求
    public void sendPost() throws Exception {
        JSONObject req = new JSONObject(Global.i.request.toString());
        req.put("libraryId", Global.i.user.getLibraryId());
        // 发起请求
        Rest2.sendRequest(Config.URL + "GetWord.api", req, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg1 = new Message();
                msg1.what = REQUEST_FAILED;
                msg1.obj = e.getMessage();
                msgHandler.sendMessage(msg1);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg2 = new Message();
                msg2.what = REQUEST_OK;
                msg2.obj = response.body().string();
                msgHandler.sendMessage(msg2);
            }
        });
    }

    // 初始化界面数据
    public void initWordList(JSONObject resp) throws JSONException {
        JSONArray wordsJson = resp.getJSONArray("data");
        words = new ArrayList<Word>();

        for (int i = 0;i< wordsJson.length(); i++) {
            JSONObject temp =  wordsJson.getJSONObject(i);
            words.add(new Word(temp.getInt("id"),
                    temp.getInt("library_id"),
                    temp.getString("content"),
                    temp.getString("translation")
            ));
        }

        if (Global.i.wordId == null) {
            wordContent.setText(words.get(0).getContent());
            wordTranslation.setText(words.get(0).getTranslation());
            totalCount.setText(String.valueOf(words.size()));
            currentCount.setText(String.valueOf(1));
            wordTranslation.setVisibility(View.GONE);
            count = 1;
        } else {
            for (int i = 0; i < words.size(); i++) {
                Word temp = words.get(i);
                if (temp.getId() == Global.i.wordId) {
                    wordContent.setText(temp.getContent());
                    wordTranslation.setText(temp.getTranslation());
                    totalCount.setText(String.valueOf(words.size()));
                    currentCount.setText(String.valueOf(i+1));
                    count = i+1;
                    wordTranslation.setVisibility(View.GONE);
                    break;
                }
                if (i == words.size() - 1) {
                    Toast.makeText(this, "当前词库已背完, 换本书背吧!", Toast.LENGTH_SHORT).show();

                    // 延时执行一个线程
                    msgHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(PracticeActivity.this, MyLibraryActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 2500);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 上传复习单词
        if (reviews.length() > 0) {
            try {
                JSONObject req1 = new JSONObject(Global.i.request.toString());
                req1.put("wordList", reviews);
                Log.d("测试", req1.toString(2));
                Rest2.sendRequest(Config.URL + "AddReviewWords.api", req1, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            Log.d("测试", new JSONObject(response.body().string()).toString(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 上传当前单词Id
        if (Global.i.wordId != null) {
                try {

                    JSONObject req2 = new JSONObject(Global.i.request.toString());
                    req2.put("wordId", Global.i.wordId);
                    Rest2.sendRequest(Config.URL + "SetCurrentWord.api", req2, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        super.onDestroy();
    }
}
