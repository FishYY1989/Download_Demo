package com.yang.download.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yang.download.AppConfig;
import com.yang.download.R;
import com.yang.download.entity.FileInfo;
import com.yang.download.service.DownloadService;

import java.util.List;

/**
 * Created by yuy on 2016/4/15.
 */
public class FileListAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<FileInfo> mFileList = null;

	public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
		this.mContext = mContext;
		this.mFileList = mFileList;
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final FileInfo fileInfo = mFileList.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, parent, false);
			viewHolder.fileName = (TextView) convertView.findViewById(R.id.fileName);
			viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
			viewHolder.startBtn = (Button) convertView.findViewById(R.id.startBtn);
			viewHolder.stopBtn = (Button) convertView.findViewById(R.id.stopBtn);

			viewHolder.fileName.setText(fileInfo.getFileName());
			viewHolder.progressBar.setMax(100);

			viewHolder.startBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//通知Service开始下载
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(AppConfig.ACTION_START);
					intent.putExtra("fileInfo", fileInfo);
					mContext.startService(intent);
				}
			});

			viewHolder.stopBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, DownloadService.class);
					intent.setAction(AppConfig.ACTION_STOP);
					intent.putExtra("fileInfo", fileInfo);
					mContext.startService(intent);
				}
			});

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		//更新进度
		viewHolder.progressBar.setProgress(mFileList.get(position).getFinished());

		return convertView;
	}

	/**
	 * 更新列表项中的进度条
	 */
	public void updateProgress(int id, int progress) {
		FileInfo fileInfo = mFileList.get(id);
		fileInfo.setFinished(progress);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		private TextView fileName;
		private ProgressBar progressBar;
		private Button startBtn;
		private Button stopBtn;
	}
}
