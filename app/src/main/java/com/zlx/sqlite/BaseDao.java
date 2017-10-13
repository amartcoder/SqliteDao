package com.zlx.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhulx on 2017/9/12.
 */

public class BaseDao<T> implements IBaseDao<T> {
	//定义数据库连接
	private SQLiteDatabase sqLiteDatabase;
	//定义操作实体
	private Class<T> entityClass;
	//表名
	private String tableName;

	private boolean isInit = false;
	//表中成员与实体bean的映射
	private Map<String, Field> cacheMap;

	BaseDao() {
	}

	protected boolean init(SQLiteDatabase sqLiteDatabase, Class<T> entityClass) {
		this.sqLiteDatabase = sqLiteDatabase;
		this.entityClass = entityClass;

		if (!isInit) {
			tableName = entityClass.getAnnotation(DbTable.class).value();
			if (!sqLiteDatabase.isOpen()) {
				return false;
			}
			//建表
			String createTableSql = createTableSql();
			sqLiteDatabase.execSQL(createTableSql);

			cacheMap = new HashMap<>();
			initCacheMap();

			isInit = true;
		}

		return isInit;
	}

	private String createTableSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists ");
		sb.append(tableName + "(");

		Field[] fields = entityClass.getDeclaredFields();
		for(Field f : fields) {
			Class type = f.getType();
			if (type == String.class) {
				sb.append(f.getAnnotation(DbField.class).value() + " TEXT,");
			} else if (type == Integer.class) {
				sb.append(f.getAnnotation(DbField.class).value() + " INTEGER,");
			} else if (type == Long.class) {
				sb.append(f.getAnnotation(DbField.class).value() + " LONG,");
			} else if (type == Double.class) {
				sb.append(f.getAnnotation(DbField.class).value() + " DOUBLE,");
			} else if (type == byte[].class) {
				sb.append(f.getAnnotation(DbField.class).value() + " BLOB,");
			} else {
				continue;
			}
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");

		return sb.toString();
	}

	@Override
	public long insert(T entity) {
		//获取到字段名和值的映射
		Map<String, String> map = getValues(entity);
		ContentValues values = getContentValues(map);
		long result = sqLiteDatabase.insert(tableName, null, values);
		return result;
	}

	/**
	 * 更新数据库
	 * @param entity 待更新实体
	 * @param conditionFields 指定条件字段名称，为null的话，更新表中所有行
	 * @return 此次更新影响的行数
	 */
	@Override
	public int update(T entity, String[] conditionFields) {
		int result = 0;
		//获取到字段名和值的映射
		Map<String, String> map = getValues(entity);
		if (conditionFields != null) {
			StringBuilder whereBuilder = new StringBuilder();
			String[] whereArgs = new String[conditionFields.length];
			ContentValues values = new ContentValues();
			generateUpdateParams(map, conditionFields, values, whereBuilder, whereArgs);

			result = sqLiteDatabase.update(tableName, values, whereBuilder.toString(), whereArgs);
		} else {
			ContentValues values = getContentValues(map);
			result = sqLiteDatabase.update(tableName, values, null, null);
		}

		return result;
	}

	@Override
	public int delete(T entity) {
		ArrayList<String> whereArgs = new ArrayList<>();
		StringBuilder whereClause = new StringBuilder();
		//获取到字段名和值的映射
		Map<String, String> map = getValues(entity);
		generateDeleteOrUpdateParams(map, whereClause, whereArgs);
		String[] args = new String[whereArgs.size()];
		int num = sqLiteDatabase.delete(tableName, whereClause.toString(), whereArgs.toArray(args));
		return num;
	}

	@Override
	public List<T> query(T entity) {
		return query(entity, null, null, null, null);
	}

