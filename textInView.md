## drawText

```
    companion object {
        private val BOLD = Typeface.defaultFromStyle(Typeface.BOLD)
        private val NORMAL = Typeface.defaultFromStyle(Typeface.NORMAL)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isBold = true
    private var textSize = sp(18f)
    private var textColor = Color.BLACK

    private fun sp(value: Float): Float = (value * resources.displayMetrics.scaledDensity)

    init {
        paint.textSize = textSize
        paint.color = textColor
        paint.typeface = if (isBold) BOLD else NORMAL
        paint.textAlign = Paint.Align.CENTER
    }
```
