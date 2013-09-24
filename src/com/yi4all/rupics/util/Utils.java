package com.yi4all.rupics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.devspark.appmsg.AppMsg;

import android.R.integer;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path.Direction;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utils {
	
	public final static String LOGTAG = "Utils";
	private static String imgDir = null;
	
	public static void toastMsg(final Activity context, final int resId, final String... args) {
		context.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				final String msg = context.getString(resId, args);
				AppMsg.makeText(context, msg, AppMsg.STYLE_INFO).show();
				
			}
		});
		
	}
	
	public static void savePref(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getOnlyWifi(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getString(key, null);

	}
	
	public static Bitmap dealImg(String path, boolean isOriginal, int SCREEN_WIDTH, int SCREEN_HEIGHT, Bitmap no_image) {
		if(path == null) return no_image;
		
		File f = new File(path);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			// System.out.print("-----zoom:" + scale);
			Bitmap bitmap = BitmapFactory.decodeStream(fis,
					null, null);

			return bitmap;
		} catch (OutOfMemoryError e) {
			Log.e(LOGTAG, "Out of memory error, path:" + path);
			
			try {
				// Decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(fis, null, o);

				// Find the correct scale value. It should be the power of 2.
				int scale = 1;

				scale = calculateInSampleSize(o, SCREEN_WIDTH, SCREEN_HEIGHT);

				// Decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				o2.inPurgeable = true;
				o2.inScaled = true;
				// System.out.print("-----zoom:" + scale);
				fis = new FileInputStream(new File(path));
				Bitmap bitmap = BitmapFactory.decodeStream(fis, null, o2);

				return bitmap;
			} catch (OutOfMemoryError oe) {
				Log.e(LOGTAG, "Out of memory error2, path:" + path);
				return no_image;
			} catch (Exception ee) {
				Log.e(LOGTAG, "get image exception2:" + e);
				return no_image;
			}finally{
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e1) {
					}
					fis = null;
				}
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "get image exception:" + e);
			return no_image;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e1) {
				}
				fis = null;
			}
		}
	}
	
	public static Bitmap dealImg(String path, boolean isOriginal, int SCREEN_WIDTH, int SCREEN_HEIGHT, int orientation, int gridNumColumns, Bitmap no_image) {
		if(path == null) return no_image;
		
		int width_tmp = 0, height_tmp = 0;
		File f = new File(path);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(fis, null, o);

			// Find the correct scale value. It should be the power of 2.
			width_tmp = o.outWidth;
			height_tmp = o.outHeight;
			int scale = 1;

			if (isOriginal) {
				while (true) {

					if (width_tmp < SCREEN_WIDTH * 3
							&& height_tmp < SCREEN_HEIGHT * 3) {
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
				scale = calculateInSampleSize(o, SCREEN_WIDTH, SCREEN_HEIGHT);
			} else {
				if (orientation == 0) {
					scale = width_tmp * gridNumColumns / SCREEN_WIDTH;
				} else {
					scale = width_tmp * gridNumColumns / SCREEN_HEIGHT;
				}
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inPurgeable = true;
			o2.inScaled = true;
			// System.out.print("-----zoom:" + scale);
			fis = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(fis, null, o2);

			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
				}
				fis = null;
			}

			return bitmap;
		} catch (OutOfMemoryError oe) {
			Log.e(LOGTAG, "Out of memory error2, path:" + path);
			return no_image;
		} catch (Exception ee) {
			Log.e(LOGTAG, "get image exception2:" + ee);
			return no_image;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
				}
				fis = null;
			}
		}
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}
	
	public static String convertUrl2Path(Context context, String url){
		String path = hashKeyForDisk(url);
		path = getImgDir(context) + File.separator + path;
		return path;
	}
	
	public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
	
	private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
	
	public static String getImgDir(Context context){
		if(imgDir == null){
			File dir = getDiskCacheDir(context, "img");
			dir.mkdirs();
			imgDir = dir.getAbsolutePath();
		}
		return imgDir;
	}

	public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
	
	@TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }
	
	@TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
	
	public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
    public static String getKBSize(long size){
    	if(size < 1024){
    		return size + "KB";
    	}else if(size >= 1024 && size < 1024*1024){
    		return size/1024 + "MB";
    	}else{
    		return size/(1024*1024) + "GB";
    	}
    }
}
