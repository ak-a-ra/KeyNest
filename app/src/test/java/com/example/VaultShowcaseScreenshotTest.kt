package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*
import com.example.core.model.ApiKeyItem
import com.example.feature.vault.ApiKeyCard
import com.example.feature.vault.KeyCardActions
import com.example.feature.vault.VaultTagFilterCarousel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class VaultShowcaseScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun showcase_screenshot() {
        val mockItems = listOf(
            ApiKeyItem(
                id = 1,
                title = "OpenAI Prod Key",
                provider = "OpenAI",
                category = "AI Models",
                apiKey = "sample_openai_key_demo_12345",
                tags = "production, llm, gpt-4",
                isPinned = true,
                colorHex = VaultCardColor.SAGE.hex ?: "",
                copyCount = 42
            ),
            ApiKeyItem(
                id = 2,
                title = "Stripe Payments Live",
                provider = "Stripe",
                category = "Payments",
                apiKey = "sample_stripe_key_demo_67890",
                tags = "billing, live",
                isPinned = true,
                colorHex = VaultCardColor.DEFAULT.hex ?: "",
                copyCount = 12
            ),
            ApiKeyItem(
                id = 3,
                title = "AWS S3 Access",
                provider = "AWS",
                category = "Cloud",
                apiKey = "sample_aws_key_demo_13579",
                tags = "storage, infra",
                isPinned = false,
                colorHex = VaultCardColor.STORM.hex ?: "",
                copyCount = 5
            ),
            ApiKeyItem(
                id = 4,
                title = "Gemini API v1",
                provider = "Gemini",
                category = "AI Models",
                apiKey = "sample_gemini_key_demo_24680",
                tags = "testing, gemini-pro",
                isPinned = false,
                colorHex = VaultCardColor.CORAL.hex ?: "",
                copyCount = 1
            )
        )

        composeTestRule.setContent {
            KeyNestTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("KeyNest Vault", color = TextPrimary) },
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                                }
                            },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                                }
                                IconButton(onClick = {}) {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List View", tint = TextPrimary)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = ObsidianBg
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {},
                            containerColor = CyberCyan,
                            contentColor = ObsidianBg
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                    containerColor = ObsidianBg
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        VaultTagFilterCarousel(
                            tags = listOf("AI Models", "production", "testing", "billing", "storage"),
                            selectedTag = null,
                            onlyFavorites = false,
                            favoritesCount = 2,
                            onTagSelected = {},
                            onClearTagFilter = {},
                            onToggleFavorites = {}
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mockItems) { item ->
                                ApiKeyCard(
                                    item = item,
                                    actions = KeyCardActions(
                                        onClick = {},
                                        onCopy = {},
                                        onTogglePin = {},
                                        onTagClick = {}
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/vault_showcase.png")
    }
}
