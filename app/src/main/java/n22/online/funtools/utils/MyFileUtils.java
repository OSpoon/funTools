package n22.online.funtools.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 文件管理类
 * <p>
 *
 * @author gdk
 * @date 2020/03/19
 */
public class MyFileUtils {

    /**
     * @param context
     * @return
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                cachePath = context.getExternalFilesDir(null).getPath();
            } else {
                cachePath = context.getFilesDir().getPath();
            }
        } catch (Exception e) {
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return cachePath;
    }


}