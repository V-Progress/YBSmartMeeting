package com.yunbiao.yb_smart_meeting.db2;

import android.util.Log;

import com.yunbiao.yb_smart_meeting.APP;

import org.greenrobot.greendao.database.Database;

import java.util.List;

public class DaoManager {
    private static final String TAG = "DaoManager";
    private static DaoManager daoManager = new DaoManager();
    private final String DB_NAME = "yb_meeting_db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public static final long FAILURE = -1;
    public static final long SUCCESS = 0;

    public static DaoManager get(){
        return daoManager;
    }

    private DaoManager(){
    }

    public void initDb(){
        Log.e(TAG, "initDb: ");
        MySQLiteHelper helper =new MySQLiteHelper(APP.getContext(),DB_NAME,null);
        Log.e(TAG, "initDb: " + helper);
        Database db = helper.getWritableDb();
        Log.e(TAG, "initDb: " + db);
        daoMaster = new DaoMaster(db);
        Log.e(TAG, "initDb: " + daoMaster);
        daoSession = daoMaster.newSession();
        Log.e(TAG, "initDb: " + daoSession);
        daoSession.clear();
        daoSession.getVisitorBeanDao().detachAll();
        daoSession.getPassageBeanDao().detachAll();
        daoSession.getUserBeanDao().detachAll();
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public DaoMaster getDaoMaster(){
        return daoMaster;
    }

    public <T> long add(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public <T> long addOrUpdate(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
       return  daoSession.insertOrReplace(clazz);
    }

    public void addEntryList(List<EntryInfo> infos){
        daoSession.getEntryInfoDao().insertOrReplaceInTx(infos);
    }
    public void addAdvertList(List<AdvertInfo> infos){
        daoSession.getAdvertInfoDao().insertOrReplaceInTx(infos);
    }
    public void addFlowList(List<FlowInfo> infos){
        daoSession.getFlowInfoDao().insertOrReplaceInTx(infos);
    }

    public <T>long update(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T>List<T> queryAll(Class<T> clazz){
        if(daoSession == null){
            return null;
        }
        return daoSession.loadAll(clazz);
    }

    public long deleteMeetInfos(List<MeetInfo> meetInfos){
        if(meetInfos == null || meetInfos.size() <= 0){
            return SUCCESS;
        }
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.getMeetInfoDao().deleteInTx(meetInfos);
        return SUCCESS;
    }

    public long deleteEntryInfos(List<EntryInfo> entryInfos){
        if(entryInfos == null || entryInfos.size() <= 0){
            return SUCCESS;
        }
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.getEntryInfoDao().deleteInTx(entryInfos);
        return SUCCESS;
    }

    public long deleteAdvertInfos(List<AdvertInfo> advertInfos){
        if(advertInfos == null || advertInfos.size() <= 0){
            return SUCCESS;
        }
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.getAdvertInfoDao().deleteInTx(advertInfos);
        return SUCCESS;
    }

    public long deleteFlowInfos(List<FlowInfo> flowInfos){
        if(flowInfos == null || flowInfos.size() <= 0){
            return SUCCESS;
        }
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.getFlowInfoDao().deleteInTx(flowInfos);
        return SUCCESS;
    }

    public List<EntryInfo> queryEntryInfoByComId(int comId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getEntryInfoDao().queryBuilder().where(EntryInfoDao.Properties.ComId.eq(comId)).list();
    }
    public List<EntryInfo> queryEntryInfoByMeetId(long meetId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getEntryInfoDao().queryBuilder().where(EntryInfoDao.Properties.MeetId.eq(meetId)).list();
    }

    public List<AdvertInfo> queryAdvertByComdId(int comId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getAdvertInfoDao().queryBuilder().where(AdvertInfoDao.Properties.ComId.eq(comId)).list();
    }

    public List<AdvertInfo> queryAdvertByMeetId(long id){
        if(daoSession == null){
            return null;
        }
        return daoSession.getAdvertInfoDao().queryBuilder().where(AdvertInfoDao.Properties.MeetId.eq(id)).list();

    }

    public List<FlowInfo> queryFlowByComId(int comId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getFlowInfoDao().queryBuilder().where(FlowInfoDao.Properties.ComId.eq(comId)).list();
    }

    public List<FlowInfo> queryFlowByMeetId(long meetId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getFlowInfoDao().queryBuilder().where(FlowInfoDao.Properties.MeetId.eq(meetId)).list();
    }

    public List<MeetInfo> queryMeetInfoByComId(long comId) {
        if(daoMaster == null){
            return null;
        }
        return daoSession.getMeetInfoDao().queryBuilder().where(MeetInfoDao.Properties.ComId.eq(comId)).list();
    }

    public MeetInfo queryMeetInfoByNum(int num) {
        if(daoMaster == null){
            return null;
        }
        return daoSession.getMeetInfoDao().queryBuilder().where(MeetInfoDao.Properties.Num.eq(num)).unique();
    }

    public MeetInfo queryMeetInfoByMeetId(long meetId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getMeetInfoDao().queryBuilder().where(MeetInfoDao.Properties.Id.eq(meetId)).unique();
    }
    /*========================================================*/

    public MeetInfo queryByMeetNum(int num){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getMeetInfoDao().queryBuilder().where(MeetInfoDao.Properties.Num.eq(num)).unique();
    }

    public List<RecordInfo> queryRecordByMeetIdAndEntryId(long meetId,String meetEntryId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getRecordInfoDao().queryBuilder().where(RecordInfoDao.Properties.MeetId.eq(meetId),RecordInfoDao.Properties.MeetEntryId.eq(meetEntryId)).list();
    }

    public boolean isSigned(long meetId,String meetEntryId){
        List<RecordInfo> recordInfos = queryRecordByMeetIdAndEntryId(meetId, meetEntryId);
        return recordInfos != null && recordInfos.size() > 0;
    }

    public List<RecordInfo> queryRecordByMeetId(long meetId){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getRecordInfoDao().queryBuilder().where(RecordInfoDao.Properties.MeetId.eq(meetId)).list();
    }

    public List<EntryInfo> queryEntryByMeetIdAndType(long meetId, int type){
        if(daoMaster == null){
            return null;
        }
        return daoSession.getEntryInfoDao()
                .queryBuilder()
                .where(EntryInfoDao.Properties.MeetId.eq(meetId),EntryInfoDao.Properties.Type.eq(type))
                .list();
    }

    public EntryInfo queryEntryByMeetIdAndEntryId(long meetId,String entryId){
        if(daoSession == null){
            return  null;
        }
        return daoSession.getEntryInfoDao().queryBuilder().where(EntryInfoDao.Properties.MeetId.eq(meetId),EntryInfoDao.Properties.MeetEntryId.eq(entryId)).unique();
    }

    public List<EntryInfo> queryEntryByComId(int comId) {
        if(daoSession == null){
            return  null;
        }
        return daoSession.getEntryInfoDao().queryBuilder().where(EntryInfoDao.Properties.ComId.eq(comId)).list();
    }

    public List<MeetInfo> queryMeetByComId(int comId){
        if(daoSession == null){
            return  null;
        }
        return daoSession.getMeetInfoDao().queryBuilder().where(MeetInfoDao.Properties.ComId.eq(comId)).list();
    }

    public List<RecordInfo> queryRecordByUpload(boolean isUpload){
        if(daoSession == null){
            return null;
        }
        return daoSession.getRecordInfoDao().queryBuilder().where(RecordInfoDao.Properties.IsUpload.eq(isUpload)).list();
    }

    public RecordInfo queryRecordByTime(long time){
        if(daoSession == null){
            return null;
        }
        return daoSession.getRecordInfoDao().queryBuilder().where(RecordInfoDao.Properties.Time.eq(time)).unique();
    }

    public List<UserBean> queryUserByFaceId(String faceId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserBeanDao().queryBuilder().where(UserBeanDao.Properties.FaceId.eq(faceId)).list();
    }

    public List<DepartBean> queryDepartByCompId(int compId) {
        if(daoSession == null){
            return null;
        }
        return daoSession.getDepartBeanDao().queryBuilder().where(DepartBeanDao.Properties.CompId.eq(compId)).list();

    }

    public List<PassageBean> queryByPassDate(String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getPassageBeanDao().queryBuilder().where(PassageBeanDao.Properties.CreateDate.eq(date)).list();
    }

    public <T>long delete(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    public <T>long deleteAll(Class<T> clazz){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.deleteAll(clazz);
        return SUCCESS;
    }

    public <T>long deleteAllByMeetId(long meetId){
        if(daoSession == null){
            return FAILURE;
        }
        List<RecordInfo> recordInfos = queryRecordByMeetId(meetId);
        if(recordInfos != null){
            for (RecordInfo recordInfo : recordInfos) {
                delete(recordInfo);
            }
        }
        return SUCCESS;
    }

    public long deleteAllMeeting(){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.getMeetInfoDao().deleteAll();
        return SUCCESS;
    }

}
