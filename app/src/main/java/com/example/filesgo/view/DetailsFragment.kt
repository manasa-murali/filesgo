package com.example.filesgo.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.filesgo.R
import com.example.filesgo.model.FileType
import com.example.filesgo.viewModel.FileSearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.*


@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    val viewModel: FileSearchViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.detailsState.observe(viewLifecycleOwner, {
            view.findViewById<TextView>(R.id.file_name).text =
                getString(R.string.file_name_tag, it.name)
            view.findViewById<TextView>(R.id.file_size).text =
                getString(R.string.file_size_tag, bytesConversion(it.size))
            view.findViewById<TextView>(R.id.date_created).text =
                getString(R.string.file_created_tag, dateConversion(it.dateCreated))
            view.findViewById<TextView>(R.id.date_modified).text =
                getString(R.string.file_modified_tag, dateConversion(it.dateModified))

            val fileTypeTextView = view.findViewById<TextView>(R.id.file_type)
            when (it.fileType) {
                is FileType.Image -> {
                    Glide.with(this@DetailsFragment)
                        .load("file://${it.path}")
                        .centerInside()
                        .into(view.findViewById(R.id.imageview))
                    fileTypeTextView.visibility = View.GONE
                }
                else -> {
                    fileTypeTextView.visibility = View.VISIBLE
                    fileTypeTextView.text = it.extension
                }
            }
        })
    }

    //Reference: https://stackoverflow.com/questions/3758606/how-can-i-convert-byte-size-into-a-human-readable-format-in-java
    private fun bytesConversion(value: Long): String {
        var bytes = value
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }

    private fun dateConversion(value: Long): String {
        return Date(value * 1000).toString()
    }

}
