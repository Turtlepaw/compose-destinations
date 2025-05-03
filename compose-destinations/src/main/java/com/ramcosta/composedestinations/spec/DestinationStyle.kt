package com.ramcosta.composedestinations.spec

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.ComposeNavigatorDestinationBuilder
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.DialogNavigatorDestinationBuilder
import androidx.navigation.get
import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.manualcomposablecalls.allDeepLinks
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.AnimatedDestinationScopeImpl
import com.ramcosta.composedestinations.scope.DestinationScopeImpl

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
abstract class DestinationStyle {

    abstract fun <T> NavGraphBuilder.addComposable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls
    )

    /**
     * This is the default style used in case none is specified for a given Destination.
     *
     * Its animations will be inherited from the ones set at the navigation graph level,
     * using `@NavGraph(defaultTransitions = SomeClass::class)`, if the destination belongs to
     * a graph defined this way, or the [com.ramcosta.composedestinations.DestinationsNavHost]'s
     * `defaultTransitions` parameter for the top level "NavHost Graph".
     */
    object Default : DestinationStyle() {
        override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            @Suppress("UNCHECKED_CAST")
            val contentWrapper = manualComposableCalls[destination.route] as? DestinationLambda<T>?

            destination(
                ComposeNavigatorDestinationBuilder(
                    navigator = provider[ComposeNavigator::class],
                    route = destination.route,
                    content = { navBackStackEntry ->
                        CallComposable(
                            destination,
                            navController,
                            navBackStackEntry,
                            dependenciesContainerBuilder,
                            contentWrapper,
                        )
                    }
                ).apply {
                    label = destination.label
                    destination.arguments.forEach { (argumentName, argument) -> argument(argumentName, argument) }
                    destination.allDeepLinks(manualComposableCalls).forEach { deepLink -> deepLink(deepLink) }
                }
            )
        }
    }

    /**
     * Marks the destination to have defined enter/exit transitions
     * when coming from or going to certain destinations.
     *
     * You will need to create an object which implements this interface
     * and use its KClass in [com.ramcosta.composedestinations.annotation.Destination.style]
     */
    abstract class Animated : DestinationStyle() {

        open val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?
            get() = null
        open val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?
            get() = null
        open val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?
            get() = enterTransition
        open val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?
            get() = exitTransition
        open val sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?
            get() = null

        /**
         * Can be used to force no animations for certain destinations, if you've overridden
         * the default animation with `defaultAnimationParams`.
         */
        object None : Animated() {
            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = { EnterTransition.None }

            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = { ExitTransition.None }
        }

        final override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            destination(
                ComposeNavigatorDestinationBuilder(
                    navigator = provider[ComposeNavigator::class],
                    route = destination.route,
                    content = { navBackStackEntry ->
                        @Suppress("UNCHECKED_CAST")
                        val contentWrapper = manualComposableCalls[destination.route] as? DestinationLambda<T>?

                        CallComposable(
                            destination,
                            navController,
                            navBackStackEntry,
                            dependenciesContainerBuilder,
                            contentWrapper,
                        )
                    }
                ).apply {
                    label = destination.label
                    destination.arguments.forEach { (argumentName, argument) -> argument(argumentName, argument) }
                    destination.allDeepLinks(manualComposableCalls).forEach { deepLink -> deepLink(deepLink) }
                    this.enterTransition = this@Animated.enterTransition
                    this.exitTransition = this@Animated.exitTransition
                    this.popEnterTransition = this@Animated.popEnterTransition
                    this.popExitTransition = this@Animated.popExitTransition
                    this.sizeTransform = this@Animated.sizeTransform
                }
            )
        }
    }

    /**
     * Marks the destination to be shown as a dialog.
     *
     * You can create implementations that define specific [DialogProperties]
     * or you can use the default values with `style = DestinationStyle.Dialog::class`
     */
    abstract class Dialog : DestinationStyle() {
        abstract val properties: DialogProperties

        companion object Default : Dialog() {
            override val properties = DialogProperties()
        }

        final override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            @Suppress("UNCHECKED_CAST")
            val contentLambda = manualComposableCalls[destination.route] as? DestinationLambda<T>?

            destination(
                DialogNavigatorDestinationBuilder(
                    navigator = provider[DialogNavigator::class],
                    route = destination.route,
                    dialogProperties = properties,
                    content = { navBackStackEntry ->
                        CallDialogComposable(
                            destination,
                            navController,
                            navBackStackEntry,
                            dependenciesContainerBuilder,
                            contentLambda
                        )
                    }
                ).apply {
                    destination.arguments.forEach { (argumentName, argument) -> argument(argumentName, argument) }
                    destination.allDeepLinks(manualComposableCalls).forEach { deepLink -> deepLink(deepLink) }
                    label = destination.label
                }
            )
        }
    }

    @InternalDestinationsApi
    object Activity: DestinationStyle() {
        override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            destination as ActivityDestinationSpec<T>

            addComposable(destination, manualComposableCalls)
        }

        internal fun <T> NavGraphBuilder.addComposable(
            destination: ActivityDestinationSpec<T>,
            manualComposableCalls: ManualComposableCalls? = null
        ) {
            activity(destination.route) {
                targetPackage = destination.targetPackage
                activityClass = destination.activityClass?.kotlin
                action = destination.action
                data = destination.data
                dataPattern = destination.dataPattern
                label = destination.label

                destination.allDeepLinks(manualComposableCalls).forEach { deepLink ->
                    deepLink {
                        action = deepLink.action
                        uriPattern = deepLink.uriPattern
                        mimeType = deepLink.mimeType
                    }
                }

                destination.arguments.forEach { navArg ->
                    argument(navArg.name) {
                        if (navArg.argument.isDefaultValuePresent) {
                            defaultValue = navArg.argument.defaultValue
                        }
                        type = navArg.argument.type
                        nullable = navArg.argument.isNullable
                    }
                }
            }
        }

    }
}

@Composable
private fun <T> CallDialogComposable(
    destination: TypedDestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?
) {
    val scope = remember(destination, navBackStackEntry, navController, dependenciesContainerBuilder) {
        DestinationScopeImpl.Default(
            destination,
            navBackStackEntry,
            navController,
            dependenciesContainerBuilder,
        )
    }

    if (contentWrapper == null) {
        with(destination) { scope.Content() }
    } else {
        contentWrapper(scope)
    }
}

@Composable
private fun <T> AnimatedVisibilityScope.CallComposable(
    destination: TypedDestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?,
) {

    val scope = remember(destination, navBackStackEntry, navController, this, dependenciesContainerBuilder) {
        AnimatedDestinationScopeImpl(
            destination,
            navBackStackEntry,
            navController,
            this,
            dependenciesContainerBuilder
        )
    }

    if (contentWrapper == null) {
        with(destination) { scope.Content() }
    } else {
        contentWrapper(scope)
    }
}
