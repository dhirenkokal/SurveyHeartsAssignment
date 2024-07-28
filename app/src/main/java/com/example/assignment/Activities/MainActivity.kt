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

        initViews()
        setupRecyclerView()

        findViewById<FloatingActionButton>(R.id.add_task_button).setOnClickListener {
            showAddTaskDialog()
        }

        if (isInternetAvailable()) {
            fetchTodos()
        } else {
            showToast("No internet connection")
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(todos, ::deleteTodo, ::updateTodo)
        recyclerView.adapter = todoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
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
                showToast("Task cannot be empty")
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
                    handleAddTaskResponse(response.body(), response.isSuccessful)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error adding task: ${e.message}")
                }
            }
        }
    }

    private fun handleAddTaskResponse(newTodo: Todo?, isSuccessful: Boolean) {
        if (isSuccessful && newTodo != null) {
            todos.add(newTodo)
            todoAdapter.notifyItemInserted(todos.size - 1)
        } else {
            showToast("Error adding task")
        }
    }

    private fun fetchTodos() {
        showProgressBar()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getTodos(limit = 30, skip = 0)
                withContext(Dispatchers.Main) {
                    handleFetchTodosResponse(response.todos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    showToast("Error fetching todos")
                }
            }
        }
    }

    private fun handleFetchTodosResponse(fetchedTodos: List<Todo>) {
        todos.clear()
        todos.addAll(fetchedTodos)
        todoAdapter.notifyDataSetChanged()
        hideProgressBar()
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
                    showToast("Error deleting todo")
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
                    handleUpdateTodoResponse(todo, updatedTodo)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error updating todo")
                }
            }
        }
    }

    private fun handleUpdateTodoResponse(todo: Todo, updatedTodo: Todo) {
        val index = todos.indexOf(todo)
        if (index != -1) {
            todos[index] = updatedTodo
            todoAdapter.notifyItemChanged(index)
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
