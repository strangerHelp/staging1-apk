package com.strangerhelp.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.Saffron

@Composable
fun StrangerHelpHeader(
    modifier: Modifier = Modifier,
    logoSize: androidx.compose.ui.unit.Dp = 40.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 28.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StrangerHelpLogo(size = logoSize)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary, fontWeight = FontWeight.Bold, fontSize = textSize)) {
                    append("stranger")
                }
                withStyle(SpanStyle(color = Saffron, fontWeight = FontWeight.Bold, fontSize = textSize)) {
                    append("help")
                }
            }
        )
    }
}
