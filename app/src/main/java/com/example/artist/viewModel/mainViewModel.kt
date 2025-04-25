package com.example.artist.viewModel

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.artist.DrawingView
import com.example.artist.MainActivity
import com.example.artist.R
import java.io.IOException

class mainViewModel() : ViewModel() {

    fun showBrushSizeDialog(drawingView: DrawingView, context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.brush_size, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.brushSizeSeekBar)
        val sizeValue = dialogView.findViewById<TextView>(R.id.brushSizeValue)

        seekBar.progress = drawingView.getCurrentBrushSize().toInt()
        sizeValue.text = "${seekBar.progress} px"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sizeValue.text = "$progress px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.okButton).setOnClickListener {
            drawingView.setBrushSize(seekBar.progress.toFloat())
            dialog.dismiss()
        }

        dialog.show()
    }

    fun saveDrawingToGallery(drawingView: DrawingView, context: Context, activity: MainActivity) {
        hasPermissions(context, PERMISSIONS_LIST, activity)

        val bitmap = drawingView.getBitmapFromView()

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            try {
                activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    Toast.makeText(context, "Рисунок сохранен в галерею", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    fun hasPermissions(context: Context, permissions: Array<String>, activity: MainActivity) {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity, permissions, 1
                    )
                }
            }
        }
    }
}