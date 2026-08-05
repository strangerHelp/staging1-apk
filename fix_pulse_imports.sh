sed -i '1,2d' app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt
sed -i '/package /a \
import androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.strangerhelp.app.data.model.HelpRequest' app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt
