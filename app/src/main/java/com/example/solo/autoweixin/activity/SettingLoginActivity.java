package com.example.solo.autoweixin.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.url.Urls;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class SettingLoginActivity extends BaseActivity {

    private TextView tv_title;
    private EditText ed_name, ed_pass;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_login);
        initView();
    }

    private void initView() {
        ed_name = findViewById(R.id.ed_name);
        ed_pass = findViewById(R.id.ed_pass);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_name.getText().toString().equals("")) {
                    Toast.makeText(SettingLoginActivity.this, "账号不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    if (ed_pass.getText().toString().equals("")) {
                        Toast.makeText(SettingLoginActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                    } else {
                        netLogin(ed_name.getText().toString(), ed_pass.getText().toString());
                    }
                }
            }
        });
    }

    private void netLogin(String userName, String passWord) {
        OkHttpUtils.post()
                .url(Urls.login)
                .addParams("userName", userName)
                .addParams("passWord", passWord)
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
                        Toast.makeText(SettingLoginActivity.this, "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            startActivity(new Intent(SettingLoginActivity.this, SettingAddActivity.class));
                        } else {
                            Toast.makeText(SettingLoginActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        dissmissProgressDialog();
                    }
                });
    }

}
