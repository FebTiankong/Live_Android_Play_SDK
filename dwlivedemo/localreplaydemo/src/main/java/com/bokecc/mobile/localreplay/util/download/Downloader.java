package com.bokecc.mobile.localreplay.util.download;

import android.util.Log;

import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.Exception.ErrorCode;
import com.bokecc.sdk.mobile.live.util.HttpUtil;
import com.bokecc.sdk.mobile.live.util.SSLClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 视频下载
 * 
 * @author CC视频
 *
 */
public class Downloader {

	public final static int WAIT = 100;
	public final static int DOWNLOAD = 200;
	public final static int PAUSE = 300;
	public final static int FINISH = 400;
	public final static int WRONG = 500;
	
	/** 每次从输入流读取字节的数组的大小 **/
	private final int BUFFER_SIZE = 1024 * 30;
	private final int FAIL = 0;

	// 下载起始点
	private long start;
	// 下载结束点
	private long end;
	
	private int status = WAIT;

	private String downloadUrl;

	private File file;

	private DownloadListener downloadListener;

	private Thread currentThread;

	private HttpClient client;

	private int timeOut = 10000; // 单位为ms

	private int reconnectLimit = 0;

	private int reconnectTimes = 0;

	private boolean isInvalid = false;

	private String tag;

	/**
	 * Downloader构造器
	 * @param downloadListener 下载监听
	 * @param file
	 * @param downloadUrl 下载地址
	 * @param tag 标识当前downloader
	 */
	public Downloader(DownloadListener downloadListener, File file, String downloadUrl, String tag) {
		this.downloadListener = downloadListener;
		this.downloadUrl = getEncodeUrl(downloadUrl);

		this.file = file;
		file.getParentFile().mkdirs();

		this.tag = tag;
		initDownloaderStart();
	}

	private String getEncodeUrl(String downloadUrl) {
		StringBuilder sb = new StringBuilder();

		try {
			int firstSlashIndex = downloadUrl.indexOf("/", 9);
			int questionIndex = downloadUrl.indexOf("?");
			sb.append(downloadUrl.substring(0, firstSlashIndex));

				if (questionIndex > 0) {
					sb.append(getPath(downloadUrl.substring(firstSlashIndex, questionIndex)));
					sb.append(downloadUrl.substring(questionIndex, downloadUrl.length()));
				} else {
					sb.append(getPath(downloadUrl.substring(firstSlashIndex, downloadUrl.length())));
				}

			return sb.toString();
		} catch (Exception e) {
			return downloadUrl;
		}
	}

	private String getPath(String path) throws UnsupportedEncodingException {

		String[] paths =path.substring(1).split("/");
		StringBuilder sb = new StringBuilder();
		for (String a: paths) {
			sb.append("/" + URLEncoder.encode(a, "UTF-8"));
		}

		return sb.toString();
	}

	private void initHttpClient() {
		if (client != null) {
			client.getConnectionManager().shutdown();
		}
		client = SSLClient.getHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, HttpUtil.getUserAgent());
		client.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
		client.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");

