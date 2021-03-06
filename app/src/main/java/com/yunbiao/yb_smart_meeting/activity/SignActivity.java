package com.yunbiao.yb_smart_meeting.activity;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.base.BaseActivity;
import com.yunbiao.yb_smart_meeting.adapter.SignAdapter;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.PassageBean;
import com.yunbiao.yb_smart_meeting.utils.SdCardUtils;
import com.yunbiao.yb_smart_meeting.utils.ThreadUitls;
import com.yunbiao.yb_smart_meeting.utils.UIUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */


public class SignActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignActivity";

    private ListView lv_sign_List;
    private TextView tv_date;
    private ImageView iv_back;
    private View pb_load_list;
    private TextView tv_load_tips;

    private final int MODE_ALL = 0;
    private final int MODE_SENDED = 1;
    private final int MODE_UNSENDED = 2;
    private int DATA_MODE = MODE_UNSENDED;

    private String queryDate = "";

    private List<PassageBean> mSignList;
    private List<PassageBean> mShowList = new ArrayList<>();
    private Spinner spnDataMode;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy年MM月dd日");

    @Override
    protected String setTitle() {
        return "离线数据";
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_table_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_table_h;
    }

    @Override
    protected void initView() {
        lv_sign_List = (ListView) findViewById(R.id.lv_sign_List);
        tv_date = (TextView) findViewById(R.id.tv_date);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        pb_load_list = findViewById(R.id.pb_load_list);
        tv_load_tips = (TextView) findViewById(R.id.tv_load_tips);
        spnDataMode = (Spinner) findViewById(R.id.spn_data_mode);
        tv_date.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String today = dateFormatter.format(new Date());
        tv_date.setText(today);
        queryDate = today;
        initSpinner();
    }

    private void initSpinner() {
        final String[] modeArray = {"全部", "已发送", "未发送"};
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_simple_text, modeArray);

        spnDataMode.setAdapter(spnAdapter);
        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnDataMode.setPopupBackgroundDrawable(drawable);
        spnDataMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DATA_MODE = position;
                loadSignList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnDataMode.setSelection(modeArray.length - 1);
    }

    private void loadSignList() {
        pb_load_list.setVisibility(View.VISIBLE);
        lv_sign_List.setVisibility(View.GONE);
        tv_load_tips.setVisibility(View.GONE);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                mShowList.clear();
                mSignList = DaoManager.get().queryByPassDate(queryDate);
                if (mSignList == null || mSignList.size() <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_load_tips.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    });
                    return;
                }

                for (PassageBean signBean : mSignList) {
                    if (DATA_MODE == MODE_UNSENDED && !signBean.isUpload()) {
                        mShowList.add(signBean);
                    } else if (DATA_MODE == MODE_SENDED && signBean.isUpload()) {
                        mShowList.add(signBean);
                    } else if (DATA_MODE == MODE_ALL) {
                        mShowList.add(signBean);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mShowList != null && mShowList.size() > 0) {
                            SignAdapter adapter = new SignAdapter(SignActivity.this, mShowList);
                            lv_sign_List.setAdapter(adapter);

                            lv_sign_List.setVisibility(View.VISIBLE);
                            pb_load_list.setVisibility(View.GONE);
                            tv_load_tips.setVisibility(View.GONE);
                        } else {
                            if (DATA_MODE == MODE_UNSENDED) {
                                tv_load_tips.setText("数据已全部上传");
                            } else {
                                tv_load_tips.setText("暂无数据");
                            }
                            tv_load_tips.setVisibility(View.VISIBLE);
                            lv_sign_List.setVisibility(View.GONE);
                            pb_load_list.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_date:
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        SignActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String yearStr = year + "年";

                                int realMonth = month + 1;
                                String monthStr = realMonth + "月";
                                if (realMonth < 10) {
                                    monthStr = "0" + realMonth + "月";
                                }

                                String dayStr = dayOfMonth + "日";
                                if(dayOfMonth<10){
                                    dayStr = "0" + dayOfMonth + "日";
                                }
                                String date = yearStr + monthStr + dayStr;
                                tv_date.setText(date);

                                queryDate = date;
                                loadSignList();
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
        }
    }

    private boolean isExporting = false;

    public void exportToUD(View view) {
        if(isExporting){
            UIUtils.showTitleTip(SignActivity.this,"正在导出，请稍等");
            return;
        }
        isExporting = true;
        String usbDiskPath = SdCardUtils.getUsbDiskPath(this);
        File file = new File(usbDiskPath);
        if(!file.exists()){
            isExporting = false;
            UIUtils.showTitleTip(SignActivity.this,"请插入U盘");
            return;
        }

        String[] list = file.list();
        for (String s : list) {
            File usbFile = new File(file,s);
            if (usbFile.isDirectory()) {
                file = usbFile;
            }
        }

        final String fileName = "faceRecord_" +dateFormat.format(new Date())+ ".txt";
        final File jsonFile = new File(file, fileName);

        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
//                List<SignBean> signBeans = signDao.selectAll();
//                Iterator<SignBean> iterator = signBeans.iterator();
//                while (iterator.hasNext()) {
//                    SignBean next = iterator.next();
//                    if(next.isUpload()){
//                        iterator.remove();
//                    }
//                }
//
//                if(signBeans.size() <= 0){
//                    isExporting = false;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            UIUtils.showTitleTip(SignActivity.this,"数据已全部上传\n没有可导出的数据");
//                        }
//                    });
//                    return;
//                }
//
//                String jsonStr = new Gson().toJson(signBeans);
//
//                OutputStream outputStream = null;
//                try {
//                    outputStream = new FileOutputStream(jsonFile);
//                    outputStream.write(jsonStr.getBytes());
//                    outputStream.flush();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (outputStream != null) {
//                        try {
//                            outputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(jsonFile.exists()){
//                            UIUtils.showTitleTip(SignActivity.this,"导出成功，文件路径：\n" + jsonFile.getPath());
//                        } else {
//                            UIUtils.showTitleTip(SignActivity.this,"导出失败");
//                        }
//                    }
//                });

                isExporting = false;
            }
        });
    }
}