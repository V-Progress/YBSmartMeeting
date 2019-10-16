package com.yunbiao.yb_smart_meeting.activity.fragment.child.child;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.SignEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

public class EntryGirdFragment extends BaseFragment {
    private static final String TAG = "EntryGirdFragment";
    private GridView gvInside;
    private TextView tvTitle;
    private List<SignModel> signModels = new ArrayList<>();
    private EntryAdapter entryAdapter;

    public EntryGirdFragment() {
    }

    public static EntryGirdFragment newInstance(long meetId, int type, String title) {
        EntryGirdFragment fragment = new EntryGirdFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putInt("type", type);
        bundle.putLong("meetId", meetId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_inside;
    }

    @Override
    protected void initView() {
        gvInside = find(R.id.gv_inside);
        tvTitle = find(R.id.tv_title);

        entryAdapter = new EntryAdapter(signModels);
        gvInside.setAdapter(entryAdapter);
    }

    @Override
    protected void initData() {
        signModels.clear();

        long meetId = getArguments().getLong("meetId");
        String title = getArguments().getString("title");
        int type = getArguments().getInt("type");
        Log.e(TAG, "initData: 当前会议ID：" + meetId);

        tvTitle.setText(title);

        List<EntryInfo> entryInfos = DaoManager.get().queryEntryByMeetIdAndType(meetId, type);
        for (EntryInfo entryInfo : entryInfos) {
            SignModel signModel = new SignModel();
            boolean signed = DaoManager.get().isSigned(entryInfo.getMeetId(), entryInfo.getMeetEntryId());

            Log.e(TAG, "initData: ----- " + entryInfo.getMeetEntryId() + " --- " +entryInfo.getName() + " ---签到：" + signed);

            signModel.setEntryInfo(entryInfo);
            signModel.setSigned(signed);
            signModels.add(signModel);
        }

        if(entryAdapter != null){
            entryAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo recordInfo) {
        if(recordInfo != null){
            long meetEntryId = recordInfo.getMeetEntryId();
            if(signModels != null){
                for (SignModel signModel : signModels) {
                    EntryInfo entryInfo = signModel.getEntryInfo();
                    if(entryInfo.getMeetEntryId() == meetEntryId){
                        signModel.setSigned(DaoManager.get().isSigned(entryInfo.getMeetId(),entryInfo.getMeetEntryId()));
                    }
                }
            }
            if(entryAdapter != null){
                entryAdapter.notifyDataSetChanged();
            }
        }
    }

    class SignModel {
        private boolean isSigned;
        private EntryInfo entryInfo;

        public boolean isSigned() {
            return isSigned;
        }

        public void setSigned(boolean signed) {
            isSigned = signed;
        }

        public EntryInfo getEntryInfo() {
            return entryInfo;
        }

        public void setEntryInfo(EntryInfo entryInfo) {
            this.entryInfo = entryInfo;
        }
    }

    public class EntryAdapter extends BaseAdapter {

        private List<SignModel> datas;

        public EntryAdapter(List<SignModel> datas) {
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
                convertView.setTag(viewHodler);
            } else {
                viewHodler = (ViewHodler) convertView.getTag();
            }
            SignModel signModel = datas.get(position);
            EntryInfo entryInfo = signModel.getEntryInfo();
            Glide.with(getActivity()).load(entryInfo.getHeadPath()).asBitmap().skipMemoryCache(true).into(viewHodler.civ);

            if(signModel.isSigned()){
                Glide.with(getActivity()).load(R.mipmap.signed).asBitmap().skipMemoryCache(true).into(viewHodler.ivMarker);
            } else {
                Glide.with(getActivity()).load(R.mipmap.nosigned).asBitmap().skipMemoryCache(true).into(viewHodler.ivMarker);
            }
            return convertView;
        }
        class ViewHodler {
            CircleImageView civ;
            ImageView ivMarker;
        }
    }
}
