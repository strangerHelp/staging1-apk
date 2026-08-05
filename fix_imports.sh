for f in app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt; do
    sed -i '1,4d' "$f"
    sed -i '/package /a \
import com.google.accompanist.permissions.ExperimentalPermissionsApi\nimport com.google.accompanist.permissions.isGranted\nimport com.google.accompanist.permissions.rememberPermissionState\nimport android.Manifest' "$f"
done
