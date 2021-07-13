package eth.sebastiankanz.decentralizedthings.qrcodemanager.data.model

import android.graphics.Bitmap

enum class QRCodeType {
    FILE
}

data class QRCodeContainer(
    val type: QRCodeType,
    val qrCode: Bitmap
)