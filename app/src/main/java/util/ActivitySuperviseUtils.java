package util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 2017/9/19.
 *
 * @author xxx
 * @desc 用于Activity管理和应用程序退出。
 * BaseActivity之OnCreate()方法中把当前Activity推入Activity管理容器，需要时遍历容器，把所有Activity都finish掉。
 */
public class ActivitySuperviseUtils {
    private static final List<Activity> ACTIVITIES = Collections.synchronizedList(new LinkedList<Activity>());

    /**
     * 添Activity到堆栈
     */
    public static void pushActivity(Activity activity) {
        ACTIVITIES.add(activity);
        LogManager.e("活动数量", String.valueOf(ACTIVITIES.size()));
        for (int i = 0; i < ACTIVITIES.size(); i++) {
            LogManager.e("概览", ACTIVITIES.get(i).getClass().getSimpleName());
        }
        LogManager.e("推入", ACTIVITIES.get(ACTIVITIES.size() - 1).getClass().getSimpleName());
    }

    /**
     * 获栈顶Activity（堆栈中最后压入）
     */
    public static Activity getCurrentActivity() {
        if (ACTIVITIES == null || ACTIVITIES.isEmpty()) {
            return null;
        }
        return ACTIVITIES.get(ACTIVITIES.size() - 1);
    }

    /**
     * 查找指定类名Activity
     *
     * @param cls cls
     * @return activity
     */
    public static Activity findActivity(Class<?> cls) {
        Activity targetActivity = null;
        if (ACTIVITIES != null) {
            for (Activity activity : ACTIVITIES) {
                if (activity.getClass().equals(cls)) {
                    targetActivity = activity;
                    break;
                }
            }
        }
        return targetActivity;
    }

    /**
     * 结束栈顶Activity（堆栈中最后压入）
     */
    public static void finishCurrentActivity() {
        if (ACTIVITIES == null || ACTIVITIES.isEmpty()) {
            return;
        }
        Activity activity = ACTIVITIES.get(ACTIVITIES.size() - 1);
        finishActivity(activity);
    }

    /**
     * 结束指定Activity
     */
    public static void finishActivity(Activity activity) {
        if (ACTIVITIES == null || ACTIVITIES.isEmpty()) {
            return;
        }
        if (activity != null) {
            ACTIVITIES.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名Activity
     */
    public static void finishActivity(Class<?> cls) {
        if (ACTIVITIES == null || ACTIVITIES.isEmpty()) {
            return;
        }
        for (Activity activity : ACTIVITIES) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    private static void finishAllActivity() {
        if (ACTIVITIES == null) {
            return;
        }
        for (Activity activity : ACTIVITIES) {
            activity.finish();
        }
        ACTIVITIES.clear();
    }

    /**
     * 退应用
     */
    public static void appExit() {
        try {
            finishAllActivity();
        } catch (Exception e) {
        }
    }

    /**
     * 获栈顶Activity实例
     *
     * @return activity
     */
    public Activity getTopActivityInstance() {
        Activity mBaseActivity = null;
        synchronized (ACTIVITIES) {
            final int size = ACTIVITIES.size() - 1;
            if (size < 0) {
                return null;
            }
            mBaseActivity = ACTIVITIES.get(size);
        }
        return mBaseActivity;
    }

    /**
     * 获栈顶Activity名字
     *
     * @return topActivityName
     */
    public String getTopActivityName() {
        Activity mBaseActivity = null;
        synchronized (ACTIVITIES) {
            final int size = ACTIVITIES.size() - 1;
            if (size < 0) {
                return null;
            }
            mBaseActivity = ACTIVITIES.get(size);
        }
        return mBaseActivity.getClass().getName();
    }

    /**
     * 获当前Activity名
     * info.topActivity.getShortClassName() Activity名
     * info.topActivity.getClassName() 类名
     * info.topActivity.getPackageName() 包名
     * info.topActivity.getClass() 类实例
     *
     * @return 当前Activity名
     */
    public static String getCurrentRunningActivityName(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo info = manager != null ? manager.getRunningTasks(1).get(0) : null;
        String activityName = info != null ? info.topActivity.getShortClassName() : null;
        LogManager.e("当前活动", activityName);
        return activityName;
    }
}
