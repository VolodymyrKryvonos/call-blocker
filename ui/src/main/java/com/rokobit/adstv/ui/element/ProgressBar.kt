package com.rokobit.adstv.ui.element

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rokobit.adstv.ui.*

private val roundedShape = RoundedCornerShape(20)

@Composable
fun ProgressBarAsLabel(max: Int, process: Int) = Surface(
    color = secondaryColor,
    shape = roundedShape,
    modifier = Modifier
        .width(progressBarAsLabelWidth)
        .height(progressBarAsLabelHeight)
        .border(
            width = 3.dp,
            color = accentColor,
            shape = roundedShape
        )
) {
    val percent: Int by animateIntAsState(
        targetValue = if (max > 0)
            process * 100 / max
        else
            0
    )

    val pbWidth: Float by animateFloatAsState(
        targetValue = if (max > 0)
                (process * 1f / max)
        else
            0f
    )

    Box {
        Surface(
            modifier = Modifier
                .height(progressBarAsLabelHeight)
                .fillMaxWidth( pbWidth )
                .border(
                    width = 3.dp,
                    color = accentColor,
                    shape = roundedShape
                ),
            color = primaryColor,
            shape = roundedShape) {}
    }

    Box(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$percent%",
            color = accentColor,
            contentAlignment = TextAlign.Center
        )
    }

}