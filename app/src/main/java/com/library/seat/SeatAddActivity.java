package com.library.seat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SeatAddActivity extends AppCompatActivity {
    private EditText add_seat_name, add_seat_min, add_seat_session, add_seat_id;
    private RadioGroup add_seat_floor_group;
    private Button button_add_seat;
    private String floor;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_add);
        add_seat_name = findViewById(R.id.add_seat_name);
        add_seat_min = findViewById(R.id.add_seat_min);
        add_seat_session = findViewById(R.id.add_seat_session);
        add_seat_id = findViewById(R.id.add_seat_id);
        add_seat_floor_group = findViewById(R.id.add_seat_floor);
        button_add_seat = findViewById(R.id.button_add_seat);


        add_seat_floor_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 处理选中逻辑
                RadioButton radioButton = findViewById(checkedId);
                floor = radioButton.getText().toString();
            }
        });
        button_add_seat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String name = add_seat_name.getText().toString();
                            int min = Integer.parseInt(add_seat_min.getText().toString());
                            String session = add_seat_session.getText().toString();
                            String incompleteId = add_seat_id.getText().toString();
                            String id = floor + "-0" + incompleteId;

                            Seat newSeat = new Seat(id, name, min, session);
                            Gson gson = new Gson();
                            String seatJson = gson.toJson(newSeat);
                            Log.i("json", seatJson);
                            sharedPreferences = getSharedPreferences("seat_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(String.valueOf(newSeat.getUuid()), seatJson);
                            boolean isSaved = editor.commit();

                            if (isSaved) {
                                // 在主线程中更新UI
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 可以关闭当前Activity或执行其他操作
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                            } else {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.w("添加操作", "onClick: 添加失败！");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 在主线程中更新UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("添加操作", e.toString());
                                }
                            });
                        }
                    }
                }).start();
            }
        });

    }
}