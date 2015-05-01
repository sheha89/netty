/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link GenericFutureListener} implementation which takes other {@link Future}s
 * and notifies them on completion.
 *
 * @param <V> the type of value returned by the future
 * @param <F> the type of future
 */
public class PromiseNotifier<V, F extends Future<V>> implements GenericFutureListener<F> {

    private final Promise<? super V>[] promises;
    private final boolean tryNotify;

    /**
     * Create a new instance.
     *
     * @param promises  the {@link Promise}s to notify once this {@link GenericFutureListener} is notified.
     */
    @SafeVarargs
    public PromiseNotifier(Promise<? super V>... promises) {
        this(false, promises);
    }

    /**
     * Create a new instance.
     *
     * @param tryNotify if {@code true} {@link Promise#trySuccess(Object)} and
     *                  {@link Promise#tryFailure(Throwable)} will be used, if {@code false}
     *                  {@link Promise#setSuccess(Object)} amd {@link Promise#setFailure(Throwable)}.
     * @param promises  the {@link Promise}s to notify once this {@link GenericFutureListener} is notified.
     */
    @SafeVarargs
    public PromiseNotifier(boolean tryNotify, Promise<? super V>... promises) {
        checkNotNull(promises, "promises");
        for (Promise<? super V> promise: promises) {
            if (promise == null) {
                throw new IllegalArgumentException("promises contains null Promise");
            }
        }
        this.promises = promises.clone();
        this.tryNotify = tryNotify;
    }

    @Override
    public void operationComplete(F future) throws Exception {
        if (future.isSuccess()) {
            V result = future.get();
            if (tryNotify) {
                trySuccess(result);
            } else {
                setSuccess(result);
            }
            return;
        }

        Throwable cause = future.cause();
        if (tryNotify) {
            tryFailure(cause);
        } else {
            setFailure(cause);
        }
    }

    private void trySuccess(V result) {
        for (Promise<? super V> p: promises) {
            p.trySuccess(result);
        }
    }

    private void setSuccess(V result) {
        for (Promise<? super V> p: promises) {
            p.setSuccess(result);
        }
    }

    private void tryFailure(Throwable cause) {
        for (Promise<? super V> p: promises) {
            p.tryFailure(cause);
        }
    }

    private void setFailure(Throwable cause) {
        for (Promise<? super V> p: promises) {
            p.setFailure(cause);
        }
    }
}
