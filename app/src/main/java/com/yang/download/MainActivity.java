package com.yang.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
		FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/mukewang.apk", "imooc.apk", 0, 0);
		FileInfo fileInfo1 = new FileInfo(1, "http://www.imooc.com/mobile/mukewang.apk", "imooc1.apk", 0, 0);
		FileInfo fileInfo2 = new FileInfo(2, "http://www.imooc.com/mobile/mukewang.apk", "tencent qq.apk", 0, 0);
		FileInfo fileInfo3 = new FileInfo(3, "http://www.imooc.com/mobile/mukewang.apk", "WeChat.apk", 0, 0);
		mFileList.add(fileInfo);
		mFileList.add(fileInfo1);
		mFileList.add(fileInfo2);
		mFileList.add(fileInfo3);

		mAdapter = new FileListAdapter(MainActivity.this, mFileList);
		fileListView.setAdapter(mAdapter);

		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AppConfig.ACTION_UPDATE);
		intentFilter.addAction(AppConfig.ACTION_FINISH);
		registerReceiver(mReceiver, intentFilter);
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
}
