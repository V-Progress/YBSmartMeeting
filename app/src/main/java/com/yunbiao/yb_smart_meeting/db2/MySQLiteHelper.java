package com.yunbiao.yb_smart_meeting.db2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

public class MySQLiteHelper extends DaoMaster.OpenHelper {
    private static final String TAG = "MySQLiteHelper";

    public MySQLiteHelper(Context context, String name) {
        super(context, name);
    }

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        d("检查数据库版本" + oldVersion + "," + newVersion);
        if (oldVersion == newVersion) {
            d("数据库版本已是最新");
            return;
        }

        d("数据库需升级");
        MigrationHelper.migrate(db, MeetInfoDao.class);
        MigrationHelper.migrate(db, EntryInfoDao.class);
        MigrationHelper.migrate(db, FlowInfoDao.class);
        MigrationHelper.migrate(db, AdvertInfoDao.class);
        MigrationHelper.migrate(db, RecordInfoDao.class);
    }

    private void d(String msg) {
        Log.d(TAG, msg);
    }
}
