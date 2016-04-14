package com.yang.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yang.download.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuy on 2016/4/14.
 */
public class ThreadDaoImpl implements ThreadDao {

	private DBHelper dbHelper = null;

	public ThreadDaoImpl(Context context) {
		dbHelper = new DBHelper(context);
	}

	@Override
	public void insertThread(ThreadInfo threadInfo) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values (?,?,?,?,?)",
				new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
		db.close();
	}

	@Override
	public void deleteThread(String url, int thread_id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("delete from thread_info where url = ? and thread_id = ?",
				new Object[]{url, thread_id});
		db.close();
	}

	@Override
	public void updateThread(String url, int thread_id, int finished) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
				new Object[]{finished, url, thread_id});
		db.close();
	}

	@Override
	public List<ThreadInfo> getThreads(String url) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		List<ThreadInfo> threadList = new ArrayList<>();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
		while (cursor.moveToNext()) {
			ThreadInfo threadInfo = new ThreadInfo();
			threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			threadList.add(threadInfo);
		}
		db.close();
		return threadList;
	}

	@Override
	public boolean isExists(String url, int thread_id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url, String.valueOf(thread_id)});
		boolean isExists = cursor.moveToNext();
		db.close();
		return isExists;
	}
}
