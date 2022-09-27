package co.anbora.labs.todo.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

/**
 * A [Disposable] which knows its own "disposed" status.
 * Usually you don't need this class if you properly registered your [Disposable] in disposable hierarchy via [Disposer.register],
 * because then
 *  1. your Disposable would be disposed automatically along with its parent and
 *  1. you wouldn't need to worry about potential race conditions when [.isDisposed] is called during the object disposal process
 *
 *
 *
 * If however you (reluctantly) do need this class, be aware of additional memory consumption for storing extra "isDisposed" information.
 *
 *
 * To obtain the instance of this class, use [Disposer.newCheckedDisposable]
 */
interface CheckedDisposable : Disposable {
    /**
     * @return true when this instance is disposed (i.e. [Disposer.dispose] was called on this,
     * or it was registered in the [Disposer] hierarchy with [Disposer.register] and its parent was disposed)
     */
    fun isDisposed(): Boolean
}