package com.example.cameratest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cameratest.databinding.ActivityRoomBinding
import com.example.cameratest.models.Message
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject


class RoomActivity : AppCompatActivity() {
    val binding by lazy { ActivityRoomBinding.inflate(layoutInflater) }
    lateinit var socket: Socket
    private lateinit var adapter: RoomMsgAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val serviceIntent = Intent(this, SocketApplication::class.java)
        startService(serviceIntent)
        socket = SocketApplication.get_private()
        socket.connect()
        val roomTitle = getIntent().getStringExtra("roomName")
        Toast.makeText(this,roomTitle,Toast.LENGTH_SHORT).show()
        binding.toolbar.title = roomTitle

        socket.emit("first_connect_room",roomTitle)
        socket.on("room_message",onMessage)
        setupEditTextMessage()
//        setupRecyclerViewMessage()
        setupRecyclerViewMessage()
        setupButtonSend(binding.etMessage.text.toString())

        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }

        binding.toolbar.setNavigationOnClickListener{
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
            socket.emit("leave_the_room", roomTitle)
        }
        SocketApplication.startHeartbeat(socket)
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
        adapter = RoomMsgAdapter()
        binding.recyclerMessage.apply {
            adapter = this@RoomActivity.adapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@RoomActivity, RecyclerView.VERTICAL, false)
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

        socket.emit("room_msg_receiver",JSONObject().apply {
            put("room", binding.toolbar.title.toString())
            put("message", message)
        })
        adapter.addItem(Message(message = message, isFromSender = true))
    }
    override fun onDestroy() {
        super.onDestroy()
        SocketApplication.stopHeartbeat()
        socket.disconnect()
        socket.off("room_message",onMessage)

    }
}