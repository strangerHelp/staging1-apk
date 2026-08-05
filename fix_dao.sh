sed -i '/suspend fun clearTasks/i \    @Query("DELETE FROM tasks WHERE _id = :id")\n    suspend fun deleteTaskById(id: String): Int\n' app/src/main/java/com/strangerhelp/app/data/local/dao/TaskDao.kt
