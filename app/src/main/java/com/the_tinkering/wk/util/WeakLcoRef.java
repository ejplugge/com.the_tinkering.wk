/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.the_tinkering.wk.util;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

/**
 * A container that holds a weak reference to a LifecycleOwner. This behaves much like a
 * regular WeakReference, with two major differences: the get() method throws an
 * exception if the referent is gone or in a destroyed state, and there is a separate
 * getOrElse() method that will return a default value in that case.
 */
public final class WeakLcoRef<T extends LifecycleOwner> {
    private final WeakReference<T> ref;

    /**
     * The constructor.
     *
     * @param lifecycleOwner the owner to wrap
     */
    public WeakLcoRef(final T lifecycleOwner) {
        ref = new WeakReference<>(lifecycleOwner);
    }

    /**
     * Get the referent, throwing a ReferentGoneException if none is available to return.
     *
     * @return the referent
     */
    public T get() {
        final @Nullable T referent = ref.get();
        if (referent == null || referent.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            throw new ReferentGoneException();
        }
        return referent;
    }

    /**
     * Like get(), return the referent, or the default value if it can't.
     *
     * @param defaultValue the default value as a fallback
     * @return the referent
     */
    public @Nullable T getOrElse(final @Nullable T defaultValue) {
        final @Nullable T referent = ref.get();
        if (referent == null || referent.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return defaultValue;
        }
        return referent;
    }

    /**
     * An exception thrown when get() can't return an existing, non-destroyed referent.
     */
    public static final class ReferentGoneException extends RuntimeException {
        private static final long serialVersionUID = -820714790194371300L;
    }
}
