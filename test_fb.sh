echo 'dependencies { implementation("com.google.firebase:firebase-messaging-ktx:23.4.0") }' >> app/build.gradle.kts
gradle :app:compileDebugKotlin
