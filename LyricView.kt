import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

/**
 * 选词控件
 * [setLines]设置歌词
 */
class LyricView : View {

    private val lineHeight = dip(LINE_HEIGHT_DP).toFloat()
    private val selectorHeight = dip(SELECTOR_HEIGHT_DP).toFloat()
    private val selectorWidth = dip(SELECTOR_WIDTH_DP).toFloat()
    private val selectorRound = dip(SELECTOR_ROUND_DP).toFloat()

    private val selectedTextColor = Color.WHITE
    private val unselectedTextColor = Color.GRAY
    private val backgroundColor = Color.BLACK

    private val selectorColor = Color.WHITE
    private var selectStartIndex = 0
    private var selectEndIndex = 1

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val lines = mutableListOf<String>()
    private val rect = Rect() //用来测量字体大小的
    private val startRectF = RectF() //起始选择器的区域
    private val endRectF = RectF() //终止选择器的区域
    private var gestureDetector: GestureDetector
    private var scrollAnimator: ValueAnimator? = null //up事件开始，down事件结束
    private var selectorAnimator: ValueAnimator? = null //move事件开始，结束  down、up事件结束

    private var currentY = 0F //当前画布的距离起点的偏移量
    private var startY = (selectStartIndex + 0.5F) * lineHeight //起始线在画布上的纵坐标
    private var endY = startY + lineHeight //终止线在画布上的纵坐标
    private var status = 0 //用来区分是移动画布、起始选择器、终止选择器

