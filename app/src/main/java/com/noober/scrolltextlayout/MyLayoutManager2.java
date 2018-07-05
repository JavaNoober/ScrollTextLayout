package com.noober.scrolltextlayout;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

import com.leochuan.ViewPagerLayoutManager;

/**
 * Created by xiaoqi on 2018/7/3
 */
public class MyLayoutManager2 extends ViewPagerLayoutManager{

    private float itemWidth = -1;
    private float itemHeight = -1;

    public MyLayoutManager2(Context context) {
        this(context, ViewPagerLayoutManager.HORIZONTAL, false);
    }

    public MyLayoutManager2(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        setEnableBringCenterToFront(true);
    }

    public void setItemWidthHeight(float itemWidth, float itemHeight) {
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        requestLayout();
    }

    @Override
    public void setInfinite(boolean enable) {
        if (!enable) {
            int positionOffset = this.mInterval == 0.0F ? 0 : Math.round(this.mOffset / this.mInterval);
            if (positionOffset > getItemCount() || positionOffset < 0) {
                mOffset = 0;
            }
        }
        super.setInfinite(enable);
    }

    /**
     * Item 之间间隔的大小
     * 默认情况下, Item之间是相互叠加显示的, 需要通过此方法, 设置间隔才能显示出线性的效果
     */
    @Override
    protected float setInterval() {
        if (getOrientation() == ViewPagerLayoutManager.VERTICAL) {
            return itemHeight;
        }
        return itemWidth;
    }

    /**
     * 用来控制item属性, 比如各种属性动画, 在滑动的时候出发
     */
    @Override
    protected void setItemViewProperty(View itemView, float targetOffset) {
        //targetOffset 和 itemInterval 密切广西
        //targetOffset 取值范围 -itemInterval/2  0  itemInterval/2
        //L.e("setItemViewProperty() -> " + targetOffset);
    }


}
