package com.mapdir.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mapdir.app.data.model.Place
import com.mapdir.app.ui.components.EmptyState
import com.mapdir.app.ui.components.ErrorState
import com.mapdir.app.ui.components.LoadingOverlay
import com.mapdir.app.ui.components.RatingBar
import com.mapdir.app.ui.components.UiState
import com.mapdir.app.ui.theme.DistanceBadge

/**
 * Detail screen — shows full information about a single place.
 *
 * ### Integration point: "Buka Rute" button
 * The [onOpenRoute] callback receives latitude & longitude.
 * The Maps/GPS teammate implements the actual routing logic
 * (e.g., launch Google Maps intent or in-app navigation).
 *
 * ```kotlin
 * // Signature yang disepakati dengan rekan Maps/GPS:
 * fun onOpenRoute(latitude: Double, longitude: Double)
 * ```
 *
 * @param onBackClick pops the back stack
 * @param onOpenRoute callback for the "Buka Rute" button — implemented by Maps teammate
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    onOpenRoute: (latitude: Double, longitude: Double) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (detailState) {
                        is UiState.Success -> (detailState as UiState.Success<Place>).data.name
                        else -> "Detail Tempat"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        when (detailState) {
            is UiState.Loading -> LoadingOverlay(
                modifier = Modifier.padding(padding)
            )
            is UiState.Empty -> EmptyState(
                message = "Tempat tidak ditemukan.",
                modifier = Modifier.padding(padding)
            )
            is UiState.Error -> ErrorState(
                message = (detailState as UiState.Error).message,
                onRetry = viewModel::retry,
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> {
                val place = (detailState as UiState.Success<Place>).data
                DetailContent(
                    place = place,
                    onOpenRoute = onOpenRoute,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    place: Place,
    onOpenRoute: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero image ─────────────────────────────────────────────────────
        if (!place.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = place.photoUrl,
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder when no photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            // ── Name ───────────────────────────────────────────────────────
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Category chip + Distance badge ─────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (place.category != null) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = place.category.name,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }

                if (place.distanceM != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.NearMe,
                            contentDescription = "Jarak",
                            modifier = Modifier.size(16.dp),
                            tint = DistanceBadge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDistance(place.distanceM),
                            style = MaterialTheme.typography.labelMedium,
                            color = DistanceBadge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Rating ─────────────────────────────────────────────────────
            if (place.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = place.rating, starSize = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.1f / 5.0", place.rating),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Address ────────────────────────────────────────────────────
            if (!place.address.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Alamat",
                    value = place.address
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Opening hours ──────────────────────────────────────────────
            if (!place.openingHours.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Jam Buka",
                    value = place.openingHours
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Description ────────────────────────────────────────────────
            if (!place.description.isNullOrBlank()) {
                Text(
                    text = "Deskripsi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── "Buka Rute" button ─────────────────────────────────────────
            // INTEGRATION POINT: callback ke rekan Maps/GPS
            if (place.latitude != null && place.longitude != null) {
                Button(
                    onClick = { onOpenRoute(place.latitude, place.longitude) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buka Rute",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000)
    } else {
        String.format("%.0f m", meters)
    }
}
