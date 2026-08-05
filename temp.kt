package com.strangerhelp.app.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.Notification
import kotlinx.coroutines.launch
import com.strangerhelp.app.ui.components.shimmerEffect
import androidx.compose.ui.draw.clip
        }
        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }
}
}
