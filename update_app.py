import re

with open("app/src/main/java/com/strangerhelp/app/StrangerHelpApp.kt", "r") as f:
    content = f.read()

# Add ImageLoaderFactory to imports
imports = """import coil.ImageLoaderFactory
import coil.ImageLoader
import com.strangerhelp.app.utils.DataUriFetcher"""

content = content.replace("import android.app.Application", f"import android.app.Application\n{imports}")

# Change class signature
content = content.replace("class StrangerHelpApp : Application() {", "class StrangerHelpApp : Application(), ImageLoaderFactory {")

# Add newImageLoader() method
new_method = """
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory())
            }
            .build()
    }
"""
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + new_method + "\n}"

with open("app/src/main/java/com/strangerhelp/app/StrangerHelpApp.kt", "w") as f:
    f.write(content)

