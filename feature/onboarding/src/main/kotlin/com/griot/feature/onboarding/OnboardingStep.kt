package com.griot.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griot.core.design.R as DesignR
import com.griot.core.design.component.BackdropShape
import com.griot.core.design.component.backdrop
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

/** Number of steps after the welcome screen: name, language, sync and import. */
internal const val ONBOARDING_STEPS = 4

private val ScreenPadding = 28.dp
private val TitleTop = 48.dp
private val ContentTop = 36.dp
private val ProgressHeight = 5.dp
private val ProgressGap = 6.dp
private val BackOffset = (-10).dp

// Background shapes, as fractions of the screen size, from the step mockups: magenta at the top right, turquoise at
// the bottom left.
private const val MAGENTA_X = 0.92f
private const val MAGENTA_Y = 0.1f
private const val TURQUOISE_X = 0.08f
private const val TURQUOISE_Y = 0.72f
private const val SHAPE_RADIUS = 0.54f

/**
 * One step of the onboarding after the welcome screen: back, the progress, the [title] and its [body], the step's
 * [content], and its [actions] at the bottom, which stay above the keyboard.
 *
 * @param step Rank of the step, from 1 to [ONBOARDING_STEPS].
 * @param actions The buttons at the bottom, usually one `GriotButton`.
 */
// A layout of every step: its back, its texts and its two slots, all needed.
@Suppress("LongParameterList")
@Composable
internal fun OnboardingStep(
    step: Int,
    onBack: () -> Unit,
    title: String,
    body: String,
    actions: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = GriotTheme.palette
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .backdrop(
                listOf(
                    BackdropShape(
                        color = palette.accent.copy(alpha = palette.blobAlpha),
                        x = MAGENTA_X,
                        y = MAGENTA_Y,
                        radius = SHAPE_RADIUS,
                    ),
                    BackdropShape(
                        color = palette.sync.copy(alpha = palette.blobAlpha),
                        x = TURQUOISE_X,
                        y = TURQUOISE_Y,
                        radius = SHAPE_RADIUS,
                    ),
                ),
            )
            .safeDrawingPadding()
            .imePadding()
            .padding(start = ScreenPadding, top = GriotSpacing.s, end = ScreenPadding, bottom = ScreenPadding),
    ) {
        Header(step = step, onBack = onBack)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(TitleTop))
            Text(text = title, style = MaterialTheme.typography.headlineLarge, color = palette.ink)
            Spacer(Modifier.height(GriotSpacing.m))
            Text(text = body, style = MaterialTheme.typography.bodyLarge, color = palette.ink2)
            Spacer(Modifier.height(ContentTop))
            content()
            Spacer(Modifier.height(GriotSpacing.l))
        }
        Column(verticalArrangement = Arrangement.spacedBy(GriotSpacing.l), content = actions)
    }
}

@Composable
private fun Header(step: Int, onBack: () -> Unit) {
    val palette = GriotTheme.palette
    val description = stringResource(R.string.onboarding_step, step, ONBOARDING_STEPS)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GriotSpacing.m)) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = BackOffset).size(GriotSpacing.touchTarget)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_back),
                contentDescription = stringResource(R.string.onboarding_back),
                tint = palette.ink,
            )
        }
        Row(
            modifier = Modifier.weight(1f).semantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(ProgressGap),
        ) {
            repeat(ONBOARDING_STEPS) { index ->
                val color by animateColorAsState(
                    targetValue = if (index < step) palette.accent else palette.track,
                    animationSpec = GriotTheme.motion.colorChange(),
                    label = "progress",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(ProgressHeight)
                        .background(color, RoundedCornerShape(ProgressHeight / 2)),
                )
            }
        }
        Text(
            text = stringResource(R.string.onboarding_progress, step, ONBOARDING_STEPS),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = palette.muted,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = GriotSpacing.xxl),
        )
    }
}
