with open("app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationViewModel.kt", "r") as f:
    content = f.read()

old_code = """            } catch (e: Exception) {
                // Ignore error
            }"""
new_code = """            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Error marking all as read", e)
            }"""

content = content.replace(old_code, new_code)
with open("app/src/main/java/com/strangerhelp/app/ui/screens/notifications/NotificationViewModel.kt", "w") as f:
    f.write(content)
