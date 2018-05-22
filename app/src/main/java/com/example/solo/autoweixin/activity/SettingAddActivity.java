package com.example.solo.autoweixin.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.bean.CodeBean;
import com.example.solo.autoweixin.url.Urls;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class SettingAddActivity extends BaseActivity {

    private TextView tv_title;
    private TextView tv_list;
    private EditText editText;
    private AppCompatSpinner appCompatSpinner;
    private TextView textView;
    private Button btn_copy;
    private Button btn_add;

    private Long totalTime;//全部可使用时间（毫秒数）
    private Long totalNum;//全部可使用次数
    private Integer vipType;//会员类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_add);
        initView();
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_list = findViewById(R.id.tv_list);
        editText = findViewById(R.id.editText);
        appCompatSpinner = findViewById(R.id.appCompatSpinner);
        textView = findViewById(R.id.textView);
        btn_copy = findViewById(R.id.btn_copy);
        btn_add = findViewById(R.id.btn_add);
        tv_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingAddActivity.this, SettingListActivity.class));
            }
        });
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                Objects.requireNonNull(cm).setText(textView.getText());
                Toast.makeText(SettingAddActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
            }
        });
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        totalTime = 3 * 24 * 60 * 60 * 1000L;
                        totalNum = 50L;
                        break;
                    case 1:
                        totalTime = 30 * 24 * 60 * 60 * 1000L;
                        totalNum = 15000L;
                        break;
                    case 2:
                        totalTime = 91 * 24 * 60 * 60 * 1000L;
                        totalNum = 182000L;
                        break;
                    case 3:
                        totalTime = 183 * 24 * 60 * 60 * 1000L;
                        totalNum = 1464000L;
                        break;
                    case 4:
                        totalTime = 365 * 24 * 60 * 60 * 1000L;
                        totalNum = 19999999L;
                        break;
                }
                vipType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(SettingAddActivity.this, "设备Id不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    netAddCode();
                }
            }
        });
    }

    private void netAddCode() {
        OkHttpUtils.post()
                .url(Urls.addCode)
                .addParams("deviceId", editText.getText().toString())
                .addParams("totalNum", "" + totalNum)
                .addParams("totalTime", "" + totalTime)
                .addParams("vipType", "" + vipType)
                .build()
                .execute(new Callback<BaseBean>() {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public BaseBean parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        return new Gson().fromJson(string, BaseBean.class);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(SettingAddActivity.this, "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            CodeBean codeBean = new Gson().fromJson(response.getData().toString(), CodeBean.class);
                            textView.setText(codeBean.getmKey());
                        }
                        Toast.makeText(SettingAddActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        dissmissProgressDialog();
                    }
                });
    }

}
