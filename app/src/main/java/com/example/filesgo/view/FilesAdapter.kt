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
    private var parentFragment: FileSearchFragment,
) :
    RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    private var searchString: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.file_item, parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileData = filesList[position]
        holder.fileNameTextView.text = fileData.name
        holder.filePathTextView.text = fileData.path
        if (!searchString.isNullOrEmpty()) {
            val startIndex = fileData.name.indexOf(searchString)
            if (startIndex != -1) {
                val editableText = SpannableStringBuilder()
                editableText.append(fileData.name)
                editableText.setSpan(BackgroundColorSpan(Color.BLUE),
                    startIndex,
                    startIndex + searchString.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.fileNameTextView.text = editableText
            }
        }
        holder.detailsButton.setOnClickListener {
            parentFragment.onItemClicked(fileData)
        }

    }



    fun setAdapterData(
        filesList: List<FileData>,
        searchString: String,
        fileSearchFragment: FileSearchFragment,
    ) {
        this.filesList = filesList
        this.searchString = searchString
        this.parentFragment = fileSearchFragment
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameValue)
        val filePathTextView: TextView = itemView.findViewById(R.id.filePathValue)
        val detailsButton: Button = itemView.findViewById(R.id.details_button)
    }
}