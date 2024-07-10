package rainbow.weather.data

import org.json.JSONObject
import rainbow.weather.toList

data class County(
    val name: String,
    val shortId: String,
    val longId: String,
    val townships: List<Township>
) {
    companion object {
        fun fromJson(json: JSONObject) = County(
            json.getString("name"),
            json.getString("shortId"),
            json.getString("longId"),
            json.getJSONArray("townships").toList(Township::fromJson),
        )
    }
}
