package com.example.assignment.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.Model.Todo
import com.example.assignment.R

class TodoAdapter(
    private val todos: List<Todo>,
    private val onDeleteClick: (Todo) -> Unit,
    private val onCheckChange: (Todo, Boolean) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.text_view_title)
        val completed: CheckBox = itemView.findViewById(R.id.check_box_completed)
        val deleteButton: ImageView = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.title.text = todo.todo
        holder.completed.isChecked = todo.completed
        holder.deleteButton.setOnClickListener { onDeleteClick(todo) }
        holder.completed.setOnCheckedChangeListener { _, isChecked ->
            onCheckChange(todo, isChecked)
        }
    }

    override fun getItemCount(): Int = todos.size
}
