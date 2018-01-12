package com.mgsoftware.kotlinapp

import android.os.Bundle

interface DialogFragmentCallback {
    fun onDialogResult(requestCode: Int, resultCode: Int, data: Bundle?)

    companion object {
        val RESULT_CANCEL = 0
        val RESULT_OK = -1
    }
}
