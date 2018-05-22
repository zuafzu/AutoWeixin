package com.example.solo.autoweixin.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.adapter.SettingListAdapter;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.bean.CodeBean;
import com.example.solo.autoweixin.url.Urls;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class SettingListActivity extends BaseActivity {

    private List<CodeBean> codeBeanList;
    private TextView tv_title;
    private ListView listView;
    private SettingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_list);
        initView();
        netFindAllCode();
    }

    private void initView() {
        codeBeanList = new ArrayList<>();
        tv_title = findViewById(R.id.tv_title);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        listView = findViewById(R.id.listView);
    }

    private void setData() {
        if (adapter == null) {
            adapter = new SettingListAdapter(codeBeanList, this);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void netFindAllCode() {
        OkHttpUtils.post()
                .url(Urls.findAllCode)
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
                        Log.e("cyf", e.getMessage());
                        Toast.makeText(SettingListActivity.this, "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            ArrayList<CodeBean> list = new Gson().fromJson(response.getData().toString(), new TypeToken<ArrayList<CodeBean>>() {
                            }.getType());
                            codeBeanList.clear();
                            codeBeanList.addAll(list);
                            setData();
                        } else {
                            Toast.makeText(SettingListActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
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
