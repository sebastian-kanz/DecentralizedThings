package eth.sebastiankanz.decentralizedthings.qrcodemanager

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter

class QRCodeManager private constructor() {

    fun generateQRCode(json: String): Bitmap {
        val width = 200
        val height = 200
        val bitMatrix = QRCodeWriter().encode(json, BarcodeFormat.QR_CODE, width, height)

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h)

        return bitmap
    }

    fun readQRCode(bitmap: Bitmap): String {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(
            intArray, 0, bitmap.width, 0, 0, bitmap.width,
            bitmap.height
        )
        val source: LuminanceSource = RGBLuminanceSource(
            bitmap.width,
            bitmap.height, intArray
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val qrCodeResult = MultiFormatReader().decode(binaryBitmap)
        return qrCodeResult.text
    }

    companion object {
        fun newInstance(): QRCodeManager {
            return QRCodeManager()
        }
    }
}