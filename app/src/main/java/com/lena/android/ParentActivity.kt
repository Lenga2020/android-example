package com.lena.android

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


open class ParentActivity: AppCompatActivity() {
    companion object {
        const val PRIVACY_POLICY_URL = "https://www.baidu.com"
        const val TERMS_URL = "https://fanyi.baidu.com"
    }

    private var navBarHeight: Int? = null
    var active: Boolean = false
    var alive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alive = true
        active = true
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false;
    }

    override fun onDestroy() {
        super.onDestroy()
        active  = false
        alive = false
    }

    fun stateCheck(): Boolean {
        if (null == App.app) {
            throw RuntimeException("Application error!")
        }
        return true
    }

    fun getScreenHeightDp(): Float {
        val metrics = resources.displayMetrics
        return metrics.heightPixels / metrics.density
    }

    fun availableActivity(): Boolean {
        return active && !isFinishing && !isDestroyed
    }

    @SuppressLint("InternalInsetResource")
    fun getNavBarHeight(): Int {
        if (navBarHeight == null) {
            navBarHeight = try {
                val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
            } catch (e: Exception) {
                0
            }
        }
        return navBarHeight ?: 0
    }

    private fun hasNavigationBar(): Boolean {
        val resourceId = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return resourceId > 0 && resources.getBoolean(resourceId)
    }

    /**
     * 部分机型除了设置主题，可能还需要以下代码才可以真正全屏
     */
    fun enableFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT // 状态栏透明
            window.setNavigationBarColor(Color.TRANSPARENT) // 导航栏透明
        }
    }

    fun setWindowLightStatusBar(status: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setStatusBarTextColor2(!status)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            setStatusBarTextColor1(!status)
        }
    }

    fun setStatusBarColorWithBlackText(backgroundColor: Int) {
        setWindowLightStatusBar(false)
        window.addFlags(Int.MIN_VALUE)
        window.statusBarColor = backgroundColor
    }

    fun setStatusBarColorWithWhiteText(backgroundColor: Int) {
        setWindowLightStatusBar(true)
        window.addFlags(Int.MIN_VALUE)
        window.statusBarColor = backgroundColor
    }

    @RequiresApi(value = Build.VERSION_CODES.LOLLIPOP)
    fun setNavigationBarColor(color: Int) {
        window.navigationBarColor = color
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val touchView = currentFocus
            if (touchView != null && isShouldHideKeyboard(touchView, ev)) {
                touchView.clearFocus()
                hideKeyboard(touchView.windowToken)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideKeyboard(view: View, event: MotionEvent): Boolean {
        if (view is EditText) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + view.width
            val bottom = top + view.height
            return event.rawX < left || event.rawX > right || event.rawY < top || event.rawY > bottom
        }
        return false
    }

    private fun hideKeyboard(token: IBinder) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun setStatusBarTextColor1(isTextBlack: Boolean) {
        var flags = window.decorView.systemUiVisibility
        flags = if (isTextBlack) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = flags
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setStatusBarTextColor2(isTextBlack: Boolean) {
        val windowInsetsController = window.insetsController
        if (isTextBlack) {
            windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            windowInsetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
    }
}