## 类文件
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.TextView

class TestPanel : DialogFragment() {

    private var message = ""

    //布局文件
    private var layoutResource: Int = R.layout.test_panel

    init {
        isCancelable = true
    }

    fun bind(message: String): DialogFragment {
        this.message = message
        this.view?.apply {
            viewBindData(this, message)
        }
        return this@TestPanel
    }

    private fun viewBindData(view: View, message: String): View {
        view.findViewById<TextView>(R.id.test_panel_text).text = message
        return view
    }

    override fun onStart() {
        super.onStart()
        val window = dialog.window
        val params = window!!.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params
        window.setWindowAnimations((R.style.KG_BottomDialog_Animation))
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(layoutResource, container, false)
        return viewBindData(view, message)
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        super.show(manager, tag)
        instance = this
    }

    override fun show(transaction: FragmentTransaction?, tag: String?): Int {
        instance = this
        return super.show(transaction, tag)
    }

    override fun dismiss() {
        super.dismiss()
        instance = null
    }

    companion object {
        private var instance: TestPanel? = null

        @Synchronized
        fun openPanel(message: String, activity: AppCompatActivity) {
            instance?.dismiss()
            TestPanel().bind(message).show(activity.supportFragmentManager, "TestPanel")
        }

        @Synchronized
        fun update(message: String) {
            instance?.apply {
                this.bind(message)
            }
        }
    }
}

## 布局文件 R.layout.test_panel
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#0E041C">

    <TextView
        android:id="@+id/test_panel_text"
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:gravity="center"
        android:textColor="#ffffff"
        android:textSize="20sp"
        tools:text="测试" />
</android.support.constraint.ConstraintLayout>

## 样式文件styles.xml
<style name="KG_BottomDialog.Animation" parent="Animation.AppCompat.Dialog">
    <item name="android:windowEnterAnimation">@anim/kg_slide_in_bottom</item>
    <item name="android:windowExitAnimation">@anim/kg_slide_out_bottom</item>
</style>

## 动画xml anim文件夹下
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="300"
        android:fromYDelta="100.0%"
        android:toYDelta="0.0%" />
</set>

<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="300"
        android:fromYDelta="0.0%"
        android:toYDelta="100.0%" />
</set>


## 还有一种写法
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.TextView

object PanelManager {

    private var instance: PrivatePanel? = null

    @Synchronized
    fun openPanel(message: String, activity: AppCompatActivity) {
        instance?.dismiss()
        PrivatePanel().bind(message).show(activity.supportFragmentManager, "aaa")
    }

    @Synchronized
    fun update(message: String) {
        instance?.apply {
            this.bind(message)
        }
    }

    @SuppressLint("ValidFragment")
    private class PrivatePanel : DialogFragment() {

        private var message = ""

        //布局文件
        private var layoutResource: Int = R.layout.test_panel

        init {
            isCancelable = true
        }

        fun bind(message: String): DialogFragment {
            this.message = message
            this.view?.apply {
                viewBindData(this, message)
            }
            return this@PrivatePanel
        }

        private fun viewBindData(view: View, message: String): View {
            view.findViewById<TextView>(R.id.test_panel_text).text = message
            return view
        }

        override fun onStart() {
            super.onStart()
            val window = dialog.window
            val params = window!!.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
            window.setWindowAnimations((R.style.KG_BottomDialog_Animation))
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = inflater.inflate(layoutResource, container, false)
            return viewBindData(view, message)
        }

        override fun show(manager: FragmentManager?, tag: String?) {
            super.show(manager, tag)
            instance = this
        }

        override fun show(transaction: FragmentTransaction?, tag: String?): Int {
            instance = this
            return super.show(transaction, tag)
        }

        override fun dismiss() {
            super.dismiss()
            instance = null
        }
    }
}
