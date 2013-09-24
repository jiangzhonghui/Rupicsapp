package com.yi4all.rupics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.renderscript.FieldPacker;
import android.util.Log;

public class DownloadUtil {

	protected static final String LOGTAG = "DownloadUtil";
	
	private HttpClient httpclient;
	private ThreadPoolExecutor executor;

	private ArrayBlockingQueue<Runnable> taskQueue;

	public static HashMap<Long, Integer> pageMap = new HashMap<Long, Integer>();// -1:
																				// means
																				// is
																				// favorite
																				// post

	public DownloadUtil() {
		this.taskQueue = new ArrayBlockingQueue<Runnable>(80);
		this.executor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS,
				this.taskQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
		httpclient = createHttpClient();
	}

	public void runSaveUrl( final String path,
			final String imageUrl, final Handler handler) {

		final File tmp = new File(path);
		if (tmp.exists()) {
			Message msg = handler.obtainMessage();
			msg.arg1 = 0;
			handler.sendMessage(msg);
			
			return;
		}
		Runnable saveUrl = new Runnable() {
			

			public void run() {
				
				int imgSize = getImageSize(httpclient, imageUrl);

				HttpGet httpget;

				HttpResponse response;

				try {

					httpget = new HttpGet(imageUrl);

					response = httpclient.execute(httpget);

					int status = response.getStatusLine().getStatusCode();

					if (status == HttpStatus.SC_OK) {
						int nRead;
						byte[] data = new byte[16384];
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						
						InputStream inputStream = response.getEntity().getContent();

						// save to sdcard
						int currentSize = 0;
						while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
							buffer.write(data, 0, nRead);
							currentSize += nRead;

							//
							Message msg = handler.obtainMessage();
							
							msg.arg1 = currentSize;
							
							//bring file size as a param
							msg.arg2 = imgSize;
							
							handler.sendMessage(msg);
						}

						buffer.flush();

						byte[] resByte = buffer.toByteArray();

						IOUtils.write(resByte, new FileOutputStream(new File(path)));

						Message msg = handler.obtainMessage();
						
						msg.arg1 = 0;
						
						//bring file size as a param
						msg.arg2 = imgSize;
						
						handler.sendMessage(msg);
						
						Log.d(LOGTAG,
								"finished to download, thread pool: active threads = "
										+ executor.getActiveCount());
						Log.d(LOGTAG,
								"finished to download, thread pool: Completed Task = "
										+ executor.getCompletedTaskCount());
						Log.d(LOGTAG, "finished to download, thread pool: Queue = " + executor.getQueue().size());
					}

				} catch (OutOfMemoryError e) {
					Log.e(LOGTAG, "Out of memory error :(");
					Message msg = handler.obtainMessage();
					msg.arg1 = -1;
					handler.sendMessage(msg);
				} catch (Exception e) {
					Log.e(LOGTAG, "download image exception :" + e);
					Log.e(LOGTAG, "download image exception imageUrl:" + imageUrl);
					Message msg = handler.obtainMessage();
					msg.arg1 = -2;
					handler.sendMessage(msg);

				} 

			}
		};
		this.executor.execute(saveUrl);
		Log.d(LOGTAG,
				"thread pool: active threads = "
						+ this.executor.getActiveCount());
		Log.d(LOGTAG,
				"thread pool: Completed Task = "
						+ this.executor.getCompletedTaskCount());
		Log.d(LOGTAG, "thread pool: Queue = " + this.executor.getQueue().size());
	}

	private static int save2card(HttpEntity resEntity, String path) {
		try {
			// save to sdcard
			FileOutputStream fos = new FileOutputStream(new File(path));
			resEntity.writeTo(fos);

			// release all instances
			fos.flush();
			fos.close();
			
			File file= new File(path);
			
			return (int) (file.length()/1000);
		} catch (Exception e) {
			Log.e(LOGTAG, "save2card:"
					+ ((e != null) ? e.getMessage() : "exception is null"));
		}
		
		return 0;
	}

	public static String initBaseDir() {
		File sdDir = Environment.getExternalStorageDirectory();
		File uadDir = null;
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir = Environment.getDataDirectory();

		}
		uadDir = new File(sdDir.getAbsolutePath() + "/download/dailybeauty/");
		if (!uadDir.exists()) {
			uadDir.mkdirs();
		}
		sdDir = new File(uadDir.getAbsolutePath() + "/source/");
		if (!sdDir.exists()) {
			sdDir.mkdirs();
		}
		return uadDir.getAbsolutePath();
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	public ArrayBlockingQueue<Runnable> getTaskQueue() {
		return taskQueue;
	}
	
	public static boolean getContentByUrl( HttpClient httpClient, String url, String path) {
		// get content from the url
		HttpEntity resEntity = null;
		
		try {

			HttpGet httpget = new HttpGet(url);
			
			HttpResponse response = httpClient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {
				
				resEntity = response.getEntity();
				
				save2card(resEntity, path);

				return true;
			}

		} catch (Exception e) {
			Log.e(LOGTAG, "getContentByUrl Exception:" + e);
		} finally {

			if (resEntity != null) {
				resEntity = null;
			}
		}
		return false;

	}
	
	private int getImageSize(HttpClient httpClient, String url){
		int size = 0;
		// get content from the url
				
				HttpResponse response = null;

				try {

					HttpHead method = new HttpHead(url);

					response = httpClient.execute(method);

					int status = response.getStatusLine().getStatusCode();

					if (status == HttpStatus.SC_OK) {

						// get reponse content type
						Header header = response.getFirstHeader("Content-Length");
						if(header != null){
						size = Integer.valueOf(header.getValue());
						}else{
							size = -1;
						}

					}

				} catch (Exception e) {
//					e.printStackTrace();
				} finally{
					if( response != null && response.getEntity() != null ) {
						response.setEntity(null);
					}
					
				}
		return size;
	}
	
	private static HttpClient createHttpClient() {
		Log.d(LOGTAG, "createHttpClient()...");
		HttpParams params = new BasicHttpParams();
//		params.setParameter("http.socket.timeout", new Integer(1000));
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
//		HttpClientParams.setRedirecting(params, true);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);
		return new DefaultHttpClient(conMgr, params);
	}

}
