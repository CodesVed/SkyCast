package com.example.weatherapp.adapters

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.models.Coord

class MyAdapter(val context: Activity, val cityList: ArrayList<Coord>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(){

    private lateinit var myListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: OnItemClickListener){
        myListener = listener
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val city = itemView.findViewById<TextView>(R.id.cityName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.city_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val currentItem = cityList[position]
        Log.d("Adapter", "MyAdapter: Binding city ${currentItem.name} at position $position")
        holder.city.text = "${currentItem.name},${currentItem.state}, ${currentItem.country}"

        holder.itemView.setOnClickListener {
            Log.d("Adapter", "Item clicked at position: $position")
            if (::myListener.isInitialized){
                myListener.onItemClick(position)
            } else {
                Log.e("Adapter", "Listener not initialized")
            }
        }
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    fun updateData(newList: List<Coord>){
        cityList.clear()
        cityList.addAll(newList)
        notifyDataSetChanged()
    }
}