    private val onGestureDetector = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            when (status) {
                STATUS_MOVE_MAIN -> {
                    addCurrentY(-distanceY)
                }
                STATUS_MOVE_START -> {
                    selectorAnimator?.cancel()
                    e2?.apply {
                        when (this.y) {
                            //是否在上下边界快速滑动
                            in 0F..lineHeight -> {
                                selectorAnimator = getSelectorAnimator(isStart = true, isUp = true)
                                selectorAnimator?.start()
                            }
                            in (height - lineHeight * 1.5F)..height.toFloat() -> {
                                selectorAnimator = getSelectorAnimator(isStart = true, isUp = false)
                                selectorAnimator?.start()
                            }
                            else -> {
                                addStartY(-distanceY)
                                selectStartIndex = calculateStartIndex()
                            }
                        }
                    }
                }
                STATUS_MOVE_END -> {
                    selectorAnimator?.cancel()
                    e2?.apply {
                        when (this.y) {
                            //上下边界快速滑动
                            in 0F..(lineHeight * 1.5F) -> {
                                selectorAnimator = getSelectorAnimator(isStart = false, isUp = true)
                                selectorAnimator?.start()
                            }
                            in (height - lineHeight)..height.toFloat() -> {
                                selectorAnimator = getSelectorAnimator(isStart = false, isUp = false)
                                selectorAnimator?.start()
                            }
                            else -> {
                                addEndY(-distanceY)
                                selectEndIndex = calculateEndIndex()
                            }
                        }
                    }
                }
            }
            postInvalidate()
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            scrollAnimator?.cancel()
            if (status == STATUS_MOVE_MAIN) {
                scrollAnimator = ValueAnimator.ofFloat(velocityY / 100, 0F)
                scrollAnimator?.duration = 1000
                scrollAnimator?.interpolator = DecelerateInterpolator()
                scrollAnimator?.addUpdateListener {
                    val y = it.animatedValue as? Float
                    if (y != null) {
                        addCurrentY(y)
                        postInvalidate()
                    }
                }
                scrollAnimator?.start()
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        lines.add("第一段")
        lines.addAll(testLyric)
        lines.add("第二段")
        lines.addAll(testLyric)
        lines.add("第三段")
        lines.addAll(testLyric)
        lines.add("第四段")
        lines.addAll(testLyric)
        lines.add("第五段")
        lines.addAll(testLyric)

        gestureDetector = GestureDetector(context, onGestureDetector, null)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            gestureDetector.onTouchEvent(event)

            if (it.action == MotionEvent.ACTION_DOWN) {
                scrollAnimator?.cancel()
                selectorAnimator?.cancel()

                //判断是不是在移动选择器
                status = when {
                    startRectF.contains(it.x, it.y - currentY) -> STATUS_MOVE_START
                    endRectF.contains(it.x, it.y - currentY) -> STATUS_MOVE_END
                    else -> STATUS_MOVE_MAIN
                }
            } else if (it.action == MotionEvent.ACTION_UP || it.action == MotionEvent.ACTION_CANCEL) {
                selectorAnimator?.cancel()
                calculateStartAndEndY()
                postInvalidate()
            }
        }
        return true
    }

    private fun getSelectorAnimator(isStart: Boolean, isUp: Boolean): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0F, 100F)
        animator.duration = 100
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            //todo 移动速度逻辑，当前固定20px
            val delta = if (isUp) 20F else -20F
            if (isStart) {
                addStartY(-delta)
            } else {
                addEndY(-delta)
            }
            addCurrentY(delta)
            selectStartIndex = calculateStartIndex()
            selectEndIndex = calculateEndIndex()

            postInvalidate()
        }
        return animator
    }

    /**
     * 根据起点终点的下标线应该在的位置
     */
    private fun calculateStartAndEndY() {
        startY = (selectStartIndex + 0.5F) * lineHeight
        endY = (selectEndIndex + 0.5F) * lineHeight
    }

    /**
     * 通过起始线的位置计算当前选中的起点下标
     */
    private fun calculateStartIndex(): Int {
        val temp = (startY / lineHeight).toInt()
        val max = Math.max(0, lines.size - 1)
        return when {
            temp < 0 -> 0
            temp > max -> max
            else -> temp
        }
    }

    /**
     * 通过终止线的位置计算当前选中的起点下标
     */
    private fun calculateEndIndex(): Int {
        if (lines.isEmpty()) return 0
        val temp = (endY / lineHeight).toInt()
        val max = lines.size
        return when {
            temp < 1 -> 1
            temp > max -> max
            else -> temp
        }
    }

    /**
     * 画布位置的变化逻辑
     * @param delta 变化量
     */
    private fun addCurrentY(delta: Float) {
        currentY += delta
        currentY = when {
            currentY > 0 -> 0F
            currentY < -getCurrentMax() -> -getCurrentMax()
            else -> currentY
        }
    }

    /**
     * 起始线位置的变化逻辑
     * @param delta 变化量
     */
    private fun addStartY(delta: Float) {
        if (lines.size in 0..1) {
            startY = 0F
            return
        }
        startY += delta
        startY = when {
            startY < lineHeight / 2 -> lineHeight / 2
            startY > getLinesHeight() - lineHeight * 1.5F -> getLinesHeight() - lineHeight * 1.5F
            else -> startY
        }
        if (endY - startY < lineHeight) {
            endY = startY + lineHeight
            selectEndIndex = calculateEndIndex()
        }
    }

    /**
     * 终止线位置的变化逻辑
     * @param delta 变化量
     */
    private fun addEndY(delta: Float) {
        if (lines.size in 0..1) {
            endY = lineHeight
            return
        }
        endY += delta
        endY = when {
            endY < lineHeight * 1.5F -> lineHeight * 1.5F
            endY > getLinesHeight() - lineHeight / 2 -> getLinesHeight() - lineHeight / 2
            else -> endY
        }
        if (endY - startY < lineHeight) {
            startY = endY - lineHeight
            selectStartIndex = calculateStartIndex()
        }
    }

    /**
     * 设置歌词
     */
    fun setLines(list: List<String>?) {
        lines.clear()
        list ?: return
        lines.addAll(list)
        postInvalidate()
    }

    /**
     * 获取画布最大的偏移量
     */
    //todo 可能为负数，即歌词不满一屏
    private fun getCurrentMax(): Float {
        if (lines.isEmpty()) return 0F
        return lineHeight * (lines.size + 1) - height
    }

    /**
     * 获取歌词的总高度
     */
    private fun getLinesHeight(): Float {
        if (lines.isEmpty()) return 0F
        return lineHeight * (lines.size + 1)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        canvas.drawColor(backgroundColor)

        canvas.save()
        canvas.translate(0F, currentY)
        canvas.clipRect(0F, -currentY, width.toFloat(), -currentY + height.toFloat())

        drawTextLines(canvas)
        drawSelector(canvas)

        canvas.restore()
    }

    /**
     * 绘制歌词
     */
    private fun drawTextLines(canvas: Canvas) {
        var height = lineHeight
        textPaint.style = Paint.Style.FILL
        textPaint.isFakeBoldText = false
        lines.forEachIndexed { index, s ->
            if (s.isNotBlank()) {
                textPaint.textSize = if (index in selectStartIndex until selectEndIndex) {
                    sp(LYRIC_SELECT_TEXT_SIZE_SP).toFloat()
                } else {
                    sp(LYRIC_UNSELECT_TEXT_SIZE_SP).toFloat()
                }
                textPaint.getTextBounds(s, 0, s.length, rect)
                textPaint.color = if (index in selectStartIndex until selectEndIndex) selectedTextColor else unselectedTextColor

                canvas.drawText(
                    s,
                    (width.toFloat() - rect.width()) / 2,
                    height - rect.height() / 2 - rect.top,
                    textPaint
                )
            }

            height += lineHeight
        }
    }

    /**
     * 绘制选择器
     */
    private fun drawSelector(canvas: Canvas) {
        if (lines.size <= 0) return

        textPaint.color = selectorColor
        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = SELECTOR_LINE_HEIGHT_PX

        startRectF.left = 0F
        startRectF.right = selectorWidth
        startRectF.top = startY - selectorHeight / 2
        startRectF.bottom = startY + selectorHeight / 2
        canvas.drawRoundRect(startRectF, selectorRound, selectorRound, textPaint)
        canvas.drawLine(selectorWidth, startY, width.toFloat(), startY + SELECTOR_LINE_HEIGHT_PX, textPaint)

        endRectF.left = width - selectorWidth
        endRectF.right = width.toFloat()
        endRectF.top = endY - selectorHeight / 2
        endRectF.bottom = endY + selectorHeight / 2
        canvas.drawRoundRect(endRectF, selectorRound, selectorRound, textPaint)
        canvas.drawLine(0F, endY, width - selectorWidth, endY + SELECTOR_LINE_HEIGHT_PX, textPaint)

        textPaint.style = Paint.Style.FILL
        textPaint.isFakeBoldText = true
        textPaint.color = backgroundColor
        textPaint.textSize = sp(SELECTOR_TEXT_SIZE_SP).toFloat()
        textPaint.getTextBounds(START_TEXT, 0, START_TEXT.length, rect)
        canvas.drawText(
            START_TEXT,
            (selectorWidth - rect.width()) / 2,
            startY - rect.height() / 2 - rect.top,
            textPaint
        )

        textPaint.getTextBounds(END_TEXT, 0, END_TEXT.length, rect)
        canvas.drawText(
            END_TEXT,
            width - (selectorWidth + rect.width()) / 2,
            endY - rect.height() / 2 - rect.top,
            textPaint
        )

    }

    companion object {
        private const val LINE_HEIGHT_DP = 50
        private const val LYRIC_SELECT_TEXT_SIZE_SP = 20
        private const val LYRIC_UNSELECT_TEXT_SIZE_SP = 20
        private const val START_TEXT = "开始"
        private const val END_TEXT = "结束"
        private const val SELECTOR_WIDTH_DP = 60
        private const val SELECTOR_HEIGHT_DP = 24
        private const val SELECTOR_ROUND_DP = 12
        private const val SELECTOR_LINE_HEIGHT_PX = 1F
        private const val SELECTOR_TEXT_SIZE_SP = 11F


        private const val STATUS_MOVE_MAIN = 0
        private const val STATUS_MOVE_START = 1
        private const val STATUS_MOVE_END = 2
    }
}

val testLyric = """
我和我的祖国一刻也不能分割
无论我走到哪里都流出一首赞歌
我歌唱每一座高山我歌唱每一条河
袅袅炊烟小小村落路上一道辙
我最亲爱的祖国我永远紧依着你的心窝
你用你那母亲的脉搏和我诉说
我的祖国和我像海和浪花一朵
浪是海的赤子海是那浪的依托
每当大海在微笑我就是笑的旋涡
我分担着海的忧愁分享海的欢乐
我最亲爱的祖国你是大海永不干涸
永远给我碧浪清波心中的歌
啦啦
永远给我碧浪清波心中的歌
""".trim().lines()
