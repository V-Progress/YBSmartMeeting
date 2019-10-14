package com.yunbiao.yb_smart_meeting.activity.fragment.child.child;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntryGirdFragment extends BaseFragment {
    private static final String TAG = "EntryGirdFragment";
    private final int TYPE_INSIDE = 1;
    private GridView gvInside;
    private TextView tvTitle;

    public EntryGirdFragment() {
    }

    public static EntryGirdFragment newInstance(long meetId,String title) {
        EntryGirdFragment fragment = new EntryGirdFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putLong("meetId",meetId);
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
    }

    @Override
    protected void initData() {
        long meetId = getArguments().getLong("meetId");
        String title = getArguments().getString("title");
        Log.e(TAG, "initData: 当前会议ID：" + meetId);
        tvTitle.setText(title);
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryByMeetIdOrType(meetId, TYPE_INSIDE);
//        List<EntryInfo> entryInfos = new ArrayList<>();
//        for (int i = 0; i < 30; i++) {
//            EntryInfo entryInfo = new EntryInfo();
//            entryInfo.setHeadPath(entryInfo1s.get(0).getHeadPath());
//            entryInfos.add(entryInfo);
//        }

        gvInside.setAdapter(new EntryAdapter(entryInfos));
    }

    public class EntryAdapter extends BaseAdapter{

        private List<EntryInfo> datas;

        public EntryAdapter(List<EntryInfo> datas) {
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
                convertView.setTag(viewHodler);
            } else {
                viewHodler = (ViewHodler) convertView.getTag();
            }
            EntryInfo entryInfo = datas.get(position);
            Glide.with(getActivity()).load(entryInfo.getHeadPath()).asBitmap().into(viewHodler.civ);
            return convertView;
        }
        class ViewHodler {
            CircleImageView civ;
        }
    }

}
