package com.example.shytext

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shytext.ui.theme.ShyTextTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShyTextTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        Card {
                            ShyText(
                                text = stringResource(R.string.sample_text),
                                moreText = "...Read More",
                                visibleLines = 3,
                                duration = 500,
                                redactedList = listOf("Lorem", "sit", "mattis"),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShyText(
    text: String,
    moreText: String = "...",
    visibleLines: Int,
    duration: Int = 0,
    redactedList: List<String> = listOf(),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val textMeasurer = rememberTextMeasurer()
        var isHidden by remember { mutableStateOf(true) }

        val measuredText =
            textMeasurer.measure(
                AnnotatedString(text),
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints.fixedWidth(constraints.maxWidth),
                style = TextStyle(
                    fontSize = 18.sp,
                )
            )

        if (measuredText.lineCount <= visibleLines) {
            Text(text, style = TextStyle(fontSize = 18.sp))
        } else {

            val hiddenMeasurement =
                textMeasurer.measure(
                    AnnotatedString(text),
                    overflow = TextOverflow.Ellipsis,
                    constraints = Constraints.fixedWidth(constraints.maxWidth),
                    style = TextStyle(
                        fontSize = 18.sp,
                    ),
                    maxLines = visibleLines
                )
            var canvasModifier: Modifier = modifier
                .height(if (isHidden) with(LocalDensity.current) { hiddenMeasurement.size.height.toDp() } else with(
                    LocalDensity.current
                ) { measuredText.size.height.toDp() })
                .fillMaxWidth()
                .clickable {
                    isHidden = !isHidden
                }
            if (duration != 0) {
                val transition = updateTransition(isHidden, label = "heightTransition")
                val height by transition.animateDp(
                    label = "animatedHeight",
                    transitionSpec = { tween(durationMillis = duration) }
                ) { hidden ->
                    if (hidden) {
                        with(LocalDensity.current) { hiddenMeasurement.size.height.toDp() }
                    } else {
                        with(LocalDensity.current) { measuredText.size.height.toDp() }
                    }
                }
                canvasModifier = modifier
                    .height(height)
                    .fillMaxWidth()
                    .clickable {
                        isHidden = !isHidden
                    }
            }

            Canvas(
                canvasModifier
            ) {

                val endOffset = measuredText.getLineEnd(visibleLines - 1, true)
                val endBoundingBox = measuredText.getCursorRect(endOffset - moreText.length)
                drawText(
                    textMeasurer,
                    if (isHidden) text.substring(
                        0,
                        measuredText.getLineStart(visibleLines) - moreText.length - 1
                    ) else text,
                    style = TextStyle(fontSize = 18.sp)
                )
                if (isHidden) drawText(
                    textMeasurer,
                    moreText,
                    topLeft = Offset(endBoundingBox.left, endBoundingBox.top),
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )

                if (redactedList.isNotEmpty()) {
                    val boundingBoxes = measuredText.getBoundingBoxList(text, redactedList)
                    if (boundingBoxes.isNotEmpty()) {
                        boundingBoxes.map {
                            drawRect(
                                color = Color.Black,
                                topLeft = it.topLeft,
                                size = Size(it.width, it.height)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Stable
fun TextLayoutResult.getBoundingBoxList(input: String, list: List<String>): List<Rect> {
    var indexList = mutableListOf<Int>()
    val result = mutableListOf<Rect>()
    for (text in list) {
        indexList.add(input.indexOf(text))
        indexList.add(input.indexOf(text[text.length-1], startIndex = input.indexOf(text)))
    }
    if (indexList.size % 2 == 0) {
        for (i in 0..<indexList.size step 2) {
            val start = getCursorRect(indexList[i])
            val end = getCursorRect(indexList[i + 1]+1)
            result.add(Rect(topLeft = start.topLeft, bottomRight = end.bottomRight))
        }
    }
    return result
}