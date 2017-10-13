package com.zlx.sqlite.bean;

import com.zlx.sqlite.DbField;
import com.zlx.sqlite.DbTable;

/**
 * Created by zhulx on 2017/9/12.
 */
@DbTable("tb_user")
public class User {
	@DbField("_id")
	private Integer id;
	@DbField("tb_name")
	private String name;
	@DbField("tb_password")
	private String password;

	public User() {
	}

	public User(Integer id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
