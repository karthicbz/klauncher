package com.karthicbz.klauncher.ui.home.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.karthicbz.klauncher.data.model.AppInfo

/**
 * TV-compatible context menu overlay.
 * Uses a solid semi-transparent backdrop (no blur) + a centred Surface card.
 * The backdrop itself is not clickable to dismiss — the Cancel button handles that,
 * which is safer for D-pad-only navigation on TV.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppInfo,
    onDismissRequest: () -> Unit,
    onReorderClick: () -> Unit,
    onHideClick: () -> Unit,
    onAddToCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            // Flat dark backdrop — no blur, safe for all devices
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = { /* consume clicks; dialog handled by buttons below */ },
            modifier = Modifier.width(400.dp),
            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                MenuButton("Reorder App") {
                    onReorderClick()
                    onDismissRequest()
                }
                MenuButton("Hide App") {
                    onHideClick()
                    onDismissRequest()
                }
                MenuButton("Move to Category") {
                    onAddToCategoryClick()
                    onDismissRequest()
                }
                MenuButton("App Info") {
                    launchAppDetails(context, app.packageName)
                    onDismissRequest()
                }
                MenuButton("Uninstall") {
                    launchUninstall(context, app.packageName)
                    onDismissRequest()
                }

                // D-pad-friendly cancel — always last so Back-key navigation is predictable
                Surface(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MenuButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

private fun launchAppDetails(context: Context, packageName: String) {
    try {
        context.startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {}
}

private fun launchUninstall(context: Context, packageName: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {}
}
