package com.griot.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.griot.core.design.component.AnimatedPresence
import com.griot.core.design.component.JumpToTopButton
import com.griot.core.design.component.ScrollControls
import com.griot.core.design.component.TopControls
import com.griot.core.design.component.rememberScrollControls
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import kotlinx.coroutines.launch

/**
 * The editor's layout: the top bar and the lyrics, with the song's and the sections' menus anchored to their « ⋯ »
 * ([menu] is the one open). Scrolling down the song hides the top bar and shows « Haut de la chanson »; scrolling up,
 * reaching the top or focusing a verse brings it back.
 */
@Suppress("LongParameterList") // One callback per change the editor makes, and the menus' items.
@Composable
internal fun EditorContent(
    uiState: EditorUiState,
    onBack: () -> Unit,
    verseInput: VerseInput,
    menu: EditorMenu?,
    onMenu: (EditorMenu?) -> Unit,
    songMenu: @Composable ColumnScope.() -> Unit,
    sectionMenu: @Composable ColumnScope.(EditorSection) -> Unit,
    onAddSection: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onShowConflict: () -> Unit,
    onDismissConflicts: () -> Unit,
) {
    val list = rememberLazyListState()
    val controls = rememberScrollControls(list)
    val scope = rememberCoroutineScope()
    // A verse taking the focus brings the controls back.
    val input = remember(verseInput, controls) {
        verseInput.copy(
            onFocused = { verseId ->
                controls.show()
                verseInput.onFocused(verseId)
            },
        )
    }
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorHeader(
                uiState = uiState,
                shown = controls.shown,
                menuOpen = menu == EditorMenu.Song,
                onBack = onBack,
                onMenu = onMenu,
                songMenu = songMenu,
                onShowConflict = onShowConflict,
                onDismissConflicts = onDismissConflicts,
            )
            if (!uiState.loading) {
                EditorLyrics(
                    uiState = uiState,
                    list = list,
                    controls = controls,
                    verseInput = input,
                    menu = menu,
                    onMenu = onMenu,
                    sectionMenu = sectionMenu,
                    onAddSection = onAddSection,
                    onFontSizeChange = onFontSizeChange,
                )
            }
        }
        JumpToTop(
            shown = !controls.shown,
            onClick = {
                controls.show()
                scope.launch { list.animateScrollToItem(0) }
            },
        )
    }
}

// The top bar and the conflict notice, which go up out of the screen while the author scrolls down the song.
@Suppress("LongParameterList") // The song, whether it shows, its menu, and one callback per action.
@Composable
private fun EditorHeader(
    uiState: EditorUiState,
    shown: Boolean,
    menuOpen: Boolean,
    onBack: () -> Unit,
    onMenu: (EditorMenu?) -> Unit,
    songMenu: @Composable ColumnScope.() -> Unit,
    onShowConflict: () -> Unit,
    onDismissConflicts: () -> Unit,
) {
    val motion = GriotTheme.motion
    TopControls(shown = shown) {
        Column {
            EditorTopBar(
                title = uiState.title,
                language = uiState.language,
                languageDetected = uiState.languageDetected,
                menuOpen = menuOpen,
                onBack = onBack,
                onMenu = { onMenu(EditorMenu.Song) },
                onMenuDismiss = { onMenu(null) },
                menu = songMenu,
            )
            AnimatedPresence(
                value = uiState.conflicts.size.takeIf { it > 0 },
                enter = motion.itemEnter(),
                exit = motion.itemExit(),
                modifier = Modifier.padding(start = GriotSpacing.l, top = GriotSpacing.s, end = GriotSpacing.l),
            ) { ConflictNotice(count = it, onShow = onShowConflict, onDismiss = onDismissConflicts) }
        }
    }
}

// The lyrics: each section's header and verses, then « Ajouter une section »; the author's scrolls drive [controls].
@Suppress("LongParameterList") // The song, its list, its menus, and how it is typed and resized.
@Composable
private fun EditorLyrics(
    uiState: EditorUiState,
    list: LazyListState,
    controls: ScrollControls,
    verseInput: VerseInput,
    menu: EditorMenu?,
    onMenu: (EditorMenu?) -> Unit,
    sectionMenu: @Composable ColumnScope.(EditorSection) -> Unit,
    onAddSection: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
) {
    LazyColumn(
        state = list,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(controls.connection)
            .pinchToResize(size = uiState.fontSize, onSizeChange = onFontSizeChange),
        contentPadding = PaddingValues(
            start = GriotSpacing.l,
            top = GriotSpacing.xl,
            end = GriotSpacing.l,
            bottom = GriotSpacing.xxl * 3,
        ),
    ) {
        uiState.sections.forEachIndexed { index, section ->
            val sectionMenuOf = EditorMenu.Section(section.section.id)
            sectionItems(
                section = section,
                first = index == 0,
                fontSize = uiState.fontSize,
                showRhymes = uiState.showRhymes,
                focus = uiState.focus,
                verseInput = verseInput,
                menuOpen = menu == sectionMenuOf,
                onActions = { onMenu(sectionMenuOf) },
                onMenuDismiss = { onMenu(null) },
                menu = { sectionMenu(section) },
            )
        }
        item(key = "add") {
            AddSectionButton(onClick = onAddSection, modifier = Modifier.animateItem().padding(top = SectionGap))
        }
    }
}

// « Haut de la chanson », while the top bar is hidden.
@Composable
private fun BoxScope.JumpToTop(shown: Boolean, onClick: () -> Unit) {
    val motion = GriotTheme.motion
    AnimatedVisibility(
        visible = shown,
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomEnd).padding(GriotSpacing.l),
    ) { JumpToTopButton(label = stringResource(R.string.editor_jump_to_top), onClick = onClick) }
}
