package com.example.paluego.view.activities


import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paluego.adpter.NotesAdapter
import com.example.paluego.databinding.ActivityNotesBinding
import com.example.paluego.model.AppPreferences
import com.example.paluego.model.Constant.AUTOSAVE_INTERVAL
import com.example.paluego.model.Constant.COLLECTION_NOTES
import com.example.paluego.model.Constant.COLLECTION_USER
import com.example.paluego.model.NoteItem
import com.example.paluego.model.SwipeToDeleteCallback
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import com.example.paluego.R
import com.example.paluego.model.Constant
import com.example.paluego.model.Constant.AUDIO
import com.example.paluego.model.Constant.CONTENT
import com.example.paluego.model.Constant.DESCRIPTION
import com.example.paluego.model.Constant.ID
import com.example.paluego.model.Constant.TITLE

class NotesActivity : AppCompatActivity(), NotesAdapter.ItemClickListener {

    private val binding by lazy{
        ActivityNotesBinding.inflate(layoutInflater)
    }

    private var backPressedOnce = false
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var originalNotesList: MutableList<NoteItem>
    private lateinit var filteredNotesList: MutableList<NoteItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogOut.setOnClickListener {
            showExitConfirmationDialog()
        }

        binding.btnAddNote.setOnClickListener{
            startActivity(Intent(this@NotesActivity, SingleNoteActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this@NotesActivity, SettingsActivity::class.java))
        }

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    filterNotes(it)
                }
                return true
            }
        })
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        //Limpiamos el searchView colapsándolo y expandiéndolo, pierde el foco con el tercer método
        with(binding.searchView){
            isIconified = true
            isIconified = false
            clearFocus()
        }
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        val userEmail = AppPreferences.getString(this, Constant.EMAIL_KEY, "")

        db = FirebaseFirestore.getInstance() // Declarar la variable db en la clase

        db.collection(COLLECTION_NOTES)
            .whereEqualTo(COLLECTION_USER, userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val notesList = querySnapshot.documents.mapNotNull { document ->
                    val title = document.getString(TITLE)
                    val description = document.getString(CONTENT)
                    val id = document.getString(ID)
                    val audio = document.getString(AUDIO)
                    if (id != null && title != null && description != null) {
                        NoteItem(id, title, description, !audio.isNullOrEmpty())
                    } else {
                        null
                    }
                }.toMutableList()

                notesAdapter = NotesAdapter(notesList , this)
                originalNotesList = notesList.toMutableList()
                filteredNotesList = originalNotesList.toMutableList()
                binding.recyclerViewNotes.apply {
                    layoutManager = LinearLayoutManager(this@NotesActivity)
                    adapter = notesAdapter
                }
                val swipeToDeleteCallback = SwipeToDeleteCallback(notesAdapter) { position: Int ->
                    val noteId = notesAdapter.getNoteId(position)

                    db.collection(COLLECTION_NOTES)
                        .document(noteId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@NotesActivity, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
                            notesAdapter.removeItem(position)
                            setupRecyclerView()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("JRB", "Error deleting note: ${exception.message}")
                            Toast.makeText(this@NotesActivity, getString(R.string.failed_to_delete_note), Toast.LENGTH_SHORT).show()
                        }
                }

                val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
                itemTouchHelper.attachToRecyclerView(binding.recyclerViewNotes)
            }
            .addOnFailureListener { exception ->
                Log.d("JRB", "Error al obtener las notas: ${exception.message}")
            }
    }

    private fun filterNotes(query: String) {
        if(::filteredNotesList.isInitialized) filteredNotesList.clear()


        if (query.isEmpty()) {
            filteredNotesList.addAll(originalNotesList)
        } else {
            val searchQuery = query.lowercase()
            for (note in originalNotesList) {
                if (note.title.lowercase().contains(searchQuery) || note.description.lowercase().contains(searchQuery)) {
                    filteredNotesList.add(note)
                }
            }
        }

        notesAdapter.updateData(filteredNotesList)
    }




    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.exit))
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_exit))
        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
            AppPreferences.clearAllPreferences(this)
            finish()
            startActivity(Intent(this@NotesActivity, LoginActivity::class.java))
        }
        builder.setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, i: Int ->
            Toast.makeText(this, getString(R.string.you_clicked_no), Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onBackPressed() {
        if (backPressedOnce) {
            finish()
        } else {
            backPressedOnce = true
            Toast.makeText(this, getString(R.string.press_back_again_to_exit_the_app), Toast.LENGTH_SHORT).show()

            // Reset backPressedOnce after a delay
            Handler().postDelayed({ backPressedOnce = false }, 2000)
        }
    }

    override fun onItemClick(note: NoteItem) {
        val intento = Intent(this@NotesActivity, SingleNoteActivity::class.java)
        intento.putExtra(ID, note.id)
        intento.putExtra(TITLE, note.title)
        intento.putExtra(DESCRIPTION, note.description)
        startActivity(intento)
    }


}