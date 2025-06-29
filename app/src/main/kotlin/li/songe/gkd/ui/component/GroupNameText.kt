package li.songe.gkd.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
<<<<<<< HEAD
<<<<<<< HEAD
=======
import androidx.compose.material3.LocalContentColor
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
import androidx.compose.material3.LocalContentColor
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
<<<<<<< HEAD
<<<<<<< HEAD
=======
import androidx.compose.ui.graphics.takeOrElse
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
import androidx.compose.ui.graphics.takeOrElse
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import li.songe.gkd.ui.icon.SportsBasketball
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast

@Composable
fun GroupNameText(
    modifier: Modifier = Modifier,
    preText: String? = null,
    isGlobal: Boolean,
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    clickDisabled: Boolean = false,
) {
    if (isGlobal) {
        val text = remember(preText, text) {
            buildAnnotatedString {
                if (preText != null) {
                    append(preText)
                }
                appendInlineContent("icon")
                append(text)
            }
        }
<<<<<<< HEAD
<<<<<<< HEAD
        val fontSize = style.fontSize
        val inlineContent = remember(fontSize, clickDisabled) {
            mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(
                        width = fontSize,
                        height = fontSize,
=======
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
        val inlineContent = remember(style, clickDisabled, textColor) {
            mapOf(
                "icon" to InlineTextContent(
                    placeholder = Placeholder(
                        width = style.fontSize,
                        height = style.lineHeight,
<<<<<<< HEAD
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = SportsBasketball,
                        modifier = Modifier
                            .runIf(!clickDisabled) {
                                clickable(onClick = throttle { toast("当前是全局规则组") })
                            }
                            .fillMaxSize(),
                        contentDescription = null,
<<<<<<< HEAD
<<<<<<< HEAD
=======
                        tint = textColor
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
=======
                        tint = textColor
>>>>>>> e09569e3b7493617a264aa7f7a0bd9903daa1b52
                    )
                }
            )
        }
        Text(
            modifier = modifier,
            text = text,
            inlineContent = inlineContent,
            style = style,
            color = color,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
        )
    } else {
        Text(
            modifier = modifier,
            text = if (preText.isNullOrEmpty()) {
                text
            } else {
                preText + text
            },
            style = style,
            color = color,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
        )
    }
}
