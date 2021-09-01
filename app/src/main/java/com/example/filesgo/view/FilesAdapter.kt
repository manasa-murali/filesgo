package com.example.filesgo.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.filesgo.R
import com.example.filesgo.model.Audio
import com.example.filesgo.model.FileData
import com.example.filesgo.model.Image
import com.example.filesgo.model.Video

class FilesAdapter(private var filesList: List<FileData>) :
    RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.file_item, parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileData = filesList[position]
        holder.fileNameTextView.text = fileData.name
        holder.filePathTextView.text = fileData.path
        when (fileData.fileType){
            is Audio -> {
                holder.durationParent.visibility = View.VISIBLE
                holder.durationValueTextView.text = fileData.fileType.duration.toString()
                holder.detailsButton.visibility = View.GONE
            }
            is Image -> {
                holder.durationParent.visibility = View.GONE
                holder.detailsButton.visibility = View.VISIBLE
            }
            is Video -> {
                holder.durationParent.visibility = View.VISIBLE
                holder.durationValueTextView.text = fileData.fileType.duration.toString()
                holder.detailsButton.visibility = View.GONE
            }
        }
    }
    fun submitList(itemsList: List<FileData>) {
        val newData = itemsList
        updateAdapter(itemsList)
        filesList = newData
    }
    private fun updateAdapter(newData: List<FileData>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return filesList.size
            }

            override fun getNewListSize(): Int {
                return newData.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return filesList[oldItemPosition].id == newData[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return filesList[oldItemPosition] == newData[newItemPosition]
            }
        }).dispatchUpdatesTo(this)
    }
    override fun getItemCount(): Int {
        return filesList.size
    }

    class FileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView  = itemView.findViewById(R.id.fileNameValue)
        val filePathTextView: TextView  = itemView.findViewById(R.id.filePathValue)
        val detailsButton: Button = itemView.findViewById(R.id.details_button)
        val durationParent: ConstraintLayout = itemView.findViewById(R.id.durationParent)
        val durationValueTextView: TextView = itemView.findViewById(R.id.durationValue)
    }
}