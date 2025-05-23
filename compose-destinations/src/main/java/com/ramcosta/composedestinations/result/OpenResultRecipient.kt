package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/**
 * Recipient where you can install a listener to be notified of results (of type [R])
 * from an non-determined [com.ramcosta.composedestinations.spec.DestinationSpec].
 * !!! ATTENTION !!! - prefer [ResultRecipient]. Use this only when your recipient is
 * in a module that doesn't depend on the corresponding [ResultBackNavigator].
 *
 * If declared as a parameter of a `@Destination` annotated Composable, you need to
 * manually call that Composable and pass the correct typed [ResultRecipient] by calling
 * [com.ramcosta.composedestinations.result.resultRecipient] with the correct type arguments.
 *
 * Type safety related limitations (compile time enforced):
 * - [R] must be a valid navigation argument type.
 *
 * @see [com.ramcosta.composedestinations.result.ResultBackNavigator]
 * @see [ResultRecipient]
 */
@Stable
interface OpenResultRecipient<R> {

    /**
     * Install a [listener] that will be called when the origin destination
     * finishes. [NavResult] will either contain a specific result [R] for [NavResult.Value]
     * or it will be [NavResult.Canceled] if that destination finishes without setting any result.
     *
     * Implementation makes sure to only do something the first time it is called,
     * so, no need to worry about recomposition.
     *
     * [listener] will not be called from a Compose scope, it should be treated
     * as a normal button click listener, you can navigate or call a method on a view model,
     * for example.
     */
    @Composable
    fun onNavResult(listener: @DisallowComposableCalls (NavResult<R>) -> Unit)

    /**
     * Same as [onNavResult] but allows you to control when the result will be delivered.
     * By default, with [onNavResult] it is delivered on first opportunity between ON_RESUME and ON_START,
     * because in some scenarios, ON_RESUME or ON_START are not triggered.
     *
     * However, if in your specific use case, you find that one works better than the other, you can
     * control it by passing specifically [DeliverResultOn.START] or [DeliverResultOn.RESUME]
     */
    @Composable
    fun onNavResult(
        deliverResultOn: DeliverResultOn,
        listener: @DisallowComposableCalls (NavResult<R>) -> Unit
    )

    enum class DeliverResultOn {
        START,
        RESUME,
        FIRST_OPPORTUNITY
    }
}

/**
 * Like [OpenResultRecipient.onNavResult] but using a different API that may feel
 * better in some cases.
 *
 * If you don't need to do anything when the result sender screen is cancelled
 * without any value, you can:
 * ```
 *     screenResult.onResult { resultValue ->
 *         println("got result $resultValue")
 *     }
 * ```
 *
 * Otherwise:
 * ```
 *     screenResult.onResult(
 *         onValue = { resultValue ->
 *             println("got result $resultValue")
 *         },
 *         onCancelled = {
 *             println("did not get result")
 *         }
 *    )
 * ```
 */
@Composable
fun <R> OpenResultRecipient<R>.onResult(
    onCancelled: () -> Unit = {},
    onValue: (R) -> Unit,
) {
    val lambda: (NavResult<R>) -> Unit = remember(onValue, onCancelled) {
        {
            when (it) {
                NavResult.Canceled -> onCancelled()
                is NavResult.Value -> onValue(it.value)
            }
        }
    }
    onNavResult(lambda)
}