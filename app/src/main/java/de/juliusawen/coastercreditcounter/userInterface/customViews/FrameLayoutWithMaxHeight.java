package de.juliusawen.coastercreditcounter.userInterface.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class FrameLayoutWithMaxHeight extends FrameLayout
{
    public static int WITHOUT_MAX_HEIGHT_VALUE = -1;

    private int maxHeight = WITHOUT_MAX_HEIGHT_VALUE;

    public FrameLayoutWithMaxHeight(Context context)
    {
        super(context);
    }

    public FrameLayoutWithMaxHeight(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FrameLayoutWithMaxHeight(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        try
        {
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE
                    && heightSize > maxHeight) {
                heightSize = maxHeight;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
            getLayoutParams().height = heightSize;
        }
        catch (Exception e)
        {
            Log.e("onMesure", "Error forcing height", e);
        }
        finally
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }
}