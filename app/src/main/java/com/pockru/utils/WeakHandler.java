/*
 * Copyright (c) 2014 Badoo Trading Limited
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Portions of documentation in this code are modifications based on work created and
 * shared by Android Open Source Project and used according to terms described in the
 * Apache License, Version 2.0
 */
package com.pockru.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Memory safer implementation of android.os.Handler
 * <p/>
 * Original implementation of Handlers always keeps hard reference to handler in queue of execution. If you create anonymous handler and post delayed message into it, it will keep all parent class for that time in memory even if it could be cleaned.
 * <p/>
 * This implementation is trickier, it will keep WeakReferences to runnables and messages, and GC could collect them once WeakHandler instance is not referenced any more
 * <p/>
 * 
 * @see android.os.Handler
 * 
 *      Created by Dmytro Voronkevych on 17/06/2014.
 */
public class WeakHandler {
	private final Handler.Callback mCallback; // hard reference to Callback. We need to keep callback in memory
	private final ExecHandler mExec;
	private final ChainedRef mRunnables = new ChainedRef(null);

	/**
	 * Default constructor associates this handler with the {@link Looper} for the current thread.
	 * 
	 * If this thread does not have a looper, this handler won't be able to receive messages so an exception is thrown.
	 */
	public WeakHandler() {
		mCallback = null;
		mExec = new ExecHandler();
	}

	/**
	 * Constructor associates this handler with the {@link Looper} for the current thread and takes a callback interface in which you can handle messages.
	 * 
	 * If this thread does not have a looper, this handler won't be able to receive messages so an exception is thrown.
	 * 
	 * @param callback
	 *            The callback interface in which to handle messages, or null.
	 */
	public WeakHandler(@Nullable Handler.Callback callback) {
		mCallback = callback; // Hard referencing body
		mExec = new ExecHandler(new WeakReference<Handler.Callback>(callback)); // Weak referencing inside ExecHandler
	}

	/**
	 * Use the provided {@link Looper} instead of the default one.
	 * 
	 * @param looper
	 *            The looper, must not be null.
	 */
	public WeakHandler(@NonNull Looper looper) {
		mCallback = null;
		mExec = new ExecHandler(looper);
	}

	/**
	 * Use the provided {@link Looper} instead of the default one and take a callback interface in which to handle messages.
	 * 
	 * @param looper
	 *            The looper, must not be null.
	 * @param callback
	 *            The callback interface in which to handle messages, or null.
	 */
	public WeakHandler(@NonNull Looper looper, @NonNull Handler.Callback callback) {
		mCallback = callback;
		mExec = new ExecHandler(looper, new WeakReference<Handler.Callback>(callback));
	}

