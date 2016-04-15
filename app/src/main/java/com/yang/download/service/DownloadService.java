package com.yang.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yang.download.AppConfig;
import com.yang.download.entity.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yuy on 2016/4/14.
 */
public class DownloadService extends Service {

	private InitThread mInitThread = null;

	//下载任务集合
	private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (AppConfig.ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.d("yang", "start fileInfo-->" + fileInfo.toString());
			//接到下载命令，启动初始化线程
			mInitThread = new InitThread(fileInfo);
//			mInitThread.start();
			DownloadTask.sExecutorService.execute(mInitThread);
		} else if (AppConfig.ACTION_STOP.equals(intent.getAction())) {
			//暂停下载
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			//从集合中取出下载任务
			DownloadTask task = mTasks.get(fileInfo.getId());
			if (task != null) {
				//停止下载任务
				task.isPause = true;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case AppConfig.MSG_INIT:
					//获得初始化结果
					FileInfo fileInfo = (FileInfo) msg.obj;
					Log.d("yang", "AppConfig.MSG_INIT ----fileInfo-->" + fileInfo);
					//启动下载任务
					DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
					task.download();
					//把下载任务添加到集合中
					mTasks.put(fileInfo.getId(), task);
					break;
			}
			return false;
		}
	});

	/**
	 * 初始化子线程
	 */
	class InitThread extends Thread {
		private FileInfo mFileInfo = null;

		public InitThread(FileInfo fileInfo) {
			this.mFileInfo = fileInfo;
		}

		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				//连接网络文件
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int length = -1;
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					//获得文件长度
					length = conn.getContentLength();
				}

				if (length < 0) {
					return;
				}

				//在本地创建文件
				File dir = new File(AppConfig.DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}

				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				//设置文件长度
				raf.setLength(length);
				mFileInfo.setLength(length);
				mHandler.obtainMessage(AppConfig.MSG_INIT, mFileInfo).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				try {
					assert conn != null;
					conn.disconnect();
					assert raf != null;
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
