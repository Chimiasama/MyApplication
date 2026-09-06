package com.example.swadebuilder.ui.dialogs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import com.example.swadebuilder.util.CharacterPortraitStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Diálogo interativo de enquadramento e corte de foto (Pinch-to-zoom com efeito de pinça, Drag/Pan, Rotação e Zoom Out/Redução).
 */
@Composable
fun ImageCropperDialog(
    sourceUri: Uri? = null,
    sourceFileName: String? = null,
    onDismiss: () -> Unit,
    onCropConfirmed: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sourceUri, sourceFileName) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                var loadedBmp: Bitmap? = null
                if (sourceFileName != null) {
                    loadedBmp = CharacterPortraitStorage.loadPortrait(context, sourceFileName)
                }
                if (loadedBmp == null && sourceUri != null) {
                    context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                        val opts = BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        loadedBmp = BitmapFactory.decodeStream(stream, null, opts)
                    }
                }
                sourceBitmap = loadedBmp
                if (loadedBmp == null) {
                    errorMessage = "Não foi possível carregar a imagem selecionada."
                }
            } catch (e: Exception) {
                errorMessage = "Não foi possível carregar a imagem selecionada."
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Carregando imagem...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (errorMessage != null || sourceBitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "Erro ao carregar imagem.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("Voltar")
                        }
                    }
                }
            } else {
                ImageCropperContent(
                    originalBitmap = sourceBitmap!!,
                    onDismiss = onDismiss,
                    onCropConfirmed = { croppedBmp ->
                        coroutineScope.launch {
                            onCropConfirmed(croppedBmp)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageCropperContent(
    originalBitmap: Bitmap,
    onDismiss: () -> Unit,
    onCropConfirmed: (Bitmap) -> Unit
) {
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val currentBitmap = remember(originalBitmap, rotationDegrees) {
        if (rotationDegrees == 0f) {
            originalBitmap
        } else {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        }
    }

    val imageBitmap = remember(currentBitmap) { currentBitmap.asImageBitmap() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Proporção da caixa de enquadramento do retrato (0.8f para caber no card de retrato 140x175)
    val cropAspectRatio = 0.8f

    val cropBoxSize = remember(containerSize) {
        if (containerSize.width == 0 || containerSize.height == 0) {
            Size.Zero
        } else {
            val maxW = containerSize.width * 0.82f
            val maxH = containerSize.height * 0.82f

            var w = maxW
            var h = w / cropAspectRatio
            if (h > maxH) {
                h = maxH
                w = h * cropAspectRatio
            }
            Size(w, h)
        }
    }

    // Escala base para cobrir a caixa de corte no tamanho inicial de referência (1.0x)
    val baseScale = remember(currentBitmap, cropBoxSize) {
        if (cropBoxSize.width == 0f || cropBoxSize.height == 0f) 1f
        else {
            max(
                cropBoxSize.width / currentBitmap.width.toFloat(),
                cropBoxSize.height / currentBitmap.height.toFloat()
            )
        }
    }

    // Função de ajuste e limite do offset permitindo mover tanto fotos ampliadas quanto reduzidas
    fun clampOffset(newOffset: Offset, currentZoom: Float): Offset {
        if (cropBoxSize.width == 0f || cropBoxSize.height == 0f) return Offset.Zero

        val effectiveScale = baseScale * currentZoom
        val displayedW = currentBitmap.width * effectiveScale
        val displayedH = currentBitmap.height * effectiveScale

        // Quando a imagem for maior que a caixa, limitamos para não deixar lacunas.
        // Quando a imagem for menor (zoom out), permitimos mover em um raio confortável.
        val maxOffsetX = if (displayedW >= cropBoxSize.width) {
            (displayedW - cropBoxSize.width) / 2f
        } else {
            cropBoxSize.width / 2f
        }

        val maxOffsetY = if (displayedH >= cropBoxSize.height) {
            (displayedH - cropBoxSize.height) / 2f
        } else {
            cropBoxSize.height / 2f
        }

        return Offset(
            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ajustar Enquadramento",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Fechar")
            }
        }

        // Área do Cropper Viewport com gesto de pinça (Pinch-to-zoom) e Arraste (Pan)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .background(Color.Black)
                .onGloballyPositioned { containerSize = it.size }
                .pointerInput(currentBitmap, cropBoxSize) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        // Pinch-to-zoom com limite de 0.2x (Reduzir) até 5.0x (Ampliar)
                        val newZoom = (zoom * gestureZoom).coerceIn(0.2f, 5.0f)
                        zoom = newZoom
                        offset = clampOffset(offset + pan, newZoom)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (cropBoxSize.width > 0f && cropBoxSize.height > 0f) {
                val effectiveScale = baseScale * zoom

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val containerCenter = Offset(size.width / 2f, size.height / 2f)
                    val cropTopLeft = Offset(
                        containerCenter.x - cropBoxSize.width / 2f,
                        containerCenter.y - cropBoxSize.height / 2f
                    )
                    val cropRect = Rect(offset = cropTopLeft, size = cropBoxSize)

                    // 1. Desenhar a Imagem com Offset e Zoom
                    val imageDrawWidth = currentBitmap.width * effectiveScale
                    val imageDrawHeight = currentBitmap.height * effectiveScale
                    val imageTopLeft = Offset(
                        containerCenter.x - (imageDrawWidth / 2f) + offset.x,
                        containerCenter.y - (imageDrawHeight / 2f) + offset.y
                    )

                    drawImage(
                        image = imageBitmap,
                        dstOffset = IntOffset(
                            imageTopLeft.x.toInt(),
                            imageTopLeft.y.toInt()
                        ),
                        dstSize = IntSize(imageDrawWidth.toInt(), imageDrawHeight.toInt())
                    )

                    // 2. Máscara Escura para destacar a área enquadrada
                    val cornerRadiusPx = 16.dp.toPx()
                    val path = Path().apply {
                        addRect(Rect(Offset.Zero, size))
                        addRoundRect(RoundRect(cropRect, CornerRadius(cornerRadiusPx, cornerRadiusPx)))
                    }

                    clipPath(path = path, clipOp = ClipOp.Difference) {
                        drawRect(Color.Black.copy(alpha = 0.65f))
                    }

                    // 3. Moldura Branca da Caixa de Enquadramento
                    drawRoundRect(
                        color = Color.White,
                        topLeft = cropRect.topLeft,
                        size = cropRect.size,
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Painel de Controles e Ferramentas
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Slider e Botões (+ e -) para Zoom e Redução
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newZoom = (zoom - 0.2f).coerceIn(0.2f, 5.0f)
                            zoom = newZoom
                            offset = clampOffset(offset, newZoom)
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir / Reduzir")
                    }

                    Slider(
                        value = zoom,
                        onValueChange = { newZoom ->
                            zoom = newZoom
                            offset = clampOffset(offset, newZoom)
                        },
                        valueRange = 0.2f..5.0f,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val newZoom = (zoom + 0.2f).coerceIn(0.2f, 5.0f)
                            zoom = newZoom
                            offset = clampOffset(offset, newZoom)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar / Expandir")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Rotação e Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            rotationDegrees = (rotationDegrees + 90f) % 360f
                            zoom = 1f
                            offset = Offset.Zero
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Girar 90°")
                    }

                    OutlinedButton(
                        onClick = {
                            zoom = 1f
                            offset = Offset.Zero
                            rotationDegrees = 0f
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Redefinir")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Botões de Confirmação / Cancelamento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val cropped = cropBitmapToPortrait(
                                currentBitmap = currentBitmap,
                                cropBoxSize = cropBoxSize,
                                baseScale = baseScale,
                                zoom = zoom,
                                offset = offset
                            )
                            onCropConfirmed(cropped)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

/**
 * Gera um Bitmap de alta definição (512 x 640) renderizando a área visível do enquadramento.
 * Garante fidelidade de 100% com o que o jogador posicionou na tela (incluindo zoom out/redução).
 */
private fun cropBitmapToPortrait(
    currentBitmap: Bitmap,
    cropBoxSize: Size,
    baseScale: Float,
    zoom: Float,
    offset: Offset
): Bitmap {
    val targetW = 512
    val targetH = (targetW / 0.8f).toInt() // 640px

    val outputBitmap = createBitmap(targetW, targetH)
    val canvas = Canvas(outputBitmap)

    if (cropBoxSize.width <= 0f || cropBoxSize.height <= 0f) return outputBitmap

    val scaleToOutput = targetW / cropBoxSize.width

    val effectiveScale = baseScale * zoom
    val drawW = currentBitmap.width * effectiveScale * scaleToOutput
    val drawH = currentBitmap.height * effectiveScale * scaleToOutput

    val centerX = targetW / 2f + offset.x * scaleToOutput
    val centerY = targetH / 2f + offset.y * scaleToOutput

    val matrix = Matrix().apply {
        postScale(
            (drawW / currentBitmap.width.toFloat()),
            (drawH / currentBitmap.height.toFloat())
        )
        postTranslate(
            centerX - drawW / 2f,
            centerY - drawH / 2f
        )
    }

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    canvas.drawBitmap(currentBitmap, matrix, paint)
    return outputBitmap
}
