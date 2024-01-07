package com.library.seat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SeatAdapt extends ArrayAdapter<Seat> {
    private SharedPreferences sharedPreferences;
    private int itemID;

    private Button seat_end, seat_pause, seat_book;
    private ApiResponseListener apiResponseListener;


    public SeatAdapt(@NonNull Context context, int resource, @NonNull List<Seat> objects,ApiResponseListener listener) {
        super(context, resource, objects);
        this.itemID = resource;
        this.apiResponseListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = View.inflate(getContext(), itemID, null);
        TextView seat_name = view.findViewById(R.id.seat_name);
        TextView seat_id = view.findViewById(R.id.seat_id);
        TextView seat_min = view.findViewById(R.id.seat_min);
        Button seat_delete = view.findViewById(R.id.seat_delete);
        Button seat_edit = view.findViewById(R.id.seat_edit);
        seat_end = view.findViewById(R.id.seat_end);
        seat_pause = view.findViewById(R.id.seat_pause);
        seat_book = view.findViewById(R.id.seat_book);


        seat_name.setText(getItem(position).getName());
        seat_id.setText(getItem(position).getId());
        seat_min.setText(getItem(position).getMin() + "");


        seat_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("确认删除");
                Gson gson = new Gson();
                String seatJson = gson.toJson(getItem(position));
                Log.i("json删除",  "ASP.NET_SessionId="+getItem(position).getSession()+" " + seatJson);
                builder.setMessage("确定要删除这个座位吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确定，执行删除操作
                        removeBillFromSharedPreferences(getItem(position));
                        remove(getItem(position));
                        // 通知适配器数据已更改
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        seat_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SeatEditActivity.class);
                intent.putExtra("seat", getItem(position));
                ((MainActivity) getContext()).startActivityForResult(intent, 1);
            }
        });
        seat_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient.Builder()
                                .followRedirects(false) // 禁用自动重定向
                                .build();
                        RequestBody requestBody = new FormBody.Builder()
                                .addEncoded("DoUserIn", "true")
                                .addEncoded("dwUseMin", getItem(position).getMin() + "")
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
                                .header("Referer", "http://seat.shengda.edu.cn/Pages/WxSeatSignMsg.aspx?type=3&title=%e5%8f%af%e7%94%a8&msg=%e7%a9%ba%e9%97%b2%3a%e8%af%b7%e9%80%89%e6%8b%a9%e4%bd%bf%e7%94%a8%e6%97%b6%e9%95%bf%ef%bc%8c%e7%a6%bb%e5%bc%80%e8%af%b7%e6%89%ab%e7%a0%81&msg2=&dwMinUseMin=60&dwMaxUseMin=240&szTrueName="+ URLEncoder.encode(getItem(position).getName()) +"&szDevName="+URLEncoder.encode(getItem(position).getId())+"&ResvMsg=&status=0")
                                .header("Accept-Encoding", "gzip, deflate")
                                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                                .header("Cookie", "ASP.NET_SessionId="+getItem(position).getSession())
                                .post(requestBody)
                                .build();

                        Call call = client.newCall(request);

                try {
                    Response response = call.execute();
                    apiResponseListener.onApiResponse(getItem(position).getName() +":      " + extractAndDecodeResult(response.body().string()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    }
                }).start();
            }
        });
        return view;
    }
    private void removeBillFromSharedPreferences(Seat seat) {
        sharedPreferences = getContext().getSharedPreferences("seat_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(String.valueOf(seat.getUuid()));
        editor.apply();
    }
    public String extractAndDecodeResult(String result) {
        // 使用正则表达式提取 href 属性中的 URL
        Pattern pattern = Pattern.compile("href=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(result);
        String decodedParams = "";
        if (matcher.find()) {
            String href = matcher.group(1);

            // 使用正则表达式提取 URL 中的参数部分
            Pattern paramPattern = Pattern.compile("\\?(.*)");
            Matcher paramMatcher = paramPattern.matcher(href);
            if (paramMatcher.find()) {
                String params = paramMatcher.group(1);

                // 解码参数并打印
                try {
                    decodedParams = URLDecoder.decode(params, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return decodedParams;
    }
    public interface ApiResponseListener {
        void onApiResponse(String response);
    }
}
