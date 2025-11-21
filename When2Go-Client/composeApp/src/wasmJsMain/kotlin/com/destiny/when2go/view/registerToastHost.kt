import androidx.compose.runtime.Composable
import multiplatform.network.cmptoast.ToastHost

@Composable
actual fun registerToastHost() = ToastHost()