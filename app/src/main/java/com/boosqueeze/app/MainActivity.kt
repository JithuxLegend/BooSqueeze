package com.boosqueeze.app

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
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
                enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { BooSqueezeApp() }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BooSqueezeApp() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    var uri by remember { mutableStateOf<Uri?>(null) }
    var info by remember { mutableStateOf<VideoInfo?>(null) }
    var preset by remember { mutableStateOf(Preset.WHATSAPP_STATUS) }
    var quality by remember { mutableStateOf(Quality.HIGH) }
    var codec by remember { mutableStateOf(Codec.FAST) }
    var state by remember { mutableStateOf("idle") }
    var progress by remember { mutableStateOf(0f) }
    var elapsed by remember { mutableStateOf(0L) }
    var errorMsg by remember { mutableStateOf("") }
    var outBytes by remember { mutableStateOf(0L) }
    var tookSecs by remember { mutableStateOf(0L) }
    var savedWhere by remember { mutableStateOf("") }
    var transformer by remember { mutableStateOf<Transformer?>(null) }
    var startTime by remember { mutableStateOf(0L) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { picked ->
        if (picked != null) {
            uri = picked
            info = readInfo(context, picked)
            state = "idle"
            errorMsg = ""
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
            elapsed = (System.currentTimeMillis() - startTime) / 1000
            delay(300)
        }
    }

    fun startCompress() {
        val src = uri ?: return
        val inf = info ?: return
        state = "working"
        progress = 0f
        elapsed = 0L
        errorMsg = ""
        startTime = System.currentTimeMillis()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val plan = planFor(inf, preset, quality, codec)
        val baseName = inf.name.substringBeforeLast('.', inf.name)
        val outName = baseName + ".mp4"
        val outFile = File(context.cacheDir, "boo_" + System.currentTimeMillis() + ".mp4")

        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder().setBitrate(plan.bitrate).build()
            )
            .build()

        val t = Transformer.Builder(context)
            .setVideoMimeType(plan.mime)
            .setEncoderFactory(encoderFactory)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    progress = 1f
                    state = "saving"
                    Thread {
                        var err = ""
                        var where = ""
                        val size = outFile.length()
                        try {
                            where = saveToGallery(context, outFile, outName)
                        } catch (e: Exception) {
                            err = "Could not save: " + e.message
                        }
                        outFile.delete()
                        val secs = (System.currentTimeMillis() - startTime) / 1000
                        activity.runOnUiThread {
                            outBytes = size
                            tookSecs = secs
                            savedWhere = where
                            errorMsg = err
                            state = if (err.isEmpty()) "done" else "error"
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
                    errorMsg = "Something went wrong: " + exportException.message
                    state = "error"
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            })
            .build()

        val item = MediaItem.fromUri(src)
        val edited = if (plan.scaleHeight > 0) {
            EditedMediaItem.Builder(item)
                .setEffects(Effects(emptyList(), listOf(Presentation.createForHeight(plan.scaleHeight))))
                .build()
        } else {
            EditedMediaItem.Builder(item).build()
        }

        transformer = t
        t.start(edited, outFile.absolutePath)
    }

    val busy = state == "working" || state == "saving"
    val card = Color(0xFF2A2266)
    val soft = Color(0xFFB9B2F5)
    val mint = Color(0xFF7ED957)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2B1E8C), Color(0xFF1B1540))))
                        .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("👻", fontSize = 56.sp)
        Text("Boo Squeeze", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Shrink videos. Keep the quality.", color = soft, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))

        Button(
            onClick = { picker.launch("video/*") },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C6CFF))
        ) { Text("🎬  Choose a video", fontSize = 18.sp, color = Color.White) }

        if (info == null) {
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(card).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pick a video and I'll squeeze it", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text("💬  📸  👥", fontSize = 28.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Ready-made sizes for WhatsApp, Instagram and Facebook. Smaller files, still sharp.",
                    color = soft, fontSize = 13.sp, textAlign = TextAlign.Center
                )
            }
        }

        info?.let { i ->
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(card).padding(16.dp)
            ) {
                Text(i.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                val dims = if (i.width > 0) "   •   " + i.width + "×" + i.height else ""
                Text(
                    "Original: " + fmt(i.sizeBytes) + "   •   " + fmtTime(i.durationMs / 1000) + dims,
                    color = soft, fontSize = 14.sp
                )
            }

            SectionTitle("Where are you sharing it?")
            Preset.values().forEach { p ->
                val estText = if (p == Preset.CUSTOM) "" else
                    "~" + fmt(estimateBytes(i, planFor(i, p, quality, codec)))
                PresetCard(p, estText, p == preset, !busy) { preset = p }
                Spacer(Modifier.height(10.dp))
            }

            if (preset == Preset.CUSTOM) {
                SectionTitle("Speed")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Codec.values().forEach { c ->
                        Chip(c.label, c.hint, c == codec, !busy, Modifier.weight(1f)) { codec = c }
                    }
                }
                SectionTitle("Quality")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Quality.values().forEach { q ->
                        Chip(q.label, q.hint, q == quality, !busy, Modifier.weight(1f)) { quality = q }
                    }
                }
            }

            val plan = planFor(i, preset, quality, codec)
            val est = estimateBytes(i, plan)
            Spacer(Modifier.height(10.dp))
            val note = when {
                preset == Preset.WHATSAPP_CHAT && plan.bitrate < 1_000_000 ->
                    "Long video: quality may drop to fit 16 MB. Trimming it first gives a sharper result."
                i.sizeBytes > 0 && est >= i.sizeBytes ->
                    "This video is already small, so there's little to save."
                else -> "Estimated result: about " + fmt(est)
            }
            Text(
                note, color = mint, fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))
            if (state == "working") {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(card).padding(18.dp)
                ) {
                    Text(
                        "Squeezing… " + (progress * 100).toInt() + "%",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = mint,
                        trackColor = Color(0xFF4A3FA8)
                    )
                    Spacer(Modifier.height(10.dp))
                    val eta = if (progress > 0.05f) (elapsed / progress - elapsed).toLong() else -1L
                    Text(
                        "Time: " + fmtTime(elapsed) +
                            (if (eta >= 0) "   •   About " + fmtTime(eta) + " left" else ""),
                        color = soft, fontSize = 14.sp
                    )
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
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mint)
                ) { Text("🧃  Squeeze it!", fontSize = 19.sp, color = Color(0xFF1B1540), fontWeight = FontWeight.Bold) }
            }

            if (state == "done") {
                val saved = if (i.sizeBytes > 0) (100 - (outBytes * 100 / i.sizeBytes)) else 0
                val speed = if (tookSecs > 0) (i.durationMs / 1000.0) / tookSecs else 0.0
                Spacer(Modifier.height(18.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(card).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✅ Done!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(saved.toString() + "% smaller", color = mint, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(fmt(i.sizeBytes) + "  →  " + fmt(outBytes), color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Took " + fmtTime(tookSecs) + "  •  " + String.format(Locale.US, "%.1f", speed) + "x playback speed",
                        color = soft, fontSize = 14.sp
                    )
                    Text("Saved in: " + savedWhere, color = soft, fontSize = 13.sp)
                }
            }
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text(
                errorMsg, color = Color(0xFFFFB4B4), fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(card).padding(16.dp)
            )
        }
        Spacer(Modifier.height(30.dp))
    }
}
