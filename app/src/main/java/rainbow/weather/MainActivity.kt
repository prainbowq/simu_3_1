package rainbow.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "alert",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Companion.from(AlertWorker::class.java)
            )
        val viewModel by viewModels<MainViewModel> { MainViewModel.Factory(this) }
        setContent {
            viewModel.screen?.invoke() ?: finish()
            BackHandler(onBack = viewModel::pop)
        }
    }
}
