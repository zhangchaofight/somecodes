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
备注：
1.这个方法仅作为简单的实现思路，可以完成简单的需求。如果执行的方法有返回值，需要改变Observable的返回值。
2.这个方法执行的线程没有做转换，可能会产生县城切换之类的问题，建议根据具体使用的场景做线程切换操作。
3.由于这个方法使用的场景一般执行时间较长，如果是在context里，建议将返回的Disposable对象添加到CompositeDisposable里，绑定context生命周期，在context生命周期结束的时候dispose一下，防止发生异常


思考：
1.可以将这个task方法返回值改成泛型参数
2.没有测试线程切换问题
