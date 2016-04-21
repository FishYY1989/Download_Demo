package com.yang.download;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.yang.download.adapter.FileListAdapter;
import com.yang.download.entity.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private ListView fileListView = null;
	private List<FileInfo> mFileList = null;
	private FileListAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fileListView = (ListView) findViewById(R.id.lvFile);

		mFileList = new ArrayList<>();
//		FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/mukewang.apk", "imooc.apk", 0, 0);
//		FileInfo fileInfo1 = new FileInfo(1, "http://www.imooc.com/mobile/mukewang.apk", "imooc1.apk", 0, 0);
//		FileInfo fileInfo2 = new FileInfo(2, "http://www.imooc.com/mobile/mukewang.apk", "tencent qq.apk", 0, 0);
//		FileInfo fileInfo3 = new FileInfo(3, "http://www.imooc.com/mobile/mukewang.apk", "WeChat.apk", 0, 0);
//		mFileList.add(fileInfo);
//		mFileList.add(fileInfo1);
//		mFileList.add(fileInfo2);
//		mFileList.add(fileInfo3);

		// 初始化文件信息对象
		FileInfo fileInfo = null;
		for (int i = 0; i < 13; i++) {
			fileInfo = new FileInfo(i,
					"http://www.imooc.com/mobile/mukewang.apk",
					"imooc" + i + ".apk", 0, 0);
			mFileList.add(fileInfo);
		}

		mAdapter = new FileListAdapter(MainActivity.this, mFileList);
		fileListView.setAdapter(mAdapter);

		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AppConfig.ACTION_UPDATE);
		intentFilter.addAction(AppConfig.ACTION_FINISH);
		registerReceiver(mReceiver, intentFilter);

		verifyStoragePermissions(MainActivity.this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (AppConfig.ACTION_UPDATE.equals(intent.getAction())) {
				//更新进度条
				int id = intent.getIntExtra("id", 0);
				int progress = intent.getIntExtra("finished", 0);
				mAdapter.updateProgress(id, progress);
			} else if (AppConfig.ACTION_FINISH.equals(intent.getAction())) {
				//下载结束
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
				//更新进度条为0
				mAdapter.updateProgress(fileInfo.getId(), 0);
				Toast.makeText(MainActivity.this, fileInfo.getFileName() + "下载完成", Toast.LENGTH_SHORT).show();
			}
		}
	};

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE};

	/**
	 * Checks if the app has permission to write to device storage
	 * <p/>
	 * If the app does not has permission then the user will be prompted to
	 * grant permissions
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}
}
