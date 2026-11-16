package com.boosqueeze.app

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

fun loadThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val r = MediaMetadataRetriever()
        r.setDataSource(context, uri)
        val frame = r.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: r.getFrameAtTime(0)
        r.release()
        if (frame == null) {
            null
        } else {
            val scale = 480f / maxOf(frame.width, frame.height)
            if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    frame, (frame.width * scale).toInt(), (frame.height * scale).toInt(), true
                )
            } else {
                frame
            }
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun VideoThumb(thumb: ImageBitmap?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 118.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B1540))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (thumb != null) {
            Image(
                bitmap = thumb,
                contentDescription = "Video preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun VideoPlayerDialog(uri: Uri, onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(uri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                        setOnClickListener {
                            if (isPlaying) pause() else start()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                onRelease = { it.stopPlayback() }
            )
            Text(
                "✕", color = Color.White, fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .clickable { onClose() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                "Tap the video to pause or play",
                color = Color(0xFFB9B2F5), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(28.dp)
            )
        }
    }
}
