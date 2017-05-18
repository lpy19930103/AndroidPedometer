package com.lipy.step.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "PedometerLog".
*/
public class PedometerEntityDao extends AbstractDao<PedometerEntity, Long> {

    public static final String TABLENAME = "PedometerLog";

    /**
     * Properties of entity PedometerEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Date = new Property(1, String.class, "date", false, "DATE");
        public final static Property DailyStep = new Property(2, Integer.class, "dailyStep", false, "DAILY_STEP");
        public final static Property TotalSteps = new Property(3, Integer.class, "totalSteps", false, "TOTAL_STEPS");
        public final static Property TagStep = new Property(4, Integer.class, "tagStep", false, "TAG_STEP");
        public final static Property LastSystemSteps = new Property(5, Integer.class, "lastSystemSteps", false, "LAST_SYSTEM_STEPS");
        public final static Property ReStart = new Property(6, Boolean.class, "reStart", false, "RE_START");
        public final static Property PunchCard = new Property(7, Boolean.class, "punchCard", false, "PUNCH_CARD");
    };


    public PedometerEntityDao(DaoConfig config) {
        super(config);
    }
    
    public PedometerEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PedometerLog\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"DATE\" TEXT NOT NULL ," + // 1: date
                "\"DAILY_STEP\" INTEGER," + // 2: dailyStep
                "\"TOTAL_STEPS\" INTEGER," + // 3: totalSteps
                "\"TAG_STEP\" INTEGER," + // 4: tagStep
                "\"LAST_SYSTEM_STEPS\" INTEGER," + // 5: lastSystemSteps
                "\"RE_START\" INTEGER," + // 6: reStart
                "\"PUNCH_CARD\" INTEGER);"); // 7: punchCard
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PedometerLog\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, PedometerEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getDate());
 
        Integer dailyStep = entity.getDailyStep();
        if (dailyStep != null) {
            stmt.bindLong(3, dailyStep);
        }
 
        Integer totalSteps = entity.getTotalSteps();
        if (totalSteps != null) {
            stmt.bindLong(4, totalSteps);
        }
 
        Integer tagStep = entity.getTagStep();
        if (tagStep != null) {
            stmt.bindLong(5, tagStep);
        }
 
        Integer lastSystemSteps = entity.getLastSystemSteps();
        if (lastSystemSteps != null) {
            stmt.bindLong(6, lastSystemSteps);
        }
 
        Boolean reStart = entity.getReStart();
        if (reStart != null) {
            stmt.bindLong(7, reStart ? 1L: 0L);
        }
 
        Boolean punchCard = entity.getPunchCard();
        if (punchCard != null) {
            stmt.bindLong(8, punchCard ? 1L: 0L);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public PedometerEntity readEntity(Cursor cursor, int offset) {
        PedometerEntity entity = new PedometerEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // date
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // dailyStep
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // totalSteps
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // tagStep
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // lastSystemSteps
            cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0, // reStart
            cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0 // punchCard
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, PedometerEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDate(cursor.getString(offset + 1));
        entity.setDailyStep(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setTotalSteps(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setTagStep(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setLastSystemSteps(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setReStart(cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0);
        entity.setPunchCard(cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(PedometerEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(PedometerEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
