package com.zlx.sqlite;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by zhulx on 2017/9/12.
 */

public class BaseDaoFactory {
	private static final BaseDaoFactory instance = new BaseDaoFactory();

	private SQLiteDatabase sqLiteDatabase;

	private String sqliteDatabasePath;

	public static BaseDaoFactory getInstance() {
		return instance;
	}

	private BaseDaoFactory() {
		sqliteDatabasePath = "data/data/com.zlx.sqlite/zlx.db";
		sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath, null);
	}

	public <T> BaseDao<T> getBaseDao(Class<T> entityClass) {
		BaseDao baseDao = null;
		try {
			baseDao = BaseDao.class.newInstance();
			baseDao.init(sqLiteDatabase, entityClass);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return baseDao;
	}
}
