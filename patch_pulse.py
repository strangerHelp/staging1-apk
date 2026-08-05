import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt'
with open(path, 'r') as f:
    content = f.read()

# Replace bitmap generation with helper and task bitmaps
bitmap_code = """
                            val helperBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                                val canvas = android.graphics.Canvas(this)
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.parseColor("#00E676") }
                                canvas.drawCircle(24f, 24f, 20f, paint)
                                paint.color = android.graphics.Color.WHITE
                                canvas.drawCircle(24f, 24f, 8f, paint)
                            }
                            style.addImage("helper-marker", helperBitmap)

                            val taskBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                                val canvas = android.graphics.Canvas(this)
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.parseColor("#FF9800") }
                                canvas.drawCircle(24f, 24f, 20f, paint)
                                paint.color = android.graphics.Color.WHITE
                                paint.textSize = 24f
                                paint.textAlign = android.graphics.Paint.Align.CENTER
                                canvas.drawText("!", 24f, 32f, paint)
                            }
                            style.addImage("task-marker", taskBitmap)
"""
content = content.replace('val defaultMarkerBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {\n                                val canvas = android.graphics.Canvas(this)\n                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.RED }\n                                canvas.drawCircle(24f, 24f, 24f, paint)\n                            }\n                            style.addImage("marker-icon", defaultMarkerBitmap)', bitmap_code.strip())

# Replace iconImage
content = content.replace('iconImage("marker-icon")', 'iconImage(org.maplibre.android.style.expressions.Expression.match(org.maplibre.android.style.expressions.Expression.get("type"), org.maplibre.android.style.expressions.Expression.literal("helper-marker"), org.maplibre.android.style.expressions.Expression.stop("helper", "helper-marker"), org.maplibre.android.style.expressions.Expression.stop("task", "task-marker")))')

with open(path, 'w') as f:
    f.write(content)
