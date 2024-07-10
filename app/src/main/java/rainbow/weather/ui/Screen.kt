package rainbow.weather.ui

import androidx.compose.runtime.Composable

interface Screen {
    @Composable
    operator fun invoke()
}
