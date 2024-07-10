package rainbow.weather

import org.json.JSONArray
import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
fun <T> JSONArray.toList() = List(length(), ::get) as List<T>

fun <T> JSONArray.toList(factory: (JSONObject) -> T) = List(length(), ::getJSONObject).map(factory)
