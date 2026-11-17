package com.griot.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
 * reaching the top or focusing a verse brings it back. The [verseSheet] of the selected verse sits at the bottom, and
 * the lyrics scroll above it.
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
    verseSheet: @Composable (Modifier) -> Unit,
) {
    val list = rememberLazyListState()
    val density = LocalDensity.current
    // The height of the verse's sheet: the lyrics scroll above it.
    var measuredSheet by remember { mutableIntStateOf(0) }
    val sheetHeight = if (uiState.selection != null) measuredSheet else 0
    KeepSelectionVisible(list = list, verseId = uiState.selection?.verseId, hiddenHeight = sheetHeight)
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
                    bottom = with(density) { sheetHeight.toDp() },
                )
            }
        }
        SelectedVerseSheet(shown = uiState.selection != null, verseSheet = verseSheet) { measuredSheet = it }
        JumpToTop(
            shown = !controls.shown,
            bottom = with(density) { sheetHeight.toDp() },
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
    bottom: Dp,
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
            bottom = GriotSpacing.xxl * 3 + bottom,
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
                selected = uiState.selection?.verseId,
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

// The sheet of the selected verse, rising from the bottom while one is [shown]; its height goes to [onHeight].
@Composable
private fun BoxScope.SelectedVerseSheet(
    shown: Boolean,
    verseSheet: @Composable (Modifier) -> Unit,
    onHeight: (Int) -> Unit,
) {
    AnimatedVisibility(
        visible = shown,
        enter = GriotTheme.motion.barEnter(),
        exit = GriotTheme.motion.barExit(),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) { verseSheet(Modifier.onSizeChanged { onHeight(it.height) }) }
}

// « Haut de la chanson », while the top bar is hidden, above the verse's sheet.
@Composable
private fun BoxScope.JumpToTop(shown: Boolean, bottom: Dp, onClick: () -> Unit) {
    val motion = GriotTheme.motion
    AnimatedVisibility(
        visible = shown,
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomEnd).padding(GriotSpacing.l).padding(bottom = bottom),
    ) { JumpToTopButton(label = stringResource(R.string.editor_jump_to_top), onClick = onClick) }
}

// Scrolls the list so that the verse [verseId], once selected, is not hidden by the last [hiddenHeight] pixels (the
// verse's sheet) nor cut at the top.
@Composable
private fun KeepSelectionVisible(list: LazyListState, verseId: String?, hiddenHeight: Int) {
    val margin = with(LocalDensity.current) { GriotSpacing.l.roundToPx() }
    LaunchedEffect(verseId, hiddenHeight) {
        if (verseId == null) return@LaunchedEffect
        val info = list.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { (it.key as? String)?.endsWith("/$verseId") == true }
            ?: return@LaunchedEffect
        val bottom = info.viewportEndOffset - hiddenHeight - margin
        val below = item.offset + item.size - bottom
        val above = info.viewportStartOffset - item.offset
        when {
            below > 0 -> list.animateScrollBy(below.toFloat())
            above > 0 -> list.animateScrollBy(-above.toFloat())
        }
    }
}
