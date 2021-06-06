package eth.sebastiankanz.decentralizedthings.ipfsstorage.extensions

import io.ipfs.multihash.Multihash

@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun String.isValidIPFSHash(): Boolean {
    return try {
        Multihash.fromBase58(this)
        true
    } catch (e: Exception) {
        false
    }
}

fun String.getMetaFileNameFromFileName() = "${this.substringBeforeLast(".")}.meta.json"