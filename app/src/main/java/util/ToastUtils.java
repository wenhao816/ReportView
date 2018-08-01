package util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.self.zsp.reportview.R;

import application.App;

/**
 * Created on 2017/9/14.
 *
 * @author xxx
 * @decs
 */
public class ToastUtils {
    private static Toast toast;
    private static Toast toast2;

    /**
     * 短吐司
     *
     * @param content 内容
     */
    public static void shortShow(String content) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(App.getInstance(), content, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 长吐司
     *
     * @param content 内容
     */
    public static void longShow(String content) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(App.getInstance(), content, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * 图文
     *
     * @param context       上下文
     * @param hint          提示
     * @param imageResource 图
     */
    public static void showImageText(final Context context, final String hint, final int imageResource) {
        if (toast2 == null) {
            toast2 = new Toast(context);
        }
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.toast, null);
        TextView tv = view.findViewById(R.id.tvToast);
        tv.setText(TextUtils.isEmpty(hint) ? "" : hint);
        ImageView iv = view.findViewById(R.id.ivToast);
        if (imageResource > 0) {
            iv.setVisibility(View.VISIBLE);
            iv.setImageResource(imageResource);
        } else {
            iv.setVisibility(View.GONE);
        }
        toast2.setView(view);
        toast2.setGravity(Gravity.CENTER, 0, 0);
        toast2.setDuration(Toast.LENGTH_SHORT);
        toast2.show();
    }
}
