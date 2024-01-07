package com.library.seat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SeatAdapt.ApiResponseListener {
    private ListView list_view;
    private List<Seat> seatList;
    private Button seat_add;
    private Button all_end,all_pause,all_book;
    private SeatAdapt seatAdapt;
    private AutoCompleteTextView console;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list_view = findViewById(R.id.list_view);
        seat_add = findViewById(R.id.seat_add);
        all_end = findViewById(R.id.all_end);
        all_pause = findViewById(R.id.all_pause);
        all_book = findViewById(R.id.all_book);
        console = findViewById(R.id.console);
        seat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SeatAddActivity.class);
                startActivityForResult(intent, 1);

            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("seat_prefs", Context.MODE_PRIVATE);
        seatList = new ArrayList<>();

        // 遍历 SharedPreferences 中的数据，解析每个对象并添加到 List 中
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();

            // 解析对象并添加到 List 中
            Seat seat = new Gson().fromJson(value, Seat.class);
            seatList.add(seat);
        }
        // 创建适配器并设置数据
        seatAdapt = new SeatAdapt(MainActivity.this, R.layout.item_list, seatList,this);

        // 将适配器设置给列表视图
        list_view.setAdapter(seatAdapt);


        all_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, ?> allEntries = sharedPreferences.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();

                    // 解析对象并添加到 List 中
                    Seat seat = new Gson().fromJson(value, Seat.class);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .followRedirects(false) // 禁用自动重定向
                                    .build();
                            RequestBody requestBody = new FormBody.Builder()
                                    .addEncoded("DoUserIn", "true")
                                    .addEncoded("dwUseMin", seat.getMin() + "")
                                    .build();

                            Request request = new Request.Builder()
                                    .url("http://seat.shengda.edu.cn/Pages/WxSeatSign.aspx")
                                    .header("Host", "seat.shengda.edu.cn")
                                    .header("Connection", "keep-alive")
                                    .header("Cache-Control", "max-age=0")
                                    .header("Upgrade-Insecure-Requests", "1")
                                    .header("Origin", "http://seat.shengda.edu.cn")
                                    .header("Content-Type", "application/x-www-form-urlencoded")
                                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; Redmi K30 Pro Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.99 XWEB/4375 MMWEBSDK/20230202 Mobile Safari/537.36 MMWEBID/2211 MicroMessenger/8.0.33.2306(0x28002144) WeChat/arm64 Weixin GPVersion/1 NetType/WIFI Language/zh_CN ABI/arm64")
                                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/wxpic,image/tpg,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                                    .header("X-Requested-With", "com.tencent.mm")
                                    .header("Referer", "http://seat.shengda.edu.cn/Pages/WxSeatSignMsg.aspx?type=3&title=%e5%8f%af%e7%94%a8&msg=%e7%a9%ba%e9%97%b2%3a%e8%af%b7%e9%80%89%e6%8b%a9%e4%bd%bf%e7%94%a8%e6%97%b6%e9%95%bf%ef%bc%8c%e7%a6%bb%e5%bc%80%e8%af%b7%e6%89%ab%e7%a0%81&msg2=&dwMinUseMin=60&dwMaxUseMin=240&szTrueName="+ URLEncoder.encode(seat.getName()) +"&szDevName="+URLEncoder.encode(seat.getId())+"&ResvMsg=&status=0")
                                    .header("Accept-Encoding", "gzip, deflate")
                                    .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                                    .header("Cookie", "ASP.NET_SessionId="+seat.getSession())
                                    .post(requestBody)
                                    .build();

                            Call call = client.newCall(request);

                            try {
                                Response response = call.execute();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String result = seatAdapt.extractAndDecodeResult(response.body().string());
                                            console.setText(console.getText() + seat.getName() + ":      " + result + "\n");
                                        }catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            SharedPreferences sharedPreferences = getSharedPreferences("seat_prefs", Context.MODE_PRIVATE);
            seatList = new ArrayList<>();

            // 遍历 SharedPreferences 中的数据，解析每个对象并添加到 List 中
            Map<String, ?> allEntries = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();

                // 解析对象并添加到 List 中
                Seat seat = new Gson().fromJson(value, Seat.class);
                seatList.add(seat);
                seatAdapt.clear();
                seatAdapt.addAll(seatList);
                seatAdapt.notifyDataSetChanged();
            }
        }
    }
    @Override
    public void onApiResponse(String response) {
        // 在控制台输出响应结果
        Log.d("MainActivity", "Response: " + response);

        // 显示一个提示框
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                console = findViewById(R.id.console);
                console.setText(response);
            }
        });
    }
}