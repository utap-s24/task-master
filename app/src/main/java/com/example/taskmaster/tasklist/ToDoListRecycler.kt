package com.example.taskmaster.tasklist

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.OnClickListener
import com.example.taskmaster.R
import com.example.taskmaster.data.Note
import com.example.taskmaster.databinding.TaskRowBinding
import com.example.taskmaster.SharedPreferences

class ToDoListRecyclerAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private val notesList: ArrayList<Note>,
    private val onClickListener: OnClickListener
) : ListAdapter<Note, ToDoListRecyclerAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = TaskRowBinding.bind(itemView)
        fun bind(note: Note) {
            with(binding) {
                tvTitle.text = note.title
                tvDueDate.text = note.date
                taskCategory.text = note.category
                priorityText.text = note.priority
                clItem.setOnClickListener {
                    onClickListener.onClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notesList[position])

    }

    override fun getItemCount(): Int {
        return notesList.size
    }

}

class DiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}