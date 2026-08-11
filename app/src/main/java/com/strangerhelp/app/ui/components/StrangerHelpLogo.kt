package com.strangerhelp.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.Saffron

@Composable
fun StrangerHelpLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Logo",
            tint = Primary,
            modifier = Modifier.size(size)
        )
        Box(
            modifier = Modifier
                .padding(bottom = size * 0.125f)
                .size(size * 0.42f)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Handshake,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(size * 0.32f)
            )
        }
    }
}
