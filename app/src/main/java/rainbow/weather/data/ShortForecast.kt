package rainbow.weather.data

import java.util.Date

data class ShortForecast(
    val timeList: List<Date>,
    val tList: List<Int>,
    val wxList: List<Pair<String, String>>,
    val atList: List<Int>,
    val popList: List<Int>,
    val rhList: List<Int>
)
