package example.loginsystem.my.http;

import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Rest2 {

    public static final MediaType REQUEST_JSON = MediaType.parse("application/json; charset=utf-8");    // 请求类型

    public static void sendRequest(String url, JSONObject req, Callback callback) throws Exception{
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(REQUEST_JSON, req.toString());

        Request okHttpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(okHttpRequest).enqueue(callback);
    }
}
