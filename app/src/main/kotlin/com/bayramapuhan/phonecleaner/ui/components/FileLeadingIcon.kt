package com.bayramapuhan.phonecleaner.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val IMAGE_EXTS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
private val VIDEO_EXTS = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "m4v")
private val AUDIO_EXTS = setOf("mp3", "wav", "m4a", "flac", "aac", "ogg", "opus", "wma")

@Composable
fun FileLeadingIcon(
    path: String,
    modifier: Modifier = Modifier,
) {
    val ext = path.substringAfterLast('.', "").lowercase()
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            ext in IMAGE_EXTS || ext in VIDEO_EXTS -> AsyncImage(
                model = File(path),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            ext in AUDIO_EXTS -> AudioThumb(source = AudioSource.PathSource(path), ext = ext)
            ext == "apk" -> ApkIconView(path)
            else -> ExtBadge(ext)
        }
    }
}

@Composable
fun MediaThumb(
    uri: Uri,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (isVideo) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AudioThumb(source = AudioSource.UriSource(uri), ext = "audio")
        }
    }
}

private sealed interface AudioSource {
    data class PathSource(val path: String) : AudioSource
    data class UriSource(val uri: Uri) : AudioSource
}

@Composable
private fun AudioThumb(source: AudioSource, ext: String) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, source) {
        value = withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                when (source) {
                    is AudioSource.PathSource -> retriever.setDataSource(source.path)
                    is AudioSource.UriSource -> retriever.setDataSource(context, source.uri)
                }
                retriever.embeddedPicture?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }
            } catch (_: Throwable) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        ExtBadge(ext)
    }
}

@Composable
private fun ApkIconView(path: String) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(path) {
        runCatching {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(path, 0)
            val appInfo = info?.applicationInfo ?: return@runCatching null
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path
            appInfo.loadIcon(pm)?.toBitmap()?.asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(4.dp),
        )
    } else {
        ExtBadge("apk")
    }
}

@Composable
private fun ExtBadge(ext: String) {
    val (color, label) = extStyle(ext)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

private fun extStyle(ext: String): Pair<Color, String> = when (ext) {
    "pdf" -> Color(0xFFEF4444) to "PDF"
    "doc", "docx" -> Color(0xFF2563EB) to "DOC"
    "xls", "xlsx", "csv" -> Color(0xFF10B981) to "XLS"
    "ppt", "pptx" -> Color(0xFFF97316) to "PPT"
    "zip", "rar", "tar", "gz", "7z" -> Color(0xFF8B5CF6) to "ZIP"
    "txt", "log", "md", "rtf" -> Color(0xFF64748B) to "TXT"
    "json", "xml", "html", "yaml", "yml" -> Color(0xFF0EA5E9) to "</>"
    "apk" -> Color(0xFF14B8A6) to "APK"
    "audio" -> Color(0xFFF59E0B) to "♪"
    in AUDIO_EXTS -> Color(0xFFF59E0B) to "♪"
    "" -> Color(0xFF94A3B8) to "?"
    else -> Color(0xFF64748B) to ext.take(3).uppercase()
}

private fun Drawable.toBitmap(size: Int = 96): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val w = if (intrinsicWidth > 0) intrinsicWidth else size
    val h = if (intrinsicHeight > 0) intrinsicHeight else size
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, w, h)
    draw(canvas)
    return bmp
}
