cat << 'INNER_EOF' >> app/build.gradle.kts

ksp {
    arg("room.generateKotlin", "true")
}
INNER_EOF
