package com.karthicbz.klauncher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthicbz.klauncher.data.model.AppInfo
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LONG_PRESS_TIMEOUT = 500L

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppCard(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<Job?>(null) }
    var longPressTriggered by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .aspectRatio(16f / 9f)
            .onPreviewKeyEvent { keyEvent ->
                when {
                    // Key held down — start the timer on first KeyDown
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                            && keyEvent.type == KeyEventType.KeyDown -> {
                        if (longPressJob == null) {
                            longPressTriggered = false
                            longPressJob = scope.launch {
                                delay(LONG_PRESS_TIMEOUT)
                                longPressTriggered = true
                                onLongClick()
                            }
                        }
                        false // let Surface handle KeyDown (visual feedback)
                    }
                    // Key released — cancel timer or consume event if long press fired
                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                            && keyEvent.type == KeyEventType.KeyUp -> {
                        longPressJob?.cancel()
                        longPressJob = null
                        if (longPressTriggered) {
                            longPressTriggered = false
                            true // consume — prevent Surface's onClick
                        } else {
                            false // let Surface handle onClick
                        }
                    }
                    // Menu button on remotes that have it
                    keyEvent.key == Key.Menu
                            && keyEvent.type == KeyEventType.KeyUp -> {
                        onLongClick()
                        true
                    }
                    else -> false
                }
            },
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(context.packageManager.getApplicationIcon(app.packageName))
                        .crossfade(true)
                        .build(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp)
                )
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
}