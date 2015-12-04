/*
 * Copyright (C) 2008 The Guava Authors
 * IMPORTED LIGHT-WEIGHT VERSION MANUALLY DUE TO COMPATIBILITY WITH AN OLDER Guava BUILD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package compat.com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.CheckReturnValue;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

@GwtCompatible(emulated = true)
public abstract class FluentIterableCompat<E> implements Iterable<E> {
    private final Iterable<E> iterable;

    FluentIterableCompat(Iterable<E> iterable) {
        this.iterable = checkNotNull(iterable);
    }

    @CheckReturnValue
    public static <E> FluentIterableCompat<E> from(final Iterable<E> iterable) {
        return (iterable instanceof FluentIterableCompat) ? (FluentIterableCompat<E>) iterable : new FluentIterableCompat<E>(iterable) {
            @Override
            public Iterator<E> iterator() {
                return iterable.iterator();
            }
        };
    }

    @Override
    @CheckReturnValue
    public String toString() {
        return Iterables.toString(iterable);
    }

    @CheckReturnValue
    public final FluentIterableCompat<E> filter(Predicate<? super E> predicate) {
        return from(Iterables.filter(iterable, predicate));
    }

    @CheckReturnValue
    public final <T> FluentIterableCompat<T> transform(Function<? super E, T> function) {
        return from(Iterables.transform(iterable, function));
    }

    @CheckReturnValue
    public final ImmutableList<E> toList() {
        return ImmutableList.copyOf(iterable);
    }
}