package com.noober.scrolltextlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

/**
 * Created by xiaoqi on 2018/7/3
 */
public class MyLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    //保存所有的Item的上下左右的偏移量信息
    private SparseArray<RectF> allItemFrames = new SparseArray<>();
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private SparseBooleanArray hasAttachedItems = new SparseBooleanArray();

    private int verticalScrollOffset; //上下滑动的距离
    private float totalHeight;//recyclerview总高度

    private float itemSpace;//文字间隔距离

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //如果没有item，直接返回
        if (getItemCount() <= 0) return;
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout()) {
            return;
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler);

        float offsetY = getPaddingTop();
        verticalScrollOffset = 0;
        totalHeight = 0;
        for(int i = 0; i < getItemCount(); i ++){
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getWidth();
            int height = getDecoratedMeasuredHeight(view);//文字高度

            RectF rect = allItemFrames.get(i);
            if(rect == null){
                rect = new RectF();
            }
            rect.set(getPaddingLeft(), offsetY, width, offsetY + height);
            allItemFrames.put(i, rect);

            hasAttachedItems.put(i, false);

            //计算每个item下边与下一个item的上边之前距离，使其一个页面只显示3个
            itemSpace = (float)((getHeight() - getPaddingBottom() - getPaddingTop()) - height * 3) / 2;
            Log.e("onLayoutChildren", "itemSpace:"+itemSpace);
            //控制居中
            if(i == getItemCount() -1){
                totalHeight += height;
            }else {
                totalHeight += height + itemSpace;
            }


            offsetY += height + itemSpace;
        }

        totalHeight = Math.max(totalHeight, getVerticalSpace());

        recycleAndFillItems(recycler, state);
    }

    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(state.isPreLayout()){
            return;
        }
        RectF displayRect = new RectF(0, verticalScrollOffset, getHorizontalSpace(), verticalScrollOffset + getVerticalSpace());
        RectF childFrame = new RectF();
        for(int i=0;i < getChildCount();i ++){
            View view = getChildAt(i);
            childFrame.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            //完全滑出屏幕
            if(!RectF.intersects(displayRect, childFrame)){
                removeAndRecycleAllViews(recycler);
            }
        }

        for(int i=0;i < getItemCount();i ++){
            if(RectF.intersects(displayRect, allItemFrames.get(i))){
                RectF frame = allItemFrames.get(i);
                TextView scrap = (TextView) recycler.getViewForPosition(i);
                measureChildWithMargins(scrap, 0, 0);

                int height = getDecoratedMeasuredHeight(scrap);

                float dx = Math.abs(frame.bottom - verticalScrollOffset - (float)height/2 - getHeight() / 2);
                Log.e("recycleAndFillItems", "dx:"+dx);
                float textDt = 3 * (itemSpace + height/2 - dx) / (itemSpace + height/2);
//                if(textDt > 3 ){
//                    textDt = 3;
//                }else if(textDt < -12){
//                    textDt = -12;
//                }
                float textSize = 15 + textDt;
                scrap.setTextSize(textSize);


                if(dx < height){
                    scrap.setTextColor(Color.RED);
                }else {
                    scrap.setTextColor(Color.BLACK);
                }


                Log.e("recycleAndFillItems", "textSize:"+textSize);
                addView(scrap);

                //将这个item布局出来
                layoutDecorated(scrap, (int)frame.left, (int)frame.top - verticalScrollOffset,
                        (int)frame.right, (int)frame.bottom - verticalScrollOffset);
            }
        }

    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    //下滑dy< 0上滑 >0
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.e("LayoutMananger", dy + "");

        detachAndScrapAttachedViews(recycler);

        int travel = dy;

        if(verticalScrollOffset + dy < 0){
            travel = 0;
        }else if(verticalScrollOffset + dy > totalHeight - getVerticalSpace()){
            travel = 0;
        }

        verticalScrollOffset += travel;

        recycleAndFillItems(recycler, state);

        offsetChildrenVertical(-travel);


        return travel;
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (this.getChildCount() == 0) {
            return null;
        } else {
            return new PointF(0.0F, (float)1);
        }
    }


    private static class TopSmoothScroller extends LinearSmoothScroller {

        TopSmoothScroller(Context context) {
            super(context);
        }

        /**
         * 以下参数以LinearSmoothScroller解释
         * @param viewStart RecyclerView的top位置
         * @param viewEnd RecyclerView的bottom位置
         * @param boxStart Item的top位置
         * @param boxEnd Item的bottom位置
         * @param snapPreference 判断滑动方向的标识（The edge which the view should snap to when entering the visible
         *                       area. One of {@link #SNAP_TO_START}, {@link #SNAP_TO_END} or
         *                       {@link #SNAP_TO_END}.）
         * @return 移动偏移量
         */
        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return boxStart - viewStart;// 这里是关键，得到的就是置顶的偏移量
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 0.5f;
        }
    }

}
