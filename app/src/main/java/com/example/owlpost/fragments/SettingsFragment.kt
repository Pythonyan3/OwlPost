package com.example.owlpost.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.databinding.FragmentSettingsBinding
import com.example.owlpost.models.cryptography.PRIVATE_KEY
import com.example.owlpost.models.cryptography.PUBLIC_KEY
import com.example.owlpost.ui.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.security.spec.InvalidKeySpecException


class SettingsFragment: Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var resetEmailAlertDialog: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        mainActivity = activity as MainActivity
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mainActivity.drawer.disableDrawer()
        initFields()
        setListeners()
    }

    override fun onStop() {
        super.onStop()
        mainActivity.drawer.enableDrawer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null){
            try {
                when (requestCode){
                    CREATE_PUBLIC_KEYS_FILE_REQUEST_CODE -> {
                        mainActivity.settings.writeKeysToFile(
                            mainActivity.activeUser.email,
                            data.data as Uri,
                            PUBLIC_KEY
                        )
                        mainActivity.shortToast(mainActivity.getString(R.string.export_success))
                    }
                    CREATE_PRIVATE_KEYS_FILE_REQUEST_CODE -> {
                        mainActivity.settings.writeKeysToFile(
                            mainActivity.activeUser.email,
                            data.data as Uri,
                            PRIVATE_KEY
                        )
                        mainActivity.shortToast(mainActivity.getString(R.string.export_success))
                    }
                    PICK_PUBLIC_KEYS_FILE_REQUEST_CODE -> {
                        mainActivity.settings.readKeysFromFile(
                            mainActivity.activeUser.email,
                            data.data as Uri,
                            PUBLIC_KEY
                        )
                        mainActivity.shortToast(mainActivity.getString(R.string.import_success))
                    }
                    PICK_PRIVATE_KEYS_FILE_REQUEST_CODE -> {
                        mainActivity.settings.readKeysFromFile(
                            mainActivity.activeUser.email,
                            data.data as Uri,
                            PRIVATE_KEY
                        )
                        mainActivity.shortToast(mainActivity.getString(R.string.import_success))
                    }
                }
            }
            catch (e: InvalidKeySpecException){
                mainActivity.shortToast(mainActivity.getString(R.string.import_error))
            }
            catch (e: FileNotFoundException){
                mainActivity.shortToast(mainActivity.getString(R.string.import_export_error))
            }
        }
    }

    private fun initFields() {
        resetEmailAlertDialog = createConfirmAlertDialog(mainActivity)
        resetEmailAlertDialog.setMessage(getString(R.string.reset_dialog_message))
        resetEmailAlertDialog.setPositiveButton(getString(R.string.dialog_yes)) { _: DialogInterface, _: Int ->
            CoroutineScope(Dispatchers.Main).launch {
                if (mainActivity.mailbox.resetMailbox()){
                    mainActivity.settings.removeActiveUser(true)
                    mainActivity.supportFragmentManager.popBackStack()
                }
                else
                    mainActivity.shortToast(mainActivity.getString(R.string.cannot_reset))
            }
        }
    }

    private fun showFileCreateIntent(requestCode: Int, filename: String = "owlKeys") {
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/octet-stream"
            intent.putExtra(Intent.EXTRA_TITLE, "$filename.okeys")
            startActivityForResult(intent, requestCode)
        }
    }

    private fun showFilePickerIntent(requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/octet-stream"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(
                Intent.createChooser(intent, "Choose File"),
                requestCode
            )
        }
    }

    private fun setListeners() {
        export_public_keys.setOnClickListener {
            showFileCreateIntent(CREATE_PUBLIC_KEYS_FILE_REQUEST_CODE, "owlPostPublicKeys")
        }

        export_private_keys.setOnClickListener {
            showFileCreateIntent(CREATE_PRIVATE_KEYS_FILE_REQUEST_CODE, "owlPostPrivateKeys")
        }

        import_public_keys.setOnClickListener {
            showFilePickerIntent(PICK_PUBLIC_KEYS_FILE_REQUEST_CODE)
        }
        import_private_keys.setOnClickListener {
            showFilePickerIntent(PICK_PRIVATE_KEYS_FILE_REQUEST_CODE)
        }
        // Remove email and reset data
        remove_reset.setOnClickListener {
            resetEmailAlertDialog.show()
        }
    }
}