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

class MyLayoutManagerWithKolin : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider{


    private val allItemFrames: SparseArray<RectF> = SparseArray()

    private val hasAttachedItems: SparseBooleanArray = SparseBooleanArray()

    private var verticalScrollOffset: Int = 0

    private var totalHeight: Float = 0f

    private var itemSpace: Float = 0f

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (itemCount <= 0) return
        // 跳过preLayout，preLayout主要用于支持动画
        if (state!!.isPreLayout) {
            return
        }
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler!!)
        var offsetY: Float = paddingTop.toFloat()
        verticalScrollOffset = 0
        totalHeight = 0f

        for(i in 0 until itemCount){
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val width = width
            val height = getDecoratedMeasuredHeight(view)

            var rect = allItemFrames.get(i)
            if(rect == null){
                rect = RectF()
                rect.set(paddingLeft.toFloat(), offsetY, width.toFloat(), offsetY + height)
                allItemFrames.put(i, rect)
                hasAttachedItems.put(i, false)
            }

            itemSpace = (getHeight() - paddingBottom - paddingTop - height * 3).toFloat() / 2

            totalHeight = if(i == itemCount - 1){
                height + totalHeight
            }else{
                height + itemSpace + totalHeight
            }

            offsetY += height + itemSpace
        }

        totalHeight = Math.max(totalHeight, getVerticalSpace().toFloat())
        recycleAndFillItems(recycler, state)
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State){
        if(state.isPreLayout){
            return
        }
        val displayRect = RectF(0f, verticalScrollOffset.toFloat(), getHorizontalSpace().toFloat(), (verticalScrollOffset + getVerticalSpace()).toFloat())
        val childFrame = RectF()
        for (i in 0 until childCount){
            val view = getChildAt(i) ?: break
            childFrame.set(view.left.toFloat(), view.top.toFloat(), view.right.toFloat(), view.bottom.toFloat())
            if(!RectF.intersects(displayRect, childFrame)){
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
                //if(textDt > 3 ){
                //    textDt = 3;
                //}else if(textDt < -12){
                //    textDt = -12;
                //}
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

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        if(recycler == null){
            return 0
        }
        detachAndScrapAttachedViews(recycler)

        var travel = dy

        if (verticalScrollOffset + dy < 0) {
            travel = 0
        } else if (verticalScrollOffset + dy > totalHeight - getVerticalSpace()) {
            travel = 0
        }

        verticalScrollOffset += travel

        recycleAndFillItems(recycler, state!!)

        offsetChildrenVertical(-travel)

        return travel
    }

    override fun canScrollHorizontally(): Boolean = false

    override fun canScrollVertically(): Boolean = true


    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        if (recyclerView == null) return
        var smoothScroller = TopSmoothScroller(recyclerView.context)
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

    private class TopSmoothScroller(context: Context) : LinearSmoothScroller(context){
        override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
            return boxStart - viewStart
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return 0.5f
        }
    }

    fun getVerticalSpace(): Int = height - paddingBottom - paddingTop

    fun getHorizontalSpace(): Int = width - paddingLeft - paddingRight
}