package com.example.mane_kelsa

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: WorkerAdapter
    private var workerList = ArrayList<Worker>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewWorkers)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        val searchView = findViewById<SearchView>(R.id.searchView)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WorkerAdapter(workerList)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddWorker).setOnClickListener { showAddDialog() }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean { adapter.filter(q ?: ""); return true }
            override fun onQueryTextChange(q: String?): Boolean { adapter.filter(q ?: ""); return true }
        })

        listenForWorkers()
    }

    private fun listenForWorkers() {
        db.collection("workers").whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val list = snapshots.toObjects(Worker::class.java)
                    workerList.clear()
                    workerList.addAll(list)
                    workerList.sortBy { it.area }
                    tvEmptyState.visibility = View.GONE
                    adapter.updateList(workerList)
                } else {
                    loadSampleData()
                }
            }
    }

    private fun loadSampleData() {
        workerList.clear()
        workerList.add(Worker("s1", "Ramesh (Sample)", "9876543210", "Electrician", "Rajajinagar", true, 10))
        workerList.add(Worker("s2", "Suresh (Sample)", "8877665544", "Plumber", "Indiranagar", true, 5))
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = "No cloud data. Showing samples."
        adapter.updateList(workerList)
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_worker, null)
        AlertDialog.Builder(this).setTitle("Register Worker").setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = view.findViewById<TextInputEditText>(R.id.etName).text.toString()
                val skill = view.findViewById<TextInputEditText>(R.id.etSkill).text.toString()
                if (name.isNotEmpty()) {
                    db.collection("workers").add(Worker(name = name, skill = skill, isAvailable = true))
                }
            }.show()
    }
}
