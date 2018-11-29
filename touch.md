## move

```
    private val duration = 300 //滑动时间
    private val mScroller = Scroller(this.context)
    private var lastX = 0f
    private var lastY = 0f

    override fun computeScroll() {
        if (!mScroller.computeScrollOffset()) {
            return
        }
        scrollTo(mScroller.currX, mScroller.currY)
        invalidate()
        super.computeScroll()

//        if (mScroller.computeScrollOffset()) {
//            scrollTo(mScroller.currX, mScroller.currY)
//            postInvalidate()
//        }
//        super.computeScroll()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mScroller.forceFinished(true)
                lastX = event.x
                lastY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mScroller.forceFinished(true)
                val moveX = lastX - event.x
                val moveY = lastY - event.y
                scrollBy(moveX.toInt(), moveY.toInt())
                lastX = event.x
                lastY = event.y
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY, duration)
                postInvalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
```
