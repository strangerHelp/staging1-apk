import os

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error loading home data", e)
            }"""

replacement = """            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e("FeedViewModel", "Error loading home data", e)
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
