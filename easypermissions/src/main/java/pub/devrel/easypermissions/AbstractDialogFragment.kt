package com.mgsoftware.kotlinapp

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

open class AbstractDialogFragment : DialogFragment() {
    companion object {
        val KEY_REQUEST_CODE = "request_code"

        fun newInstance(requestCode: Int): AbstractDialogFragment {
            val fragment = AbstractDialogFragment()
            val args = Bundle()
            args.putInt(KEY_REQUEST_CODE, requestCode)
            fragment.arguments = args
            return fragment
        }
    }

    private var requestCode: Int = 0
    private var resultCode = DialogFragmentCallback.RESULT_CANCEL
    private var data: Bundle? = null

    private var callback: DialogFragmentCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCode = arguments!!.getInt(KEY_REQUEST_CODE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            callback = context as DialogFragmentCallback
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(context.toString() + " must implement Callback")
        }

    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (callback != null)
            callback!!.onDialogResult(requestCode, resultCode, data)
    }

    protected fun setResultCode(resultCode: Int) {
        this.resultCode = resultCode
    }

    protected fun setData(data: Bundle) {
        this.data = data
    }
}
