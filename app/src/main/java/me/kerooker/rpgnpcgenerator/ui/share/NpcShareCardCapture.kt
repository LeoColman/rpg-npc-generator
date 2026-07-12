package me.kerooker.rpgnpcgenerator.ui.share

import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.R

private const val MAX_CAPTURE_FRAMES = 10
private const val TAG = "NpcShareCardCapture"

/**
 * Off-screen renderer that turns [request] into a shareable file — a PNG image or a PDF, per the
 * request's format — and fires the system share sheet, then calls [onFinished]. Renders nothing
 * visible: when [request] is null it is inert, otherwise it draws the [NpcShareCard] into a
 * [rememberGraphicsLayer] at alpha 0 (invisible but still drawn), captures the layer once it has been
 * laid out, and hands the result to [NpcCardSharer] (PNG) or [NpcPdfExporter] (PDF).
 *
 * GraphicsLayer capture is the platform-recommended way to snapshot a composable and works from
 * minSdk 26 upward; it needs no window token or view hierarchy, so the render can stay off-screen.
 */
@Composable
fun NpcShareCardCapture(
    request: NpcExportRequest?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (request == null) return
    val npc = request.npc

    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()

    val footer = stringResource(R.string.share_card_watermark, stringResource(R.string.app_name))
    val chooserTitle = stringResource(R.string.share_npc_chooser)
    val errorMessage = stringResource(R.string.share_npc_error)
    val labels = NpcShareCardLabels(
        profession = stringResource(R.string.share_card_profession),
        alignment = stringResource(R.string.share_card_alignment),
        motivation = stringResource(R.string.share_card_motivation),
        personality = stringResource(R.string.share_card_personality),
        languages = stringResource(R.string.individual_npc_languages_label),
        combat = CombatSheetLabels(
            title = stringResource(R.string.combat_stats_label),
            strength = stringResource(R.string.combat_strength),
            dexterity = stringResource(R.string.combat_dexterity),
            constitution = stringResource(R.string.combat_constitution),
            intelligence = stringResource(R.string.combat_intelligence),
            wisdom = stringResource(R.string.combat_wisdom),
            charisma = stringResource(R.string.combat_charisma),
            armorClass = stringResource(R.string.combat_armor_class),
            hitPoints = stringResource(R.string.combat_hit_points),
            challengeRating = stringResource(R.string.combat_challenge_rating)
        )
    )

    // Decode the portrait up-front so it is guaranteed present when we capture (async loading could
    // race the snapshot). A null path is a valid "no portrait" sheet, so it is ready immediately.
    val portraitPath = npc.imagePath
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

    // The sheet must lay out at its FULL intrinsic size even on short screens: measured with the
    // Scaffold's bounded constraints it gets capped at the viewport, cutting the bottom (combat
    // block) out of the capture on small phones. The outer Layout measures the sheet with no
    // constraints and reports a coerced size (parents enforce their bounds on THIS node), while the
    // INNER box — the node the layer records at — keeps the uncoerced full sheet size.
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .alpha(0f)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                NpcShareCard(npc = npc, portrait = portrait, footer = footer, labels = labels)
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(Constraints())
        layout(
            placeable.width.coerceAtMost(constraints.maxWidth),
            placeable.height.coerceAtMost(constraints.maxHeight)
        ) { placeable.place(0, 0) }
    }

    LaunchedEffect(request, ready) {
        // Wait until the off-screen sheet has actually been laid out and recorded into the layer.
        var frames = 0
        while (graphicsLayer.size.width == 0 && frames < MAX_CAPTURE_FRAMES) {
            withFrameNanos { }
            frames++
        }
        withFrameNanos { } // one more frame so the recorded draw is complete before we read it
        runCatching {
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            val file = when (request.format) {
                NpcExportFormat.PNG -> NpcCardSharer.saveCardPng(context, bitmap)
                NpcExportFormat.PDF -> NpcPdfExporter.savePdf(context, bitmap)
            }
            NpcCardSharer.share(context, file, npcShareText(npc, footer), chooserTitle, request.format.mimeType)
        }.onFailure {
            Log.w(TAG, "NPC export (${request.format}) failed", it)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
        onFinished()
    }
}
