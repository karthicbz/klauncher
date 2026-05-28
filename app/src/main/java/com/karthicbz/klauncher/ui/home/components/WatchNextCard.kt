package com.karthicbz.klauncher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.karthicbz.klauncher.data.model.WatchNextProgram

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WatchNextCard(
    program: WatchNextProgram,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(240.dp)
            .aspectRatio(16f / 9f),
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = program.posterArtUri,
                contentDescription = program.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay with title at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    program.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
