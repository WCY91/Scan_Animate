package com.example.cameratest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.Arrays


class ScanActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private var beepManager: BeepManager? = null
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text
            barcodeView!!.setStatusText(result.text)
            beepManager!!.playBeepSoundAndVibrate()

            //Added preview of scanned barcode
            val imageView = findViewById<ImageView>(R.id.barcodePreview)
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW))
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        val formats: Collection<BarcodeFormat> =
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView.getBarcodeView().decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeContinuous(callback)
        beepManager = BeepManager(this)
        val save_btn = findViewById<Button>(R.id.save)
        save_btn.setOnClickListener {
            barcodeView.barcodeView.cameraInstance.requestPreview(object : PreviewCallback {
                override fun onPreview(sourceData: SourceData) {
                    sourceData.cropRect = Rect(0, 0, 500, 500)
                    val bmp = sourceData.bitmap
                    try {
                        val dir =
                            File(Environment.getExternalStorageDirectory().path + "/MyCaptureDirectory")
                        dir.mkdirs()
                        val filepath = dir.path + String.format("/%d.jpg", System.currentTimeMillis())
                        val stream: OutputStream = FileOutputStream(filepath)
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                        //Make a Toast with cream cheese
                        val bagel = Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_SHORT)
                        bagel.show()
                    } catch (e: FileNotFoundException) {
                        Log.e("LOGTAG", e.message!!)
                    }
                }

                override fun onPreviewError(e: Exception?) {
                    TODO("Not yet implemented")
                }
            })
        }

    }

    override fun onResume() {
        super.onResume()
        barcodeView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView!!.pause()
    }

    fun pause(view: View?) {
        barcodeView!!.pause()
    }

    fun resume(view: View?) {
        barcodeView!!.resume()
    }

    fun triggerScan(view: View?) {
        barcodeView!!.decodeSingle(callback)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
