package com.zlx.sqlite;

import java.util.List;

/**
 * Created by zhulx on 2017/9/12.
 * 框架顶层接口，
 * insert delete update select batchInsert batchUpdate...
 */

public interface IBaseDao<T> {
	/**
	 * 插入对象
	 * @param entity 待插入对象
	 * @return 插入结果的行号，成功返回大于0，否则返回-1
	 */
	long insert(T entity);
	/**
	 * 更新指定数据表内容
	 * @param entity 待更新实体
	 * @param whereClause 指定条件字段名称，为null的话，更新表中所有行
	 * @return 此次更新影响的行数
	 */
	int update(T entity, String[] whereClause);
	/**
	 * 删除指定数据表内容
	 * @param entity 待删除实体
	 * @return 此次删除影响的行数
	 */
	int delete(T entity);

	/**
	 * 查询数据表中内容
	 * @param entity 查询条件
	 * @return 返回符合条件的数据，封装为T对象，放到List中返回
	 */
	List<T> query(T entity);
	/**
	 * 查询数据表中内容
	 * @param entity 查询条件
	 * @param groupBy groupBy sql
	 * @param having having sql
	 * @param orderBy orderBy sql
	 * @param limit limit
	 * @return 返回符合条件的数据，封装为T对象，放到List中返回
	 */
	List<T> query(T entity, String groupBy, String having, String orderBy, String limit);
}
