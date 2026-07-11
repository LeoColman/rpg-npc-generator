package me.kerooker.rpgnpcgenerator.ui.share

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.data.Npc

private const val MAX_CAPTURE_FRAMES = 10

/**
 * Off-screen renderer that turns [request] into a shareable image and fires the system share sheet,
 * then calls [onFinished]. Renders nothing visible: when [request] is null it is inert, otherwise it
 * draws the [NpcShareCard] into a [rememberGraphicsLayer] at alpha 0 (invisible but still drawn),
 * captures the layer once it has been laid out, and hands the PNG to [NpcCardSharer].
 *
 * GraphicsLayer capture is the platform-recommended way to snapshot a composable and works from
 * minSdk 26 upward; it needs no window token or view hierarchy, so the render can stay off-screen.
 */
@Composable
fun NpcShareCardCapture(
    request: Npc?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (request == null) return

    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()

    val footer = stringResource(R.string.share_card_watermark, stringResource(R.string.app_name))
    val chooserTitle = stringResource(R.string.share_npc_chooser)
    val errorMessage = stringResource(R.string.share_npc_error)
    val labels = NpcShareCardLabels(
        profession = stringResource(R.string.share_card_profession),
        alignment = stringResource(R.string.share_card_alignment),
        motivation = stringResource(R.string.share_card_motivation),
        personality = stringResource(R.string.share_card_personality)
    )

    // Decode the portrait up-front so it is guaranteed present when we capture (async loading could
    // race the snapshot). A null path is a valid "no portrait" card, so it is ready immediately.
    val portraitPath = request.imagePath
    var portrait by remember(request) { mutableStateOf<ImageBitmap?>(null) }
    var ready by remember(request) { mutableStateOf(portraitPath == null) }
    LaunchedEffect(request) {
        if (portraitPath != null) {
            portrait = withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(portraitPath)?.asImageBitmap() }.getOrNull()
            }
            ready = true
        }
    }

    if (!ready) return

    Box(
        modifier = modifier
            .alpha(0f)
            .drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }
    ) {
        NpcShareCard(npc = request, portrait = portrait, footer = footer, labels = labels)
    }

    LaunchedEffect(request, ready) {
        // Wait until the off-screen card has actually been laid out and recorded into the layer.
        var frames = 0
        while (graphicsLayer.size.width == 0 && frames < MAX_CAPTURE_FRAMES) {
            withFrameNanos { }
            frames++
        }
        withFrameNanos { } // one more frame so the recorded draw is complete before we read it
        runCatching {
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            val file = NpcCardSharer.saveCardPng(context, bitmap)
            NpcCardSharer.share(context, file, npcShareText(request, footer), chooserTitle)
        }.onFailure {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
        onFinished()
    }
}
