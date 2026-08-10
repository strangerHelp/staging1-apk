import os

path = 'app/src/main/java/com/strangerhelp/app/navigation/AppNavigation.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''fun AppNavigation(user: User, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    
    Scaffold('''

replacement = '''import com.strangerhelp.app.ui.components.LocalSnackbarHostState

@Composable
fun AppNavigation(user: User, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    
    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },'''

target2 = '''            }
        }
    }
}'''

replacement2 = '''            }
        }
    }
    }
}'''

if target in content:
    content = content.replace(target, replacement)
    content = content.replace("    Scaffold(", replacement, 1) # Just in case target is slightly different
    
    # We need to find the last closing bracket of Scaffold and close CompositionLocalProvider
    # Let's use a simpler replace strategy:
    
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Not found.")
