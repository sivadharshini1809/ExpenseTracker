package com.example.expensetracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.expensetracker.databinding.ActivityScanBinding
import com.example.expensetracker.util.BillParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class ScanActivity : AppCompatActivity() {

    private lateinit var b: ActivityScanBinding
    private var imageCapture: ImageCapture? = null

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityScanBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera() else permLauncher.launch(Manifest.permission.CAMERA)

        b.btnCapture.setOnClickListener { capture() }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(b.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            provider.unbindAll()
            provider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capture() {
        val file = File(cacheDir, "bill_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        b.btnCapture.isEnabled = false
        imageCapture?.takePicture(
            options, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(r: ImageCapture.OutputFileResults) = readText(file)
                override fun onError(e: ImageCaptureException) {
                    b.btnCapture.isEnabled = true
                    Toast.makeText(this@ScanActivity, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun readText(file: File) {
        val image = InputImage.fromFilePath(this, Uri.fromFile(file))
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { result ->
                val r = BillParser.parse(result.text)
                startActivity(
                    Intent(this, AddExpenseActivity::class.java)
                        .putExtra("amount", r.amount)
                        .putExtra("merchant", r.merchant)
                        .putExtra("date", r.date)
                )
                finish()
            }
            .addOnFailureListener {
                b.btnCapture.isEnabled = true
                Toast.makeText(this, "Couldn't read the bill. Try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
