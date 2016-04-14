package com.yang.download.service;

import android.content.Context;
import android.content.Intent;

import com.yang.download.AppConfig;
import com.yang.download.db.ThreadDao;
import com.yang.download.db.ThreadDaoImpl;
import com.yang.download.entity.FileInfo;
import com.yang.download.entity.ThreadInfo;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by yuy on 2016/4/14.
 */
public class DownloadTask {

	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDao mDao = null;
	private int mFinished = 0;
	public boolean isPause = false;

	public DownloadTask(Context mContext, FileInfo fileInfo) {
		this.mContext = mContext;
		this.mFileInfo = fileInfo;
		mDao = new ThreadDaoImpl(mContext);
	}

	public void download() {
		//读取数据的线程信息
		List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;

		if (threadInfos.isEmpty()) {
			threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}

		new DownloadThread(threadInfo).start();
	}

	/**
	 * 下载线程
	 */
	class DownloadThread extends Thread {
		private ThreadInfo threadInfo = null;

		public DownloadThread(ThreadInfo threadInfo) {
			this.threadInfo = threadInfo;
		}

		@Override
		public void run() {
			//向数据库插入线程信息
			if (!mDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
				mDao.insertThread(threadInfo);
			}
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			try {
				URL url = new URL(threadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				//设置下载位置
				int start = threadInfo.getStart() + threadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());

				//设置文件写入位置
				File file = new File(AppConfig.DOWNLOAD_PATH, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);

				Intent intent = new Intent(AppConfig.ACTION_UPDATE);
				mFinished += threadInfo.getFinished();
				//开始下载
				if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
					//读取数据
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int len = -1;
					long time = System.currentTimeMillis();
					while ((len = input.read(buffer)) != -1) {
						//写入文件
						raf.write(buffer, 0, len);
						//把下载进度发送广播给activity
						mFinished += len;
						if (System.currentTimeMillis() - time > 500) {
							time = System.currentTimeMillis();
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							mContext.sendBroadcast(intent);
						}
						//在下载暂停时，保存下载进度
						if (isPause) {
							mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), mFinished);
						}
					}

					//删除线程信息
					mDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					conn.disconnect();
					input.close();
					raf.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
}
