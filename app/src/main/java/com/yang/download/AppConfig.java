package com.yang.download;

import android.os.Environment;

/**
 * Created by yuy on 2016/4/14.
 */
public class AppConfig {

	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
	public static final int MSG_INIT = 0;
}
