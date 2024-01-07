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

public class SeatEditActivity extends AppCompatActivity {
    private Seat seat;
    private EditText edit_seat_name, edit_seat_min, edit_seat_session, edit_seat_id;
    private RadioGroup edit_seat_floor_group;
    private Button button_edit_seat;
    private String floor;
    private SharedPreferences sharedPreferences;
    private String originName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_edit);
        seat = (Seat) getIntent().getSerializableExtra("seat");
        edit_seat_name = findViewById(R.id.edit_seat_name);
        edit_seat_min = findViewById(R.id.edit_seat_min);
        edit_seat_session = findViewById(R.id.edit_seat_session);
        edit_seat_id = findViewById(R.id.edit_seat_id);
        edit_seat_floor_group = findViewById(R.id.edit_seat_floor);
        button_edit_seat = findViewById(R.id.button_edit_seat);

        edit_seat_id.setText(seat.getId().substring(seat.getId().length()-3));
        edit_seat_name.setText(seat.getName());
        edit_seat_session.setText(seat.getSession());
        edit_seat_min.setText(String.valueOf(seat.getMin()));

        originName = seat.getName();


        edit_seat_floor_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 处理选中逻辑
                RadioButton radioButton = findViewById(checkedId);
                floor = radioButton.getText().toString();
            }
        });
        String selectedFloor = seat.getId().substring(0, 2);
        if (selectedFloor != null) {
            // 遍历 RadioGroup 中的 RadioButton，找到匹配的项并选中它
            for (int i = 0; i < edit_seat_floor_group.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) edit_seat_floor_group.getChildAt(i);
                if (selectedFloor.equals(radioButton.getText().toString())) {
                    radioButton.setChecked(true);
                    break; // 选中后跳出循环
                }
            }
        }
        button_edit_seat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动后台线程执行网络请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String name = edit_seat_name.getText().toString();
                            int min = Integer.parseInt(edit_seat_min.getText().toString());
                            String session = edit_seat_session.getText().toString();
                            String incompleteId = edit_seat_id.getText().toString();
                            String id = floor + "-0" + incompleteId;

                            // 构造修改后的座位对象
//                            Seat updatedSeat = new Seat(id, name, min, session);
                            seat.setId(id);
                            seat.setName(name);
                            seat.setMin(min);
                            seat.setSession(session);
                            Gson gson = new Gson();
                            String seatJson = gson.toJson(seat);
                            Log.i("json", seatJson);
                            sharedPreferences = getSharedPreferences("seat_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            // 根据座位对象的唯一标识（这里使用座位名称）更新对应的数据
                            editor.putString(String.valueOf(seat.getUuid()), seatJson);
                            boolean isSaved = editor.commit();


                            if (isSaved) {
                                // 在主线程中更新UI
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("修改操作", "onClick: 修改成功!");
                                        // 可以关闭当前Activity或执行其他操作
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.w("修改操作", "onClick: 修改失败！");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("修改操作", e.toString());
                                }
                            });
                        }
                    }
                }).start();
            }
        });

    }
}