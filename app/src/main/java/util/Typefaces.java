package util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * @decs: 样式
 * @author: 郑少鹏
 * @date: 2018/5/28 20:52
 * @version: v 1.0
 */
public class Typefaces {
    private static final String TAG = Typefaces.class.getSimpleName();
    private static final Hashtable<String, Typeface> CACHE = new Hashtable<>();

    private Typefaces() {
        // no instances
    }

    private static Typeface get(Context context, String assetPath) {
        synchronized (CACHE) {
            if (!CACHE.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(), assetPath);
                    CACHE.put(assetPath, t);
                } catch (Exception e) {
                    LogUtils.e(TAG, "Could not get typeface '" + assetPath + "' Error: " + e.getMessage());
                    return null;
                }
            }
            return CACHE.get(assetPath);
        }
    }

    public static Typeface getRobotoMedium(Context context) {
        return get(context, "fonts/Roboto-Medium.ttf");
    }
}

