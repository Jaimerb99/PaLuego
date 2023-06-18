package com.example.paluego.adpter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.paluego.R
import com.example.paluego.model.NoteItem

class NotesAdapter(private val notesList: List<NoteItem>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View, itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.itemNoteTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.itemNotDescription)
        val audioShow: ImageView = itemView.findViewById(R.id.imgSoundWave)

        fun bind(note: NoteItem) {
            titleTextView.text = note.title
            descriptionTextView.text = note.description

            itemView.setOnClickListener {
                itemClickListener.onItemClick(note)
            }
        }



    }

    interface ItemClickListener {
        fun onItemClick(note: NoteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView, itemClickListener)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]
        if(note.audio){
            holder.audioShow.visibility = View.VISIBLE
        } else{
            holder.audioShow.visibility = View.GONE
        }
        holder.bind(note)


    }

    override fun getItemCount(): Int {
        return notesList.size
    }
}