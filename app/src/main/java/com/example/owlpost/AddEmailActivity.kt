package com.example.owlpost

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.owlpost.models.IMAPWrapper
import com.example.owlpost.models.Settings
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.User
import com.example.owlpost.ui.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_mail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException


class AddEmailActivity : AppCompatActivity() {
    private lateinit var setting: Settings
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mail)
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
                showLoading(loadingDialog)
                val imap = IMAPWrapper(email, password)
                val store = imap.getStore()
                CoroutineScope(Dispatchers.Main).launch{
                    try{
                        withContext(Dispatchers.IO){
                            store.connect(imap.emailHost, email, password)
                            store.close()
                            setting.addUser(User(email, password))
                        }
                        setResult(RESULT_OK)
                        this@AddEmailActivity.finish()
                    }
                    catch (e: SettingsException) {
                        shortToast(getString(R.string.email_already_exists))
                    }
                    catch (e: AuthenticationFailedException) {
                        shortToast(getString(R.string.auth_error))
                    }
                    catch (e: MessagingException){
                        Snackbar.make(
                            this@AddEmailActivity.
                            addEmailLayout,
                            getString(R.string.internet_connection),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    finally {
                        hideLoading(loadingDialog)
                    }
                }
            }
        }
    }


}