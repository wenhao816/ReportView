package application;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

import com.self.zsp.reportview.BuildConfig;

import util.ActivitySuperviseUtils;
import util.AzCache;
import util.CrashManager;
import util.LogManager;
import util.LogUtils;

/**
 * Created on 2017/8/22.
 *
 * @author xxx
 * @desc 官方文档
 * Base class for those who need to maintain global application state.
 * You can provide your own implementation by specifying its name in your AndroidManifest.xml's <application> tag,
 * which will cause that class to be instantiated for you when the process for your application/package is created.
 * Application类（基础类）用于维护应用程序全局状态。
 * 你可提供自己的实现，在AndroidManifest.xml文件<application>标签指定它的名字，
 * 这将引起你的应用进程被创建时Application类为你被实例化。
 * <p>
 * Android系统在每应用程序运行时创且仅创一Application实例，故Application可当单例（Singleton）模式一类；
 * 对象生命周期整应用程序最长，等同应用程序生命周期；
 * 全局唯一，不同Activity、Service中获实例相同；
 * 数据传递、数据共享、数据缓存等。
 */
public class App extends Application {
    /**
     * 实例
     */
    private static App instance = null;
    /**
     * 缓存
     */
    private AzCache azCache;

    /**
     * 应用程序创调
     * 创和实例化任何应用程序状态变量或共享资源变量，方法内获Application单例。
     */
    @Override
    public void onCreate() {
        LogManager.e("Application", "onCreate");
        super.onCreate();
        // Application本已单例，可如下处理
        instance = this;
        // 初始化配置
        initConfiguration();
    }

    /**
     * 应用程序对象终止调
     * 不定调。当应用程序被内核终止为别应用程序释放资源，将不提醒且不调用应用程序对象onTerminate()而直接终止进程。
     */
    @Override
    public void onTerminate() {
        LogManager.e("Application", "onTerminate");
        super.onTerminate();
    }

    /**
     * 系统资源匮乏调
     * 通在后台进程已结束且前台应用程序仍缺内存时调，重写该方法清空缓存或释放非必要资源。
     */
    @Override
    public void onLowMemory() {
        LogManager.e("Application", "onLowMemory");
        super.onLowMemory();
    }

    /**
     * 运行时决定当前应用程序应减内存开销时（通常进入后台运行）调，含一level参数，用于提供请求的上下文。
     *
     * @param level 级别
     */
    @Override
    public void onTrimMemory(int level) {
        LogManager.e("Application", "onTrimMemory");
        super.onTrimMemory(level);
    }

    /**
     * 与Activity不同，配置改变时应用程序对象不终止和重启。若应用程序用值依赖特定配置，则重写该方法加载这些值或于应用程序级处理配置值改变。
     *
     * @param newConfig 配置
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogManager.e("Application", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 初始化配置
     */
    private void initConfiguration() {
        initLog();
        /*
          全局监听Activity生命周期
         */
        registerActivityListener();
        /*
          未捕获异常处理器
         */
        CrashManager.getInstance(this);
        /*
          缓存
         */
        azCache = AzCache.get(this);
    }

    private void initLog() {
        LogUtils.getConfig()
                // log总开关（输出到控制台和文件 默开）
                .setLogSwitch(BuildConfig.DEBUG)
                // 输出到控制台（默开）
                .setConsoleSwitch(BuildConfig.DEBUG)
                // log全局标签（默空）
                .setGlobalTag(null)
                // 全局标签不空输出log全为该tag（空且传tag空显类名，否显tag）
                // log头信息（默开）
                .setLogHeadSwitch(true)
                // 打印log存文件（默关）
                .setLog2FileSwitch(false)
                // 自定义路径空写入应用目录/cache/log/
                .setDir("")
                // 文件前缀空（默util）写入文件util-MM-dd.txt
                .setFilePrefix("")
                // 日志边框（默开）
                .setBorderSwitch(true)
                // log控制台过滤器和logcat过滤器同理（默Verbose）
                .setConsoleFilter(LogUtils.V)
                // log文件过滤器和logcat过滤器同理（默Verbose）
                .setFileFilter(LogUtils.V)
                // log栈深度（默1）
                .setStackDeep(1);
    }

    /**
     * Activity全局监听
     */
    private void registerActivityListener() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // 将监听到创事件Activity加入集合
                ActivitySuperviseUtils.pushActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // 将监听到销事件Activity移除集合
                ActivitySuperviseUtils.finishActivity(activity);
            }
        });
    }

    public static App getInstance() {
        return instance;
    }

    public AzCache azCache() {
        return azCache;
    }
}
