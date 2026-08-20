import re

with open('app/src/main/java/com/strangerhelp/app/StrangerHelpApp.kt', 'r') as f:
    content = f.read()

target1 = """        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()"""

replacement1 = """        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true) // Battery-saving mode: reduces background task intensity
            .build()"""

content = content.replace(target1, replacement1)

target2 = """        setupBackgroundSync()
        com.strangerhelp.app.util.MapHelper.initMap(this)"""

replacement2 = """        setupBackgroundSync()
        com.strangerhelp.app.utils.BatteryMonitor.init(this)
        com.strangerhelp.app.util.MapHelper.initMap(this)"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/strangerhelp/app/StrangerHelpApp.kt', 'w') as f:
    f.write(content)
