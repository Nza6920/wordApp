package example.loginsystem.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import example.loginsystem.R;
import example.loginsystem.my.bean.Library;
import example.loginsystem.my.bean.Word;
import example.loginsystem.my.config.Config;
import example.loginsystem.my.config.Global;
import example.loginsystem.my.http.Rest2;
import example.loginsystem.untils.Popup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static example.loginsystem.my.config.Config.DISSMISS_POPUPWINDOW;
import static example.loginsystem.my.config.Config.HTTP_OK;
import static example.loginsystem.my.config.Config.MESSAGE_ERROR;
import static example.loginsystem.my.config.Config.REQUEST_FAILED;
import static example.loginsystem.my.config.Config.REQUEST_OK;
import static example.loginsystem.my.config.Config.REQUEST_OK2;
import static example.loginsystem.my.config.Global.i;

public class MyLibraryActivity extends AppCompatActivity {

    Spinner spinner;
    ListView listView;
    ArrayList<MySpinnerListItem> libraryList;
    MySpinnerAdapter adapter;
    MyListAdapter listAdapter;  // listView 适配器

    JSONObject jreq;
    JSONObject jresp;
    JSONObject jresp2;          // 二次请求
    Integer wordIdtemp = Global.i.wordId;
    Integer libraryIdtemp = Global.i.user.getLibraryId();

    // 消息循环, 更新UI
    Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ERROR:
                    Toast.makeText(MyLibraryActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_FAILED:
                    Toast.makeText(MyLibraryActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case DISSMISS_POPUPWINDOW:
                    ((Popup)msg.obj).dismiss();
                    break;
                case REQUEST_OK:
                    try {
                        jresp = new JSONObject((String) msg.obj);
                        JSONArray librarys = jresp.getJSONArray("data");

                        // 初始化Spinner
                        initSpinner(librarys);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case REQUEST_OK2:
                    try {
                        jresp2 = new JSONObject((String) msg.obj);
                        JSONArray words = jresp2.getJSONArray("data");

                        // 初始化 ListView
                        initListView(words);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_library);

        libraryList = new ArrayList<>();
        spinner = (Spinner) findViewById(R.id.id_spinner_library);
        listView = (ListView) findViewById(R.id.id_listView_wordList);
        listAdapter = new MyListAdapter();
        listView.setAdapter(listAdapter);

        try {
            jreq = new JSONObject(i.request.toString());
            // 发起请求

            Rest2.sendRequest(Config.URL + "GetLibrary.api", jreq, new Callback() {
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
                    if (response.code() == HTTP_OK) {
                        try {
                            JSONObject resp = new JSONObject(response.body().string());
                            Message msg2 = new Message();
                            msg2.what = REQUEST_OK;
                            msg2.obj = resp.toString();
                            msgHandler.sendMessage(msg2);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // sipnner 事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (libraryIdtemp != adapter.getItemId(position)){
                    Global.i.wordId = null;
                    Global.i.user.setLibraryId((int) adapter.getItemId(position));
                } else {
                    Global.i.wordId = wordIdtemp;
                    Global.i.user.setLibraryId(libraryIdtemp);
                }

                try {
                    JSONObject jreq = new JSONObject(Global.i.request.toString());
                    jreq.put("libraryId", (int) adapter.getItemId(position));

                    // 发起请求
                    Rest2.sendRequest(Config.URL + "GetWord.api", jreq, new Callback() {
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
                            if (response.code() == HTTP_OK) {
                                try {
                                    JSONObject resp = new JSONObject(response.body().string());
                                    Message msg2 = new Message();
                                    msg2.what = REQUEST_OK2;
                                    msg2.obj = resp.toString();
                                    msgHandler.sendMessage(msg2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // 初始化Spinner
    public void initSpinner(JSONArray librarys) throws JSONException {
        for (int i = 0; i < librarys.length(); i++) {
            JSONObject library = librarys.getJSONObject(i);

            if (libraryList == null) continue;

            libraryList.add(new MySpinnerListItem(new Library(library.getInt("id"),
                    library.getString("name"),
                    library.getInt("count"),
                    library.getString("created_at"))));
        }

        adapter = new MySpinnerAdapter();
        adapter.datas = libraryList;
        spinner.setAdapter(adapter);

        for (int i = 0; i < adapter.getCount(); i++) {
            if (Global.i.user.getLibraryId() == adapter.getItemId(i)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // 初始化ListView
    public void initListView(JSONArray words) throws JSONException {
        listAdapter.datas.clear();
        for (int i = 0; i < words.length(); i++) {
            JSONObject word = words.getJSONObject(i);
            listAdapter.datas.add(new MyListItem(new Word(word.getInt("id"),
                    word.getInt("library_id"),
                    word.getString("content"),
                    word.getString("translation")
            )));
        }
        listAdapter.notifyDataSetChanged();
    }

    // 返回键
    public void back(View view) {
        finish();
    }

    // listView 描述每一项
    private class MyListItem
    {
        MyListItem(Word word) {
            this.word = word;
        }

        Word word;    // 显示单词
    }

    // ListView适配器
    private class MyListAdapter extends BaseAdapter{

        ArrayList<MyListItem> datas = new ArrayList<>();

        MyListAdapter()
        {

        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHoler viewHoler;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_view_word, parent, false);
                viewHoler = new ListViewHoler();
                viewHoler.wordContent = convertView.findViewById(R.id.id_textView_content);
                viewHoler.wordTranslation = convertView.findViewById(R.id.id_textView_translation);
                convertView.setTag(viewHoler);
            } else {
                viewHoler = (ListViewHoler) convertView.getTag();
            }

            MyListItem item = (MyListItem) getItem(position);

            viewHoler.wordContent.setText(item.word.getContent());
            viewHoler.wordTranslation.setText(item.word.getTranslation());

            return convertView;
        }
    }

    class ListViewHoler {
        TextView wordContent;
        TextView wordTranslation;
    }

    // spinnerListView 描述每一项
    private class MySpinnerListItem {
        MySpinnerListItem(Library library)
        {
            this.library = library;
        }
        Library library;
    }

    // spinnerListView 适配器
    private class MySpinnerAdapter extends BaseAdapter {
        ArrayList<MySpinnerListItem> datas = new ArrayList<>();

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return datas.get(position).library.getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerViewHolder viewHolder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_view_library,parent, false);
                viewHolder = new SpinnerViewHolder();
                viewHolder.countTextView = convertView.findViewById(R.id.id_textView_count);
                viewHolder.nameTextView = convertView.findViewById(R.id.id_textView_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (SpinnerViewHolder) convertView.getTag();
            }

            MySpinnerListItem item = (MySpinnerListItem) getItem(position);

            viewHolder.nameTextView.setText(item.library.getName());
            viewHolder.countTextView.setText(item.library.getCount().toString());

            return convertView;
        }
    }

    class SpinnerViewHolder {
        TextView countTextView;     // 单词内容
        TextView nameTextView;       // 单词翻译
    }

    @Override
    protected void onDestroy() {
        try {
            JSONObject req = new JSONObject(i.request.toString());
            req.put("libraryId", i.user.getLibraryId());
            // 更新当前词库
            Rest2.sendRequest(Config.URL + "SetCurrentLibrary.api", req, new Callback() {
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
        super.onDestroy();
    }
}
