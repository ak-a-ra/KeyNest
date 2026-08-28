# AGENTS.md — Feature Vault

## Scope

Governs `com.example.feature.vault`: `VaultHomeScreen.kt`, `VaultViewModel.kt`, `VaultApiKeyCard.kt`, `VaultComponents.kt`, `VaultDrawerContent.kt`, `VaultEmptyState.kt`, `VaultTagFilterCarousel.kt`, `VaultTopSearchBar.kt`, `VaultTrashView.kt`, and `VaultClipboardBanners.kt`.

## Responsibilities

- Main vault view, search bar, list/grid feed, tag filter carousel, navigation drawer, trash bin, and primary ViewModel state.

## Invariants

- Support responsive multi-pane layouts via `ListDetailPaneScaffold` on expanded screens.
- Implement responsive clipboard toast clear countdowns and sensitive content protection.
