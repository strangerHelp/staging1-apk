sed -i 's/@OptIn(ExperimentalLayoutApi::class)/@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)/' app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt

awk '/val isLoading by viewModel.isLoading.collectAsState\(\)/ {
    print
    print "    val pullRefreshState = rememberPullToRefreshState()"
    print "    if (pullRefreshState.isRefreshing) {"
    print "        LaunchedEffect(true) {"
    print "            viewModel.loadTasks()"
    print "            pullRefreshState.endRefresh()"
    print "        }"
    print "    }"
    next
}
/LazyColumn\(/ {
    print "    Box(Modifier.fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {"
    print
    next
}
1' app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt
