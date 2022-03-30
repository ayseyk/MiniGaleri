package com.example.minigaleri

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class ResimViewHolder(itemView : View, itemClick : (position : Int)->Unit, itemLongClick: (position : Int)->Unit) :
    RecyclerView.ViewHolder(itemView) {

    var imageResim : ImageView

    init {
        imageResim = itemView.findViewById(R.id.imageListe)

        itemView.setOnClickListener { itemClick(adapterPosition) }
        itemView.setOnLongClickListener{
            itemLongClick(adapterPosition)
            return@setOnLongClickListener true
        }
    }

    fun bindData(resim : Uri?)
    {
        imageResim.setImageURI(resim)
    }
}