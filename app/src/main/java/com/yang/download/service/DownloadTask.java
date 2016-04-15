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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yuy on 2016/4/14.
 */
public class DownloadTask {

	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDao mDao = null;
	private int mFinished = 0;
	public boolean isPause = false;
	//线程集合
	private List<DownloadThread> mThreadList = null;
	private int mThreadCount = 1;

	//线程池
	public static ExecutorService sExecutorService = Executors.newCachedThreadPool();

	public DownloadTask(Context mContext, FileInfo fileInfo, int mThreadCount) {
		this.mContext = mContext;
		this.mFileInfo = fileInfo;
		this.mThreadCount = mThreadCount;
		mDao = new ThreadDaoImpl(mContext);
	}

	/**
	 * 下载方法
	 */
	public void download() {
		//读取数据的线程信息
		List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;

		if (threadInfos.isEmpty()) {
			//获得每个线程的下载长度
			int length = mFileInfo.getLength() / mThreadCount;
			for (int i = 0; i < mThreadCount; i++) {
				//创建线程信息
				threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), length * i, length * (i + 1) - 1, 0);
				if (i == mThreadCount - 1) {
					threadInfo.setEnd(mFileInfo.getLength());
				}
				//添加到线程信息集合
				threadInfos.add(threadInfo);
				//插入下载线程信息
				mDao.insertThread(threadInfo);
			}
		}

		mThreadList = new ArrayList<>();
		//启动多个线程进行下载
		for (ThreadInfo info : threadInfos) {
			DownloadThread thread = new DownloadThread(info);
//			thread.start();
			DownloadTask.sExecutorService.execute(thread);
			//添加线程到集合中
			mThreadList.add(thread);
		}
	}

	/**
	 * 判断是否所有线程都执行完毕
	 */
	private synchronized void checkAllThreadsFinished() {
		boolean allFinished = true;

		//遍历线程集合，判断线程是否都执行完毕
		for (DownloadThread thread : mThreadList) {
			if (!thread.isFinishded) {
				allFinished = false;
				break;
			}
		}

		if (allFinished) {
			//删除线程信息
			mDao.deleteThread(mFileInfo.getUrl());
			//发送广播通知Activity下载完成
			Intent intent = new Intent(AppConfig.ACTION_FINISH);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
		}
	}

	/**
	 * 下载线程
	 */
	class DownloadThread extends Thread {
		private ThreadInfo threadInfo = null;
		public boolean isFinishded = false;

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
						//累加整个文件的完成进度
						mFinished += len;
						//累加每个线程的完成进度
						threadInfo.setFinished(threadInfo.getFinished() + len);

						//间隔500毫秒更新一次进度
						if (System.currentTimeMillis() - time > 1000) {
							time = System.currentTimeMillis();
							//把下载进度发送广播给activity
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							intent.putExtra("id", mFileInfo.getId());
							mContext.sendBroadcast(intent);
						}
						//在下载暂停时，保存下载进度
						if (isPause) {
							mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
						}
					}

					//标识线程执行完毕
					isFinishded = true;
					//检查下载任务是否执行完毕
					checkAllThreadsFinished();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					assert conn != null;
					conn.disconnect();
					assert input != null;
					input.close();
					raf.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
}
