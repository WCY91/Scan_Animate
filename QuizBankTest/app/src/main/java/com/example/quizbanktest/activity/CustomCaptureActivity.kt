package com.example.quizbanktest.activity

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quizbanktest.R
import com.example.quizbanktest.utils.ConstantsFunction
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Arrays
import kotlin.math.roundToInt


class CustomCaptureActivity : AppCompatActivity() {

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this@CustomCaptureActivity, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this@CustomCaptureActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
            val barcodeBitmap = result.contents

        }
    }
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
            Log.e("image", result.getBitmapWithResultPoints(Color.YELLOW).toString())
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.quizbanktest.R.layout.activity_custom_capture)
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
        options.setPrompt("Scan a barcode")
        options.setCameraId(0) // Use a specific camera of the device
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setBarcodeImageEnabled(true)
//        barcodeLauncher.launch(options)
        barcodeView  = findViewById(com.example.quizbanktest.R.id.barcode_scanner)
        val formats: Collection<BarcodeFormat> =
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView.getBarcodeView().decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.viewFinder.setLaserVisibility(true)

        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeContinuous(callback)
        val save_btn = findViewById<Button>(R.id.save_photo)
//        val save_btn = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.save_photo)
        save_btn.setOnClickListener{
            Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_SHORT).show()
            barcodeView.barcodeView.cameraInstance.requestPreview(object : PreviewCallback {
                override fun onPreview(sourceData: SourceData) {
                    val r: Rect= Rect()
                    Log.e("measuredHeight",barcodeView.viewFinder.measuredHeight.toString())
                    Log.e("Height",barcodeView.viewFinder.height.toString())
                    Log.e("minimumHeight",barcodeView.viewFinder.minimumHeight.toString())
                    Log.e("measuredWidth",barcodeView.viewFinder.measuredWidth.toString())
                    Log.e("Width",barcodeView.viewFinder.width.toString())
                    Log.e("minimumWidth",barcodeView.viewFinder.minimumWidth.toString())

                    Log.e("measuredHeightAndState",barcodeView.viewFinder.measuredHeightAndState.toString())
                    Log.e("measuredWidthAndState",barcodeView.viewFinder.measuredWidthAndState.toString())

                    Log.e("barcodeViewHeight",barcodeView.barcodeView.measuredHeight.toString())
                    Log.e("barcodeViewHeight",barcodeView.barcodeView.measuredWidth.toString())
                    barcodeView.barcodeView.height
                    val scanBoxRect = barcodeView.viewFinder.getClipBounds(r)
                    Toast.makeText(applicationContext, "Saved!      "+sourceData.dataHeight.toString()+sourceData.dataWidth.toString(), Toast.LENGTH_SHORT).show()
                    sourceData.cropRect = Rect(r.left, r.top, r.right, r.bottom)
                    val bmp = sourceData.bitmap
                    var bmp1 = cropBitmap(bmp,r)
                    var base644 = encodeImage(bmp)
                    Log.e("image",base644!!)
                    lifecycleScope.launch{
                        var path = saveBitmapFileForPicturesDir(bmp1!!)
                        Toast.makeText(applicationContext, path, Toast.LENGTH_SHORT).show()
                    }

                    try {
                        val dir =
                            File(Environment.getExternalStorageDirectory().path + "/MyCaptureDirectory")
                        dir.mkdirs()
                        val filepath = dir.path + String.format("/%d.jpg", System.currentTimeMillis())
                        val stream: OutputStream = FileOutputStream(filepath)
                        bmp1?.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                        //Make a Toast with cream cheese
                        val bagel = Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_SHORT)
                        bagel.show()
                    } catch (e: FileNotFoundException) {
                        Log.e("LOGTAG", e.message!!)
                    }
              }

                override fun onPreviewError(e: Exception?) {
                    Log.e("preview error", e?.message!!)
                }
            })
            beepManager = BeepManager(this)
        }
    }


    // Register the launcher and result handler
    override  fun onResume() {
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
    fun cropBitmap(bitmap: Bitmap, rect: Rect): Bitmap? {
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    }

    fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
//        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        bm.compress(Bitmap.CompressFormat.JPEG, 75, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
    suspend fun saveBitmapFileForPicturesDir(mBitmap: Bitmap?): String {

        var result = ""
        if (mBitmap != null) {
            var base64URL = ConstantsFunction.encodeImage(mBitmap)
        }
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {
                    val fileName = "QuizBank_100000000000.jpg"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }
                    }

                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    contentResolver.openOutputStream(uri!!).use { outputStream ->
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    result = uri.toString()

                    runOnUiThread {
                        if (!result.isEmpty()) {
                            Toast.makeText(
                                this@CustomCaptureActivity,
                                "success scan",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Toast.makeText(
                                this@CustomCaptureActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()

                }
            }
        }
        return result
    }


}




