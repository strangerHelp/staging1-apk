package com.strangerhelp.app.data.repository

import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.local.dao.TaskDao
import com.strangerhelp.app.data.model.Task
import com.strangerhelp.app.data.model.sanitized
import com.strangerhelp.app.data.model.ClaimTaskRequest
import com.strangerhelp.app.data.model.ClaimResponse
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

val DEFAULT_SEED_TASKS: List<Task> = listOf(
    Task(
        _id = "8559f42316dd3a3f3d3080ba",
        title = "Bike Service & Delivery",
        description = "Need bike service and delivery from shop",
        category = "Task",
        budget = 1500,
        deadline = "Tomorrow",
        location = "Chennai",
        city = "Chennai",
        lat = 13.1396159,
        lng = 80.138263,
        status = "open",
        posterId = "ae0287050347d4c7e5a94227",
        posterName = "Vadivelan Site",
        urgent = 1,
        priority = "High",
        maxClaimers = 1,
        createdAt = "2026-08-16 17:56:09"
    ),
    Task(
        _id = "dd23127c7a080f2522a5d2fc",
        title = "Requirements - 10 boys Volunteers",
        description = "Event date: Aug 31- Sept 4. Login Time: 8am. 750+ food provided. Venue: Manyatha Tech Park",
        category = "Event / Group Work",
        budget = 750,
        deadline = "Today",
        location = "Manyatha Tech Park",
        city = "Bangalore",
        status = "open",
        posterId = "543a2adbf4cc043fcf9843b2",
        posterName = "Roshan Ahmed",
        urgent = 1,
        priority = "High",
        maxClaimers = 10,
        createdAt = "2026-08-26 09:53:22"
    ),
    Task(
        _id = "56615445624d854abf28a0d6",
        title = "Fix plumbing in kitchen",
        description = "Need someone to fix the leaky pipe under kitchen sink",
        category = "Task",
        budget = 100,
        deadline = "Today",
        location = "Bangalore",
        city = "Bangalore",
        status = "open",
        posterId = "2407c891b726f74e4337353e",
        posterName = "testuser",
        urgent = 0,
        priority = "Low",
        maxClaimers = 1,
        createdAt = "2026-08-30 13:10:29"
    ),
    Task(
        _id = "99999880d3047b205a7d6fe5",
        title = "Laminated sheets installation",
        description = "Urgent requirement for laminated sheets installation in Ramamurthy Nagar",
        category = "Other",
        budget = 1500,
        deadline = "Today",
        location = "Ramamurthy Nagar",
        city = "Bangalore",
        status = "open",
        posterName = "Anonymous",
        urgent = 1,
        priority = "High",
        maxClaimers = 1,
        createdAt = "2026-08-26 06:13:39"
    ),
    Task(
        _id = "0447f4f372a9c2364181485c",
        title = "Entry exit guidance",
        description = "Need volunteers for BookMyShow Activity in Mall",
        category = "Event / Group Work",
        budget = 700,
        deadline = "Tomorrow",
        location = "Garuda Mall & Mall of Asia",
        city = "Bangalore",
        status = "open",
        posterId = "3d46e9207cad0e7dc2742176",
        posterName = "Ravi Kiran",
        urgent = 0,
        priority = "Medium",
        maxClaimers = 12,
        createdAt = "2026-08-23 15:29:58"
    )
)

fun filterSeedTasks(category: String?, query: String?): List<Task> {
    val cleanCat = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
    val cleanQuery = if (query.isNullOrBlank()) null else query.trim()
    return DEFAULT_SEED_TASKS.filter { t ->
        (cleanCat == null || t.category.equals(cleanCat, ignoreCase = true)) &&
        (cleanQuery == null || t.title.contains(cleanQuery, ignoreCase = true) ||
         t.description.contains(cleanQuery, ignoreCase = true) ||
         t.location.contains(cleanQuery, ignoreCase = true) ||
         t.city.contains(cleanQuery, ignoreCase = true))
    }
}

data class TasksLoadResult(
    val tasks: List<Task>,
    val isOffline: Boolean = false,
    val isFromCache: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String? = null
)

