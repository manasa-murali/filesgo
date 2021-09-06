package com.example.filesgo.view

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filesgo.R
import com.example.filesgo.model.FileData

class FilesAdapter(
    private var filesList: List<FileData>,
    private var ITalkToFragment: ITalkToFragment,
    private var searchString: String,
) :
    RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.file_item, parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val context = holder.itemView.context
        val file = filesList[position]
        holder.fileNameTextView.text = context.getString(R.string.file_name_tag, file.name)
        holder.filePathTextView.text = context.getString(R.string.file_path_tag, file.path)

        val startIndex = file.name.lowercase().indexOf(searchString)
        if (startIndex != -1) {
            val editableText = SpannableStringBuilder()
            editableText.append(file.name)
            editableText.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                startIndex + searchString.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.fileNameTextView.text = editableText
        }
        holder.detailsButton.setOnClickListener {
            ITalkToFragment.onItemClicked(file)
        }
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    fun submitItems(filesList: List<FileData>, searchString: String) {
        this.filesList = filesList
        this.searchString = searchString
        notifyDataSetChanged()
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.file_name)
        val filePathTextView: TextView = itemView.findViewById(R.id.file_path)
        val detailsButton: Button = itemView.findViewById(R.id.details_button)
    }
}
