package com.yunbiao.yb_smart_meeting.db2;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ADVERT_INFO".
*/
public class AdvertInfoDao extends AbstractDao<AdvertInfo, Long> {

    public static final String TABLENAME = "ADVERT_INFO";

    /**
     * Properties of entity AdvertInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property MeetId = new Property(1, long.class, "meetId", false, "MEET_ID");
        public final static Property Type = new Property(2, int.class, "type", false, "TYPE");
        public final static Property Url = new Property(3, String.class, "url", false, "URL");
        public final static Property AdvertId = new Property(4, int.class, "advertId", false, "ADVERT_ID");
        public final static Property Path = new Property(5, String.class, "path", false, "PATH");
        public final static Property ReadNum = new Property(6, long.class, "readNum", false, "READ_NUM");
        public final static Property GoodNum = new Property(7, long.class, "goodNum", false, "GOOD_NUM");
        public final static Property Time = new Property(8, int.class, "time", false, "TIME");
        public final static Property ShareUrl = new Property(9, String.class, "shareUrl", false, "SHARE_URL");
    }


    public AdvertInfoDao(DaoConfig config) {
        super(config);
    }
    
    public AdvertInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ADVERT_INFO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"MEET_ID\" INTEGER NOT NULL ," + // 1: meetId
                "\"TYPE\" INTEGER NOT NULL ," + // 2: type
                "\"URL\" TEXT," + // 3: url
                "\"ADVERT_ID\" INTEGER NOT NULL ," + // 4: advertId
                "\"PATH\" TEXT," + // 5: path
                "\"READ_NUM\" INTEGER NOT NULL ," + // 6: readNum
                "\"GOOD_NUM\" INTEGER NOT NULL ," + // 7: goodNum
                "\"TIME\" INTEGER NOT NULL ," + // 8: time
                "\"SHARE_URL\" TEXT);"); // 9: shareUrl
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ADVERT_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, AdvertInfo entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getMeetId());
        stmt.bindLong(3, entity.getType());
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(4, url);
        }
        stmt.bindLong(5, entity.getAdvertId());
 
        String path = entity.getPath();
        if (path != null) {
            stmt.bindString(6, path);
        }
        stmt.bindLong(7, entity.getReadNum());
        stmt.bindLong(8, entity.getGoodNum());
        stmt.bindLong(9, entity.getTime());
 
        String shareUrl = entity.getShareUrl();
        if (shareUrl != null) {
            stmt.bindString(10, shareUrl);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, AdvertInfo entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getMeetId());
        stmt.bindLong(3, entity.getType());
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(4, url);
        }
        stmt.bindLong(5, entity.getAdvertId());
 
        String path = entity.getPath();
        if (path != null) {
            stmt.bindString(6, path);
        }
        stmt.bindLong(7, entity.getReadNum());
        stmt.bindLong(8, entity.getGoodNum());
        stmt.bindLong(9, entity.getTime());
 
        String shareUrl = entity.getShareUrl();
        if (shareUrl != null) {
            stmt.bindString(10, shareUrl);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public AdvertInfo readEntity(Cursor cursor, int offset) {
        AdvertInfo entity = new AdvertInfo( //
            cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // meetId
            cursor.getInt(offset + 2), // type
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // url
            cursor.getInt(offset + 4), // advertId
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // path
            cursor.getLong(offset + 6), // readNum
            cursor.getLong(offset + 7), // goodNum
            cursor.getInt(offset + 8), // time
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // shareUrl
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, AdvertInfo entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setMeetId(cursor.getLong(offset + 1));
        entity.setType(cursor.getInt(offset + 2));
        entity.setUrl(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAdvertId(cursor.getInt(offset + 4));
        entity.setPath(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setReadNum(cursor.getLong(offset + 6));
        entity.setGoodNum(cursor.getLong(offset + 7));
        entity.setTime(cursor.getInt(offset + 8));
        entity.setShareUrl(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(AdvertInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(AdvertInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(AdvertInfo entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
