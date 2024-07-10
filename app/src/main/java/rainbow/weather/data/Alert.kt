package rainbow.weather.data

import java.util.Date

data class Alert(
    val description: String,
    val issueTime: Date,
    val endTime: Date,
    val content: String
)
