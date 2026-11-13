package com.boosqueeze.app

import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BooSqueezeApp() }
    }
}

enum class Quality(val label: String, val hint: String, val factor: Float) {
    HIGH("High", "Same look", 0.6f),
    BALANCED("Balanced", "Smaller", 0.4f),
    SMALL("Small", "Smallest", 0.25f)
}

data class VideoInfo(val name: String, val sizeBytes: Long, val bitrate: Int, val durationMs: Long)

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
    try {
        val r = MediaMetadataRetriever()
        r.setDataSource(context, uri)
        bitrate = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
        duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        r.release()
    } catch (e: Exception) {
    }
    if (bitrate == 0 && duration > 0 && size > 0) {
        bitrate = ((size * 8 * 1000) / duration).toInt()
    }
    return VideoInfo(name, size, bitrate, duration)
}

fun fmt(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) String.format(Locale.US, "%.2f GB", mb / 1024)
    else String.format(Locale.US, "%.1f MB", mb)
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
        "Movies/BooSqueeze"
    } else {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        file.copyTo(File(dir, name), overwrite = true)
        dir.absolutePath
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BooSqueezeApp() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    var uri by remember { mutableStateOf<Uri?>(null) }
    var info by remember { mutableStateOf<VideoInfo?>(null) }
    var quality by remember { mutableStateOf(Quality.HIGH) }
    var state by remember { mutableStateOf("idle") }
    var progress by remember { mutableStateOf(0f) }
    var message by remember { mutableStateOf("") }
    var transformer by remember { mutableStateOf<Transformer?>(null) }
    var startTime by remember { mutableStateOf(0L) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { picked ->
        if (picked != null) {
            uri = picked
            info = readInfo(context, picked)
            state = "idle"
            message = ""
        }
    }

    LaunchedEffect(state) {
        while (state == "working") {
            val t = transformer
            if (t != null) {
                val holder = ProgressHolder()
                if (t.getProgress(holder) != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    progress = holder.progress / 100f
                }
            }
            delay(300)
        }
    }

    fun startCompress() {
        val src = uri ?: return
        val inf = info ?: return
        state = "working"
        progress = 0f
        message = ""
        startTime = System.currentTimeMillis()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val srcBitrate = if (inf.bitrate > 0) inf.bitrate else 8_000_000
        val target = (srcBitrate * quality.factor).toInt().coerceAtLeast(500_000)
        val outFile = File(context.cacheDir, "boo_" + System.currentTimeMillis() + ".mp4")

        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder().setBitrate(target).build()
            )
            .build()

        val t = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .setEncoderFactory(encoderFactory)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    progress = 1f
                    state = "saving"
                    Thread {
                        val msg = try {
                            val outSize = outFile.length()
                            val where = saveToGallery(
                                context, outFile,
                                "BooSqueeze_" + System.currentTimeMillis() + ".mp4"
                            )
                            val secs = (System.currentTimeMillis() - startTime) / 1000
                            val saved = if (inf.sizeBytes > 0)
                                100 - (outSize * 100 / inf.sizeBytes) else 0
                            "Done in " + secs + "s\n" +
                                fmt(inf.sizeBytes) + "  →  " + fmt(outSize) +
                                "  (" + saved + "% smaller)\nSaved in: " + where
                        } catch (e: Exception) {
                            "Could not save: " + e.message
                        }
                        outFile.delete()
                        activity.runOnUiThread {
                            message = msg
                            state = "done"
                            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }.start()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    outFile.delete()
                    message = "Something went wrong: " + exportException.message
                    state = "error"
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            })
            .build()

        transformer = t
        t.start(MediaItem.fromUri(src), outFile.absolutePath)
    }

    val busy = state == "working" || state == "saving"
    val bg = Color(0xFF1B1540)
    val card = Color(0xFF2A2266)
    val soft = Color(0xFFB9B2F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Text("👻 Boo Squeeze", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Shrink videos. Keep the quality.", color = soft, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { picker.launch("video/*") },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("🎬  Choose a video", fontSize = 18.sp) }

        info?.let { i ->
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
            ) {
                Text(i.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Size: " + fmt(i.sizeBytes) + "   •   " + (i.durationMs / 1000) + " sec",
                    color = soft, fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Quality", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Quality.values().forEach { q ->
                    val selected = q == quality
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Color(0xFF6D5DF6) else card)
                            .clickable(enabled = !busy) { quality = q }
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(q.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(q.hint, color = if (selected) Color.White else soft, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            if (state == "working") {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
                ) {
                    Text("Squeezing… " + (progress * 100).toInt() + "%", color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    transformer?.cancel()
                    state = "idle"
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }) { Text("Cancel", color = Color.White) }
            } else if (state == "saving") {
                Text("Saving to your gallery…", color = Color.White, fontSize = 16.sp)
            } else {
                Button(
                    onClick = { startCompress() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("🧃  Squeeze it!", fontSize = 18.sp) }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                message, color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
            )
        }
    }
}
