package jp.com.ymj.gtihubclient

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KType
import kotlin.time.Duration

inline fun <reified T : Any> NavGraphBuilder.restorableComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        null,
    noinline exitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        null,
    noinline popEnterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        enterTransition,
    noinline popExitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        exitTransition,
    noinline sizeTransform:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    SizeTransform?)? =
        null,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry, isRestored: Boolean) -> Unit
) {
    composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform,
    ) { backStackEntry ->
        val key = "restored"

        val isRestored =
            backStackEntry.savedStateHandle.get<Boolean>(key)

        DisposableEffect(Unit) {
            onDispose {
                backStackEntry.savedStateHandle[key] = true
            }
        }
        content(backStackEntry, isRestored ?: false)
    }
}

class InvokeDebounce(
    private val debounceTimeMs: Duration,
    private val call: () -> Unit
) {
    private var debounceJob: Job? = null

    operator fun invoke() {
        debounceJob?.cancel()
        debounceJob = CoroutineScope(Dispatchers.Main).launch {
            delay(debounceTimeMs)
            call()
        }
    }
}

@Composable
fun rememberRetainedCoroutineScope(
): CoroutineScope {
    val viewModelFactory = remember {
        viewModelFactory {
            addInitializer(ViewModel::class) {
                object : ViewModel() {}
            }
        }
    }
    val viewModel: ViewModel =
        viewModel(modelClass = ViewModel::class, factory = viewModelFactory)

    return viewModel.viewModelScope
}
