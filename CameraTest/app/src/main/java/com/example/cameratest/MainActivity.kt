package com.example.cameratest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cameratest.databinding.ActivityMainBinding
import com.example.cameratest.models.Message

import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var socket: Socket
    private lateinit var adapter: ChatMessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        socket = SocketApplication.get()
        socket.connect()

        val actionBtn : ImageButton = findViewById(R.id.btn_add)
        actionBtn.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Select Action")
            val pictureDialogItems =
                arrayOf("創建包廂", "新增私人訊息包廂")
            dialog.setItems(
                pictureDialogItems
            ) { dialog, which ->
                when (which) {
                    // Here we have create the methods for image selection from GALLERY
                    0 -> createRoom()
                    1 -> createPrivateRoom()
                }
            }
            dialog.show()
        }


        socket.on("get message",onMessage)
        setupEditTextMessage()
        setupRecyclerViewMessage()
        setupButtonSend(binding.etMessage.text.toString())

    }
    var onMessage = Emitter.Listener { args ->
        val obj = JSONObject(args[0].toString())
        var text: String
        Thread(object : Runnable{
            override fun run() {
                runOnUiThread(Runnable {
                    kotlin.run {
                        text = "" + obj.get("name") + ": " + obj.get("message")
                        Log.e("text",obj.get("message").toString())
                        handleOnMessageReceived(obj.get("message").toString())
                    }
                })
            }
        }).start()
    }
    private fun setupRecyclerViewMessage() {
        adapter = ChatMessageAdapter()
        binding.recyclerMessage.apply {
            adapter = this@MainActivity.adapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        }
    }
    private fun handleOnMessageReceived(message: String) {
        adapter.addItem(Message(message, false))
        binding.etMessage.setText("")
    }
    private fun setupEditTextMessage() {
        binding.etMessage.doAfterTextChanged {
            setupButtonSend(it.toString())
        }
    }

    private fun setupButtonSend(message: String) {
        binding.btnSend.isEnabled = message.isNotBlank()
        binding.btnSend.setOnClickListener { sendMessage(message) }
    }

    private fun sendMessage(message: String) {
        socket.emit("send message",message)
        adapter.addItem(Message(message = message, isFromSender = true))
    }
    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off("get message", onMessage)
    }

    fun createPrivateRoom(){}

    fun createRoom(){
        val intent = Intent(this, RoomListActivity::class.java)
        startActivity(intent)
        finish()
    }
}