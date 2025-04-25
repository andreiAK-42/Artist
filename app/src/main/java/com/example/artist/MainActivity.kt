package com.example.artist


import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.artist.viewModel.mainViewModel
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {
    private val mainViewModel: mainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()
    }

    fun setListener() {
        val chip_palette = findViewById<Chip>(R.id.chip_palette)
        val chip_brush = findViewById<Chip>(R.id.chip_brush)
        val chip_save = findViewById<Chip>(R.id.chip_save)
        val chip_import = findViewById<Chip>(R.id.chip_import)
        val drawingView = findViewById<DrawingView>(R.id.drawingView)

        chip_palette.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)                // Pass Activity Instance
                .setTitle("Выбери цвет")             // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)    // Default ColorShape.CIRCLE
                .setColorSwatch(ColorSwatch._300)    // Default ColorSwatch._500
                .setDefaultColor(Color.WHITE.toInt()) // Pass Default Color
                .setColorListener { color, colorHex ->
                    drawingView.setBrushColor(color)
                }
                .show()
        }

        chip_import.setOnClickListener {
            pickImageFromGallery()
        }

        chip_save.setOnClickListener {
            mainViewModel.saveDrawingToGallery(drawingView, this, this)
        }

        chip_brush.setOnClickListener {
            mainViewModel.showBrushSizeDialog(drawingView, this)
        }
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
        private const val PICK_IMAGE_REQUEST = 102
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                findViewById<DrawingView>(R.id.drawingView).setBackgroundFromBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        }
    }
}