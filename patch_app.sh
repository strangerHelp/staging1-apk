sed -i 's/setupBackgroundSync()/setupBackgroundSync()\n        com.strangerhelp.app.util.MapHelper.initMap(this)/g' app/src/main/java/com/strangerhelp/app/StrangerHelpApp.kt
