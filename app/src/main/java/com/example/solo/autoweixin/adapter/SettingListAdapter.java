package com.example.solo.autoweixin.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.bean.CodeBean;

import java.util.List;
import java.util.Objects;

public class SettingListAdapter extends BaseAdapter {

    private List<CodeBean> codeBeanList;
    private Context context;

    public SettingListAdapter(List<CodeBean> codeBeanList, Context context) {
        this.codeBeanList = codeBeanList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return codeBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return codeBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_setting_list, null);
            holder = new ViewHolder();
            holder.textView1 = convertView.findViewById(R.id.textView1);
            holder.textView2 = convertView.findViewById(R.id.textView2);
            holder.textView3 = convertView.findViewById(R.id.textView3);
            holder.textView4 = convertView.findViewById(R.id.textView4);
            holder.btn_copy = convertView.findViewById(R.id.btn_copy);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView1.setText(codeBeanList.get(position).getmKey());
        holder.textView2.setText(codeBeanList.get(position).getDeviceId());
        switch (codeBeanList.get(position).getVipType()) {
            case 0:
                holder.textView3.setText("体验会员");
                break;
            case 1:
                holder.textView3.setText("月度会员");
                break;
            case 2:
                holder.textView3.setText("季度会员");
                break;
            case 3:
                holder.textView3.setText("半年度会员");
                break;
            case 4:
                holder.textView3.setText("年度会员");
                break;
        }
        if (0 == codeBeanList.get(position).getIsActivated()) {
            holder.textView4.setText("未激活");
            holder.textView4.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.textView4.setText("已激活");
            holder.textView4.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
        }
        holder.btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                Objects.requireNonNull(cm).setText(codeBeanList.get(position).getmKey());
                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;

    }

    static class ViewHolder {
        TextView textView1, textView2, textView3, textView4;
        Button btn_copy;
    }

}
