cat << 'INNEREOF' >> app/src/main/java/com/strangerhelp/app/data/model/Models.kt

data class UserStats(
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val tasksCompleted: Int = 0,
    val completionRate: Int = 0,
    val trustScore: Int = 0
)

data class StatsResponse(val stats: UserStats)
INNEREOF
chmod +x update_models_stats.sh
./update_models_stats.sh