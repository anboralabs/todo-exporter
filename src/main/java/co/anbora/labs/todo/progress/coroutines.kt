package co.anbora.labs.todo.progress

import com.intellij.concurrency.currentThreadContext
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.Computable
import com.intellij.util.concurrency.annotations.RequiresBlockingContext
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@RequiresBlockingContext
@Throws(ProcessCanceledException::class)
fun <T> blockingContextToIndicator(action: () -> T): T {
    val ctx = currentThreadContext()
    return try {
        contextToIndicator(ctx, action)
    }
    catch (pce : ProcessCanceledException) {
        throw pce
    }
    catch (ce: CancellationException) {
        throw RuntimeException(ce)
    }
}

@Throws(CancellationException::class)
private fun <T> contextToIndicator(ctx: CoroutineContext, action: () -> T): T {
    val job = ctx.job
    job.ensureActive()
    val contextModality = ModalityState.nonModal()
    val indicator = EmptyProgressIndicator(contextModality)
    return jobToIndicator(job, indicator, action)
}

@Throws(CancellationException::class)
fun <T> jobToIndicator(job: Job, indicator: ProgressIndicator, action: () -> T): T {
    try {
        return ProgressManager.getInstance().runProcess(Computable {
            // Register handler inside runProcess to avoid cancelling the indicator before even starting the progress.
            // If the Job was canceled while runProcess was preparing,
            // then CompletionHandler is invoked right away and cancels the indicator.
            @OptIn(InternalCoroutinesApi::class)
            val completionHandle = job.invokeOnCompletion(onCancelling = true) {
                if (it is CancellationException) {
                    indicator.cancel()
                }
            }
            try {
                indicator.checkCanceled()
                action()
            }
            finally {
                completionHandle.dispose()
            }
        }, indicator)
    }
    catch (e: ProcessCanceledException) {
        if (job.isCancelled) {
            @OptIn(InternalCoroutinesApi::class)
            throw job.getCancellationException()
        }
        throw e
    }
}