package pub.devrel.easypermissions

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StyleRes
import android.support.v7.app.AlertDialog
import com.mgsoftware.kotlinapp.AbstractDialogFragment
import com.mgsoftware.kotlinapp.DialogFragmentCallback

/**
 * Created by Mariusz
 */
class AppSettingDialogFragment : AbstractDialogFragment() {

    @StyleRes
    private var mThemeResId: Int? = 0
    private var mTitle: String? = ""
    private var mRationale: String? = ""
    private var mPositiveButtonText: String? = ""
    private var mNegativeButtonText: String? = ""

    val positiveListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
        activity?.startActivityForResult(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", context?.packageName, null)),
                DEFAULT_SETTINGS_REQ_CODE)
        setResultCode(DialogFragmentCallback.RESULT_OK)
    }

    val negativeListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ ->
        setResultCode(DialogFragmentCallback.RESULT_CANCEL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = arguments?.getBundle(KEY_CONFIG)
        mThemeResId = config?.getInt(KEY_THEME_RED_ID)
        mTitle = config?.getString(KEY_TITLE, getString(R.string.title_settings_dialog))
        mRationale = config?.getString(KEY_RATIONALE, getString(R.string.rationale_ask_again))
        mPositiveButtonText = config?.getString(KEY_POSITIVE_BUTTON_TEXT, getString(android.R.string.ok))
        mNegativeButtonText = config?.getString(KEY_NEGATIVE_BUTTON_TEXT, getString(android.R.string.cancel))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder
        builder = if (mThemeResId!! > 0) {
            AlertDialog.Builder(context!!, mThemeResId!!)
        } else {
            AlertDialog.Builder(context!!)
        }
        return builder
                .setCancelable(false)
                .setTitle(mTitle)
                .setMessage(mRationale)
                .setPositiveButton(mPositiveButtonText, positiveListener)
                .setNegativeButton(mNegativeButtonText, negativeListener)
                .show()
    }

    companion object {
        val TAG = "AppSettingDialogFragment"
        val DEFAULT_SETTINGS_REQ_CODE = 16061

        private val KEY_CONFIG = "config"

        val KEY_THEME_RED_ID = "themeResId"
        val KEY_TITLE = "title"
        val KEY_RATIONALE = "rationale"
        val KEY_POSITIVE_BUTTON_TEXT = "positiveButtonText"
        val KEY_NEGATIVE_BUTTON_TEXT = "negativeButtonText"

        fun newInstance(requestCode: Int, config: Bundle): AppSettingDialogFragment {
            val fragment = AppSettingDialogFragment()
            val args = Bundle()
            args.putInt(AbstractDialogFragment.KEY_REQUEST_CODE, requestCode)
            args.putBundle(KEY_CONFIG, config)
            fragment.arguments = args
            return fragment
        }
    }
}
