package net.metalbrain.paysmart.navigator

import android.util.Log
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

private const val NavGuardTag = "NavGuard"

fun NavHostController.graphStartDestinationIdOrNull(): Int? {
    return runCatching { graph.findStartDestination().id }.getOrNull()
}

fun NavHostController.navigateSafely(
    route: String,
    currentRoute: String?,
    source: String,
    suppressSameRoute: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    val normalizedRoute = route.trim()
    if (normalizedRoute.isEmpty()) {
        Log.w(NavGuardTag, "skip_empty_route source=$source")
        return
    }

    if (graphStartDestinationIdOrNull() == null) {
        Log.w(
            NavGuardTag,
            "skip_navigation_graph_not_ready source=$source target=$normalizedRoute current=$currentRoute"
        )
        return
    }

    if (suppressSameRoute && currentRoute == normalizedRoute) {
        Log.d(
            NavGuardTag,
            "skip_duplicate_navigation source=$source route=$normalizedRoute"
        )
        return
    }

    runCatching {
        navigate(normalizedRoute, builder)
    }.onFailure { throwable ->
        Log.e(
            NavGuardTag,
            "navigate_failed source=$source target=$normalizedRoute current=$currentRoute",
            throwable
        )
    }
}

fun NavHostController.navigateInGraph(
    route: String,
    source: String = "nav_graph",
    suppressSameRoute: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigateSafely(
        route = route,
        currentRoute = currentDestination?.route,
        source = source,
        suppressSameRoute = suppressSameRoute,
        builder = builder,
    )
}

fun NavHostController.navigateClearingBackStackInGraph(
    route: String,
    source: String = "nav_graph",
    inclusive: Boolean = true,
    suppressSameRoute: Boolean = false,
) {
    navigateClearingBackStackSafely(
        route = route,
        currentRoute = currentDestination?.route,
        source = source,
        inclusive = inclusive,
        suppressSameRoute = suppressSameRoute,
    )
}

fun NavHostController.navigateClearingBackStackSafely(
    route: String,
    currentRoute: String?,
    source: String,
    inclusive: Boolean = true,
    suppressSameRoute: Boolean = false,
) {
    val startDestinationId = graphStartDestinationIdOrNull()
    if (startDestinationId == null) {
        Log.w(
            NavGuardTag,
            "skip_clear_back_stack_navigation_graph_not_ready source=$source target=$route current=$currentRoute"
        )
        return
    }

    navigateSafely(
        route = route,
        currentRoute = currentRoute,
        source = source,
        suppressSameRoute = suppressSameRoute,
    ) {
        popUpTo(startDestinationId) {
            this.inclusive = inclusive
        }
        launchSingleTop = true
    }
}
