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

/**
 * Created by xiaoqi on 2018/7/3
 */
public class MyLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    //保存所有的Item的上下左右的偏移量信息
    private SparseArray<Rect> allItemFrames = new SparseArray<>();
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private SparseBooleanArray hasAttachedItems = new SparseBooleanArray();

    int verticalScrollOffset;
    int totalHeight;


    public MyLayoutManager(){
//        mVerticalBoundCheck = new ViewBoundsCheck(this.mVerticalBoundCheckCallback);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        Log.e("MyLayoutManager", "generateDefaultLayoutParams");
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        Log.e("MyLayoutManager", "onLayoutChildren");
        //如果没有item，直接返回
        if (getItemCount() <= 0) return;
        // 跳过preLayout，preLayout主要用于支持动画
        if (state.isPreLayout()) {
            return;
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler);

        int offsetY = 0;
        verticalScrollOffset = 0;
        totalHeight = 0;
        for(int i = 0; i < getItemCount(); i ++){
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            Log.e("RECT", "layout: "+ i);
            Rect rect = allItemFrames.get(i);
            if(rect == null){
                rect = new Rect();
            }
            rect.set(0, offsetY, width, offsetY + height);
            allItemFrames.put(i, rect);

            hasAttachedItems.put(i, false);

            layoutDecorated(view, 0, offsetY, width, offsetY + height);

            totalHeight += height + getHeight() / 2 - height * 3 / 2;

            offsetY += getHeight() / 2 - height / 2;
        }

        totalHeight = Math.max(totalHeight, getVerticalSpace());

        recycleAndFillItems(recycler, state, 0);
    }

    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        if(state.isPreLayout()){
            return;
        }
        Rect displayRect = new Rect(0, verticalScrollOffset, getHorizontalSpace(), verticalScrollOffset + getVerticalSpace());
        Rect childFrame = new Rect();
        Log.e("RECT", "getChildCount: "+ getChildCount());
        for(int i=0;i < getChildCount();i ++){
            View view = getChildAt(i);
            childFrame.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            //完全滑出屏幕
            if(!Rect.intersects(displayRect, childFrame)){
                removeAndRecycleAllViews(recycler);
            }
        }
        int start = -1;
        boolean isCenter = false;

        int centerY = getHeight() / 2;

        for(int i=0;i<getItemCount();i ++){
            Log.e("TTTTT", "recycleAndFillItems: "+ i);
            if(Rect.intersects(displayRect, allItemFrames.get(i))){
                Log.e("TTTTT", "in: "+ i);
                Rect frame = allItemFrames.get(i);
                TextView scrap = (TextView) recycler.getViewForPosition(i);
                measureChildWithMargins(scrap, 0, 0);

                if(isCenter){
                    Log.e("TEXT2222", Math.abs((frame.top - verticalScrollOffset) - centerY) + "");

                    scrap.setTextSize(18);
                }else {
                    scrap.setTextSize(15);
                }

                if(start < 0){
                    isCenter = true;

                }else {
                    isCenter = false;

                }
                start = i;
                addView(scrap);

                //将这个item布局出来
                layoutDecorated(scrap, frame.left, frame.top - verticalScrollOffset,
                        frame.right, frame.bottom - verticalScrollOffset);
            }else {
                Log.e("TTTTT", "out: "+ i);
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

        Log.e("LayoutMananger", "verticalScrollOffset:"+ verticalScrollOffset);
        if(verticalScrollOffset + dy < 0){
            travel = -verticalScrollOffset;
        }else if(verticalScrollOffset + dy > totalHeight - getVerticalSpace()){
            travel = totalHeight - getVerticalSpace() - verticalScrollOffset;
        }

        verticalScrollOffset += travel;

        offsetChildrenVertical(-travel);

        recycleAndFillItems(recycler, state, travel);
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

//    public int findFirstVisibleItemPosition() {
//        View child = this.findOneVisibleChild(0, this.getChildCount(), false, true);
//        return child == null ? -1 : this.getPosition(child);
//    }

//    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
////        this.ensureLayoutState();
//        int preferredBoundsFlag = false;
//        int acceptableBoundsFlag = 0;
//        short preferredBoundsFlag;
//        if (completelyVisible) {
//            preferredBoundsFlag = 24579;
//        } else {
//            preferredBoundsFlag = 320;
//        }
//
//        if (acceptPartiallyVisible) {
//            acceptableBoundsFlag = 320;
//        }
//
//        return this.mOrientation == 0 ? this.mHorizontalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag) : this.mVerticalBoundCheck.findOneViewWithinBoundFlags(fromIndex, toIndex, preferredBoundsFlag, acceptableBoundsFlag);
//    }
}
