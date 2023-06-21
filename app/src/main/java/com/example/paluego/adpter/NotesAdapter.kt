package com.example.paluego.adpter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.paluego.R
import com.example.paluego.model.Constant.COLLECTION_NOTES
import com.example.paluego.model.NoteItem
import com.google.firebase.firestore.FirebaseFirestore

class NotesAdapter(private var notesList: MutableList<NoteItem>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

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

    fun getNoteId(position: Int): String {
        return notesList[position].id
    }

    fun updateData(newData: List<NoteItem>) {
        // Limpiar la lista actual
        notesList.clear()
        // Agregar los nuevos elementos
        notesList.addAll(newData)
        // Notificar al RecyclerView que los datos han cambiado
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        val note = notesList[position]

        // Eliminar el documento de Firebase
        val db = FirebaseFirestore.getInstance()
        db.collection(COLLECTION_NOTES)
            .whereEqualTo("title", note.title)
            .whereEqualTo("description", note.description)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Eliminar el elemento de la lista
                            val updatedList = ArrayList(notesList)
                            updatedList.removeAt(position)
                            notesList = updatedList
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener { exception ->
                            Log.d("JRB", "Error al eliminar la nota: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("JRB", "Error al obtener el documento: ${exception.message}")
            }
    }

    fun deleteItem(position: Int) {
        if (position in notesList.indices) {
            val deletedNoteId = notesList[position].id
            removeItem(position)
            notifyItemRemoved(position)

            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_NOTES).document(deletedNoteId)
                .delete()
                .addOnSuccessListener {
                    Log.d("JRB", "Documento eliminado exitosamente")
                }
                .addOnFailureListener { exception ->
                    Log.d("JRB", "Error al eliminar el documento: ${exception.message}")
                }
            notifyDataSetChanged()
        }
    }


}