		// 只设置链接建立超时时间，setSoTimeout 不能设置否则可能会导致下载失败
		HttpConnectionParams.setConnectionTimeout(client.getParams(), timeOut);
	}

	/**
	 * 初始化文件的下载start点
	 */
	private void initDownloaderStart(){
		long fileLength = file.length();
		if(fileLength >= 0l){
			start = fileLength;
		}
	}
	
	/**
	 * 获取当前下载状态
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 设置当前下载状态
	 *
	 * @return
	 */
	public Downloader setStatus(int status) {
		this.status = status;
		return this;
	}
	
	/**
	 * 开始下载
	 * 
	 */
	public void start() {
		if (currentThread == null || !currentThread.isAlive()) {
			currentThread = new Thread(new Runnable() {
				@Override
				public void run() {
					initDownloaderConfig();
					initAndDownload();
				}
			});
			currentThread.start();
		}
	}
	
	private void initAndDownload() {
		try {

			initHttpClient();
			
			if (!isInvalid) {
				startDownload();
			}
		} catch (DWLiveException e) {
			Log.e("Downloader", e.getMessage() + "");
			processException(e.getErrorCode());
		} catch (IOException e) {
			Log.e("Downloader", e.getLocalizedMessage() + "");
			processException(ErrorCode.NETWORK_ERROR);
		} catch (JSONException e) {
			Log.e("Downloader", e.getLocalizedMessage() + "");
			processException(ErrorCode.NETWORK_ERROR);
		} catch (Exception e) {
			Log.e("Downloader", e.getLocalizedMessage() + "");
			processException(ErrorCode.NETWORK_ERROR);
		}
	}

	/**
	 * 暂停下载
	 * 
	 */
	public void pause() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				status = PAUSE;
				isInvalid = true;
				reconnectTimes = reconnectLimit;
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
				
				handleDownloadStatus();
				
			}
		}).start();
	}
	
	private void initDownloaderConfig() {
		isInvalid = false;
		reconnectTimes = 0;
	}

	/**
	 * 取消下载
	 * 
	 */
	public void cancel() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				isInvalid = true;
				reconnectTimes = reconnectLimit;
				
				if (status == FINISH) {
					return;
				}
				
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
				
				start = 0l;
				end = 0l;
				if (downloadListener == null) {
					return;
				}
				
				downloadListener.handleCancel(tag);
			}
		}).start();
		
	}
	
	private void startDownload() throws DWLiveException, IOException, JSONException, Exception {
		
		status = DOWNLOAD;
		handleDownloadStatus();

		setEnd();

		// 如果文件终结点为0，直接返回
		if (end <= 0 || file == null) {
			throw new DWLiveException(ErrorCode.NETWORK_ERROR, "file is null.");
		}
		
		//如果start大于end，文件现在完成，则直接返回
		if (start >= end) {
			status = FINISH;
			handleDownloadStatus();
			start = end;
			return;
		}
		
		if(status != DOWNLOAD){
			return;
		}
		
		RandomAccessFile randomAccessFile = null;
		InputStream inputStream = null;
		
		try {
			
			HttpGet httpGet = new HttpGet(downloadUrl);
			if (start > 0) {
				httpGet.setHeader("Range", (new StringBuilder("bytes=")).append(start).append("-").toString());
			}
			httpGet.setHeader("Accept","image/gif, image/jpeg, image/pjpeg, "
					+ "image/pjpeg, application/x-shockwave-flash, "
					+ "application/xaml+xml, application/vnd.ms-xpsdocument, "
					+ "application/x-ms-xbap, application/x-ms-application, "
					+ "application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			httpGet.setHeader("Accept-Language", "zh-CN");
//			httpGet.setHeader("Referer","https://union.bokecc.com/flash/player.swf");
			httpGet.setHeader("Connection", "Keep-Alive");
			HttpResponse httpResponse = client.execute(httpGet);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if(responseCode >= 400){
				throw new DWLiveException(ErrorCode.NETWORK_ERROR, "http connection fail!");
			}

			// 在文件前加保护壳头，并将指针定位到start处
			randomAccessFile = new RandomAccessFile(file, "rwd");
			randomAccessFile.seek(start);
			inputStream = httpResponse.getEntity().getContent();

			byte[] buffer = new byte[BUFFER_SIZE];
			
			while (status == DOWNLOAD) {
				
				// 当文件未读完，并且下载开关是打开状态
				int bufferCount = 0;
				while(bufferCount < BUFFER_SIZE) {
					int offset = inputStream.read(buffer, bufferCount, BUFFER_SIZE-bufferCount);
					if (offset == -1) {
						break;
					} else {
						bufferCount = bufferCount + offset;
					}
				}

				randomAccessFile.write(buffer, 0, bufferCount);
				// 更新下载起始节点
				start += bufferCount;
				// 读到文件尾时
				if (start >= end) {
					status = FINISH;
					handleDownloadStatus();
					start = end;
				}
				
				if (downloadListener != null) {
					// 刷新进度条
					downloadListener.handleProcess(start, end, tag);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally{
			try {
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
			} catch (IOException e) {
				Log.e("Downloader", e + "");
			}
		}
		
	}

	private void setEnd() throws IOException, DWLiveException, JSONException, Exception {
		HttpGet httpGet = new HttpGet(downloadUrl);
		HttpResponse httpResponse = client.execute(httpGet);
		int responseCode = httpResponse.getStatusLine().getStatusCode();
		if ( responseCode != 200) {
			throw new DWLiveException(ErrorCode.NETWORK_ERROR, "http connection fail.");
		}
		HttpEntity entity = httpResponse.getEntity();
		end = entity.getContentLength();

		if (downloadListener != null) {
			downloadListener.handleVideoLength(end, tag);
		}

		initHttpClient();
	}

	private void processException(ErrorCode errorCode) {
		if (isInvalid) {
			return;
		}
		if (++reconnectTimes <= reconnectLimit && (status == DOWNLOAD || status == WAIT)) {
			initAndDownload();
			return;
		}
		
		status = WRONG;
		if (downloadListener == null) {
			return;
		}
		
		downloadListener.handleException(new DWLiveException(errorCode, "下载失败，ErrorCode: " + errorCode.name()), status);
		handleDownloadStatus();
	}
	
	private void handleDownloadStatus(){
		if (downloadListener == null) {
			return;
		}
		
		downloadListener.handleStatus(tag, status);
	}
	
	/**
	 * 重置downloader，需要再次调用start()方法下载视频。
	 */
	public void reset(){
		start = 0l;
		end = 0l;
		this.status = WAIT;
	}
	
	/**
	 * 设置网络请求的超时时间,区间[5000-30000]，单位为ms，默认值为10000ms
	 * @param timeOut 
	 */
	public void setTimeOut(int timeOut) {
		if (timeOut < 5000 || timeOut >30000) {
			return;
		}
		this.timeOut = timeOut;
	}
	
	/**
	 * 设置下载重试次数，默认为0，可设置区间[0, 100]
	 * @param reconnectLimit
	 */
	public void setReconnectLimit(int reconnectLimit) {
		if (reconnectLimit >=0 && reconnectLimit <=100) {
			this.reconnectLimit = reconnectLimit;
		}
	}

	public long getStart() {
		return start;
	}

	public Downloader setStart(long start) {
		this.start = start;
		return this;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getEnd() {
		return end;
	}
}