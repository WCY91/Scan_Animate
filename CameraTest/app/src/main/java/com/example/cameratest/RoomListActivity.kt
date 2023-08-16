package com.example.cameratest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cameratest.databinding.ActivityRoomListBinding
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class RoomListActivity : AppCompatActivity() {
    val binding by lazy { ActivityRoomListBinding.inflate(layoutInflater) }

    lateinit var socket : Socket
    private lateinit var adapter: RoomAdapter
    var roomList : ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        socket = SocketApplication.get_private()
        socket.connect()

        val actionBtn : ImageButton = findViewById(R.id.btn_add)
        val leaveBtn : ImageButton = findViewById(R.id.btn_leave)
        actionBtn.setOnClickListener{
            socket.emit("create_room",binding.groupName.text)
        }

        socket.on("return_room_list",onRoomList)

        socket.on("room_create_status",onRoomCreate)

        setupRecyclerViewMessage()
        SocketApplication.startHeartbeat(socket)
    }
    var onRoomCreate = Emitter.Listener { args ->
        val obj = JSONObject(args[0].toString())
        var text: String
        var flag = 0
        Thread(object : Runnable{
            override fun run() {
                runOnUiThread(Runnable {
                    kotlin.run {
                        text = "" + obj.get("name") + ": " + obj.get("message")
                        Log.e("text",obj.get("message").toString())
                        if(obj.get("message").toString().equals("sucess")){

                           val intent = Intent(this@RoomListActivity, RoomActivity::class.java)
                           intent.putExtra("roomName",binding.groupName.text.toString())
                           startActivity(intent)
                            finish()
                            Toast.makeText(this@RoomListActivity,"a new room create",Toast.LENGTH_SHORT).show()
                        }else if(obj.get("message").toString().equals("fail")){
                            Toast.makeText(this@RoomListActivity,"目前已有相同名稱的包廂喔",Toast.LENGTH_SHORT).show()
                        }

                    }
                })
            }
        }).start()

    }
    var onRoomList = Emitter.Listener { args ->
        val obj = JSONObject(args[0].toString())

        Thread(object : Runnable{
            override fun run() {
                runOnUiThread(Runnable {
                    kotlin.run {
                        val roomObj = obj.getJSONObject("roomList")
                        adapter.clearIem()
                        for (key in roomObj.keys()) {
                            adapter.addItem(key)
                        }
                        Log.e("room list",roomObj.toString())
                        adapter.notifyDataSetChanged()
                    }
                })
            }
        }).start()
    }


    private fun setupRecyclerViewMessage() {
        socket.emit("room_list")
        adapter = RoomAdapter(this,roomList)
        binding.roomCount.apply {
            adapter = this@RoomListActivity.adapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(this@RoomListActivity, RecyclerView.VERTICAL, false)
            (adapter as RoomAdapter).setOnItemClickListener(object : RoomAdapter.OnClickListener {
                override fun onClick(position: Int, model: String) {
                    socket.emit("connect_room",model)
                    val intent = Intent(this@RoomListActivity, RoomActivity::class.java)
                    intent.putExtra("roomName",model)
                    startActivity(intent)
                    finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        socket.disconnect()
        socket.off("return_room_list",onRoomList)

        socket.off("room_create_status",onRoomCreate)
        SocketApplication.stopHeartbeat()
    }
}