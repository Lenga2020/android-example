package com.lena.android.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

open class ParentFragment:Fragment() {
    var active: Boolean = false
    var alive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alive = true;
    }

    override fun onDestroy() {
        super.onDestroy()
        alive = false
    }

    override fun onResume() {
        super.onResume()
        active = true;
    }

    override fun onStop() {
        super.onStop()
        active = false;
    }
}