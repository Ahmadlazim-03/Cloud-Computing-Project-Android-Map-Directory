package com.mapdir.app.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapdir.app.ui.components.EmptyState
import com.mapdir.app.ui.components.ErrorState
import com.mapdir.app.ui.components.LoadingOverlay
import com.mapdir.app.ui.components.PlaceCard
import com.mapdir.app.ui.components.SearchField
import com.mapdir.app.ui.components.UiState

/**
 * Place List screen — shows places filtered by a category (or all).
 *
 * Includes a search field and handles Loading/Empty/Error states.
 *
 * @param onPlaceClick navigates to Detail screen
 * @param onBackClick pops the back stack
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(
    onPlaceClick: (placeId: Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlaceListViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val placesState by viewModel.placesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.categoryName,
                        style = MaterialTheme.typography.titleLarge
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search ─────────────────────────────────────────────────────
            SearchField(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQuery,
                placeholder = "Cari di ${viewModel.categoryName}..."
            )

            // ── Place list ─────────────────────────────────────────────────
            when (placesState) {
                is UiState.Loading -> LoadingOverlay(modifier = Modifier.weight(1f))
                is UiState.Empty   -> EmptyState(
                    message = "Tidak ada tempat ditemukan di kategori ini.",
                    modifier = Modifier.weight(1f)
                )
                is UiState.Error   -> ErrorState(
                    message = (placesState as UiState.Error).message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.weight(1f)
                )
                is UiState.Success -> {
                    val places = (placesState as UiState.Success).data
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(places, key = { it.id }) { place ->
                            PlaceCard(
                                place = place,
                                onClick = { onPlaceClick(place.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
