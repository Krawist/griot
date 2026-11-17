package com.griot.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import com.griot.core.design.component.BottomControls
import com.griot.core.design.component.JumpToTopButton
import com.griot.core.design.component.ScrollControls
import com.griot.core.design.component.TopControls
import com.griot.core.design.component.rememberScrollControls
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme
import kotlinx.coroutines.launch

/**
 * The editor's layout: the top bar, the lyrics and the floating toolbar. Scrolling down the song hides the bars and
 * shows « Haut de la chanson »; scrolling up, reaching the top or focusing a verse brings them back.
 */
@Suppress("LongParameterList") // One callback per change the editor makes.
@Composable
internal fun EditorContent(
    uiState: EditorUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    verseInput: VerseInput,
    onSectionActions: (String) -> Unit,
    onAddSection: () -> Unit,
    onFontSize: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onLanguage: () -> Unit,
    onMenu: () -> Unit,
    onShowRhymesChange: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    onAiHelp: () -> Unit,
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
                onBack = onBack,
                onTitleChange = onTitleChange,
                onFontSize = onFontSize,
                onLanguage = onLanguage,
                onMenu = onMenu,
                onShowConflict = onShowConflict,
                onDismissConflicts = onDismissConflicts,
            )
            if (!uiState.loading) {
                EditorLyrics(
                    uiState = uiState,
                    list = list,
                    controls = controls,
                    verseInput = input,
                    onSectionActions = onSectionActions,
                    onAddSection = onAddSection,
                    onFontSizeChange = onFontSizeChange,
                )
            }
        }
        FloatingControls(
            uiState = uiState,
            shown = controls.shown,
            onAddSection = onAddSection,
            onShowRhymesChange = onShowRhymesChange,
            onAiHelp = onAiHelp,
            onOpenHistory = onOpenHistory,
            onJumpToTop = {
                controls.show()
                scope.launch { list.animateScrollToItem(0) }
            },
        )
    }
}

// The top bar and the conflict notice, which go up out of the screen while the author scrolls down the song.
@Suppress("LongParameterList") // The song, whether it shows, and one callback per action.
@Composable
private fun EditorHeader(
    uiState: EditorUiState,
    shown: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onFontSize: () -> Unit,
    onLanguage: () -> Unit,
    onMenu: () -> Unit,
    onShowConflict: () -> Unit,
    onDismissConflicts: () -> Unit,
) {
    val motion = GriotTheme.motion
    TopControls(shown = shown) {
        Column {
            EditorTopBar(
                title = uiState.title,
                language = uiState.language,
                detectedLanguage = uiState.detectedLanguage,
                onBack = onBack,
                onTitleChange = onTitleChange,
                onFontSize = onFontSize,
                onLanguage = onLanguage,
                onMenu = onMenu,
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
@Suppress("LongParameterList") // The song, its list, and how it is typed and resized.
@Composable
private fun EditorLyrics(
    uiState: EditorUiState,
    list: LazyListState,
    controls: ScrollControls,
    verseInput: VerseInput,
    onSectionActions: (String) -> Unit,
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
            bottom = GriotSpacing.xxl + EditorToolbarHeight + GriotSpacing.xl,
        ),
    ) {
        uiState.sections.forEachIndexed { index, section ->
            sectionItems(
                section = section,
                first = index == 0,
                fontSize = uiState.fontSize,
                showRhymes = uiState.showRhymes,
                focus = uiState.focus,
                verseInput = verseInput,
                onActions = { onSectionActions(section.section.id) },
            )
        }
        item(key = "add") {
            AddSectionButton(onClick = onAddSection, modifier = Modifier.animateItem().padding(top = SectionGap))
        }
    }
}

// The toolbar, which goes down while the author scrolls down the song, and then « Haut de la chanson » in its place.
@Suppress("LongParameterList") // The song, whether the controls show, and one callback per button.
@Composable
private fun BoxScope.FloatingControls(
    uiState: EditorUiState,
    shown: Boolean,
    onAddSection: () -> Unit,
    onShowRhymesChange: (Boolean) -> Unit,
    onAiHelp: () -> Unit,
    onOpenHistory: () -> Unit,
    onJumpToTop: () -> Unit,
) {
    val motion = GriotTheme.motion
    BottomControls(shown = shown, modifier = Modifier.align(Alignment.BottomCenter).padding(GriotSpacing.l)) {
        EditorToolbar(
            showRhymes = uiState.showRhymes,
            onAddSection = onAddSection,
            onShowRhymesChange = onShowRhymesChange,
            onAiHelp = onAiHelp,
            onOpenHistory = onOpenHistory,
        )
    }
    AnimatedVisibility(
        visible = !shown,
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomEnd).padding(GriotSpacing.l),
    ) { JumpToTopButton(label = stringResource(R.string.editor_jump_to_top), onClick = onJumpToTop) }
}
