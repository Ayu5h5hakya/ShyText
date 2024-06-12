package com.example.shytext

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shytext.ui.theme.ShyTextTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sampletext =
            "longTextSample asdasdas asdasd asdasdas dasd asd asd asdasd asd as adasd asd redacted ada sd asdasd asd  asdas d asda sd as  asdasdasd asdas asd asdasda asdasd asdsadasdasdasda sdfsdfs dsdfs dfsd fsdfdd sdfsdfs dfsdfergdbddcbff tbfgnjkyuk ykyukhjk,uiluifhf g edrgdfgdrtgr ttrthgfbhfghrtyfhbfg rt hrthfg ffff   fghfghrtyrthfgn s"

        enableEdgeToEdge()
        setContent {
            ShyTextTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShyText(
                        text = sampletext,
                        moreText = "...See More",
                        visibleLines = 3,
                        modifier = Modifier.padding(innerPadding)
                    )
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
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val textMeasurer = rememberTextMeasurer()
        val measuredText =
            textMeasurer.measure(
                AnnotatedString(text),
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints.fixed(constraints.maxWidth, constraints.maxHeight),
                style = TextStyle(fontSize = 18.sp)
            )

        Canvas(
            modifier
                .height((measuredText.firstBaseline * visibleLines).dp)
                .fillMaxWidth()
        ) {

            val endOffset = measuredText.getLineEnd(visibleLines - 1, true)+1
            val endBoundingBox = measuredText.getCursorRect(endOffset - moreText.length)
            drawText(
                textMeasurer,
                text.substring(0, measuredText.getLineStart(visibleLines) - moreText.length),
                style = TextStyle(fontSize = 18.sp)
            )
            drawText(
                textMeasurer,
                moreText,
                topLeft = Offset(endBoundingBox.left, endBoundingBox.top),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}