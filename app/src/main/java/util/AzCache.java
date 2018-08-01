package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import value.Magic;

/**
 * Created on 2018/4/17.
 *
 * @author 郑少鹏
 * @desc 数据缓存工具类
 * 支持jsonObject、bitmap、object、String、drawable、byte、jsonArray
 * 自定缓时
 */
public class AzCache {
    private static final String STRING = "String";
    private static final int TIME_HOUR = 60 * 60;
    private static final int TIME_DAY = TIME_HOUR * 24;
    /**
     * 50mb
     */
    private static final int MAX_SIZE = 1000 * 1000 * 50;
    /**
     * 不限存数据数量
     */
    private static final int MAX_COUNT = Integer.MAX_VALUE;
    private static final String BINARY = "BINARY";
    private static final String BITMAP = "BITMAP";
    private static final String DRAWABLE = "DRAWABLE";
    private static final String OBJECT = "OBJECT";
    private static final String JSON_ARRAY = "JSON_ARRAY";
    private static final String JSON_OBJECT = "JSON_OBJECT";
    private static Map<String, AzCache> mInstanceMap = new HashMap<>();
    private AzCacheManager mCache;

    private AzCache(File cacheDir, long maxSize, int maxCount) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can't make dirs in " + cacheDir.getAbsolutePath());
        }
        mCache = new AzCacheManager(cacheDir, maxSize, maxCount);
    }

    public static AzCache get(Context ctx) {
        return get(ctx, "AzCache");
    }

    public static AzCache get(Context ctx, String cacheName) {
        File f = new File(ctx.getCacheDir(), cacheName);
        return get(f, MAX_SIZE, MAX_COUNT);
    }

    public static AzCache get(File cacheDir) {
        return get(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    public static AzCache get(Context ctx, long maxSize, int maxCount) {
        File f = new File(ctx.getCacheDir(), "AzCache");
        return get(f, maxSize, maxCount);
    }

    public static AzCache get(File cacheDir, long maxSize, int maxCount) {
        AzCache manager = mInstanceMap.get(cacheDir.getAbsoluteFile() + myPid());
        if (manager == null) {
            manager = new AzCache(cacheDir, maxSize, maxCount);
            mInstanceMap.put(cacheDir.getAbsolutePath() + myPid(), manager);
        }
        return manager;
    }

    private static String myPid() {
        return "_" + android.os.Process.myPid();
    }

    /**
     * 本地数据存否
     *
     * @param cacheKey 缓存key
     * @param dataType 数据类型
     * @return 存否
     */
    public boolean isExist(String cacheKey, String dataType) {
        if (!TextUtils.isEmpty(dataType)) {
            switch (dataType) {
                case STRING:
                    if (!TextUtils.isEmpty(getAsString(cacheKey))) {
                        return true;
                    }
                    break;
                case BINARY:
                    if (getAsBinary(cacheKey) != null) {
                        return true;
                    }
                    break;
                case BITMAP:
                    if (getAsBitmap(cacheKey) != null) {
                        return true;
                    }
                    break;
                case DRAWABLE:
                    if (getAsDrawable(cacheKey) != null) {
                        return true;
                    }
                    break;
                case OBJECT:
                    if (getAsObject(cacheKey) != null) {
                        return true;
                    }
                    break;
                case JSON_ARRAY:
                    if (getAsJSONArray(cacheKey) != null) {
                        return true;
                    }
                    break;
                case JSON_OBJECT:
                    if (getAsJSONObject(cacheKey) != null) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * 读String
     *
     * @param key key
     * @return String
     */
    public String getAsString(String key) {
        File file = mCache.get(key);
        if (!file.exists()) {
            return null;
        }
        boolean removeFile = false;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            StringBuilder readString = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                readString.append(currentLine);
            }
            if (!Utils.isDue(readString.toString())) {
                return Utils.clearDateInfo(readString.toString());
            } else {
                removeFile = true;
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (removeFile) {
                remove(key);
            }
        }
    }
    // =======================================
    // ============ String读写 ==============
    // =======================================

    /**
     * 存String到缓存
     *
     * @param key   存key
     * @param value 存String
     */
    public void put(String key, String value) {
        File file = mCache.newFile(key);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file), 1024);
            out.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCache.put(file);
        }
    }

    /**
     * 存String到缓存
     *
     * @param key      存key
     * @param value    存String
     * @param saveTime 存时间 单位秒
     */
    private void put(String key, String value, int saveTime) {
        put(key, Utils.newStringWithDateInfo(saveTime, value));
    }

    /**
     * 读JSONObject
     *
     * @param key key
     * @return JSONObject
     */
    private JSONObject getAsJSONObject(String key) {
        String jsonString = getAsString(key);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return new JSONObject(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    // =======================================
    // ============= JSONObject读写 ==============
    // =======================================

    /**
     * 存JSONObject到缓存
     *
     * @param key   存key
     * @param value 存json
     */
    public void put(String key, JSONObject value) {
        put(key, value.toString());
    }

    /**
     * 存JSONObject到缓存
     *
     * @param key      存key
     * @param value    存JSONObject
     * @param saveTime 存时间 单位秒
     */
    public void put(String key, JSONObject value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读JSONArray
     *
     * @param key key
     * @return JSONArray
     */
    private JSONArray getAsJSONArray(String key) {
        String jsonString = getAsString(key);
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // =======================================
    // ============ JSONArray读写 =============
    // =======================================

    /**
     * 存JSONArray到缓存
     *
     * @param key   存key
     * @param value 存JSONArray
     */
    public void put(String key, JSONArray value) {
        put(key, value.toString());
    }

    /**
     * 存JSONArray到缓存
     *
     * @param key      存key
     * @param value    存JSONArray
     * @param saveTime 存时间 单位秒
     */
    public void put(String key, JSONArray value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读byte
     *
     * @param key key
     * @return byte
     */
    private byte[] getAsBinary(String key) {
        RandomAccessFile raFile = null;
        boolean removeFile = false;
        try {
            File file = mCache.get(key);
            if (!file.exists()) {
                return null;
            }
            raFile = new RandomAccessFile(file, "r");
            byte[] byteArray = new byte[(int) raFile.length()];
            raFile.read(byteArray);
            if (!Utils.isDue(byteArray)) {
                return Utils.clearDateInfo(byteArray);
            } else {
                removeFile = true;
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (removeFile) {
                remove(key);
            }
        }
    }
    // =======================================
    // ============== byte读写 =============
    // =======================================

    /**
     * 存byte到缓存
     *
     * @param key   存key
     * @param value 存数据
     */
    private void put(String key, byte[] value) {
        File file = mCache.newFile(key);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCache.put(file);
        }
    }

    /**
     * Cache for a stream
     *
     * @param key the file name.
     * @return OutputStream stream for writing data.
     * @throws FileNotFoundException if the file can not be created.
     */
    public OutputStream put(String key) throws FileNotFoundException {
        return new ZsFileOutputStream(mCache.newFile(key));
    }

    /**
     * @param key the file name.
     * @return (InputStream or null) stream previously saved in cache.
     * @throws FileNotFoundException if the file can not be opened
     */
    public InputStream get(String key) throws FileNotFoundException {
        File file = mCache.get(key);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }

    /**
     * 存byte到缓存
     *
     * @param key      存key
     * @param value    存数据
     * @param saveTime 存时间 单位秒
     */
    private void put(String key, byte[] value, int saveTime) {
        put(key, Utils.newByteArrayWithDateInfo(saveTime, value));
    }

    /**
     * 存Serializable到缓存
     *
     * @param key      存key
     * @param value    存value
     * @param saveTime 存时间 单位秒
     */
    private void put(String key, Serializable value, int saveTime) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            byte[] data = baos.toByteArray();
            if (saveTime != -1) {
                put(key, data, saveTime);
            } else {
                put(key, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                LogUtils.e(e.getMessage());
            }
        }
    }
    // =======================================
    // ============= 序列化读写 ===============
    // =======================================

    /**
     * 存Serializable到缓存
     *
     * @param key   存key
     * @param value 存value
     */
    public void put(String key, Serializable value) {
        put(key, value, TIME_DAY * 90);
    }

    /**
     * 读Serializable
     *
     * @param key key
     * @return Serializable
     */
    public Object getAsObject(String key) {
        byte[] data = getAsBinary(key);
        if (data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                return ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 读bitmap
     *
     * @param key key
     * @return bitmap
     */
    private Bitmap getAsBitmap(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return Utils.bytes2Bitmap(getAsBinary(key));
    }
    // =======================================
    // ============== bitmap读写 =============
    // =======================================

    /**
     * 存bitmap到缓存
     *
     * @param key   存key
     * @param value 存bitmap
     */
    private void put(String key, Bitmap value) {
        put(key, Utils.bitmap2Bytes(value));
    }

    /**
     * 存bitmap到缓存
     *
     * @param key      存key
     * @param value    存bitmap
     * @param saveTime 存时间 单位秒
     */
    private void put(String key, Bitmap value, int saveTime) {
        put(key, Utils.bitmap2Bytes(value), saveTime);
    }

    /**
     * 读drawable
     *
     * @param key key
     * @return drawable
     */
    private Drawable getAsDrawable(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return Utils.bitmap2Drawable(Utils.bytes2Bitmap(getAsBinary(key)));
    }
    // =======================================
    // ============= drawable读写 =============
    // =======================================

    /**
     * 存drawable到缓存
     *
     * @param key   存key
     * @param value 存drawable
     */
    public void put(String key, Drawable value) {
        put(key, Utils.drawable2Bitmap(value));
    }

    /**
     * 存drawable到缓存
     *
     * @param key      存key
     * @param value    存drawable
     * @param saveTime 存时间 单位秒
     */
    public void put(String key, Drawable value, int saveTime) {
        put(key, Utils.drawable2Bitmap(value), saveTime);
    }

    /**
     * 获缓存文件
     *
     * @param key key
     * @return value
     */
    public File file(String key) {
        File f = mCache.newFile(key);
        if (f.exists()) {
            return f;
        }
        return null;
    }

    /**
     * 移某key
     *
     * @param key key
     * @return 移成否
     */
    public boolean remove(String key) {
        return mCache.remove(key);
    }

    /**
     * 清所有数据
     */
    public void clear() {
        mCache.clear();
    }

    /**
     * 算时工具类
     */
    private static class Utils {
        private static final char M_SEPARATOR = ' ';

        /**
         * 缓存String到期否
         *
         * @param str str
         * @return true到期 false没到期
         */
        private static boolean isDue(String str) {
            return isDue(str.getBytes());
        }

        /**
         * 缓存byte到期否
         *
         * @param data date
         * @return true到期 false没到期
         */
        private static boolean isDue(byte[] data) {
            String[] str = getDateInfoFromDate(data);
            if (str != null && str.length == Magic.INT_ER) {
                String saveTimeStr = str[0];
                while (saveTimeStr.startsWith(Magic.STRING_L)) {
                    saveTimeStr = saveTimeStr.substring(1, saveTimeStr.length());
                }
                long saveTime = Long.valueOf(saveTimeStr);
                long deleteAfter = Long.valueOf(str[1]);
                return System.currentTimeMillis() > saveTime + deleteAfter * Magic.LONG_YQ;
            }
            return false;
        }

        private static String newStringWithDateInfo(int second, String strInfo) {
            return createDateInfo(second) + strInfo;
        }

        private static byte[] newByteArrayWithDateInfo(int second, byte[] data2) {
            byte[] data1 = createDateInfo(second).getBytes();
            byte[] retData = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, retData, 0, data1.length);
            System.arraycopy(data2, 0, retData, data1.length, data2.length);
            return retData;
        }

        private static String clearDateInfo(String strInfo) {
            if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
                strInfo = strInfo.substring(strInfo.indexOf(M_SEPARATOR) + 1, strInfo.length());
            }
            return strInfo;
        }

        private static byte[] clearDateInfo(byte[] data) {
            if (hasDateInfo(data)) {
                return copyOfRange(data, indexOf(data, M_SEPARATOR) + 1, data.length);
            }
            return data;
        }

        private static boolean hasDateInfo(byte[] data) {
            return data != null && data.length > 15 && data[13] == '-' && indexOf(data, M_SEPARATOR) > 14;
        }

        private static String[] getDateInfoFromDate(byte[] data) {
            if (hasDateInfo(data)) {
                String saveDate = new String(copyOfRange(data, 0, 13));
                String deleteAfter = new String(copyOfRange(data, 14, indexOf(data, M_SEPARATOR)));
                return new String[]{saveDate, deleteAfter};
            }
            return null;
        }

        private static int indexOf(byte[] data, char c) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == c) {
                    return i;
                }
            }
            return -1;
        }

        private static byte[] copyOfRange(byte[] original, int from, int to) {
            int newLength = to - from;
            if (newLength < 0) {
                throw new IllegalArgumentException(from + " > " + to);
            }
            byte[] copy = new byte[newLength];
            System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
            return copy;
        }

        private static String createDateInfo(int second) {
            String currentTime = System.currentTimeMillis() + "";
            while (currentTime.length() < Magic.INT_SS) {
                currentTime = new StringBuilder().append("0").append(currentTime).toString();
            }
            return currentTime + "-" + second + M_SEPARATOR;
        }

        /**
         * bitmap->byte[]
         *
         * @param bm bitmap
         * @return byte[]
         */
        private static byte[] bitmap2Bytes(Bitmap bm) {
            if (bm == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }

        /**
         * byte[]->bitmap
         *
         * @param b byte[]
         * @return bitmap
         */
        private static Bitmap bytes2Bitmap(byte[] b) {
            if (b.length == 0) {
                return null;
            }
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        /**
         * drawable->bitmap
         *
         * @param drawable drawable
         * @return bitmap
         */
        private static Bitmap drawable2Bitmap(Drawable drawable) {
            if (drawable == null) {
                return null;
            }
            // 取drawable长宽
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            // 取drawable颜色格式
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            // 建对应bitmap
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            // 建对应bitmap画布
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            // 画drawable内容到画布
            drawable.draw(canvas);
            return bitmap;
        }

        /**
         * bitmap->drawable
         *
         * @param bm bitmap
         * @return drawable
         */
        @SuppressWarnings("deprecation")
        private static Drawable bitmap2Drawable(Bitmap bm) {
            if (bm == null) {
                return null;
            }
            BitmapDrawable bd = new BitmapDrawable(bm);
            bd.setTargetDensity(bm.getDensity());
            return new BitmapDrawable(bm);
        }
    }

    /**
     * Provides a means to save a cached file before the data are available.
     * Since writing about the file is complete, and its close method is called,
     * its contents will be registered in the cache. Example of use:
     * <p>
     * AzCache cache = new AzCache(this) try { OutputStream stream =
     * cache.put("myFileName") stream.write("some bytes".getBytes()); // now
     * update cache! stream.close(); } catch(FileNotFoundException e){
     * e.printStackTrace() }
     */
    class ZsFileOutputStream extends FileOutputStream {
        File file;

        ZsFileOutputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            mCache.put(file);
        }
    }

    /**
     * 缓存管理器
     */
    public class AzCacheManager {
        private final AtomicLong cacheSize;
        private final AtomicInteger cacheCount;
        private final long sizeLimit;
        private final int countLimit;
        private final Map<File, Long> lastUsageDates = Collections.synchronizedMap(new HashMap<File, Long>());
        File cacheDir;

        private AzCacheManager(File cacheDir, long sizeLimit, int countLimit) {
            this.cacheDir = cacheDir;
            this.sizeLimit = sizeLimit;
            this.countLimit = countLimit;
            cacheSize = new AtomicLong();
            cacheCount = new AtomicInteger();
            calculateCacheSizeAndCacheCount();
        }

        /**
         * 算cacheSize和cacheCount
         */
        private void calculateCacheSizeAndCacheCount() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int size = 0;
                    int count = 0;
                    File[] cachedFiles = cacheDir.listFiles();
                    if (cachedFiles != null) {
                        for (File cachedFile : cachedFiles) {
                            size += calculateSize(cachedFile);
                            count += 1;
                            lastUsageDates.put(cachedFile, cachedFile.lastModified());
                        }
                        cacheSize.set(size);
                        cacheCount.set(count);
                    }
                }
            }).start();
        }

        private void put(File file) {
            int curCacheCount = cacheCount.get();
            while (curCacheCount + 1 > countLimit) {
                long freedSize = removeNext();
                cacheSize.addAndGet(-freedSize);
                curCacheCount = cacheCount.addAndGet(-1);
            }
            cacheCount.addAndGet(1);
            long valueSize = calculateSize(file);
            long curCacheSize = cacheSize.get();
            while (curCacheSize + valueSize > sizeLimit) {
                long freedSize = removeNext();
                curCacheSize = cacheSize.addAndGet(-freedSize);
            }
            cacheSize.addAndGet(valueSize);
            Long currentTime = System.currentTimeMillis();
            file.setLastModified(currentTime);
            lastUsageDates.put(file, currentTime);
        }

        private File get(String key) {
            File file = newFile(key);
            Long currentTime = System.currentTimeMillis();
            file.setLastModified(currentTime);
            lastUsageDates.put(file, currentTime);
            return file;
        }

        private File newFile(String key) {
            return new File(cacheDir, key.hashCode() + "");
        }

        private boolean remove(String key) {
            File image = get(key);
            return image.delete();
        }

        private void clear() {
            lastUsageDates.clear();
            cacheSize.set(0);
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }

        /**
         * 移旧文件
         *
         * @return long
         */
        private long removeNext() {
            if (lastUsageDates.isEmpty()) {
                return 0;
            }
            Long oldestUsage = null;
            File mostLongUsedFile = null;
            Set<Entry<File, Long>> entries = lastUsageDates.entrySet();
            synchronized (lastUsageDates) {
                for (Entry<File, Long> entry : entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = entry.getKey();
                        oldestUsage = entry.getValue();
                    } else {
                        Long lastValueUsage = entry.getValue();
                        if (lastValueUsage < oldestUsage) {
                            oldestUsage = lastValueUsage;
                            mostLongUsedFile = entry.getKey();
                        }
                    }
                }
            }
            long fileSize = calculateSize(mostLongUsedFile);
            if (mostLongUsedFile.delete()) {
                lastUsageDates.remove(mostLongUsedFile);
            }
            return fileSize;
        }

        private long calculateSize(File file) {
            return file.length();
        }
    }
}