	/**
	 * Causes the Runnable r to be added to the message queue. The runnable will be run on the thread to which this handler is attached.
	 * 
	 * @param r
	 *            The Runnable that will be executed.
	 * 
	 * @return Returns true if the Runnable was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean post(@NonNull Runnable r) {
		return mExec.post(wrapRunnable(r));
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run at a specific time given by <var>uptimeMillis</var>. <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b> The runnable will be run on the thread to which this handler is attached.
	 * 
	 * @param r
	 *            The Runnable that will be executed.
	 * @param uptimeMillis
	 *            The absolute time at which the callback should run, using the {@link android.os.SystemClock#uptimeMillis} time-base.
	 * 
	 * @return Returns true if the Runnable was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting. Note that a result of true does not mean the Runnable will be processed -- if the looper is quit before the delivery time of the message occurs then the message will be dropped.
	 */
	public final boolean postAtTime(@NonNull Runnable r, long uptimeMillis) {
		return mExec.postAtTime(wrapRunnable(r), uptimeMillis);
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run at a specific time given by <var>uptimeMillis</var>. <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b> The runnable will be run on the thread to which this handler is attached.
	 * 
	 * @param r
	 *            The Runnable that will be executed.
	 * @param uptimeMillis
	 *            The absolute time at which the callback should run, using the {@link android.os.SystemClock#uptimeMillis} time-base.
	 * 
	 * @return Returns true if the Runnable was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting. Note that a result of true does not mean the Runnable will be processed -- if the looper is quit before the delivery time of the message occurs then the message will be dropped.
	 * 
	 * @see android.os.SystemClock#uptimeMillis
	 */
	public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
		return mExec.postAtTime(wrapRunnable(r), token, uptimeMillis);
	}

	/**
	 * Causes the Runnable r to be added to the message queue, to be run after the specified amount of time elapses. The runnable will be run on the thread to which this handler is attached.
	 * 
	 * @param r
	 *            The Runnable that will be executed.
	 * @param delayMillis
	 *            The delay (in milliseconds) until the Runnable will be executed.
	 * 
	 * @return Returns true if the Runnable was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting. Note that a result of true does not mean the Runnable will be processed -- if the looper is quit before the delivery time of the message occurs then the message will be dropped.
	 */
	public final boolean postDelayed(Runnable r, long delayMillis) {
		return mExec.postDelayed(wrapRunnable(r), delayMillis);
	}

	/**
	 * Posts a message to an object that implements Runnable. Causes the Runnable r to executed on the next iteration through the message queue. The runnable will be run on the thread to which this handler is attached. <b>This method is only for use in very special circumstances -- it can easily starve the message queue, cause ordering problems, or have other unexpected side-effects.</b>
	 * 
	 * @param r
	 *            The Runnable that will be executed.
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean postAtFrontOfQueue(Runnable r) {
		return mExec.postAtFrontOfQueue(wrapRunnable(r));
	}

	/**
	 * Remove any pending posts of Runnable r that are in the message queue.
	 */
	public final void removeCallbacks(Runnable r) {
		final ChainedRef runnableRef = mRunnables.findForward(r);
		if (runnableRef != null) {
			mExec.removeCallbacks(runnableRef.wrapper);
		}
	}

	/**
	 * Remove any pending posts of Runnable <var>r</var> with Object <var>token</var> that are in the message queue. If <var>token</var> is null, all callbacks will be removed.
	 */
	public final void removeCallbacks(Runnable r, Object token) {
		final ChainedRef runnableRef = mRunnables.findForward(r);
		if (runnableRef != null) {
			mExec.removeCallbacks(runnableRef.wrapper, token);
		}
	}

	/**
	 * Pushes a message onto the end of the message queue after all pending messages before the current time. It will be received in callback, in the thread attached to this handler.
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean sendMessage(Message msg) {
		return mExec.sendMessage(msg);
	}

	/**
	 * Sends a Message containing only the what value.
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean sendEmptyMessage(int what) {
		return mExec.sendEmptyMessage(what);
	}

	/**
	 * Sends a Message containing only the what value, to be delivered after the specified amount of time elapses.
	 * 
	 * @see #sendMessageDelayed(android.os.Message, long)
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
		return mExec.sendEmptyMessageDelayed(what, delayMillis);
	}

	/**
	 * Sends a Message containing only the what value, to be delivered at a specific time.
	 * 
	 * @see #sendMessageAtTime(android.os.Message, long)
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
		return mExec.sendEmptyMessageAtTime(what, uptimeMillis);
	}

	/**
	 * Enqueue a message into the message queue after all pending messages before (current time + delayMillis). You will receive it in callback, in the thread attached to this handler.
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting. Note that a result of true does not mean the message will be processed -- if the looper is quit before the delivery time of the message occurs then the message will be dropped.
	 */
	public final boolean sendMessageDelayed(Message msg, long delayMillis) {
		return mExec.sendMessageDelayed(msg, delayMillis);
	}

	/**
	 * Enqueue a message into the message queue after all pending messages before the absolute time (in milliseconds) <var>uptimeMillis</var>. <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b> You will receive it in callback, in the thread attached to this handler.
	 * 
	 * @param uptimeMillis
	 *            The absolute time at which the message should be delivered, using the {@link android.os.SystemClock#uptimeMillis} time-base.
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting. Note that a result of true does not mean the message will be processed -- if the looper is quit before the delivery time of the message occurs then the message will be dropped.
	 */
	public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
		return mExec.sendMessageAtTime(msg, uptimeMillis);
	}

	/**
	 * Enqueue a message at the front of the message queue, to be processed on the next iteration of the message loop. You will receive it in callback, in the thread attached to this handler. <b>This method is only for use in very special circumstances -- it can easily starve the message queue, cause ordering problems, or have other unexpected side-effects.</b>
	 * 
	 * @return Returns true if the message was successfully placed in to the message queue. Returns false on failure, usually because the looper processing the message queue is exiting.
	 */
	public final boolean sendMessageAtFrontOfQueue(Message msg) {
		return mExec.sendMessageAtFrontOfQueue(msg);
	}

	/**
	 * Remove any pending posts of messages with code 'what' that are in the message queue.
	 */
	public final void removeMessages(int what) {
		mExec.removeMessages(what);
	}

