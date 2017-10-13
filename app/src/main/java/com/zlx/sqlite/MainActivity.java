package com.zlx.sqlite;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zlx.sqlite.bean.User;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

	private static final String TAG = "Sqlite";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}
	@OnClick({R.id.add, R.id.delete, R.id.update, R.id.query})
	public void click(View v) {
		BaseDao<User> userDao = BaseDaoFactory.getInstance().getBaseDao(User.class);
		switch (v.getId()) {
			case R.id.add:
				User u = new User((int)(Math.random() * 100000000000000d / 100000000), "zlx", "a123456");
				long id = userDao.insert(u);
				Log.i(TAG, "insert complete, rowId=" + id);
				break;
			case R.id.delete:
				User delete = new User(101, null, null);
				int deleteNum = userDao.delete(delete);
				Log.i(TAG, "delete complete, delete num=" + deleteNum);
				break;
			case R.id.update:
				User update = new User(101, null, "99999999");
				String[] params = new String[]{"id"};
				int num = userDao.update(update, params);
				Log.i(TAG, "update complete, update num=" + num);
				break;
			case R.id.query:
				User query = new User(null, null, null);
				List<User> userList = userDao.query(query);
				for (User user : userList) {
					Log.i(TAG, user.toString());
				}
				break;
		}
	}
}
