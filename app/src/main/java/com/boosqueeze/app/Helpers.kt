package com.boosqueeze.app

import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MimeTypes
import java.io.File
import java.util.Locale

enum class Codec(val label: String, val hint: String, val mime: String) {
    FAST("⚡ Fast", "H.264 · quickest", MimeTypes.VIDEO_H264),
    SMALL("📦 Smaller", "H.265 · slower", MimeTypes.VIDEO_H265)
}

enum class Quality(val label: String, val hint: String, val fastFactor: Float, val smallFactor: Float) {
    HIGH("High", "Same look", 0.70f, 0.45f),
    BALANCED("Balanced", "Smaller", 0.50f, 0.32f),
    SMALL("Small", "Smallest", 0.33f, 0.22f)
}

enum class Preset(
    val emoji: String,
    val title: String,
    val hint: String,
    val maxShortSide: Int,
    val bitrate: Int,
    val fitMb: Int
) {
    WHATSAPP_STATUS("💬", "WhatsApp Status", "720p · smooth & light", 720, 2_500_000, 0),
    INSTAGRAM("📸", "Instagram Story / Reel", "1080p · extra sharp", 1080, 4_000_000, 0),
    FACEBOOK("👥", "Facebook Story", "720p · light", 720, 3_000_000, 0),
    WHATSAPP_CHAT("📨", "WhatsApp chat", "Fits under 16 MB", 720, 0, 16),
    CUSTOM("🎛️", "Custom", "Pick speed & quality yourself", 0, 0, 0)
}

data class VideoInfo(
    val name: String,
    val sizeBytes: Long,
    val bitrate: Int,
    val durationMs: Long,
    val width: Int,
    val height: Int
)

data class Plan(val bitrate: Int, val mime: String, val scaleHeight: Int)
var lastSavedUri: Uri? = null

fun readInfo(context: Context, uri: Uri): VideoInfo {
    var name = "video"
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val si = c.getColumnIndex(OpenableColumns.SIZE)
            if (ni >= 0) name = c.getString(ni) ?: name
            if (si >= 0) size = c.getLong(si)
        }
    }
    var bitrate = 0
    var duration = 0L
    var w = 0
    var h = 0
    try {
        val r = MediaMetadataRetriever()
        r.setDataSource(context, uri)
        bitrate = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
        duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        val rot = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        if (rot == 90 || rot == 270) {
            val tmp = w
            w = h
            h = tmp
        }
        r.release()
    } catch (e: Exception) {
    }
    if (bitrate == 0 && duration > 0 && size > 0) {
        bitrate = ((size * 8 * 1000) / duration).toInt()
    }
    return VideoInfo(name, size, bitrate, duration, w, h)
}

fun fmt(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) String.format(Locale.US, "%.2f GB", mb / 1024)
    else String.format(Locale.US, "%.1f MB", mb)
}

fun fmtTime(sec: Long): String {
    val s = if (sec < 0) 0 else sec
    return if (s >= 60) (s / 60).toString() + "m " + (s % 60) + "s" else s.toString() + "s"
}

fun planFor(inf: VideoInfo, p: Preset, q: Quality, c: Codec): Plan {
    if (p == Preset.CUSTOM) {
        val src = if (inf.bitrate > 0) inf.bitrate else 8_000_000
        val f = if (c == Codec.FAST) q.fastFactor else q.smallFactor
        return Plan((src * f).toInt().coerceAtLeast(500_000), c.mime, 0)
    }
    val dur = maxOf(1.0, inf.durationMs / 1000.0)
    var br = if (p.fitMb > 0) {
        val total = p.fitMb * 1024.0 * 1024.0 * 8.0 * 0.92
        ((total - 128_000.0 * dur) / dur).toInt().coerceIn(300_000, 6_000_000)
    } else p.bitrate
    if (inf.bitrate > 0) br = minOf(br, (inf.bitrate * 0.9).toInt())
    br = br.coerceAtLeast(300_000)

    var scaleH = 0
    val shortSide = minOf(inf.width, inf.height)
    if (p.maxShortSide > 0 && shortSide > p.maxShortSide) {
        val scale = p.maxShortSide.toDouble() / shortSide
        val newH = (inf.height * scale).toInt()
        scaleH = newH - newH % 2
    }
    return Plan(br, MimeTypes.VIDEO_H264, scaleH)
}

fun estimateBytes(inf: VideoInfo, plan: Plan): Long {
    val dur = inf.durationMs / 1000.0
        return (plan.bitrate / 8.0 * dur * 1.17 + 16000.0 * dur).toLong()
}

fun saveToGallery(context: Context, file: File, name: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/BooSqueeze")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Could not create file")
        resolver.openOutputStream(uri)?.use { out ->
            file.inputStream().use { it.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
                lastSavedUri = uri
        "Movies/BooSqueeze"
    } else {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        file.copyTo(File(dir, name), overwrite = true)
        dir.absolutePath
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 8.dp)
    )
}

@Composable
fun Chip(
    title: String, sub: String, selected: Boolean, enabled: Boolean,
    modifier: Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF7C6CFF) else Color(0xFF2A2266))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(sub, color = if (selected) Color.White else Color(0xFFB9B2F5), fontSize = 12.sp)
    }
}

@Composable
fun PresetCard(
    p: Preset, estText: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Color(0xFF3B308F) else Color(0xFF2A2266))
            .border(2.dp, if (selected) Color(0xFF7ED957) else Color.Transparent, shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(p.emoji, fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(p.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(p.hint, color = Color(0xFFB9B2F5), fontSize = 12.sp)
        }
        if (estText.isNotEmpty()) {
            Text(estText, color = Color(0xFF7ED957), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
