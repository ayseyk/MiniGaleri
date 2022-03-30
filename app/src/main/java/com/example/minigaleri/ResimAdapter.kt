package com.example.minigaleri

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ResimAdapter(val context : Context, var liste : ArrayList<Uri?>, val itemClick : (position: Int) ->Unit, val itemLongClick: (position : Int)->Unit) : RecyclerView.Adapter<ResimViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResimViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.resim_liste, parent, false)
        return ResimViewHolder(v, itemClick,itemLongClick)
    }

    override fun onBindViewHolder(holder: ResimViewHolder, position: Int) {
        if(liste.first() == null) {
            liste.removeAt(0)
        }
        else {
            holder.bindData(liste.get(position))
        }
    }

    override fun getItemCount(): Int {
        return liste.size
    }

}