package util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2018/5/24.
 *
 * @author 郑少鹏
 * @desc 异常捕获
 */
public class CrashManager implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashManager";
    /**
     * CrashHandler实例
     */
    private static CrashManager instance;
    /**
     * 应用Context对象
     */
    private Application application;
    /**
     * 系统默UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * 存储设备信息和异常信息
     */
    private Map<String, String> infos = new HashMap<>();
    /**
     * 格式化日期（日志文件名一部分）
     */
    @SuppressLint("SimpleDateFormat")
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 仅一CrashHandler实例
     */
    private CrashManager(Context context) {
        application = (Application) context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * CrashHandler实例（单模）
     */
    public static CrashManager getInstance(Context context) {
        CrashManager inst = instance;
        if (inst == null) {
            synchronized (CrashManager.class) {
                inst = instance;
                if (inst == null) {
                    inst = new CrashManager(context.getApplicationContext());
                    instance = inst;
                }
            }
        }
        return inst;
    }

    /**
     * UncaughtException时转该函数处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtils.e(TAG, thread.getName(), ex);
        if (!handleException(ex) && mDefaultHandler != null) {
            // 程序没处理则系统默异常处理器处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            SystemClock.sleep(2000);
            // 重启
            Intent intent = application.getPackageManager().getLaunchIntentForPackage(application.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
            // 关已崩进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 自定错误处理、收集错误信息、发送错误报告等均在此完成
     *
     * @param ex 异常
     * @return true处理异常，不再上抛；false不处理异常，将信息存储并交上层（系统异常处理）处理
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(application);
        // 存错误信息到文件
        saveCrashInfoToFile(ex);
        return true;
    }

    /**
     * 收设备参数信息
     *
     * @param ctx 上下文
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.eT(TAG, "an error occurred when collecting package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                LogManager.eT(TAG, "an error occurred when collecting crash info", e);
            }
        }
    }

    /**
     * 存错误信息到文件
     *
     * @param ex 异常
     * @return 文件名 便将文件传服务器
     */
    private String saveCrashInfoToFile(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = dateFormat.format(new Date());
            String fileName = "dfs" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = "/sdcard/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                LogUtils.eTag("saveCrashInfoToFile", sb.toString());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            LogManager.eT(TAG, "An error occurred while writing to file.", e);
        }
        return null;
    }
}
