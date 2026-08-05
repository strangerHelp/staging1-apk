cat build.gradle.kts | sed 's/id("org.jetbrains.kotlin.plugin.compose").*/&\n    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false/' > temp.kts
mv temp.kts build.gradle.kts
cat app/build.gradle.kts | sed '/id("org.jetbrains.kotlin.plugin.compose")/a\    id("com.google.devtools.ksp")' > app/temp.kts
mv app/temp.kts app/build.gradle.kts
gradle compileDebugKotlin
