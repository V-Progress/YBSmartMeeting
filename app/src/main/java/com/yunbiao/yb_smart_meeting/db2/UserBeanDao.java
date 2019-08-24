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
 * DAO for table "USER_BEAN".
*/
public class UserBeanDao extends AbstractDao<UserBean, Long> {

    public static final String TABLENAME = "USER_BEAN";

    /**
     * Properties of entity UserBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property FaceId = new Property(1, String.class, "faceId", false, "FACE_ID");
        public final static Property HeadPath = new Property(2, String.class, "headPath", false, "HEAD_PATH");
        public final static Property DownloadTag = new Property(3, int.class, "downloadTag", false, "DOWNLOAD_TAG");
        public final static Property DepartId = new Property(4, long.class, "departId", false, "DEPART_ID");
        public final static Property DepartName = new Property(5, String.class, "departName", false, "DEPART_NAME");
        public final static Property Autograph = new Property(6, String.class, "autograph", false, "AUTOGRAPH");
        public final static Property Birthday = new Property(7, String.class, "birthday", false, "BIRTHDAY");
        public final static Property CardId = new Property(8, String.class, "cardId", false, "CARD_ID");
        public final static Property Head = new Property(9, String.class, "head", false, "HEAD");
        public final static Property Name = new Property(10, String.class, "name", false, "NAME");
        public final static Property Age = new Property(11, int.class, "age", false, "AGE");
        public final static Property Number = new Property(12, String.class, "number", false, "NUMBER");
        public final static Property Position = new Property(13, String.class, "position", false, "POSITION");
        public final static Property Sex = new Property(14, int.class, "sex", false, "SEX");
    }


    public UserBeanDao(DaoConfig config) {
        super(config);
    }
    
    public UserBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: id
                "\"FACE_ID\" TEXT UNIQUE ," + // 1: faceId
                "\"HEAD_PATH\" TEXT," + // 2: headPath
                "\"DOWNLOAD_TAG\" INTEGER NOT NULL ," + // 3: downloadTag
                "\"DEPART_ID\" INTEGER NOT NULL ," + // 4: departId
                "\"DEPART_NAME\" TEXT," + // 5: departName
                "\"AUTOGRAPH\" TEXT," + // 6: autograph
                "\"BIRTHDAY\" TEXT," + // 7: birthday
                "\"CARD_ID\" TEXT," + // 8: cardId
                "\"HEAD\" TEXT," + // 9: head
                "\"NAME\" TEXT," + // 10: name
                "\"AGE\" INTEGER NOT NULL ," + // 11: age
                "\"NUMBER\" TEXT," + // 12: number
                "\"POSITION\" TEXT," + // 13: position
                "\"SEX\" INTEGER NOT NULL );"); // 14: sex
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String faceId = entity.getFaceId();
        if (faceId != null) {
            stmt.bindString(2, faceId);
        }
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(3, headPath);
        }
        stmt.bindLong(4, entity.getDownloadTag());
        stmt.bindLong(5, entity.getDepartId());
 
        String departName = entity.getDepartName();
        if (departName != null) {
            stmt.bindString(6, departName);
        }
 
        String autograph = entity.getAutograph();
        if (autograph != null) {
            stmt.bindString(7, autograph);
        }
 
        String birthday = entity.getBirthday();
        if (birthday != null) {
            stmt.bindString(8, birthday);
        }
 
        String cardId = entity.getCardId();
        if (cardId != null) {
            stmt.bindString(9, cardId);
        }
 
        String head = entity.getHead();
        if (head != null) {
            stmt.bindString(10, head);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(11, name);
        }
        stmt.bindLong(12, entity.getAge());
 
        String number = entity.getNumber();
        if (number != null) {
            stmt.bindString(13, number);
        }
 
        String position = entity.getPosition();
        if (position != null) {
            stmt.bindString(14, position);
        }
        stmt.bindLong(15, entity.getSex());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String faceId = entity.getFaceId();
        if (faceId != null) {
            stmt.bindString(2, faceId);
        }
 
        String headPath = entity.getHeadPath();
        if (headPath != null) {
            stmt.bindString(3, headPath);
        }
        stmt.bindLong(4, entity.getDownloadTag());
        stmt.bindLong(5, entity.getDepartId());
 
        String departName = entity.getDepartName();
        if (departName != null) {
            stmt.bindString(6, departName);
        }
 
        String autograph = entity.getAutograph();
        if (autograph != null) {
            stmt.bindString(7, autograph);
        }
 
        String birthday = entity.getBirthday();
        if (birthday != null) {
            stmt.bindString(8, birthday);
        }
 
        String cardId = entity.getCardId();
        if (cardId != null) {
            stmt.bindString(9, cardId);
        }
 
        String head = entity.getHead();
        if (head != null) {
            stmt.bindString(10, head);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(11, name);
        }
        stmt.bindLong(12, entity.getAge());
 
        String number = entity.getNumber();
        if (number != null) {
            stmt.bindString(13, number);
        }
 
        String position = entity.getPosition();
        if (position != null) {
            stmt.bindString(14, position);
        }
        stmt.bindLong(15, entity.getSex());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public UserBean readEntity(Cursor cursor, int offset) {
        UserBean entity = new UserBean( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // faceId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // headPath
            cursor.getInt(offset + 3), // downloadTag
            cursor.getLong(offset + 4), // departId
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // departName
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // autograph
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // birthday
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // cardId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // head
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // name
            cursor.getInt(offset + 11), // age
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // number
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // position
            cursor.getInt(offset + 14) // sex
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserBean entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setFaceId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setHeadPath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDownloadTag(cursor.getInt(offset + 3));
        entity.setDepartId(cursor.getLong(offset + 4));
        entity.setDepartName(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAutograph(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setBirthday(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setCardId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setHead(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setName(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setAge(cursor.getInt(offset + 11));
        entity.setNumber(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setPosition(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setSex(cursor.getInt(offset + 14));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserBean entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
