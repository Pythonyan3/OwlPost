package com.example.owlpost

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.example.owlpost.ui.LoadingDialog
import com.example.owlpost.ui.hideLoading
import com.example.owlpost.ui.shortToast
import com.example.owlpost.ui.showLoading
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class AddEmailActivity : AppCompatActivity() {
    private lateinit var setting: Settings
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initFields()
        initViews()
    }

    private fun initFields() {
        setting = Settings(this)
        loadingDialog = LoadingDialog(this)
        loadingDialog.setTitle(getString(R.string.loading_title_add))
    }

    private fun initViews(){
        login_button.setOnClickListener{
            val email = user_email.text.toString()
            val password = user_password.text.toString()

            if (email.isEmpty() || password.isEmpty())
                shortToast(getString(R.string.empty_fields))
            else if (!isValidEmail(email)){
                shortToast(getString(R.string.incorrect_email))
            }
            else {
                CoroutineScope(Dispatchers.IO).launch{
                    try {
                        showLoading(loadingDialog)
                        setting.addUser(User(email, password))
                        setResult(RESULT_OK)
                        this@AddEmailActivity.finish()
                    }
                    catch (e: SettingsException) {
                        shortToast(getString(R.string.email_already_exists))
                    }
                    finally {
                        hideLoading(loadingDialog)
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}