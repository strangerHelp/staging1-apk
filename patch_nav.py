import os

path = 'app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {'''

replacement = '''    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }

    androidx.compose.runtime.CompositionLocalProvider(com.strangerhelp.app.ui.components.LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {'''

target_end = '''        }
    }
}'''

replacement_end = '''        }
    }
    }
}'''

if target in content:
    content = content.replace(target, replacement)
    content = content.replace(target_end, replacement_end)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Not found target.")
