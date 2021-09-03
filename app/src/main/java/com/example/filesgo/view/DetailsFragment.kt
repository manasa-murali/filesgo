package com.example.filesgo.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.filesgo.MainActivity
import com.example.filesgo.R
import com.example.filesgo.model.Audio
import com.example.filesgo.model.Image
import com.example.filesgo.model.Video
import com.example.filesgo.utils.Constants
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.*


@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var viewModel: FileSearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel
        lifecycleScope.launchWhenCreated {
            viewModel.uiDataFlow.collect { appState ->
                when (appState.uiState) {
                    is MyUIState.Failure -> {
                    }
                    MyUIState.Initial -> {
                    }
                    MyUIState.Loading -> {
                    }
                    is MyUIState.Success -> {
                        if (appState.fileDetails != null) {
                            when (appState.fileDetails.fileType) {
                                is Audio -> {
                                    view.findViewById<ImageView>(R.id.imageview).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.dimensionParent).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.durationParent).visibility =
                                        View.VISIBLE
                                    view.findViewById<ConstraintLayout>(R.id.albumParent).visibility =
                                        View.VISIBLE

                                    val audioData = appState.fileDetails.fileType
                                    val date =
                                        Date(audioData.dateCreated.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_added_value).text = date
                                    val dateModified =
                                        Date(audioData.dateModified.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_modified_value).text =
                                        dateModified
                                    view.findViewById<TextView>(R.id.durationValue).text =
                                        Constants.convertSectoHMS(audioData.duration)
                                    view.findViewById<TextView>(R.id.albumValue).text =
                                        audioData.album

                                }
                                is Image -> {
                                    view.findViewById<ConstraintLayout>(R.id.durationParent).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.albumParent).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.dimensionParent).visibility =
                                        View.VISIBLE
                                    val imageData = appState.fileDetails.fileType
                                    val dateAdded =
                                        Date(imageData.dateCreated.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_added_value).text =
                                        dateAdded
                                    val dateModified =
                                        Date(imageData.dateModified.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_modified_value).text =
                                        dateModified
                                    view.findViewById<TextView>(R.id.widthValue).text =
                                        imageData.width.toString()
                                    view.findViewById<TextView>(R.id.heightValue).text =
                                        imageData.height.toString()


                                    val uri = "file://" + (appState.fileDetails.path)
                                    val file = File(appState.fileDetails.path)
                                    val imageView = view.findViewById<ImageView>(R.id.imageview)
                                    if (file.exists()) {
                                        Glide.with(this@DetailsFragment)
                                            .load(uri)
                                            .centerInside()
                                            .into(imageView)
                                    } else {
                                        view.findViewById<ImageView>(R.id.imageview)
                                            .setImageResource(R.drawable.ic_baseline_camera_alt_24)
                                    }
                                }
                                is Video -> {
                                    view.findViewById<ConstraintLayout>(R.id.dimensionParent).visibility =
                                        View.GONE
                                    view.findViewById<ImageView>(R.id.imageview).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.albumParent).visibility =
                                        View.GONE
                                    view.findViewById<ConstraintLayout>(R.id.durationParent).visibility =
                                        View.VISIBLE
                                    val videoData = appState.fileDetails.fileType
                                    val date =
                                        Date(videoData.dateCreated.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_added_value).text = date

                                    val dateModified =
                                        Date(videoData.dateModified.toLong() * 1000).toString()
                                    view.findViewById<TextView>(R.id.date_modified_value).text =
                                        dateModified

                                    view.findViewById<TextView>(R.id.durationValue).text =
                                        Constants.convertSectoHMS(videoData.duration)

                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
