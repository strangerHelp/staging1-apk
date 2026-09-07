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
    Icon(
        painter = androidx.compose.ui.res.painterResource(id = com.strangerhelp.app.R.drawable.ic_logo_brand_image),
        contentDescription = "StrangerHelp Logo",
        tint = Color.Unspecified, // Important: don't tint to preserve the original colors
        modifier = modifier.size(size)
    )
}