package example.loginsystem.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import example.loginsystem.R;
import example.loginsystem.my.bean.Word;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Helper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;
import static example.loginsystem.my.config.Config.REVIEW_NULL;

public class ReviewByENActivity extends AppCompatActivity {
    // 对错控件
    ImageView yOrN1;
    ImageView yOrN2;
    ImageView yOrN3;
    ImageView yOrN4;

    // 按钮控件
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    TextView currentCount;      // 当前位置
    TextView totalCount;        // 本次复习单词总数
    TextView wordTranslation;   // 单词翻译

    ArrayList<Word> words;       // 单词列表
    ArrayList<Word> unskillWords;    // 生词列表
    Word currentWord;            // 当前单词
    Button[] buttons;            // 按钮数组
    int[] buttonRandoms;         // 按钮数组下标
    int count;               // 当前单词位置

    // 图片资源
    Drawable yes;
    Drawable no;


    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(ReviewByENActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(ReviewByENActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_OK:
                    try {
                        JSONObject resp = new JSONObject((String)msg.obj);
                        JSONArray wordsJ = resp.getJSONArray("data");
                        for (int i = 0; i < wordsJ.length(); i++) {
                            JSONObject wordJ = wordsJ.getJSONObject(i);

                            if (wordJ.getInt("skill_level") <= 5) {
                                Word word = new Word(wordJ.getInt("id"),
                                        wordJ.getInt("library_id"),
                                        wordJ.getString("content"),
                                        wordJ.getString("translation"),
                                        wordJ.getInt("skill_level")
                                );
                                words.add(word);
                            }
                        }

                        count = 1;

                        nextWord();

                    } catch (Exception e) {
                        Toast.makeText(ReviewByENActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
                case REVIEW_NULL:
                    try {
                        reviewNull(msg);
                    } catch (Exception e) {
                        Toast.makeText(ReviewByENActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_review_by_en);
        // 初始化变量
        initView();
        // 设置按钮
        try {
            // 发送请求
            sendPost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 下一个单词
    public void nextWord()
    {
        // 防止越界
        if (count > words.size()) {
            Toast.makeText(this, "今天的任务完成啦, 明天再来吧!", Toast.LENGTH_SHORT).show();
            Global.i.reviewCountEn = words.size() + 1;
            msgHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ReviewByENActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);
            return;
        }

        buttonRandoms = Helper.randomCommon(1,5,4);         // 按钮下标

        currentWord = words.get(count-1);                   // 当前单词
        totalCount.setText(String.valueOf(words.size()));   // 本次复习总数
        currentCount.setText(String.valueOf(count));        // 当前单词位置

        // 设置翻译
        wordTranslation.setText(currentWord.getContent());
        wordTranslation.setTag(currentWord);

        buttons[buttonRandoms[0] - 1].setTag(currentWord);
        buttons[buttonRandoms[0] - 1].setText(currentWord.getTranslation());

        ArrayList<Word> words2 = new ArrayList<>(words);
        words2.remove(count - 1);

        int[] randWordIndex = Helper.randomCommon(1, words2.size(), 3);
        for (int i = 1; i < buttonRandoms.length; i++) {
            Word word = words2.get(randWordIndex[i-1]);
            buttons[buttonRandoms[i] - 1].setTag(word);
            buttons[buttonRandoms[i] - 1].setText(word.getTranslation());
        }

        count++;
    }

    // 没有复习单词
    public void reviewNull(Message msg) throws JSONException {
        JSONObject resp = new JSONObject((String)msg.obj).getJSONObject("data");
        String tips = resp.getString("message");
        Toast.makeText(ReviewByENActivity.this, tips + ",快去背几个吧.", Toast.LENGTH_LONG).show();
        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    // 发起请求
    public void sendPost() throws Exception {
        JSONObject req = new JSONObject(Global.i.request.toString());
        req.put("offset", Global.i.reviewCountEn);

        Rest2.sendRequest(Config.URL + "GetReviewWords.api", req, new okhttp3.Callback() {
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
                try {
                    // 判断是否有复习单词
                    if (response.code() == 403) {
                        Message msg4 = new Message();
                        msg4.what = REVIEW_NULL;
                        msg4.obj = new JSONObject(response.body().string()).toString();
                        msgHandler.sendMessage(msg4);
                    }else {
                        Message msg2 = new Message();
                        msg2.what = REQUEST_OK;
                        msg2.obj = new JSONObject(response.body().string()).toString();
                        msgHandler.sendMessage(msg2);
                    }
                } catch (Exception e) {
                    Message msg3 = new Message();
                    msg3.what = REQUEST_FAILED;
                    msg3.obj = e.getMessage();
                    msgHandler.sendMessage(msg3);
                    e.printStackTrace();
                }
            }
        });
    }

    // 初始化变量
    public void initView()
    {
        yOrN1 = (ImageView) findViewById(R.id.id_imageView_reviewByEn_yesOrNo1);
        yOrN2 = (ImageView) findViewById(R.id.id_imageView_reviewByEn_yesOrNo2);
        yOrN3 = (ImageView) findViewById(R.id.id_imageView_reviewByEn_yesOrNo3);
        yOrN4 = (ImageView) findViewById(R.id.id_imageView_reviewByEn_yesOrNo4);

        button1 = (Button) findViewById(R.id.id_button_reviewByEn_contentButton1);
        button2 = (Button) findViewById(R.id.id_button_reviewByEn_contentButton2);
        button3 = (Button) findViewById(R.id.id_button_reviewByEn_contentButton3);
        button4 = (Button) findViewById(R.id.id_button_reviewByEn_contentButton4);

        currentCount = (TextView) findViewById(R.id.id_textView_reviewByEn_currentCount);
        totalCount = (TextView) findViewById(R.id.id_textView_reviewByEn_totalCount);
        wordTranslation = (TextView) findViewById(R.id.id_textView_reviewByEn_translation);

        // 设置图片不可见
        imageVorG(yOrN1, false);
        imageVorG(yOrN2, false);
        imageVorG(yOrN3, false);
        imageVorG(yOrN4, false);

        // 数组
        words = new ArrayList<Word>();       // 单词列表
        unskillWords = new ArrayList<Word>();    // 生词列表
        buttons = new Button[] {button1,button2,button3,button4};  // 按钮数组

        // 图片资源
        yes = getDrawable(R.drawable.ic_yes);
        no = getDrawable(R.drawable.ic_no);

    }

    // 设置图片是否可见
    public void imageVorG(View image, boolean vOrg)
    {
        if (vOrg)
            image.setVisibility(View.VISIBLE);
        else
            image.setVisibility(View.INVISIBLE);
    }

    // 显示图片并在 time(ms) 后消失
    public void imageVandG(final View image, int time)
    {
        imageVorG(image, true);

        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageVorG(image, false);
            }
        }, time);
    }

    // 正确的情况调用
    public void imageVandG(final View image, int time, final MyCallback callback) {
        imageVorG(image, true);

        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.toDoSth();
                imageVorG(image, false);
            }
        }, time);
    }

    // 返回主页
    public void backToHome(View view) {
        finish();
    }

    // 加入生词本
    public void addToUnskill(View view) {
        unskillWords.add(currentWord);
        Toast.makeText(this, "已添加.", Toast.LENGTH_SHORT).show();
        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextWord();
            }
        }, 600);
    }

    // 按钮1
    public void contentButton1(View view) {
        Word word = (Word) view.getTag();
        if (word.getId() == currentWord.getId()) {
            yOrN1.setImageDrawable(yes);
            imageVandG(yOrN1, 500, new MyCallback() {
                @Override
                public void toDoSth() {
                    nextWord();
                }
            });
        } else {
            yOrN1.setImageDrawable(no);
            imageVandG(yOrN1, 500);
        }
    }

    // 按钮2
    public void contentButton2(View view) {
        Word word = (Word) view.getTag();
        if (word.getId() == currentWord.getId()) {
            yOrN2.setImageDrawable(yes);
            imageVandG(yOrN2, 500, new MyCallback() {
                @Override
                public void toDoSth() {
                    nextWord();
                }
            });
        } else {
            yOrN2.setImageDrawable(no);
            imageVandG(yOrN2, 500);
        }
    }

    // 按钮3
    public void contentButton3(View view) {
        Word word = (Word) view.getTag();
        if (word.getId() == currentWord.getId()) {
            yOrN3.setImageDrawable(yes);
            imageVandG(yOrN3, 500, new MyCallback() {
                @Override
                public void toDoSth() {
                    nextWord();
                }
            });
        } else {
            yOrN3.setImageDrawable(no);
            imageVandG(yOrN3, 500);
        }
    }

    // 按钮4
    public void contentButton4(View view) {
        Word word = (Word) view.getTag();

        if (word.getId() == currentWord.getId()) {
            yOrN4.setImageDrawable(yes);
            imageVandG(yOrN4, 500, new MyCallback() {
                @Override
                public void toDoSth() {
                    nextWord();
                }
            });
        } else {
            yOrN4.setImageDrawable(no);
            imageVandG(yOrN4, 500);
        }
    }

    // 回调接口
    public interface MyCallback
    {
        public void toDoSth();
    }

    public void sendPost2() throws Exception {
        JSONObject req1 = new JSONObject(Global.i.request.toString());
        req1.put("currentReviewEn", Global.i.reviewCountEn);


        Rest2.sendRequest(Config.URL + "SetCurrentReviewEn.api", req1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

        if (unskillWords.size() != 0) {
            JSONObject req2 = new JSONObject(Global.i.request.toString());
            JSONArray unskills = new JSONArray();
            for (int i = 0; i < unskillWords.size(); i++) {
                Word word =  unskillWords.get(i);
                JSONObject wordJs = new JSONObject();

                wordJs.put("id", word.getId());
                wordJs.put("translation", word.getTranslation());
                wordJs.put("library_id", word.getLibrary_id());
                wordJs.put("content", word.getContent());

                unskills.put(wordJs);
            }
            req2.put("unskills", unskills);
            Rest2.sendRequest(Config.URL + "AddUnskillWords.api", req2, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });

        }
    }
    @Override
    protected void onDestroy() {
        try {
            sendPost2();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
