package base;

import android.Manifest;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.self.zsp.reportview.R;

import util.ActivitySuperviseUtils;
import util.KeyboardUtils;
import util.RxPermissions;
import util.ToastUtils;

/**
 * Created on 2017/7/19.
 *
 * @author xxx
 * @desc 基类好处
 * 方便代码编写，减重复代码，快开发；
 * 优化代码结构，降耦合度，方便修改；
 * 提代码可读性，显井井有条、优美。
 * <p>
 * BaseActivity抽象类。
 * initContentView()、stepUI()、initConfiguration()、initData()、startLogic()和initListener()六抽象方法，子类须实现。
 * initLocalConfiguration()、applyPermissions()本地方法，子类不需实现。
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 点击间隔
     */
    private static final int SPOT_INTERVAL = 2000;
    /**
     * 头点回退键时
     */
    private static long FirstClickTime;
    /**
     * 点回退键提示
     */
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载视图
        initContentView(savedInstanceState);
        // 初始化控件
        stepUI();
        // 初始化配置
        initConfiguration();
        // 初始化数据
        initData();
        // 逻辑操作
        startLogic();
        // 添监听事件
        setListener();
        // 动获权限
        applyPermissions();
    }

    /**
     * 加载视图
     *
     * @param savedInstanceState 状态保存
     */
    protected abstract void initContentView(Bundle savedInstanceState);

    /**
     * 初始化控件
     */
    protected abstract void stepUI();

    /**
     * 初始化配置
     */
    protected abstract void initConfiguration();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    /**
     * 逻辑操作
     */
    protected abstract void startLogic();

    /**
     * 添监听事件
     */
    protected abstract void setListener();

    /**
     * 动获权限
     */
    private void applyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RxPermissions.
                    with(this).
                    addPermission(Manifest.permission.READ_EXTERNAL_STORAGE).
                    addPermission(Manifest.permission.READ_PHONE_STATE).
                    initPermission();
        }
    }

    /**
     * 携Bundle跳页
     *
     * @param targetActivityClass 目标活动
     * @param bundle              bundle
     */
    public void jumpWithBundle(Class<?> targetActivityClass, Bundle bundle) {
        Intent intent = new Intent(this, targetActivityClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 不携Bundle跳页
     *
     * @param targetActivityClass 目标活动
     */
    public void jumpNoBundle(Class<?> targetActivityClass) {
        jumpWithBundle(targetActivityClass, null);
    }

    /**
     * 携Bundle跳并关当前页
     *
     * @param targetActivityClass 目标活动
     * @param bundle              bundle
     */
    public void jumpWithBundleAndFinish(Class<?> targetActivityClass, Bundle bundle) {
        jumpWithBundle(targetActivityClass, bundle);
        this.finish();
    }

    /**
     * 不携Bundle跳并关当前页
     *
     * @param targetActivityClass 目标活动
     */
    public void jumpNoBundleAndFinish(Class<?> targetActivityClass) {
        jumpNoBundle(targetActivityClass);
        this.finish();
    }

    /**
     * 短吐司
     *
     * @param text 文本
     */
    public void toastShort(String text) {
        ToastUtils.shortShow(text);
    }

    /**
     * 长吐司
     *
     * @param text 文本
     */
    public void toastLong(String text) {
        ToastUtils.longShow(text);
    }

    /**
     * 清EditText焦点
     *
     * @param v   焦点所在View
     * @param ids 输入框
     */
    public void clearViewFocus(View v, int... ids) {
        if (null != v && null != ids && ids.length > 0) {
            for (int id : ids) {
                if (v.getId() == id) {
                    v.clearFocus();
                    break;
                }
            }
        }
    }

    /**
     * 隐键盘
     *
     * @param v   焦点所在View
     * @param ids 输入框
     * @return true表焦点在EditText
     */
    public boolean isFocusEditText(View v, int... ids) {
        if (v instanceof EditText) {
            EditText editText = (EditText) v;
            for (int id : ids) {
                if (editText.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isTouchView(filterViewByIds(), ev)) {
                return super.dispatchTouchEvent(ev);
            }
            if (hideSoftByEditViewIds() == null || hideSoftByEditViewIds().length == 0) {
                return super.dispatchTouchEvent(ev);
            }
            View v = getCurrentFocus();
            if (isFocusEditText(v, hideSoftByEditViewIds())) {
                if (isTouchView(hideSoftByEditViewIds(), ev)) {
                    return super.dispatchTouchEvent(ev);
                }
                // 隐键盘
                KeyboardUtils.hideKeyboard(this);
                clearViewFocus(v, hideSoftByEditViewIds());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 传EditText的Id
     * 没传入EditText不处理
     *
     * @return id数组
     */
    public int[] hideSoftByEditViewIds() {
        return null;
    }

    /**
     * 传需过滤View
     * 过滤后点无隐软键盘操作
     *
     * @return id数组
     */
    public View[] filterViewByIds() {
        return null;
    }

    /**
     * 双退
     *
     * @param keyCode 键码值
     * @param event   按键事件
     * @return boolean
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                switch (ActivitySuperviseUtils.getCurrentRunningActivityName(this)) {
                    /*
                      主页
                     */
                    case ".MainActivity":
                        // 系统当前时
                        long mainSecondClickTime = System.currentTimeMillis();
                        // 两秒内连点回退键否，是退否不退
                        if (mainSecondClickTime - FirstClickTime > SPOT_INTERVAL) {
                            toast = Toast.makeText(this, R.string.exitAppHint, Toast.LENGTH_SHORT);
                            toast.show();
                            // 赋系统当前时给exitTime
                            FirstClickTime = mainSecondClickTime;
                            return true;
                        } else {
                            // 退出程序
                            ActivitySuperviseUtils.appExit();
                            toast.cancel();
                        }
                        break;
                    /*
                      登录页
                     */
                    case ".LoginActivity":
                        // 系统当前时
                        long loginSecondClickTime = System.currentTimeMillis();
                        // 两秒内连点回退键否，是退否不退
                        if (loginSecondClickTime - FirstClickTime > SPOT_INTERVAL) {
                            toast = Toast.makeText(this, R.string.exitAppHint, Toast.LENGTH_SHORT);
                            toast.show();
                            // 赋系统当前时给exitTime
                            FirstClickTime = loginSecondClickTime;
                            return true;
                        } else {
                            // 退出程序
                            ActivitySuperviseUtils.appExit();
                            toast.cancel();
                        }
                        break;
                    /*
                      闪屏页
                      点击事件消耗，不向下分发，点击无反应
                      该分支无break，详看break、continue、return区别
                     */
                    case ".subject.SplashActivity":
                        return true;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 是否触摸指定View（过滤控件）
     *
     * @param views 视图
     * @param ev    手势事件
     * @return boolean
     */
    public boolean isTouchView(View[] views, MotionEvent ev) {
        if (views == null || views.length == 0) {
            return false;
        }
        int[] location = new int[2];
        for (View view : views) {
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (ev.getX() > x && ev.getX() < (x + view.getWidth())
                    && ev.getY() > y && ev.getY() < (y + view.getHeight())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否触摸指定View（过滤控件）
     *
     * @param ids 控件数组
     * @param ev  手势事件
     * @return boolean
     */
    public boolean isTouchView(int[] ids, MotionEvent ev) {
        int[] location = new int[2];
        for (int id : ids) {
            View view = findViewById(id);
            if (view == null) {
                continue;
            }
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (ev.getX() > x && ev.getX() < (x + view.getWidth()) && ev.getY() > y && ev.getY() < (y + view.getHeight())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            /*
              所有UI组件不可见 释放资源
              此不可销Activity，因调手机相册或拍照后执行onTrimMemory()存销Activity操作。
             */
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                break;
            default:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                break;
        }
    }
}