	@Override
	public List<T> query(T entity, String groupBy, String having, String orderBy, String limit) {
		//selection, selectionArgs, groupBy, having, orderBy, limit
		ArrayList<String> selectionArgs = new ArrayList<>();
		StringBuilder selection = new StringBuilder();
		//获取到字段名和值的映射
		Map<String, String> map = getValues(entity);
		generateDeleteOrUpdateParams(map, selection, selectionArgs);
		String[] args = new String[selectionArgs.size()];
		Cursor cursor = sqLiteDatabase.query(tableName, null, selection.toString(), selectionArgs.toArray(args), groupBy, having, orderBy, limit);

		List<T> result = new ArrayList<>();

		while (cursor.moveToNext()) {
			String[] columns = cursor.getColumnNames();
			T newEntity = null;
			try {
				newEntity = entityClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if (newEntity == null) {
				continue;
			}
			for (String column : columns) {
				Field f = cacheMap.get(column);
				f.setAccessible(true);
				Class type = f.getType();
				try {
					if (type == String.class) {
						String val = cursor.getString(cursor.getColumnIndex(column));
						f.set(newEntity, val);
					} else if (type == Integer.class) {
						int val = cursor.getInt(cursor.getColumnIndex(column));
						f.set(newEntity, val);
					} else if (type == Long.class) {
						long val = cursor.getLong(cursor.getColumnIndex(column));
						f.set(newEntity, val);
					} else if (type == Double.class) {
						double val = cursor.getDouble(cursor.getColumnIndex(column));
						f.set(newEntity, val);
					} else if (type == byte[].class) {
						byte[] val = cursor.getBlob(cursor.getColumnIndex(column));
						f.set(newEntity, val);
					} else {
						continue;
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			result.add(newEntity);
		}
		return result;
	}

	private void generateDeleteOrUpdateParams(Map<String, String> map, StringBuilder whereClause, ArrayList<String> whereArgs) {
		Set<String> keys = map.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = map.get(key);
			if (value != null) {
				whereClause.append(key).append("=? and ");
				whereArgs.add(value);
			}
		}
		int whereLength = whereClause.length();
		if (whereLength > 3) {
			whereClause.delete(whereClause.length() - 4, whereLength);
		}
	}

	private void generateUpdateParams(Map<String, String> map, String[] conditionFields, ContentValues values,
									  StringBuilder whereBuilder, String[] whereArgs) {

		int argsIndex = 0;
		Set<String> keys = map.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = map.get(key);
			if (value != null) {
				boolean isCondition = false;
				//拼装更新条件
				Field f = cacheMap.get(key);
				if (f != null) {
					String fName = f.getName();
					for (String fieldName : conditionFields) {
						if (fieldName.equals(fName)) {
							whereBuilder.append(key).append("=? and ");
							whereArgs[argsIndex] = value;
							argsIndex++;
							isCondition = true;
							break;
						}
					}
				}
				//保存待更新的值
				if (!isCondition) {
					values.put(key, value);
				}
			}
		}
		int whereLength = whereBuilder.length();
		if (whereLength > 3) {
			whereBuilder.delete(whereBuilder.length() - 4, whereLength);
		}
	}

	//拼装ContentValues对象
	private ContentValues getContentValues(Map<String, String> map) {
		ContentValues values = new ContentValues();
		Set<String> keys = map.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = map.get(key);
			if (value != null) {
				values.put(key, value);
			}
		}
		return values;
	}
	//获取到字段名和值的映射
	private Map<String, String> getValues(T entity) {
		HashMap<String, String> map = new HashMap<>();
		Iterator<Field> fieldIterator = cacheMap.values().iterator();
		while (fieldIterator.hasNext()) {
			Field f = fieldIterator.next();
			f.setAccessible(true);
			try {
				Object object = f.get(entity);
				if (object == null) {
					continue;
				}
				String value = object.toString();
				//获取列名
				String key = f.getAnnotation(DbField.class).value();
				if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
					map.put(key, value);
				}

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	private void initCacheMap() {
		//1.获取所有列名---查空表
		String sql = "select * from " + tableName + " limit 1,0;";
		Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
		String[] columnNames = cursor.getColumnNames();
		//2.获取所有成员变量
		Field[] columnFields = entityClass.getDeclaredFields();
		//3.做映射
		for (String columnName : columnNames) {
			Field result = null;
			for (Field field : columnFields) {
				String fieldAnnotationName = field.getAnnotation(DbField.class).value();
				if (columnName.equals(fieldAnnotationName)) {
					result = field;
					break;
				}
 			}
 			if (result != null) {
				cacheMap.put(columnName, result);
			}
		}
	}
}
