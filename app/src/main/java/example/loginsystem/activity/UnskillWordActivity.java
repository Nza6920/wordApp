package example.loginsystem.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import example.loginsystem.R;
import example.loginsystem.my.bean.Word;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;

public class UnskillWordActivity extends AppCompatActivity {

    ListView listView;
//    ArrayList<Word> datas = new ArrayList<>();      // 数据源
    MyListAdapter adapter;
    ArrayList<Word> deleteWords = new ArrayList<>(); // 删除的单词

    boolean chooseModel = false;   // 选择模式


    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(UnskillWordActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(UnskillWordActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:
                    break;
                case REQUEST_OK:
                    try {
                        initListData(msg);
                    } catch (Exception e) {
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
        setContentView(R.layout.activity_unskill_word);

        listView = (ListView) findViewById(R.id.id_listView_unskillWords);
        adapter = new MyListAdapter();
        listView.setAdapter(adapter);

        // 发起请求
        try {
            JSONObject req = new JSONObject(Global.i.request.toString());
            Rest2.sendRequest(Config.URL + "GetUnskilledWords.api", req, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message msg1 = new Message();
                    msg1.what = REQUEST_OK;
                    msg1.obj = e.toString();
                    msgHandler.sendMessage(msg1);
                }
                @Override
                public void onResponse(Call call, Response response){
                    try {
                        JSONObject resp = new JSONObject(response.body().string());
                        Message msg2 = new Message();
                        msg2.what = REQUEST_OK;
                        msg2.obj = resp.toString();
                        msgHandler.sendMessage(msg2);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 长按事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                onItemLongClicked(position);
                return true;
            }
        });

        // 单击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(position);
            }
        });

        chooseModel(false);   // 默认为非选择模式

    }


    public void initListData(Message msg) throws JSONException {
        JSONObject resp = new JSONObject((String) msg.obj);
        JSONArray unskills = resp.getJSONArray("data");

        for (int i = 0; i<unskills.length(); i++) {
            JSONObject wordJ = unskills.getJSONObject(i);
            Word word = new Word(wordJ.getInt("id"),
                    wordJ.getInt("library_id"),
                    wordJ.getString("content"),
                    wordJ.getString("translation"));

            adapter.listData.add(new MyListItem(word, false));
        }
        adapter.notifyDataSetChanged();
    }
    // 单点事件
    private void onItemClicked(int position) {

        // 判断是否未选择模式
        if (chooseModel) {
            MyListItem item = (MyListItem) adapter.getItem(position);
            item.checked = !item.checked;

            adapter.notifyDataSetChanged();
        }
    }


    // 长按事件
    private void onItemLongClicked(int position) {

        MyListItem item = (MyListItem) adapter.getItem(position);
        item.checked = !item.checked;

        chooseModel(!chooseModel);
        adapter.notifyDataSetChanged();
    }

    // 删除所选
    public void onDelete(View view) {

        Iterator<MyListItem> iter = adapter.listData.iterator();

        while (iter.hasNext()) {
            MyListItem item = iter.next();

            if (item.checked) {
                Word word = item.data;
                word.setSkill_level(1);
                deleteWords.add(word);        // 记录删除单词
                iter.remove();                // 从适配器中删除
            }
        }
        chooseModel(false);
        adapter.notifyDataSetChanged();
    }

    // 取消所选
    public void onCancel(View view) {
        for (MyListItem item : adapter.listData) {
            item.checked = false;
        }

        chooseModel(false);

        adapter.notifyDataSetChanged();
    }

    // 切换模式
    public void chooseModel(boolean yes) {

        View bar = findViewById(R.id.id_bar);
        if (yes) {
            bar.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
        }
        chooseModel = yes;
    }

    // 返回主页
    public void backToHome(View view) {
        finish();
    }

    // listView 的每一项
    private class MyListItem
    {
        Word data;          // 单词数据
        boolean checked;    // 是否被选中

        MyListItem(Word word, boolean checked){
            this.data = word;
            this.checked = checked;
        }

    }

    @Override
    protected void onDestroy() {

        if (deleteWords.size() > 0) {
            try {
                JSONArray wordsJ = new JSONArray();
                JSONObject req = new JSONObject(Global.i.request.toString());
                for (int i = 0; i < deleteWords.size(); i++) {
                    Word word = deleteWords.get(i);
                    JSONObject wordJ = new JSONObject();

                    wordJ.put("id", word.getId());
                    wordJ.put("content", word.getContent());
                    wordJ.put("translation", word.getTranslation());
                    wordJ.put("library_id", word.getLibrary_id());
                    wordJ.put("skill_level", word.getSkill_level());

                    wordsJ.put(wordJ);
                }

                req.put("words", wordsJ);

                Rest2.sendRequest(Config.URL + "DeleteUnskilledWord.api", req, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
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

    // 数据适配器
    private class MyListAdapter extends BaseAdapter
    {
        // 列表数据
        ArrayList<MyListItem> listData = new ArrayList<>();
        Drawable ic_checked, ic_unchecked;

        MyListAdapter() {
            ic_checked = getDrawable(R.drawable.ic_checked);
            ic_unchecked = getDrawable(R.drawable.ic_unchecked);
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.unskill_word_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.check = convertView.findViewById(R.id.id_imageView_unskill_check);
                viewHolder.usContent = convertView.findViewById(R.id.id_textView_unskill_content);
                viewHolder.usTranslation = convertView.findViewById(R.id.id_textView_unskill_translation);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();    // 重新获取ViewHolder
            }

            MyListItem item = (MyListItem) getItem(position);


            viewHolder.usContent.setText(item.data.getContent());
            viewHolder.usTranslation.setText(item.data.getTranslation());

            if (chooseModel) {
                viewHolder.check.setVisibility(View.VISIBLE);
                if (item.checked)
                    viewHolder.check.setImageDrawable(ic_checked);
                else
                    viewHolder.check.setImageDrawable(ic_unchecked);
            } else {
                viewHolder.check.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    class ViewHolder {
        ImageView check;        // check控件
        TextView usContent;     // 单词内容
        TextView usTranslation; // 单词翻译
    }
}
