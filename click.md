## 点击事件
```
    private val LONG_PRESS_TIME = ViewConfiguration.getLongPressTimeout()
    private val SLOP = ViewConfiguration.get(this.context).scaledTouchSlop
    private var onClickListener: OnClickListener? = null
    private var onLongClickListener: OnLongClickListener? = null

    override fun setOnClickListener(l: OnClickListener) {
        super.setOnClickListener(l)
        onClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener) {
        super.setOnLongClickListener(l)
        onLongClickListener = l
    }

    private fun shouldPerformClick() = onClickListener != null || onLongClickListener != null

    private var lastX = 0f
    private var lastY = 0f
    private var downTime = 0L

    override fun performClick(): Boolean {
        Log.d(javaClass.name, "performClick")
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (!shouldPerformClick()) {
                return super.onTouchEvent(event)
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    downTime = System.currentTimeMillis()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val moveX = event.x - lastX
                    val moveY = event.y - lastY
                    val current = System.currentTimeMillis()
                    if (Math.abs(moveX) <= slop && Math.abs(moveY) <= slop) {
                        if (current - downTime < LONG_PRESS_TIME || onLongClickListener == null) {
                            performClick()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                performLongClick(event.x, event.y)
                            } else {
                                performLongClick()
                            }
                        }
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
```
