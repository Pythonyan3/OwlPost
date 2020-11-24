package com.example.owlpost.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.owlpost.MainActivity
import com.example.owlpost.R
import com.example.owlpost.SendMailActivity
import com.example.owlpost.databinding.FragmentMailboxBinding
import com.example.owlpost.models.MailboxFolderException
import com.example.owlpost.models.SettingsException
import com.example.owlpost.models.email.OwlMessage
import com.example.owlpost.ui.SEND_EMAIL_REQUEST_CODE
import com.example.owlpost.ui.adapters.OnMessageItemClickListener
import com.example.owlpost.ui.adapters.RecyclerMessageItemAdapter
import com.example.owlpost.ui.shortToast
import kotlinx.android.synthetic.main.fragment_mailbox.*
import kotlinx.coroutines.*
import javax.mail.MessagingException

class MailboxFragment: Fragment() {
    private val messages = ArrayList<OwlMessage?>()
    private var isLoading = false
    private lateinit var syncJob: Job
    private lateinit var loadJob: Job
    private lateinit var binding: FragmentMailboxBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMailboxBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mainActivity = activity as MainActivity
        try{
            initViews()
            mainActivity.updateActiveUser()
        }
        catch (e: MessagingException) {
            mainActivity.shortToast(getString(R.string.internet_connection))
        }
        catch (e: SettingsException) {
            mainActivity.toolbar.title = mainActivity.getString(R.string.app_name)
            mainActivity.startAddEmailActivity()
        }
    }

    private fun initViews() {
        swipe_refresh.setColorSchemeColors(
            ResourcesCompat.getColor(
                mainActivity.resources,
                R.color.colorAccent,
                null
            )
        )

        swipe_refresh.setOnRefreshListener {
            if (!this::syncJob.isInitialized || (syncJob.isCompleted || syncJob.isCancelled))
                if (this::loadJob.isInitialized)
                    syncJob = CoroutineScope(Dispatchers.Main).launch{
                        loadJob.join()
                        if (mainActivity.mailbox.syncFolder()){
                            println("GOT CHANgES DO RESET")
                            resetMail()
                        }
                        else
                            println("NO changes")
                        swipe_refresh.isRefreshing = false
                    }
        }

        newMailActionButton.setOnClickListener{
            val activityMain = activity as MainActivity
            val intent = Intent(activityMain, SendMailActivity::class.java)
            intent.putExtra("email", activityMain.activeUser.email)
            intent.putExtra("password", activityMain.activeUser.password)
            activityMain.startActivityForResult(intent, SEND_EMAIL_REQUEST_CODE)
        }

        val adapter = RecyclerMessageItemAdapter(messages, mainActivity)
        adapter.onMessageItemClickListener = object : OnMessageItemClickListener{
            override fun onItemClick(message: OwlMessage) {
                mainActivity.selectedMessage = message
                mainActivity.supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, MessageFragment())
                    .commit()
            }
        }

        messageItems?.adapter = adapter
        messageItems?.layoutManager = LinearLayoutManager(this.context)
        messageItems.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == messages.size - 1){
                    if (!isLoading && (loadJob.isCompleted || loadJob.isCancelled)){
                        isLoading = true
                        readMessages(messages.size)
                        isLoading = false
                    }
                }
            }
        })
    }

    fun resetMail(){
        if (this::loadJob.isInitialized && loadJob.isActive)
            loadJob.cancel()
        messages.clear()
        readMessages()
    }

    private fun readMessages(offset: Int = 0, msgCount: Int = 10){
        loadJob = CoroutineScope(Dispatchers.Main).launch {
            messages.add(null)
            val progressBarPosition = messages.size - 1
            messageItems.adapter?.notifyDataSetChanged()
            try {
                withContext(Dispatchers.IO){
                    messages.addAll(mainActivity.mailbox.getMessages(offset, msgCount))
                }
            }
            catch (e: MailboxFolderException){ }
            messages.removeAt(progressBarPosition)
            messageItems.adapter?.notifyDataSetChanged()
        }
    }
}