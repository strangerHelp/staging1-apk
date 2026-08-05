import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.expressions.Expression.*

fun getFeatureCollection(): FeatureCollection {
    val feature = Feature.fromGeometry(Point.fromLngLat(77.0, 28.0))
    feature.addBooleanProperty("isHelper", true)
    return FeatureCollection.fromFeatures(listOf(feature))
}

fun createMarkerBitmap(isHelper: Boolean): android.graphics.Bitmap {
    val size = 64
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    
    paint.color = if (isHelper) android.graphics.Color.parseColor("#00E676") else android.graphics.Color.parseColor("#FF9800")
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 6f
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    
    return bitmap
}

fun setStyleProperties(symbolLayer: SymbolLayer) {
    symbolLayer.setProperties(
        iconImage(switchCase(get("isHelper"), literal("marker-helper"), literal("marker-task"))),
        iconAllowOverlap(true),
        iconIgnorePlacement(true),
        iconSize(
            interpolate(
                exponential(1.5f), zoom(),
                stop(10f, 0.5f),
                stop(15f, 1.0f),
                stop(18f, 2.0f)
            )
        )
    )
}
