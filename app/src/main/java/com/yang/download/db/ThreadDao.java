package com.yang.download.db;

import com.yang.download.entity.ThreadInfo;

import java.util.List;

/**
 * Created by yuy on 2016/4/14.
 */
public interface ThreadDao {
	public void insertThread(ThreadInfo threadInfo);

	public void deleteThread(String url);

	public void updateThread(String url, int thread_id, int finished);

	public List<ThreadInfo> getThreads(String url);

	public boolean isExists(String url, int thread_id);
}
