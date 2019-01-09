```
/**
 * 用Rxjava来做方法超时返回的功能
 * 
 * task:需要执行的任务
 * success:超时时间内正常返回后续执行
 * timeout:超时后执行
 * error:方法执行期间抛出异常执行
 * timeoutTime:设置超时时长(默认3)
 * timeoutUnit:设置超时时长单位(默认TimeUnit.SECONDS)
 */
fun timeoutModel(task: () -> Unit, success: () -> Unit, timeout: () -> Unit, error: () -> Unit,
                 timeoutTime: Long = 3, timeoutUnit: TimeUnit = TimeUnit.SECONDS): Disposable {
    return Observable.create<Unit> {
        try {
            task.invoke()
        } catch (e: Exception) {
            it.onError(e)
        }
        it.onNext(Unit)
        it.onComplete()
    }.timeout(timeoutTime, timeoutUnit)
            .subscribeBy(
                    onNext = {
                        success.invoke()
                    },
                    onError = {
                        if (it is TimeoutException) {
                            timeout.invoke()
                        } else {
                            error.invoke()
                        }
                    }
            )
}
```
