package com.strangerhelp.app.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition

object MapHelper {
    private const val TAG = "MapHelper"
    const val DEFAULT_STYLE = "https://tiles.openfreemap.org/styles/liberty"

    private val _offlineDownloadProgress = MutableStateFlow<Float?>(null)
    val offlineDownloadProgress: StateFlow<Float?> = _offlineDownloadProgress.asStateFlow()

    fun initMap(context: Context) {
        try {
            MapLibre.getInstance(context)
            // Initialize SQLite-based ambient caching
            val manager = OfflineManager.getInstance(context)
            manager.setMaximumAmbientCacheSize(100 * 1024 * 1024, object : OfflineManager.FileSourceCallback {
                override fun onSuccess() {
                    Log.d(TAG, "Ambient cache size set successfully")
                }
                override fun onError(message: String) {
                    Log.e(TAG, "Error setting ambient cache size: $message")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MapLibre", e)
        }
    }

    fun downloadOfflineRegion(context: Context, center: LatLng, radiusKm: Double = 10.0, regionName: String = "LocalRegion") {
        try {
            val manager = OfflineManager.getInstance(context)
            
            // Calculate a rough bounding box
            val latOffset = radiusKm / 111.0
            val lngOffset = radiusKm / (111.0 * Math.cos(Math.toRadians(center.latitude)))
            
            val bounds = LatLngBounds.Builder()
                .include(LatLng(center.latitude + latOffset, center.longitude + lngOffset))
                .include(LatLng(center.latitude - latOffset, center.longitude - lngOffset))
                .build()
                
            val pixelRatio = context.resources.displayMetrics.density
            val minZoom = 10.0
            val maxZoom = 15.0
            
            val definition = OfflineTilePyramidRegionDefinition(
                DEFAULT_STYLE,
                bounds,
                minZoom,
                maxZoom,
                pixelRatio
            )
            
            val metadata = try {
                val jsonObject = JSONObject()
                jsonObject.put("FIELD_REGION_NAME", regionName)
                jsonObject.toString().toByteArray(Charsets.UTF_8)
            } catch (e: Exception) {
                ByteArray(0)
            }
            
            _offlineDownloadProgress.value = 0f
            manager.createOfflineRegion(definition, metadata, object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(offlineRegion: OfflineRegion) {
                    Log.d(TAG, "Offline region created successfully: $regionName")
                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                    
                    offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                        override fun onStatusChanged(status: OfflineRegionStatus) {
                            val percentage = if (status.requiredResourceCount >= 0) {
                                (100f * status.completedResourceCount / status.requiredResourceCount)
                            } else 0f
                            
                            if (status.isComplete) {
                                Log.d(TAG, "Offline region download complete: $regionName")
                                _offlineDownloadProgress.value = null // Done
                            } else {
                                Log.d(TAG, "Offline region downloading: $percentage%")
                                _offlineDownloadProgress.value = percentage
                            }
                        }
                        
                        override fun onError(error: OfflineRegionError) {
                            Log.e(TAG, "Offline region download error: ${error.reason}, ${error.message}")
                            _offlineDownloadProgress.value = null
                        }
                        
                        override fun mapboxTileCountLimitExceeded(limit: Long) {
                            Log.e(TAG, "Offline region tile count limit exceeded: $limit")
                            _offlineDownloadProgress.value = null
                        }
                    })
                }
                
                override fun onError(error: String) {
                    Log.e(TAG, "Error creating offline region: $error")
                    _offlineDownloadProgress.value = null
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating offline region download", e)
            _offlineDownloadProgress.value = null
        }
    }
}
