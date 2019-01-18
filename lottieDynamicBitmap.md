```
/**
 * 将lottie动画中的占位图换成自己想要的图片
 * lottieView:目标控件
 * sourcePath:该动画使用的资源路径名
 * targetFileName:目标占位图在资源路径中的文件名
 * bitmap:用来替换的图片
 * 
 * 注意:
 * 1.如果lottieView这个对象被复用，在动画结束之后要调用lottieView.setImageAssetDelegate(null)防止代理逻辑影响下一次动画
 * 2.sourcePath一定是以"/"做结尾
 * 3.该方法是在运行期间才生效
 */
fun changeBitmapInLottie(lottieView: LottieAnimationView, sourcePath: String, targetFileName: String, bitmap: Bitmap) {
    lottieView.setImageAssetDelegate{
        if (it.fileName == targetFileName) {
            bitmap
        } else {
            val inputStream: InputStream
            try {
                if (TextUtils.isEmpty(it.dirName)) {
                    null
                } else {
                    val opts = BitmapFactory.Options()
                    opts.inScaled = true
                    opts.inDensity = 160
                    inputStream = lottieView.context.assets.open(sourcePath + it.fileName)
                    BitmapFactory.decodeStream(inputStream, null, opts)
                }
            } catch (e: IOException) {
                null
            }
        }
    }
}
```
