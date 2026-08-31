import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "r") as f:
    text = f.read()

text = text.replace("""    val logo = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo)
    }""", """    val logo = remember {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_strangerhelp_logo)
        if (drawable != null) {
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 100,
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 100,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        }
    }""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "w") as f:
    f.write(text)
