package example.loginsystem.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import example.loginsystem.R;
import example.loginsystem.my.bean.Word;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Helper;
import example.loginsystem.untils.QuickTimer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;
import static example.loginsystem.my.config.Config.REVIEW_NULL;

public class WordTestActivity extends AppCompatActivity {

    // 对错图片控件
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
    TextView word;              // 单词
    TextView testTime;          // 测试时间

    // 图片资源
    Drawable yes;
    Drawable no;

    ArrayList<Word> words;            // 单词列表
    ArrayList<Word> errorWordList;    // 错词列表
    Queue<Word> errorQueue;           // 错误单词队列
    Word currentWord;                 // 当前单词
    Button[] buttons;                 // 按钮数组
    int[] buttonRandoms;              // 按钮数组下标
    int count;                        // 当前单词显示位置
    int index;                        // List下标
    int errorInterval;                // 错误间隔
    MyTimer timer;                    // 计时器

    boolean model;                   // 当前模式(true: cn模式 false: en模式)
    boolean finish;                  // 是否完成
    boolean timerRun;                // timer是否在运行
    long time = Config.TEST_TIME;    // 测试时间

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(WordTestActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(WordTestActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
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

                        totalCount.setText(String.valueOf(words.size()));   // 本次复习总数

                        count = 1;
                        index = 0;

                        nextWord();

                    } catch (Exception e) {
                        Toast.makeText(WordTestActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
                case REVIEW_NULL:
                    try {
                        reviewNull(msg);
                    } catch (Exception e) {
                        Toast.makeText(WordTestActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_word_test);
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

    // 初始化变量
    public void initView() {
        timerRun = false;    // 是否在计时状态
        model = false;       // 模式
        finish = false;      // 是否完成

        yOrN1 = (ImageView) findViewById(R.id.id_imageView_wordTest_yesOrNo1);
        yOrN2 = (ImageView) findViewById(R.id.id_imageView_wordTest_yesOrNo2);
        yOrN3 = (ImageView) findViewById(R.id.id_imageView_wordTest_yesOrNo3);
        yOrN4 = (ImageView) findViewById(R.id.id_imageView_wordTest_yesOrNo4);

        button1 = (Button) findViewById(R.id.id_button_wordTest_contentButton1);
        button2 = (Button) findViewById(R.id.id_button_wordTest_contentButton2);
        button3 = (Button) findViewById(R.id.id_button_wordTest_contentButton3);
        button4 = (Button) findViewById(R.id.id_button_wordTest_contentButton4);

        testTime = (TextView) findViewById(R.id.id_textView_wordTest_time);
        currentCount = (TextView) findViewById(R.id.id_textView_wordTest_currentCount);
        totalCount = (TextView) findViewById(R.id.id_textView_wordTest_totalCount);
        word = (TextView) findViewById(R.id.id_textView_wordTest_word);

        // 设置图片不可见
        imageVorG(yOrN1, false);
        imageVorG(yOrN2, false);
        imageVorG(yOrN3, false);
        imageVorG(yOrN4, false);

        words = new ArrayList<Word>();                              // 单词列表
        errorWordList = new ArrayList<Word>();                      // 生词列表
        errorQueue = new LinkedList<Word>();                        // 错误队列
        buttons = new Button[] {button1,button2,button3,button4};   // 按钮数组

        // 图片资源
        yes = getDrawable(R.drawable.ic_yes);
        no = getDrawable(R.drawable.ic_no);

        // 设置错误间隔
        if (Global.i.user.getErrorInterval() == null) {
            errorInterval = 1;
        } else {
            errorInterval = Global.i.user.getErrorInterval();
        }
    }

    // 设置图片是否可见
    public void imageVorG(View image, boolean vOrg)
    {
        if (vOrg)
            image.setVisibility(View.VISIBLE);
        else
            image.setVisibility(View.INVISIBLE);
    }

    // 显示图片并在 time(ms) 后消失 并执行回调
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

    // 下一个单词
    public void nextWord()
    {
        ArrayList<Word> words2 = new ArrayList<>(words);       // 除了正确单词以外的所有单词

        // 防止越界
        if (index >= words.size() && errorQueue.size() == 0) {
            Global.i.currentTest = words.size() + 1;
            time = 0;
            return;
        }else if (index >= words.size()  && errorQueue != null && errorQueue.size() > 0) {      // 只剩错误单词
            currentWord = errorQueue.poll();

            Iterator iter = words2.iterator();
            while (iter.hasNext()) {
                Word temp = (Word) iter.next();
                if (temp.getId() == currentWord.getId()) {
                    iter.remove();
                    break;
                }
            }
            count-=1;
        }else if(errorQueue != null && errorQueue.size() > 0 && (index % errorInterval)==0) {    // 显示错误单词
            currentWord = errorQueue.poll();
            Iterator iter = words2.iterator();
            while (iter.hasNext()) {
                Word temp = (Word) iter.next();
                if (temp.getId() == currentWord.getId()) {
                    iter.remove();
                    break;
                }
            }
        }

        buttonRandoms = Helper.randomCommon(1,5,4);         // 生成随机数组下标

        if (currentWord == null)
            currentWord = words.get(index);                 // 当前单词

        currentCount.setText(String.valueOf(count));        // 当前单词显示位置

        // 随机设置模式
        if (buttonRandoms[0]%2 == 0) {
            // 设置翻译
            model = true;  // 中文选英模式
            word.setText(currentWord.getTranslation());
            buttons[buttonRandoms[0] - 1].setText(currentWord.getContent());   // 设置正确答案按钮
        } else {
            // 设置翻译
            model = false;  // 英文选中模式
            word.setText(currentWord.getContent());
            buttons[buttonRandoms[0] - 1].setText(currentWord.getTranslation()); // 设置正确答案按钮
        }

        word.setTag(currentWord);
        setTextSize();  // 设置字体

        buttons[buttonRandoms[0] - 1].setTag(currentWord);  // 设置正确答案按钮

        // 相等说明为普通情况
        if (words2.size() == words.size())
            words2.remove(index);

        Log.d("测试", words.size() + "\t" + words2.size());


        int[] randWordIndex = Helper.randomCommon(1, words2.size(), 3);

        for (int i = 1; i < buttonRandoms.length; i++) {
            Word word = words2.get(randWordIndex[i-1]);
            buttons[buttonRandoms[i] - 1].setTag(word);

            // 中文选英文
            if( model ) {
                buttons[buttonRandoms[i] - 1].setText(word.getContent());
            } else{  // 英文选中文
                buttons[buttonRandoms[i] - 1].setText(word.getTranslation());
            }
        }

        count++;
        index++;
    }

    // 设置字体大小
    public void setTextSize() {
        if (model) {
            button1.setTextSize(28);
            button2.setTextSize(28);
            button3.setTextSize(28);
            button4.setTextSize(28);
        } else {
            button1.setTextSize(22);
            button2.setTextSize(22);
            button3.setTextSize(22);
            button4.setTextSize(22);
        }

    }

    // 一次请求
    public void sendPost() throws Exception {
        JSONObject req = new JSONObject(Global.i.request.toString());
        req.put("start", Global.i.currentTest);
        req.put("end", Global.i.user.getRandInterval());

        Rest2.sendRequest(Config.URL + "GetReviewWords2.api", req, new okhttp3.Callback() {
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

    // 没有复习单词
    public void reviewNull(Message msg) throws JSONException {
        JSONObject resp = new JSONObject((String)msg.obj).getJSONObject("data");
        String tips = resp.getString("message");
        Toast.makeText(WordTestActivity.this, tips + ",快去背几个吧.", Toast.LENGTH_LONG).show();
        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    // 返回主页
    public void backToHome(View view) {
        finish();
    }

    // 开始计时
    public void startTime(View view) {
        if (finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return ;
        }
        // 判断对象是否为空
       if (timer == null) {
           timer = new MyTimer();
       }
       // 判断 timer 是否在运行
       if (!timerRun) {
           timerRun = true;
           timer.schedule(1000);
       }

    }

    //暂停计时
    public void stopTime(View view) {
        // 判断Timer状态
        if (finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return ;
        }
        if(timerRun) {
            timerRun = false;
            timer.cancel();
            timer = null;
        }
    }

    // 按钮1
    public void contentButton1(View view) {
        // 是否在计时
        if (!timerRun) {
            Toast.makeText(this, "请先开始计时.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return;
        }
        Word word = (Word) view.getTag();

        if (word.getId() != currentWord.getId()) {
            removeRepeat();                                     // 去重
            errorWordList.add(currentWord);                     // 记录错误单词
            errorQueue.add(currentWord);                        // 将错词加入队列
            yOrN1.setImageDrawable(no);
        } else {
            yOrN1.setImageDrawable(yes);
        }
        currentWord = null;
        imageVandG(yOrN1, 700, new MyCallback() {
            @Override
            public void toDoSth() {
                nextWord();
            }
        });

    }

    // 按钮2
    public void contentButton2(View view) {
        // 是否在计时
        if (!timerRun) {
            Toast.makeText(this, "请先开始计时.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return;
        }

        Word word = (Word) view.getTag();
        if (word.getId() != currentWord.getId()) {
            // 去重
            removeRepeat();
            errorWordList.add(currentWord);
            errorQueue.add(currentWord);                        // 将错词加入队列
            yOrN2.setImageDrawable(no);
        } else {
            yOrN2.setImageDrawable(yes);
        }
        currentWord = null;
        imageVandG(yOrN2, 700, new MyCallback() {
            @Override
            public void toDoSth() {
                nextWord();
            }
        });
    }

    // 按钮3
    public void contentButton3(View view) {
        // 是否在计时
        if (!timerRun) {
            Toast.makeText(this, "请先开始计时.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return;
        }

        Word word = (Word) view.getTag();
        if (word.getId() != currentWord.getId()) {
            removeRepeat();                     // 去重
            errorWordList.add(currentWord);     // 加入错词数组
            errorQueue.add(currentWord);        // 将错词加入队列
            yOrN3.setImageDrawable(no);
        } else {
            yOrN3.setImageDrawable(yes);
        }

        currentWord = null;
        imageVandG(yOrN3, 700, new MyCallback() {
            @Override
            public void toDoSth() {
                nextWord();
            }
        });
    }

    // 按钮4
    public void contentButton4(View view) {
        // 是否在计时
        if (!timerRun) {
            Toast.makeText(this, "请先开始计时.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(finish) {
            Toast.makeText(this, "本次测试已完成, 下次再来吧.", Toast.LENGTH_SHORT).show();
            return;
        }

        Word word = (Word) view.getTag();

        if (word.getId() != currentWord.getId()) {
            removeRepeat();                            // 去重
            errorWordList.add(currentWord);            // 加入错词list
            errorQueue.add(currentWord);               // 将错词加入队列
            yOrN4.setImageDrawable(no);
        } else {
            yOrN4.setImageDrawable(yes);
       }
        currentWord = null;
        imageVandG(yOrN4, 700, new MyCallback() {
            @Override
            public void toDoSth() {
                nextWord();
            }
        });
    }

    // 去重
    public void removeRepeat()
    {
        if (errorWordList == null) {
            return;
        }
        Iterator iter = errorWordList.iterator();
        while (iter.hasNext()){
            Word t = (Word) iter.next();
            if (t.getId() == currentWord.getId()) {
                iter.remove();
                break;
            }
        }
    }

    // 回调接口
    public interface MyCallback
    {
        void toDoSth();       // 需要做的事
    }

    // 二次请求
    public void sendPost2() throws Exception {
        JSONObject req1 = new JSONObject(Global.i.request.toString());
        req1.put("currentTest", Global.i.currentTest);
        Rest2.sendRequest(Config.URL + "SetCurrentTest.api", req1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

        if (errorWordList.size() != 0) {
            JSONObject req2 = new JSONObject(Global.i.request.toString());
            JSONArray unskills = new JSONArray();
            for (int i = 0; i < errorWordList.size(); i++) {
                Word word =  errorWordList.get(i);
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
        if (timer != null && timerRun) {
            timer.cancel();
            timer = null;
        }

        try {
            sendPost2();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    // 计时器逻辑
    private class MyTimer extends QuickTimer
    {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

        @Override
        protected Object doInBackground() {
            String timerStr = sdf.format(time);
            time-=1000;   // 减一秒

            if (time < 0){
                timer.cancel();
                return "end";
            }
            return timerStr;
        }

        @Override
        protected void onPostExecute(Object result) {
            String timerStr = (String) result;

            if (timerStr.equals("end")) {
                finish = true;
                if (errorWordList.size() == 0) {
                    testTime.setText("得分: 100");
                } else {
                    double score = (double)100 - ((double)errorWordList.size()/(double)words.size() * (double)100);
                    testTime.setText("得分: " + String.format("%.1f",score));
                }
                timer.cancel();
                timer = null;
            } else {
                testTime.setText(timerStr);
            }
        }
    }
}
