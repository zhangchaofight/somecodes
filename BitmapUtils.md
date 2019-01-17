```
import android.graphics.*

/**
 * 自己实现的图片中心裁剪成方图（会释放原图）
 */
fun Bitmap.centerCrop(): Bitmap {
    val width = this.width
    val height = this.height
    if (width == 0 || height == 0) {
        return this
    }
    return when {
        width > height -> {
            if (this.isRecycled) {
                this.recycle()
            }
            Bitmap.createBitmap(this, (width - height) / 2, 0, height, height)
        }
        width < height -> {
            if (this.isRecycled) {
                this.recycle()
            }
            Bitmap.createBitmap(this, 0, (height - width), width, width)
        }
        else -> this
    }
}

/**
 * 自己实现的图片缩放（会释放原图）
 * newWidth:缩放后的宽度
 * newHeight:缩放后的高度
 */
fun Bitmap.scaleBitmap(newWidth: Int, newHeight: Int): Bitmap {
    val height = this.height
    val width = this.width
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)// 使用后乘
    val newBM = Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
    if (!this.isRecycled) {
        this.recycle()
    }
    return newBM
}

/**
 * 自己实现的将图片变成圆形（会释放原图）
 */
fun Bitmap.transToRoundBitmap(): Bitmap {
    val paint = Paint()
    //设置抗锯齿
    paint.isAntiAlias = true
    //创建一个与原bitmap一样宽度的正方形bitmap
    val circleBitmap = Bitmap.createBitmap(this.width, this.width, Bitmap.Config.ARGB_8888)
    //以该bitmap为低创建一块画布
    val canvas = Canvas(circleBitmap)
    //以（width/2, width/2）为圆心，width/2为半径画一个圆
    canvas.drawCircle((this.width / 2).toFloat(), (this.width / 2).toFloat(), (this.width / 2).toFloat(), paint)

    //设置画笔为取交集模式
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    //裁剪图片
    canvas.drawBitmap(this, 0F, 0F, paint)

    if (!this.isRecycled) {
        this.recycle()
    }

    return circleBitmap
}

/**
 * 将文字转化为图片
 * text:文字的内容
 * textSize:文字字号(需要把字号变为sp的值)
 * textColor:文字颜色
 * isBold:是否使用粗体
 * width:图片的宽度
 * height:图片的高度
 */
fun textToBitmap(text: String, textSize: Float, textColor: Int, isBold: Boolean = false,
                           width: Int, height: Int): Bitmap {
    val textBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(textBitmap)
    val paint = Paint()
    paint.isAntiAlias = true
    paint.color = textColor
    paint.textSize = textSize
    paint.isFakeBoldText = isBold
    val endIndex = paint.breakText(text, true, width.toFloat(), null)
    val temp = text.substring(0, endIndex)
    val rect = Rect()
    paint.getTextBounds(temp, 0, temp.length, rect)
    canvas.drawText(temp, 0f, -rect.top.toFloat(), paint)
    return textBitmap
}
```
