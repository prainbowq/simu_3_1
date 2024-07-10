package rainbow.weather.data

import org.json.JSONObject
import rainbow.weather.toList

data class Township(
    val name: String,
    val stations: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject) = Township(
            name = json.getString("name"),
            stations = json.getJSONArray("stations").toList()
        )
    }
}
