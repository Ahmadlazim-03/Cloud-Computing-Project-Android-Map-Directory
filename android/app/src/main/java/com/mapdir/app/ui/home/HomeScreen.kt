package com.mapdir.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.mapdir.app.data.model.Category
import com.mapdir.app.data.model.Place
import com.mapdir.app.ui.components.CategoryChip
import com.mapdir.app.ui.components.EmptyState
import com.mapdir.app.ui.components.ErrorState
import com.mapdir.app.ui.components.LoadingOverlay
import com.mapdir.app.ui.components.PlaceCard
import com.mapdir.app.ui.components.SearchField
import com.mapdir.app.ui.components.UiState

/**
 * Home screen — the main entry point of the app.
 *
 * Layout (top to bottom):
 * 1. TopAppBar with app title
 * 2. Search field
 * 3. Horizontally scrollable category chips
 * 4. Vertical list of place cards
 *
 * @param onPlaceClick navigates to Detail screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlaceClick: (placeId: Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val placesState by viewModel.placesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Map Directory",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
            // ── Search bar ─────────────────────────────────────────────────
            SearchField(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQuery
            )

            // ── Category chips ─────────────────────────────────────────────
            CategoryChipsRow(
                state = categoriesState,
                selectedSlug = selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Section header ─────────────────────────────────────────────
            Text(
                text = "Tempat Populer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ── Places list ────────────────────────────────────────────────
            PlacesList(
                state = placesState,
                onPlaceClick = onPlaceClick,
                onRetry = viewModel::retry,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryChipsRow(
    state: UiState<List<Category>>,
    selectedSlug: String?,
    onCategorySelected: (String?) -> Unit
) {
    when (state) {
        is UiState.Loading -> { /* chips will appear once loaded */ }
        is UiState.Error   -> { /* silently skip — places error will show */ }
        is UiState.Empty   -> { /* no categories — skip */ }
        is UiState.Success -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(state.data, key = { it.id }) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedSlug == category.slug,
                        onSelected = { onCategorySelected(category.slug) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlacesList(
    state: UiState<List<Place>>,
    onPlaceClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is UiState.Loading -> LoadingOverlay(modifier = modifier)
        is UiState.Empty   -> EmptyState(
            message = "Tidak ada tempat ditemukan.\nCoba ubah pencarian atau filter.",
            modifier = modifier
        )
        is UiState.Error   -> ErrorState(
            message = state.message,
            onRetry = onRetry,
            modifier = modifier
        )
        is UiState.Success -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.data, key = { it.id }) { place ->
                    PlaceCard(
                        place = place,
                        onClick = { onPlaceClick(place.id) }
                    )
                }
            }
        }
    }
}
