package com.example.swadebuilder.ui.components

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

@Composable
fun CharacterPortraitCard(
    modifier: Modifier = Modifier,
    placeholderText: String = "Toque para escolher o retrato",
    imageUri: Uri? = null,
    onSelectImage: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val aspectRatio = if (isLandscape) 16f / 9f else 3f / 4f
    val context = LocalContext.current
    val imageBitmapState = produceState<ImageBitmap?>(initialValue = null, imageUri) {
        value = if (imageUri == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val maxSide = 1024
                    // 1. Decode bounds only
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(imageUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                    }

                    // 2. Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, maxSide, maxSide)
                    options.inJustDecodeBounds = false

                    // 3. Decode actual bitmap with sample size
                    context.contentResolver.openInputStream(imageUri)?.use { stream ->
                        val sampled = BitmapFactory.decodeStream(stream, null, options)
                            ?: return@runCatching null

                        // 4. Optionally scale down further if needed (inSampleSize is power of 2)
                        val currentMax = max(sampled.width, sampled.height)
                        val finalBitmap = if (currentMax > maxSide) {
                             val ratio = maxSide.toFloat() / currentMax.toFloat()
                             val targetWidth = (sampled.width * ratio).toInt().coerceAtLeast(1)
                             val targetHeight = (sampled.height * ratio).toInt().coerceAtLeast(1)
                             Bitmap.createScaledBitmap(sampled, targetWidth, targetHeight, true)
                        } else {
                             sampled
                        }
                        finalBitmap.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clickable(onClick = onSelectImage),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val imageBitmap = imageBitmapState.value
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
