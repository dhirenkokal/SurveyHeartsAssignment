package com.example.assignment.Activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.example.assignment.Api.RetrofitInstance
import com.example.assignment.Model.Todo
import com.example.assignment.Adapter.TodoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var progressBar: View
    private var todos = mutableListOf<Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        todoAdapter = TodoAdapter(todos, ::deleteTodo, ::updateTodo)
        recyclerView.adapter = todoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.add_task_button).setOnClickListener {
            showAddTaskDialog()
        }

        if (isInternetAvailable()) {
            fetchTodos()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val taskInput = dialogView.findViewById<TextInputEditText>(R.id.et_task)
        val addButton = dialogView.findViewById<Button>(R.id.add_button)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            val taskText = taskInput.text.toString()
            if (taskText.isNotEmpty()) {
                addTask(taskText)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addTask(taskText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newTodo = Todo(
                    id = 0,
                    todo = taskText,
                    completed = true,
                    userId = 1
                )
                val response = RetrofitInstance.api.addTodo(newTodo)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            todos.add(it)
                            todoAdapter.notifyItemInserted(todos.size - 1)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Error adding task", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error adding task: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchTodos() {
        showProgressBar()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getTodos(limit = 30, skip = 0)
                withContext(Dispatchers.Main) {
                    todos.clear()
                    todos.addAll(response.todos)
                    todoAdapter.notifyDataSetChanged()
                    hideProgressBar()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    Toast.makeText(this@MainActivity, "Error fetching todos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTodo(todo: Todo) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitInstance.api.deleteTodo(todo.id)
                withContext(Dispatchers.Main) {
                    todos.remove(todo)
                    todoAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error deleting todo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTodo(todo: Todo, isChecked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedTodo = todo.copy(completed = isChecked)
                RetrofitInstance.api.updateTodo(todo.id, updatedTodo)
                withContext(Dispatchers.Main) {
                    val index = todos.indexOf(todo)
                    if (index != -1) {
                        todos[index] = updatedTodo
                        todoAdapter.notifyItemChanged(index)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error updating todo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}