class TaskRepository(
    private val api: StrangerHelpApi,
    private val taskDao: TaskDao? = null
) {
    private val effectiveTaskDao: TaskDao?
        get() = taskDao ?: runCatching { StrangerHelpApp.instance.database.taskDao() }.getOrNull()

    /**
     * Reactive stream of cached tasks from Room database.
     */
    fun observeCachedTasks(category: String? = null, query: String? = null): Flow<List<Task>> {
        val dao = effectiveTaskDao ?: return flowOf(filterSeedTasks(category, query))
        val cleanCat = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
        val cleanQuery = if (query.isNullOrBlank()) null else query.trim()
        return dao.getFilteredTasks(cleanCat, cleanQuery)
    }

    /**
     * Directly fetch cached tasks from Room database with seed fallback.
     */
    suspend fun getCachedTasks(category: String? = null, query: String? = null): List<Task> {
        val cleanCat = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
        val cleanQuery = if (query.isNullOrBlank()) null else query.trim()
        val dao = effectiveTaskDao
        val list = if (dao != null) {
            try {
                if (cleanCat == null && cleanQuery == null) {
                    dao.getAllTasksList()
                } else {
                    dao.getFilteredTasksList(cleanCat, cleanQuery)
                }
            } catch (e: Exception) {
                AppLogger.e("TaskRepository", "Error getting cached tasks", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        if (list.isEmpty()) {
            val fallback = filterSeedTasks(cleanCat, cleanQuery)
            if (fallback.isNotEmpty()) {
                saveTasksToCache(fallback)
            }
            return fallback
        }
        return list
    }

    /**
     * Save tasks into local Room cache.
     */
    suspend fun saveTasksToCache(tasks: List<Task>) {
        if (tasks.isEmpty()) return
        try {
            val safeTasks = tasks.map { it.sanitized() }
            effectiveTaskDao?.insertTasks(safeTasks)
        } catch (e: Exception) {
            AppLogger.e("TaskRepository", "Failed to cache tasks to Room", e)
        }
    }

    /**
     * Fetch tasks from API, automatically caching them in Room upon success,
     * and seamlessly falling back to Room cached tasks when offline or on network failure.
     */
    suspend fun getTasks(
        category: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        sort: String = if (lat != null && lng != null) "distance" else "newest"
    ): TasksLoadResult {
        val cleanCat = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
        val cleanQuery = if (search.isNullOrBlank()) null else search.trim()

        try {
            val apiLimit = if (cleanQuery != null) 50 else limit
            val response = api.getTasks(
                category = cleanCat,
                limit = apiLimit,
                offset = offset,
                search = cleanQuery,
                q = cleanQuery,
                sort = sort,
                lat = lat,
                lng = lng
            )

            if (response.isSuccessful) {
                val rawTasks = response.body() ?: emptyList()
                AppLogger.d("TaskRepository", "Successfully fetched ${rawTasks.size} tasks from API (category=$cleanCat, search=$cleanQuery, sort=$sort)")
                val fetchedTasks = rawTasks.map { raw ->
                    val sanitized = raw.sanitized()
                    if (lat != null && lng != null && sanitized.lat != null && sanitized.lng != null && (sanitized.distance == null || sanitized.distance == 0.0)) {
                        sanitized.copy(distance = calculateDistanceKm(lat, lng, sanitized.lat, sanitized.lng))
                    } else {
                        sanitized
                    }
                }
                if (fetchedTasks.isNotEmpty()) {
                    saveTasksToCache(fetchedTasks)
                }

                val filteredTasks = if (cleanQuery != null) {
                    fetchedTasks.filter { t ->
                        t.title.contains(cleanQuery, ignoreCase = true) ||
                        t.description.contains(cleanQuery, ignoreCase = true) ||
                        t.location.contains(cleanQuery, ignoreCase = true) ||
                        t.city.contains(cleanQuery, ignoreCase = true) ||
                        t.posterName.contains(cleanQuery, ignoreCase = true)
                    }
                } else {
                    fetchedTasks
                }

                return TasksLoadResult(
                    tasks = filteredTasks,
                    isOffline = false,
                    isFromCache = false,
                    hasMore = fetchedTasks.size >= apiLimit
                )
            } else {
                // HTTP error, log full error response and fallback to Room database cache
                val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                AppLogger.e(
                    "TaskRepository",
                    "HTTP ${response.code()} ${response.message()} loading tasks (cat=$cleanCat, search=$cleanQuery). Response body: $errBody"
                )
                val cached = getCachedTasks(cleanCat, cleanQuery)
                return TasksLoadResult(
                    tasks = cached,
                    isOffline = true,
                    isFromCache = true,
                    hasMore = false,
                    errorMessage = "Server error (${response.code()}). Showing cached tasks."
                )
            }
        } catch (e: Exception) {
            AppLogger.e("TaskRepository", "Network failure fetching tasks: ${e.javaClass.simpleName} - ${e.message}", e)
            val cached = getCachedTasks(cleanCat, cleanQuery)
            return TasksLoadResult(
                tasks = cached,
                isOffline = true,
                isFromCache = true,
                hasMore = false,
                errorMessage = e.localizedMessage ?: "Offline mode: viewing cached tasks"
            )
        }
    }

    suspend fun getMyTasks(filter: String = "all"): Response<List<Task>> {
        val queryMap = mutableMapOf(
            "mine" to "true",
            "limit" to "100"
        )
        val response = api.getTasksWithQueryMap(queryMap)
        if (response.isSuccessful) {
            response.body()?.let { tasks ->
                AppLogger.d("TaskRepository", "Successfully fetched ${tasks.size} user tasks from API")
                saveTasksToCache(tasks)
            }
        } else {
            val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
            AppLogger.w("TaskRepository", "Failed to fetch user tasks: HTTP ${response.code()} $errBody")
        }
        return response
    }

    suspend fun claimTask(taskId: String, offeredBudget: Int?, message: String?): Response<ClaimResponse> {
        val request = ClaimTaskRequest(action = "claim", offeredBudget = offeredBudget, message = message)
        return api.claimTask(taskId, request)
    }
    
    suspend fun getTask(taskId: String): Response<Task> {
        val cleanId = taskId.trim().removeSurrounding("\"").substringBefore('?').substringBefore('#').trimEnd('/')
        return try {
            val response = api.getTask(cleanId)
            if (response.isSuccessful) {
                response.body()?.let { task ->
                    AppLogger.d("TaskRepository", "Successfully loaded task $cleanId from API")
                    try {
                        effectiveTaskDao?.insertTask(task.sanitized())
                    } catch (e: Exception) {
                        AppLogger.e("TaskRepository", "Failed to cache single task $cleanId in Room", e)
                    }
                }
            } else {
                val errBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                AppLogger.w("TaskRepository", "Failed to load task $cleanId: HTTP ${response.code()} $errBody")
            }
            response
        } catch (e: Exception) {
            AppLogger.w("TaskRepository", "Network failure loading task $cleanId: ${e.message}")
            val cached = try { effectiveTaskDao?.getTaskById(cleanId) } catch (_: Exception) { null }
                ?: DEFAULT_SEED_TASKS.find { it._id == cleanId }
            if (cached != null) {
                Response.success(cached)
            } else {
                throw e
            }
        }
    }

    suspend fun getTaskFromCache(taskId: String): Task? {
        val cleanId = taskId.trim().removeSurrounding("\"").substringBefore('?').substringBefore('#').trimEnd('/')
        return try {
            effectiveTaskDao?.getTaskById(cleanId) ?: DEFAULT_SEED_TASKS.find { it._id == cleanId }
        } catch (e: Exception) {
            AppLogger.w("TaskRepository", "Error getting task from cache for $cleanId: ${e.message}")
            DEFAULT_SEED_TASKS.find { it._id == cleanId }
        }
    }

    fun observeTaskFromCache(taskId: String): Flow<Task?> {
        val cleanId = taskId.trim().removeSurrounding("\"").substringBefore('?').substringBefore('#').trimEnd('/')
        return effectiveTaskDao?.observeTaskById(cleanId) ?: flowOf(null)
    }
    
    suspend fun createConversation(body: Map<String, String>) = api.createConversation(body)
    
    suspend fun patchTask(taskId: String, body: Map<String, Any?>): Response<Map<String, Any>> {
        val safeBody = body.filterValues { it != null }.mapValues { it.value as Any }
        return api.updateTracking(taskId, safeBody)
    }
    
    suspend fun submitProof(taskId: String, proofFiles: List<File>): Response<Map<String, Any>> {
        val action = "complete".toRequestBody("text/plain".toMediaTypeOrNull())
        val firstFile = proofFiles.firstOrNull()
        val part = firstFile?.let {
            val reqBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("proof", it.name, reqBody)
        }
        return api.completeTask(taskId, action, part)
    }

    suspend fun submitProofBytes(taskId: String, proofBytes: ByteArray): Response<Map<String, Any>> {
        val action = "complete".toRequestBody("text/plain".toMediaTypeOrNull())
        val reqBody = proofBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, proofBytes.size)
        val part = MultipartBody.Part.createFormData("proof", "proof.jpg", reqBody)
        return api.completeTask(taskId, action, part)
    }
    
    suspend fun deleteTask(taskId: String): Response<Map<String, Any>> {
        return api.deleteTask(taskId)
    }
}

