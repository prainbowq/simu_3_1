package rainbow.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun WeatherIcon(code: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Image(
        painter = painterResource(
            context.resources.getIdentifier("weather_day_$code", "drawable", context.packageName)
        ),
        contentDescription = null,
        modifier = modifier
    )
}
