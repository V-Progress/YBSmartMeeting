package com.yunbiao.yb_smart_meeting.activity.fragment.child;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntryAdapter extends BaseAdapter {

    private List<SignModel> datas;
    private Activity mAct;

    public EntryAdapter(Activity activity,List<SignModel> datas) {
        mAct = activity;
        this.datas = datas;
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
        ViewHodler viewHodler;
        if (convertView == null) {
            viewHodler = new ViewHodler();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry_info, parent, false);
            viewHodler.civ = convertView.findViewById(R.id.civ_head);
            viewHodler.ivMarker = convertView.findViewById(R.id.iv_marker);
            viewHodler.tvName = convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHodler);
        } else {
            viewHodler = (ViewHodler) convertView.getTag();
        }
        SignModel signModel = datas.get(position);
        EntryInfo entryInfo = signModel.getEntryInfo();
        viewHodler.tvName.setText(entryInfo.getName());

        String head;
        File file = new File(entryInfo.getHeadPath());
        if (file.exists()) {
            head = entryInfo.getHeadPath();
        } else {
            head = entryInfo.getHead();
        }

        Glide.with(mAct)
                .load(head)
                .asBitmap()
                .override(50, 50)
                .skipMemoryCache(true)
                .into(viewHodler.civ);

        Glide.with(mAct)
                .load(signModel.isSigned() ? R.mipmap.signed : R.mipmap.nosigned)
                .asBitmap()
                .override(50, 50)
                .skipMemoryCache(true)
                .into(viewHodler.ivMarker);

        return convertView;
    }

    class ViewHodler {
        CircleImageView civ;
        ImageView ivMarker;
        TextView tvName;
    }
}
