package com.yang.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yang.download.entity.FileInfo;
import com.yang.download.service.DownloadService;

public class MainActivity extends AppCompatActivity {

	private TextView title = null;
	private Button startBtn = null;
	private Button stopBtn = null;
	private ProgressBar progressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		title = (TextView) findViewById(R.id.fileName);
		startBtn = (Button) findViewById(R.id.startBtn);
		stopBtn = (Button) findViewById(R.id.stopBtn);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);

		final FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/mukewang.apk", "imooc.apk", 0, 0);

		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, DownloadService.class);
				intent.setAction(AppConfig.ACTION_START);
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});

		stopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, DownloadService.class);
				intent.setAction(AppConfig.ACTION_STOP);
				intent.putExtra("fileInfo", fileInfo);
				startService(intent);
			}
		});

		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AppConfig.ACTION_UPDATE);
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
				int progress = intent.getIntExtra("finished", 0);
				progressBar.setProgress(progress);
			}
		}
	};
}
