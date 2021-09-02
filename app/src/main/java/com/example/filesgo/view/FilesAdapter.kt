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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.filesgo.R
import com.example.filesgo.model.Audio
import com.example.filesgo.model.FileData
import com.example.filesgo.model.Image
import com.example.filesgo.model.Video

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
        when (fileData.fileType) {
            is Audio -> {
                holder.durationParent.visibility = View.VISIBLE
                holder.durationValueTextView.text = convertSectoHMS(fileData.fileType.duration)
                holder.detailsButton.visibility = View.GONE
            }
            is Image -> {
                holder.durationParent.visibility = View.GONE
                holder.detailsButton.visibility = View.VISIBLE
                holder.detailsButton.setOnClickListener {
                    parentFragment.onImageClicked(fileData)
                }
            }
            is Video -> {
                holder.durationParent.visibility = View.VISIBLE
                holder.durationValueTextView.text = convertSectoHMS(fileData.fileType.duration)
                holder.detailsButton.visibility = View.GONE
            }
        }

    }

    private fun convertSectoHMS(duration: Int): String {
        val hours = duration / 3600
        val minutes = (hours) / 60
        val seconds = duration % 60
        val stringBuilder = StringBuilder()
        if (hours != 0) {
            stringBuilder.append(hours).append(" H:")
        }
        if (hours == 0 && minutes == 0) {
            stringBuilder.append(seconds).append(" S")
        } else {
            stringBuilder.append(minutes).append(" M:")
            stringBuilder.append(seconds).append(" S")
        }
        return stringBuilder.toString()
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
        val durationParent: ConstraintLayout = itemView.findViewById(R.id.durationParent)
        val durationValueTextView: TextView = itemView.findViewById(R.id.durationValue)
    }
}