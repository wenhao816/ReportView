package util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 2017/10/16.
 *
 * @author xxx
 * @desc Error级别错误日志打印工具，带定位功能及输出调用处类信息（类名、方法名、行等），点可追踪。
 */
public class LogManager {
    /**
     * Debug模式
     */
    private static boolean isDebug = true;

    /**
     * 打错误信息
     *
     * @param msg
     */
    public static void e(Object msg) {
        if (!isDebug) {
            return;
        }
        printMsg(getStackTrace(), "", msg.toString());
    }

    /**
     * 打错误信息
     *
     * @param msg
     */
    public static void e(String tag, Object msg) {
        if (!isDebug) {
            return;
        }
        printMsg(getStackTrace(), tag, msg.toString());
    }

    /**
     * 打错误信息
     *
     * @param msg
     */
    public static void file(String fileName, String msg) {
        if (!isDebug) {
            return;
        }
        printMsgToFile(getStackTrace(), fileName, msg);
    }

    /**
     * 打错误信息
     *
     * @param msg
     */
    public static void eT(String tag, String msg, Throwable t) {
        if (!isDebug) {
            return;
        }
        Log.e(tag, msg, t);
    }

    /**
     * 获StackTraceElement对象即当前调LogManager.e()处类信息（类名、方法名、行等）
     *
     * @return
     */
    private static StackTraceElement getStackTrace() {
        return Thread.currentThread().getStackTrace()[4];
    }

    /**
     * 打印e方法日志
     *
     * @param isDebug
     */
    public static void setIsDebug(boolean isDebug) {
        LogManager.isDebug = isDebug;
    }

    /**
     * 打日志
     *
     * @param element 当前调LogManager.e()处类信息（类名、方法名、行等）
     * @param msg
     */
    private static void printMsg(StackTraceElement element, String tag, String msg) {
        // 替()为中文（防冲突）
        msg = msg.replace("(", "（").replace(")", "）");
        StringBuilder sb = new StringBuilder();
        // 暂无需类名
        /*String className = traceElement.getClassName();*/
        String fileName = element.getFileName();
        sb.append(element.getMethodName())
                .append("(").append(fileName).append(":")
                .append(element.getLineNumber())
                .append(")");
        String msgT = (TextUtils.isEmpty(msg) ? "" : (msg + " -> ")) + sb.toString();
        Log.e(tag, msgT);
    }

    /**
     * 打日志到文件
     *
     * @param element  当前调LogManager.e()处类信息（类名、方法名、行等）
     * @param fileName
     * @param msg
     */
    private static void printMsgToFile(StackTraceElement element, String fileName, String msg) {
        StringBuilder sb = new StringBuilder();
        String className = element.getFileName();
        sb.append(element.getMethodName())
                .append(" (").append(className).append(":")
                .append(element.getLineNumber())
                .append(") ");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String content = "-----------------------   " +
                format.format(new Date(System.currentTimeMillis()))
                + "   -----------------------\n" + sb.toString() + "\n" +
                msg + "\n\n\n";
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/ELog/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dir.getPath() + "/" + fileName + ".txt", true);
            fos.write(content.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
