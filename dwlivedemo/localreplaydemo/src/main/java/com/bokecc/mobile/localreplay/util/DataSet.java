package com.bokecc.mobile.localreplay.util;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bokecc.mobile.localreplay.manage.DownloadInfo;
import com.bokecc.mobile.localreplay.manage.DownloaderWrapper;

/**
 * 数据库，数据管理类
 */

public class DataSet {

	private static String DOWNLOAD_INFO = "downloadinfo"; //数据库表名

	private static Map<String, DownloaderWrapper> downloadWrapperMap = new LinkedHashMap<>();

	private static SQLiteOpenHelper sqLiteOpenHelper;
	
	public static void init(Context context){

		sqLiteOpenHelper = new SQLiteOpenHelper(context, "demo", null, 1) {
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				String sql = "CREATE TABLE IF NOT EXISTS "+ DOWNLOAD_INFO + "(" +
						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"downloadUrl VERCHAR, " +
						"fileName VERCHAR, " +
						"start INTEGER, " +
						"end INTEGER, " +
						"status INTEGER)";

				db.execSQL(sql);

			}
		};
		reloadData();
	}
	
	private static void reloadData(){

		if (downloadWrapperMap.size() > 0) {
			return;
		}
		
		SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
		Cursor cursor = null; 
		try {
			// 重载下载信息
			synchronized (downloadWrapperMap) {
				cursor = db.rawQuery("SELECT * FROM ".concat("downloadinfo"), null);
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					try {
						DownloadInfo downloadInfo = buildDownloadInfo(cursor);
						DownloaderWrapper wrapper = new DownloaderWrapper();
						wrapper.setDownloadInfo(downloadInfo);
						downloadWrapperMap.put(downloadInfo.getFileName(), wrapper);
					} catch (ParseException e) {
						Log.e("Parse date error", e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			Log.e("cursor error", e.getMessage());
		} finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		db.close();
	}

	private static DownloadInfo buildDownloadInfo(Cursor cursor) throws ParseException{
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.setDownloadUrl(cursor.getString(cursor.getColumnIndex("downloadUrl")));
		downloadInfo.setFileName(cursor.getString(cursor.getColumnIndex("fileName")));
		downloadInfo.setStart(cursor.getLong(cursor.getColumnIndex("start")));
		downloadInfo.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
		downloadInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
		return downloadInfo;
	}

	public static Map<String, DownloaderWrapper> getDownloadWrapperMap() {
		return downloadWrapperMap;
	}
	
	public static void addDownloadInfo(String downloadUrl){
//		synchronized (downloadWrapperMap) {

		String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
		String tempName = fileName;
		int i = 1;

		while (downloadWrapperMap.containsKey(tempName)) {
			StringBuilder sb  = new StringBuilder(fileName);
			sb.insert(sb.indexOf("."),"(" + i + ")");
			tempName = sb.toString();
			i++;
		}

		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.setStatus(DownloadUtil.WAIT);
		downloadInfo.setDownloadUrl(downloadUrl);
		downloadInfo.setFileName(tempName);

		insertInfo2db(downloadInfo);

		DownloaderWrapper wrapper = new DownloaderWrapper();
		wrapper.setDownloadInfo(downloadInfo);
		wrapper.createDownloaderAndUnziper();

		downloadWrapperMap.put(downloadInfo.getFileName(), wrapper);

		checkNewDownload(tempName);
//		}
	}

	public static void removeDownloadInfo(String fileName){
		if (downloadWrapperMap.containsKey(fileName)) {

			removeDownloadInfo2db(fileName);

			downloadWrapperMap.get(fileName).deleteDownload();
			downloadWrapperMap.remove(fileName);

		}
	}

	public static synchronized  void checkNewDownload(String fileName) {

		int downloadCount = 0;

		for (DownloaderWrapper wrapper: downloadWrapperMap.values()) {
			if (wrapper.getStatus() == DownloadUtil.DOWNLOAD) {
				downloadCount++;
			}
		}

		if (fileName != null) {
			if (downloadCount < DownloadConfig.MULTI_TASK_MAX) {
				downloadWrapperMap.get(fileName).setStatus(DownloadUtil.DOWNLOAD);
				downloadWrapperMap.get(fileName).startDownload();
			}
		} else {

			if (downloadCount < DownloadConfig.MULTI_TASK_MAX) {
				for (DownloaderWrapper wrapper : downloadWrapperMap.values()) {
					if (wrapper.getStatus() == DownloadUtil.WAIT) {
						wrapper.setStatus(DownloadUtil.DOWNLOAD);
						wrapper.startDownload();
						break;
					}
				}
			}
		}

		for (Map.Entry<String, DownloaderWrapper> entry: DataSet.getDownloadWrapperMap().entrySet()) {
			entry.getValue().update();
		}

	}

	public static void checkNewUnzip() {
		for (DownloaderWrapper wrapper : downloadWrapperMap.values()) {
			if (wrapper.getStatus() == DownloadUtil.ZIP_WAIT) {
				wrapper.startUnzip();
				break;
			}
		}
	}

	//-----------------------------下面都是对数据库的操作------------------------------------------
	public static void insertInfo2db(DownloadInfo downloadInfo) {
		// 添加进数据库
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("downloadUrl", downloadInfo.getDownloadUrl());
			values.put("fileName", downloadInfo.getFileName());
			values.put("start", downloadInfo.getStart());
			values.put("end", downloadInfo.getEnd());
			values.put("status", downloadInfo.getStatus());
			db.insert(DOWNLOAD_INFO, null, values);
			db.close();
		}
	}

	public static void removeDownloadInfo2db(String fileName) {
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

		if (db.isOpen()) {
			db.delete(DOWNLOAD_INFO, "fileName=?", new String[]{fileName});
			db.close();
		}

	}

	public static void saveData(){
		SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
		db.beginTransaction();

		try {
			//清除当前数据
			db.delete(DOWNLOAD_INFO, null, null);

			for(DownloaderWrapper wrapper : downloadWrapperMap.values()){
				ContentValues values = new ContentValues();
				values.put("downloadUrl", wrapper.getDownloadUrl());
				values.put("fileName", wrapper.getFileName());
				values.put("start", wrapper.getStart());
				values.put("end", wrapper.getEnd());
				values.put("status", wrapper.getStatus());
				db.insert(DOWNLOAD_INFO, null, values);
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e("db error", e.getMessage());
		} finally {
			db.endTransaction();
		}

		db.close();
	}
	
	public static void updateDownloadInfo2db(DownloadInfo downloadInfo){
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("start", downloadInfo.getStart());
			values.put("end", downloadInfo.getEnd());
			values.put("status", downloadInfo.getStatus());
			db.update(DOWNLOAD_INFO, values, "fileName=?", new String[]{downloadInfo.getFileName()});
			db.close();
		}
	}

	public static void updateDownloadInfo2db(DownloadInfo downloadInfo, int status){
		SQLiteDatabase db = sqLiteOpenHelper.getWritableDatabase();

		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put("start", downloadInfo.getStart());
			values.put("end", downloadInfo.getEnd());
			values.put("status", status);
			db.update(DOWNLOAD_INFO, values, "fileName=?", new String[]{downloadInfo.getFileName()});
			db.close();
		}
	}
}