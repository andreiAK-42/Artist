package com.example.artist

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.google.android.material.chip.Chip
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val chip_palette = findViewById<Chip>(R.id.chip_palette)
        val chip_brush = findViewById<Chip>(R.id.chip_brush)
        val chip_save = findViewById<Chip>(R.id.chip_save)
        val chip_import = findViewById<Chip>(R.id.chip_import)
        val drawingView = findViewById<DrawingView>(R.id.drawingView)

        chip_palette.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)                            // Pass Activity Instance
                .setTitle("Выбери цвет")                // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)    // Default ColorShape.CIRCLE
                .setColorSwatch(ColorSwatch._300)    // Default ColorSwatch._500
                .setDefaultColor(Color.WHITE.toInt())        // Pass Default Color
                .setColorListener { color, colorHex ->
                    drawingView.setBrushColor(color)
                }
                .show()
        }

        chip_import.setOnClickListener {
            pickImageFromGallery()
        }

        chip_save.setOnClickListener {
            saveDrawingToGallery()
        }

        chip_brush.setOnClickListener {
            showBrushSizeDialog()
        }

    }

    var PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this, permissions, 1
                    )
                    return true
                }
            }
        }
        return true
    }

    private fun saveDrawingToGallery() {
        // Проверка разрешений (для Android 6.0+)


        if (hasPermissions(this, PERMISSIONS_LIST) == false) {
            return
        }

        val drawingView = findViewById<DrawingView>(R.id.drawingView)
        val bitmap = drawingView.getBitmapFromView()

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    Toast.makeText(this, "Рисунок сохранен в галерею", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
        private const val PICK_IMAGE_REQUEST = 102
    }


    private fun showBrushSizeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.brush_size, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.brushSizeSeekBar)
        val sizeValue = dialogView.findViewById<TextView>(R.id.brushSizeValue)
        val drawingView = findViewById<DrawingView>(R.id.drawingView)

        seekBar.progress = drawingView.getCurrentBrushSize().toInt()
        sizeValue.text = "${seekBar.progress} px"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sizeValue.text = "$progress px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.okButton).setOnClickListener {
            drawingView.setBrushSize(seekBar.progress.toFloat())
            dialog.dismiss()
        }

        dialog.show()
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