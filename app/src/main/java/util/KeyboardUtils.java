package util;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created on 2017/8/22.
 *
 * @author xxx
 * @decs Soft keyboard management.
 */
public class KeyboardUtils {
    private KeyboardUtils() {

    }

    /**
     * 显键盘
     */
    private static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 显键盘
     */
    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 显键盘
     *
     * @param context 上下文
     * @param view    视图
     */
    public static void showKeyboardView(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 显键盘
     *
     * @param context  上下文
     * @param editText 输入框
     */
    public static void showKeyboardEditText(final Context context, final EditText editText) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                editText.setSelection(editText.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        }, 300);
    }

    /**
     * 关Activity中显键盘
     *
     * @param activity 活动
     */
    private static void closeKeyboardActivity(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 关Dialog中显键盘
     *
     * @param dialog 弹框
     */
    private static void closeKeyboardDialog(Dialog dialog) {
        View view = dialog.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 关键盘
     *
     * @param editText 输入框
     * @param mContext 上下文
     */
    public static void closeKeybord(EditText editText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 切键盘显隐
     *
     * @param activity 活动
     */
    public static void switchKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 隐键盘
     *
     * @param activity 活动
     */
    public static void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null) {
            return;
        }
        ((InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken()
                , InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 点非EditText区域自关键盘
     *
     * @param isAutoCloseKeyboard 自关键盘否
     * @param currentFocusView    当前获焦控件
     * @param motionEvent         触摸事件
     * @param dialogOrActivity    Dialog或Activity
     */
    public static void autoCloseKeyboard(boolean isAutoCloseKeyboard, View currentFocusView, MotionEvent motionEvent, Object dialogOrActivity) {
        if (isAutoCloseKeyboard &&
                motionEvent.getAction() == MotionEvent.ACTION_DOWN &&
                currentFocusView != null &&
                (currentFocusView instanceof EditText) &&
                dialogOrActivity != null) {
            int[] leftTop = {0, 0};
            currentFocusView.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + currentFocusView.getHeight();
            int right = left + currentFocusView.getWidth();
            if (!(motionEvent.getX() > left && motionEvent.getX() < right && motionEvent.getY() > top && motionEvent.getY() < bottom)) {
                if (dialogOrActivity instanceof Dialog) {
                    closeKeyboardDialog((Dialog) dialogOrActivity);
                } else if (dialogOrActivity instanceof Activity) {
                    closeKeyboardActivity((Activity) dialogOrActivity);
                }
            }
        }
    }

    /**
     * 拷文档到黏贴板
     *
     * @param context 上下文
     * @param text    文本
     */
    public static void copy(Context context, String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(text.trim());
        } else {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("content", text.trim()));
        }
    }

    /**
     * 定显键盘
     */
    public static void showKeyboardTiming(final View view, long delayMillis) {
        if (view == null) {
            return;
        }
        // 显键盘
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showKeyboard(view);
            }
        }, delayMillis);
    }
}
