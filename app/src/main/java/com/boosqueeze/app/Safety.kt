package com.boosqueeze.app

// How many bits each pixel of each frame gets in the original video.
// Assumes about 30 frames per second. Low numbers mean the video is already squeezed.
fun sourceDensity(inf: VideoInfo): Double {
    val pixels = inf.width.toDouble() * inf.height.toDouble()
    if (pixels <= 0.0 || inf.bitrate <= 0) return 1.0
    return inf.bitrate / (pixels * 30.0)
}

// True when squeezing is unlikely to save much space.
fun isAlreadySmall(inf: VideoInfo, plan: Plan, preset: Preset): Boolean {
    if (inf.bitrate <= 0) return false
    if (plan.scaleHeight > 0) return false
    if (preset.fitMb > 0) return false
    val keepsMost = plan.bitrate >= inf.bitrate * 0.8
    val dense = sourceDensity(inf) < 0.10
    return keepsMost || dense
}
