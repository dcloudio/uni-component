package uts.sdk.modules.uniCamera

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraState
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
// import com.google.mlkit.vision.barcode.BarcodeScannerOptions
// import com.google.mlkit.vision.barcode.BarcodeScanning
// import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
// import com.google.mlkit.vision.barcode.common.Barcode
// import com.google.mlkit.vision.common.InputImage
// import org.mozilla.universalchardet.UniversalDetector
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Scanner {
    interface ScanSuccessCallback {
        fun onScanSuccess(result: String, scanType: String, charset: String, rawData: String)
    }

    interface ScanFailureCallback {
        fun onScanFailure(error: String)
    }

    companion object {
        var frameCount = 0
        private val isProcessing = AtomicBoolean(false) // 控制帧率
        private var lastAnalysisTime = 0L // 时间戳限制

        @OptIn(ExperimentalGetImage::class)
        fun processScanBarCode(
            imageProxy: ImageProxy,
            scanType: List<String>,
            camera: Camera?,
            successCallback: ScanSuccessCallback?,
            failureCallback: ScanFailureCallback?
        ) {
            // val currentTime = System.currentTimeMillis()
            // if (currentTime - lastAnalysisTime < 200 || !isProcessing.compareAndSet(false, true)) {
            //     imageProxy.close() // 确保立即关闭未使用的 imageProxy
            //     return
            // }
            
            // val mediaImage = imageProxy.image
            // if (mediaImage == null) {
            //     imageProxy.close()
            //     isProcessing.set(false)
            //     return
            // }

            // imageProxy.image?.let {
            //     val inputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            //     _processScanBarCode(imageProxy, inputImage, scanType, camera, successCallback, failureCallback)
            // }
        }

        fun processScanBarCode(
            context: Context,
            uri: Uri,
            scanType: List<String>,
            successCallback: ScanSuccessCallback?,
            failureCallback: ScanFailureCallback?
        ) {
            // val image = InputImage.fromFilePath(context, uri)
            // _processScanBarCode(null, image, scanType, null, successCallback, failureCallback)
        }


        // @kotlin.OptIn(ExperimentalEncodingApi::class)
        // private fun _processScanBarCode(
        //     imageProxy: ImageProxy?, image: InputImage, scanType: List<String>, camera: Camera?, successCallback:
        //     ScanSuccessCallback?, failureCallback: ScanFailureCallback?
        // ) {
        //     try { // 添加异常捕获
        //         val maxValue = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f
        //         val options = BarcodeScannerOptions.Builder()
        //             .setBarcodeFormats(getScanTypeFromStrings(scanType))
        //             .enableAllPotentialBarcodes()
        //             .setZoomSuggestionOptions(ZoomSuggestionOptions.Builder {
        //                 var zoom = it
        //                 if (camera != null) {
        //                     val value = camera.cameraInfo.cameraState.value
        //                     value.let {
        //                         if (it?.type == CameraState.Type.CLOSED) {
        //                             return@Builder false
        //                         }
        //                     }
        //                     val minValue = 1.0f
        //                     zoom = if (zoom < minValue) {
        //                         minValue
        //                     } else if (zoom > maxValue) {
        //                         maxValue
        //                     } else {
        //                         zoom
        //                     }
        //                     camera.cameraControl.setZoomRatio(zoom)
        //                     return@Builder true
        //                 } else {
        //                     return@Builder false
        //                 }
        //             }.setMaxSupportedZoomRatio(maxValue).build())
        //             .build()
        //         val barcodeScanner = BarcodeScanning.getClient(options)

        //         barcodeScanner.let {
        //             it.process(image)
        //                 .addOnSuccessListener { barcodes ->
        //                     for (barcode in barcodes) {
        //                         val rawValue = barcode.rawValue ?: ""
        //                         val format = getBarcodeFormatStr(barcode.format) ?: ""
        //                         barcode.rawBytes?.let {
        //                             if (it.isEmpty()) {
        //                                 return@let
        //                             }
        //                             val rawBytesBase64 = Base64.encode(it)
        //                             val charset = getCharset(it) ?: ""
        //                             Handler(Looper.getMainLooper()).post {
        //                                 successCallback?.onScanSuccess(rawValue, format, charset, rawBytesBase64)
        //                             }
        //                         }
        //                     }
        //                 }
        //                 .addOnFailureListener {
        //                     failureCallback?.onScanFailure(it.message ?: "")
        //                 }
        //                 .addOnCompleteListener {
        //                     try {
        //                         lastAnalysisTime = System.currentTimeMillis()
        //                         isProcessing.set(false)
        //                         imageProxy?.close()
        //                         barcodeScanner.close() // 显式关闭扫描器（如果支持）
        //                     } catch (e: Exception) {
        //                         // 记录日志
        //                     }
        //                 }
        //         }
        //     } catch (e: Exception) {
        //         failureCallback?.onScanFailure(e.message ?: "")
        //         imageProxy?.close()
        //     }
        // }

        // private fun getScanTypeFromStrings(scanType: List<String>): Int {
        //     var barcodeFormat = Barcode.FORMAT_ALL_FORMATS
        //     if (scanType.isEmpty()) {
        //         return Barcode.FORMAT_ALL_FORMATS
        //     }
        //     for (type in scanType) {
        //         when (type) {
        //             "DATA_MATRIX" -> {
        //                 barcodeFormat = barcodeFormat or Barcode.FORMAT_DATA_MATRIX
        //             }

        //             "PDF417" -> {
        //                 barcodeFormat = barcodeFormat or Barcode.FORMAT_PDF417
        //             }

        //             "QR_CODE" -> {
        //                 barcodeFormat = barcodeFormat or Barcode.FORMAT_QR_CODE
        //             }
        //         }
        //     }
        //     return barcodeFormat
        // }


        // private fun getCharset(bytes: ByteArray): String? {
        //     val detector = UniversalDetector(null)
        //     detector.handleData(bytes)
        //     detector.dataEnd()
        //     return detector.detectedCharset
        // }


        // private fun getBarcodeFormatStr(format: Int): String? {
        //     return when (format) {
        //         Barcode.FORMAT_QR_CODE -> "QR_CODE"
        //         Barcode.FORMAT_AZTEC -> "AZTEC"
        //         Barcode.FORMAT_CODABAR -> "CODABAR"
        //         Barcode.FORMAT_CODE_39 -> "CODE_39"
        //         Barcode.FORMAT_CODE_93 -> "CODE_93"
        //         Barcode.FORMAT_CODE_128 -> "CODE_128"
        //         Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        //         Barcode.FORMAT_EAN_8 -> "EAN_8"
        //         Barcode.FORMAT_EAN_13 -> "EAN_13"
        //         Barcode.FORMAT_ITF -> "ITF"
        //         Barcode.FORMAT_PDF417 -> "PDF_417"
        //         Barcode.FORMAT_UPC_A -> "UPC_A"
        //         Barcode.FORMAT_UPC_E -> "UPC_E"
        //         else -> null
        //     }
        // }
    }

}