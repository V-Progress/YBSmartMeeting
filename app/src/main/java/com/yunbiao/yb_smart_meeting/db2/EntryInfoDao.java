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
 * DAO for table "ENTRY_INFO".
*/
public class EntryInfoDao extends AbstractDao<EntryInfo, Long> {

    public static final String TABLENAME = "ENTRY_INFO";

    /**
     * Properties of entity EntryInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property MeetId = new Property(1, long.class, "meetId", false, "MEET_ID");
        public final static Property MeetEntryId = new Property(2, long.class, "meetEntryId", false, "MEET_ENTRY_ID");
        public final static Property Sex = new Property(3, int.class, "sex", false, "SEX");
        public final static Property Phone = new Property(4, String.class, "phone", false, "PHONE");
        public final static Property Tectitle = new Property(5, String.class, "tectitle", false, "TECTITLE");
        public final static Property ComName = new Property(6, String.class, "comName", false, "COM_NAME");
        public final static Property Name = new Property(7, String.class, "name", false, "NAME");
        public final static Property SeatNumber = new Property(8, String.class, "seatNumber", false, "SEAT_NUMBER");
        public final static Property Type = new Property(9, int.class, "type", false, "TYPE");
        public final static Property Head = new Property(10, String.class, "head", false, "HEAD");
        public final static Property HeadPath = new Property(11, String.class, "headPath", false, "HEAD_PATH");
    }


    public EntryInfoDao(DaoConfig config) {
        super(config);
    }
    
    public EntryInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ENTRY_INFO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"MEET_ID\" INTEGER NOT NULL ," + // 1: meetId
                "\"MEET_ENTRY_ID\" INTEGER NOT NULL UNIQUE ," + // 2: meetEntryId
                "\"SEX\" INTEGER NOT NULL ," + // 3: sex
                "\"PHONE\" TEXT," + // 4: phone
                "\"TECTITLE\" TEXT," + // 5: tectitle
                "\"COM_NAME\" TEXT," + // 6: comName
                "\"NAME\" TEXT," + // 7: name
                "\"SEAT_NUMBER\" TEXT," + // 8: seatNumber
                "\"TYPE\" INTEGER NOT NULL ," + // 9: type
                "\"HEAD\" TEXT," + // 10: head
                "\"HEAD_PATH\" TEXT);"); // 11: headPath
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ENTRY_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, EntryInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getMeetId());
        stmt.bindLong(3, entity.getMeetEntryId());
        stmt.bindLong(4, entity.getSex());
 
        String phone = entity.getPhone();
        if (phone != null) {
            stmt.bindString(5, phone);
        }
 
        String tectitle = entity.getTectitle();
        if (tectitle != null) {
            stmt.bindString(6, tectitle);
        }
 
        String comName = entity.getComName();
        if (comName != null) {
            stmt.bindString(7, comName);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(8, name);
        }
 
        String seatNumber = entity.getSeatNumber();
        if (seatNumber != null) {
            stmt.bindString(9, seatNumber);
        }
        stmt.bindLong(10, entity.getType());
 
        String head = entity.getHead();
        if (head != null) {
            stmt.bindString(11, head);
        }
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(12, headPath);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, EntryInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getMeetId());
        stmt.bindLong(3, entity.getMeetEntryId());
        stmt.bindLong(4, entity.getSex());
 
        String phone = entity.getPhone();
        if (phone != null) {
            stmt.bindString(5, phone);
        }
 
        String tectitle = entity.getTectitle();
        if (tectitle != null) {
            stmt.bindString(6, tectitle);
        }
 
        String comName = entity.getComName();
        if (comName != null) {
            stmt.bindString(7, comName);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(8, name);
        }
 
        String seatNumber = entity.getSeatNumber();
        if (seatNumber != null) {
            stmt.bindString(9, seatNumber);
        }
        stmt.bindLong(10, entity.getType());
 
        String head = entity.getHead();
        if (head != null) {
            stmt.bindString(11, head);
        }
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(12, headPath);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public EntryInfo readEntity(Cursor cursor, int offset) {
        EntryInfo entity = new EntryInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // meetId
            cursor.getLong(offset + 2), // meetEntryId
            cursor.getInt(offset + 3), // sex
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // phone
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // tectitle
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // comName
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // name
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // seatNumber
            cursor.getInt(offset + 9), // type
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // head
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11) // headPath
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, EntryInfo entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeetId(cursor.getLong(offset + 1));
        entity.setMeetEntryId(cursor.getLong(offset + 2));
        entity.setSex(cursor.getInt(offset + 3));
        entity.setPhone(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setTectitle(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setComName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setSeatNumber(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setType(cursor.getInt(offset + 9));
        entity.setHead(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setHeadPath(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(EntryInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(EntryInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(EntryInfo entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
