sed -i 's/implementation("org.osmdroid:osmdroid-android:6.1.18")/implementation("org.maplibre.gl:android-sdk:11.11.0")/' app/build.gradle.kts
gradle dependencies | grep maplibre
