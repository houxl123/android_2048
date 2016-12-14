package com.example.houxl.my2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by houxl on 16/11/29.
 */
class PieceView extends View {

    private static int[] _colors= {0xffccc0b4,0xffeee4db, 0xffede0c9,0xfff1b17d, 0xfff39568,0xfff47c63,0xfff45f43,
            0xffedce71,0xffedce00, 0xffe12345,0xffcccadb,0xffddc123,0xff123456,0xff654321,0xffabcd12,0xffefc123,0xffac12df};
    private static int[] _textColors= {0xff776e65,0xff776e65, 0xff776e65,0xfff9f6f2, 0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,
            0xfff9f6f2,0xfff9f6f2, 0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,0xfff9f6f2,0xfff9f6f2};
    private int _value = 0;

    public PieceView(Context context) {
        super(context);
    }

    public PieceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getValue() {
        return _value;
    }

    public void setValue(int value)
    {
        _value = value;
        invalidate();;
    }
    @Override
    protected void onDraw(Canvas canvas) {

        Paint p = new Paint();
        int temp = getColor();
        p.setColor(_colors[temp]);
        canvas.drawRect(4, 4, getMeasuredWidth() - 4, getMeasuredHeight() - 4, p);
        if (_value == 0)return;
        p.setColor(_textColors[temp]);
        p.setTextSize(38);
        //p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.LEFT);
        p.setFlags(p.getFlags() | Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        Rect bounds = new Rect();
        String str = _value+"";
        p.getTextBounds(str, 0, str.length(), bounds);
        Paint.FontMetricsInt fontMetrics = p.getFontMetricsInt();
        int baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(str,getMeasuredWidth() / 2 - bounds.width() / 2, baseline, p);
    }

    protected int getColor()
    {
        int tmp = _value;
        int count = 0;
        while (tmp>1) {
            tmp = tmp>>1;
            count++;
        }

        return  count%_colors.length;
    }

}