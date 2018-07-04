package com.noober.scrolltextlayout

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.widget.TextView

class MyLayoutManager3 : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    //保存所有的Item的上下左右的偏移量信息
    private val allItemFrames = SparseArray<RectF>()
    //记录Item是否出现过屏幕且还没有回收。true表示出现过屏幕上，并且还没被回收
    private val hasAttachedItems = SparseBooleanArray()

    private var verticalScrollOffset: Int = 0 //上下滑动的距离
    private var totalHeight: Float = 0.toFloat()//recyclerview总高度

    private var itemSpace: Float = 0.toFloat()//文字间隔距离

    private val horizontalSpace: Int
        get() = width - paddingLeft - paddingRight

    private val verticalSpace: Int
        get() = height - paddingBottom - paddingTop

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        //如果没有item，直接返回
        if (itemCount <= 0) return
        // 跳过preLayout，preLayout主要用于支持动画
        if (state!!.isPreLayout) {
            return
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler!!)

        var offsetY = paddingTop.toFloat()
        verticalScrollOffset = 0
        totalHeight = 0f
        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val width = getDecoratedMeasuredWidth(view)
            val height = getDecoratedMeasuredHeight(view)//文字高度

            var rect: RectF? = allItemFrames.get(i)
            if (rect == null) {
                rect = RectF()
            }
            rect.set(paddingLeft.toFloat(), offsetY, width.toFloat(), offsetY + height)
            allItemFrames.put(i, rect)

            hasAttachedItems.put(i, false)

            //计算每个item下边与下一个item的上边之前距离，使其一个页面只显示3个
            itemSpace = (getHeight() - paddingBottom - paddingTop - height * 3).toFloat() / 2
            Log.e("onLayoutChildren", "itemSpace:$itemSpace")
            //控制居中
            if (i == itemCount - 1) {
                totalHeight += height.toFloat()
            } else {
                totalHeight += height + itemSpace
            }


            offsetY += height + itemSpace
        }

        totalHeight = Math.max(totalHeight, verticalSpace.toFloat())

        recycleAndFillItems(recycler, state)
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.isPreLayout) {
            return
        }
        val displayRect = RectF(0f, verticalScrollOffset.toFloat(), horizontalSpace.toFloat(), (verticalScrollOffset + verticalSpace).toFloat())
        val childFrame = RectF()
        for (i in 0 until childCount) {
            val view = getChildAt(i) ?: break
            childFrame.set(view!!.left.toFloat(), view.top.toFloat(), view.right.toFloat(), view.bottom.toFloat())
            //完全滑出屏幕
            if (!RectF.intersects(displayRect, childFrame)) {
                removeAndRecycleAllViews(recycler)
            }
        }

        for (i in 0 until itemCount) {
            if (RectF.intersects(displayRect, allItemFrames.get(i))) {
                val frame = allItemFrames.get(i)
                val scrap = recycler.getViewForPosition(i) as TextView
                measureChildWithMargins(scrap, 0, 0)

                val height = getDecoratedMeasuredHeight(scrap)

                val dx = Math.abs(frame.bottom - verticalScrollOffset.toFloat() - height.toFloat() / 2 - (getHeight() / 2).toFloat())
                Log.e("recycleAndFillItems", "dx:$dx")
                val textDt = 3 * (itemSpace + height / 2 - dx) / (itemSpace + height / 2)
                //                if(textDt > 3 ){
                //                    textDt = 3;
                //                }else if(textDt < -12){
                //                    textDt = -12;
                //                }
                val textSize = 15 + textDt
                scrap.textSize = textSize


                if (dx < height) {
                    scrap.setTextColor(Color.RED)
                } else {
                    scrap.setTextColor(Color.BLACK)
                }


                Log.e("recycleAndFillItems", "textSize:$textSize")
                addView(scrap)

                //将这个item布局出来
                layoutDecorated(scrap, frame.left.toInt(), frame.top.toInt() - verticalScrollOffset,
                        frame.right.toInt(), frame.bottom.toInt() - verticalScrollOffset)
            }
        }

    }

    //下滑dy< 0上滑 >0
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        Log.e("LayoutMananger", dy.toString() + "")

        detachAndScrapAttachedViews(recycler!!)

        var travel = dy

        if (verticalScrollOffset + dy < 0) {
            travel = 0
        } else if (verticalScrollOffset + dy > totalHeight - verticalSpace) {
            travel = 0
        }

        verticalScrollOffset += travel

        recycleAndFillItems(recycler, state!!)

        offsetChildrenVertical(-travel)


        return travel
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val smoothScroller = TopSmoothScroller(recyclerView!!.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return if (this.childCount == 0) {
            null
        } else {
            PointF(0.0f, 1.toFloat())
        }
    }


    private class TopSmoothScroller internal constructor(context: Context) : LinearSmoothScroller(context) {

        /**
         * 以下参数以LinearSmoothScroller解释
         * @param viewStart RecyclerView的top位置
         * @param viewEnd RecyclerView的bottom位置
         * @param boxStart Item的top位置
         * @param boxEnd Item的bottom位置
         * @param snapPreference 判断滑动方向的标识（The edge which the view should snap to when entering the visible
         * area. One of [.SNAP_TO_START], [.SNAP_TO_END] or
         * [.SNAP_TO_END].）
         * @return 移动偏移量
         */
        override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
            return boxStart - viewStart// 这里是关键，得到的就是置顶的偏移量
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return 0.5f
        }
    }

}