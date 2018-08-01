package widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * Created on 2018/8/1.
 *
 * @author 郑少鹏
 * @desc 分散对齐
 */
public class DistributeTextView extends android.support.v7.widget.AppCompatTextView {
    /**
     * 总行高
     */
    private int mLineY = 0;
    /**
     * TextView总宽
     */
    private int mViewWidth;
    private TextPaint paint;

    public DistributeTextView(Context context) {
        super(context);
        init();
    }

    public DistributeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DistributeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mLineY = 0;
        // TextView实宽
        mViewWidth = getMeasuredWidth();
        mLineY += getTextSize();
        String text = getText().toString();
        Layout layout = getLayout();
        int lineCount = layout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            // 每行循环
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            // TextView每行内容
            String lineText = text.substring(lineStart, lineEnd);
            if (needScale(lineText)) {
                if (i == lineCount - 1) {
                    // 末行无需重绘
                    canvas.drawText(lineText, 0, mLineY, paint);
                } else {
                    float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, paint);
                    drawScaleText(canvas, lineText, width);
                }
            } else {
                canvas.drawText(lineText, 0, mLineY, paint);
            }
            // 写完一行后高增一行高
            mLineY += getLineHeight();
        }
    }

    /**
     * 重绘此行
     *
     * @param canvas    画布
     * @param lineText  该行所有文字
     * @param lineWidth 该行每文字宽总和
     */
    private void drawScaleText(Canvas canvas, String lineText, float lineWidth) {
        float x = 0;
        if (isFirstLineOfParagraph(lineText)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, paint);
            float width = StaticLayout.getDesiredWidth(blanks, paint);
            x += width;
            lineText = lineText.substring(3);
        }
        // 5个字中有4个间隔
        // 整TextView宽 - 5个字宽
        // 减后除以4并填补这4个空隙
        float interval = (mViewWidth - lineWidth) / (lineText.length() - 1);
        for (int i = 0; i < lineText.length(); i++) {
            String character = String.valueOf(lineText.charAt(i));
            float cw = StaticLayout.getDesiredWidth(character, paint);
            canvas.drawText(character, x, mLineY, paint);
            x += (cw + interval);
        }
    }

    /**
     * 段落头行否
     * 一汉字相当一字符（据字符长大3且前两字符为空格判头行否）
     *
     * @param lineText 该行所有文字
     */
    private boolean isFirstLineOfParagraph(String lineText) {
        return lineText.length() > 3 && lineText.charAt(0) == ' ' && lineText.charAt(1) == ' ';
    }

    /**
     * 需缩放否
     *
     * @param lineText 该行所有文字
     * @return true该行末字符非换行符 false该行末字符是换行符
     */
    private boolean needScale(String lineText) {
        if (lineText.length() == 0) {
            return false;
        } else {
            return lineText.charAt(lineText.length() - 1) != '\n';
        }
    }
}