	/**
	 * Remove any pending posts of messages with code 'what' and whose obj is 'object' that are in the message queue. If <var>object</var> is null, all messages will be removed.
	 */
	public final void removeMessages(int what, Object object) {
		mExec.removeMessages(what, object);
	}

	/**
	 * Remove any pending posts of callbacks and sent messages whose <var>obj</var> is <var>token</var>. If <var>token</var> is null, all callbacks and messages will be removed.
	 */
	public final void removeCallbacksAndMessages(Object token) {
		mExec.removeCallbacksAndMessages(token);
	}

	/**
	 * Check if there are any pending posts of messages with code 'what' in the message queue.
	 */
	public final boolean hasMessages(int what) {
		return mExec.hasMessages(what);
	}

	/**
	 * Check if there are any pending posts of messages with code 'what' and whose obj is 'object' in the message queue.
	 */
	public final boolean hasMessages(int what, Object object) {
		return mExec.hasMessages(what, object);
	}

	public final Looper getLooper() {
		return mExec.getLooper();
	}

	private WeakRunnable wrapRunnable(Runnable r) {
		final ChainedRef hardRef = ChainedRef.obtain(r);
		mRunnables.insertAbove(hardRef);
		return hardRef.wrapper = new WeakRunnable(new WeakReference<Runnable>(r), new WeakReference<ChainedRef>(hardRef));
	}

	private static class ExecHandler extends Handler {
		private final WeakReference<Handler.Callback> mCallback;

		ExecHandler() {
			mCallback = null;
		}

		ExecHandler(WeakReference<Handler.Callback> callback) {
			mCallback = callback;
		}

		ExecHandler(Looper looper) {
			super(looper);
			mCallback = null;
		}

		ExecHandler(Looper looper, WeakReference<Handler.Callback> callback) {
			super(looper);
			mCallback = callback;
		}

		@Override
		public void handleMessage(Message msg) {
			if (mCallback == null) {
				return;
			}
			final Handler.Callback callback = mCallback.get();
			if (callback == null) { // Already disposed
				return;
			}
			callback.handleMessage(msg);
		}
	}

	static class WeakRunnable implements Runnable {
		private final WeakReference<Runnable> mDelegate;
		private final WeakReference<ChainedRef> mReference;

		WeakRunnable(WeakReference<Runnable> delegate, WeakReference<ChainedRef> reference) {
			mDelegate = delegate;
			mReference = reference;
		}

		@Override
		public void run() {
			final Runnable delegate = mDelegate.get();
			final ChainedRef reference = mReference.get();
			if (reference != null) {
				reference.remove();
			}
			if (delegate != null) {
				delegate.run();
			}
		}
	}

	static class ChainedRef {
		@Nullable
		ChainedRef next;
		@Nullable
		ChainedRef prev;
		@Nullable
		Runnable runnable;
		@Nullable
		WeakRunnable wrapper;

		@Nullable
		static ChainedRef sPool;
		static int sPoolSize;
		static final int MAX_POOL_SIZE = 15;

		public ChainedRef(Runnable r) {
			this.runnable = r;
		}

		public void remove() {
			if (prev != null) {
				prev.next = next;
			}
			if (next != null) {
				next.prev = prev;
			}
			this.prev = null;
			this.runnable = null;
			this.wrapper = null;
			synchronized (ChainedRef.class) {
				if (sPoolSize > MAX_POOL_SIZE) {
					return;
				}
				this.next = sPool;
				sPool = this;
				sPoolSize++;
			}
		}

		public void insertAbove(@NonNull ChainedRef candidate) {
			if (this.next != null) {
				this.next.prev = candidate;
			}

			candidate.next = this.next;
			this.next = candidate;
			candidate.prev = this;
		}

		@Nullable
		public ChainedRef findForward(Runnable obj) {
			ChainedRef curr = this;
			while (curr != null) {
				if (curr.runnable != null) {
					if (curr.runnable.equals(obj)) {
						return curr;
					}
				} else if (obj == null) {
					return curr;
				}
				curr = curr.next;
			}
			return null;
		}

		public static ChainedRef obtain(Runnable r) {
			ChainedRef result = null;
			synchronized (ChainedRef.class) {
				if (sPool != null) {
					result = sPool;
					sPool = sPool.next;
					sPoolSize--;
				}
			}
			if (result != null) {
				result.runnable = r;
				return result;
			}
			return new ChainedRef(r);
		}
	}
}
