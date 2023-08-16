package com.example.cameratest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

open class RoomAdapter (
    private val context: Context,
    private var list: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var items = ArrayList<String>(list)
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_room,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = items[position]

        if (holder is MyViewHolder) {

            holder.itemView.findViewById<TextView>(R.id.RoomName).text = model


            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    fun clearIem(){
        this.items.clear()
    }

    fun addItem(item: String) {
        this.items.add(item)
        Log.e("room notify",this.items.toString())
        notifyDataSetChanged()
    }
    fun setOnClickListener(onClickListener: RoomAdapter.OnClickListener?) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: String)
    }
    fun setOnItemClickListener(listener: OnClickListener) {
        this.onClickListener = listener
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}