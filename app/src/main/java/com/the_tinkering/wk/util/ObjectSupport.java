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

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import static com.the_tinkering.wk.Constants.DAY;
import static com.the_tinkering.wk.Constants.HOUR;
import static com.the_tinkering.wk.Constants.MINUTE;
import static java.util.Objects.requireNonNull;

/**
 * Various generic object manipulation methods. These are mostly simple methods
 * that in later Java versions are part of the standard library.
 */
public final class ObjectSupport {
    private static final Logger LOGGER = Logger.get(ObjectSupport.class);
    private static final Random random = new Random(System.currentTimeMillis());

    private ObjectSupport() {
        //
    }

    /**
     * CharSequence.isEmpty(), but will return true if the value is null.
     *
     * @param value the string to check
     * @return true if value is empty or null
     */
    public static boolean isEmpty(final @Nullable CharSequence value) {
        return value == null || value.length() == 0;
    }

    /**
     * String.equalsIgnoreCase(), but handle null values. null equals null, but
     * null doesn't equal any other value.
     *
     * @param first the first string to compare
     * @param second the second string to compare
     * @return true of the strings are equals except for case
     */
    public static boolean isEqualIgnoreCase(final @Nullable String first, final @Nullable String second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }
        return first.equalsIgnoreCase(second);
    }

    /**
     * Object.equals(), but handle null values. null equals null, but
     * null doesn't equal any other value.
     *
     * @param first the first object to compare
     * @param second the second object to compare
     * @return true if the objects are equal
     */
    public static boolean isEqual(final @Nullable Object first, final @Nullable Object second) {
        if (first == null) {
            return second == null;
        }
        if (second == null) {
            return false;
        }
        return first.equals(second);
    }

    /**
     * COmpute a hash code for an array of objects, any of which could be null.
     *
     * @param values the objects to calculate for
     * @return the hash code
     */
    public static int hash(final Object... values) {
        return Arrays.hashCode(values);
    }

    /**
     * Return value if it's not empty and not null, otherwise return fallback.
     *
     * @param value the value to check
     * @param fallback the fallback value in case value is empty
     * @return value or fallback
     */
    public static String orElse(final @Nullable String value, final String fallback) {
        if (isEmpty(value)) {
            return fallback;
        }
        return value;
    }

    /**
     * Build a string that concatenates all elements in the iterable of parts.
     *
     * @param delimiter the delimiter between elements
     * @param prefix the prefix before the first element
     * @param suffix the suffix after the last element
     * @param parts the items to list
     * @return the resulting string
     */
    public static String join(final String delimiter, final String prefix, final String suffix, final Iterable<?> parts) {
        final StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        boolean first = true;

        for (final Object part: parts) {
            if (!first) {
                sb.append(delimiter);
            }
            sb.append(part);
            first = false;
        }

        sb.append(suffix);
        return sb.toString();
    }

    /**
     * Remove all empty/null elements from the list in-place.
     *
     * @param list the list to alter
     * @param <T> the type of elements
     */
    public static <T extends CharSequence> void removeEmpty(final List<T> list) {
        int i = 0;
        while (i < list.size()) {
            final T value = list.get(i);
            if (isEmpty(value)) {
                list.remove(i);
            }
            else {
                i++;
            }
        }
    }

    /**
     * Edit a list in-place to remove duplicate elements.
     *
     * @param list the list to clean up
     * @param <T> the type of elements in the list
     */
    public static <T> void removeDuplicates(final @Nullable List<T> list) {
        if (list == null) {
            return;
        }
        int i = 0;
        while (i < list.size()) {
            final T value = list.get(i);
            final int pos = list.indexOf(value);
            if (pos >= 0 && pos < i) {
                list.remove(i);
            }
            else {
                i++;
            }
        }
    }

    /**
     * Generate a random permutation of a list.
     *
     * @param list the list to create a permutation for
     * @param <T> the type of elements
     * @return the randomized list
     */
    public static <T> List<T> shuffle(final List<? extends T> list) {
        final List<T> todo = new ArrayList<>(list);
        final List<T> result = new ArrayList<>();
        while (!todo.isEmpty()) {
            final T t = todo.remove(random.nextInt(todo.size()));
            result.add(t);
        }
        return result;
    }

    /**
     * Return the next random int in the range 0..bound (lower inclusive, upper exclusive).
     * @param bound the upper bund for the returned number
     * @return the random number
     */
    public static int nextRandomInt(final int bound) {
        return random.nextInt(bound);
    }

    /**
     * Helper for comparators: generate the order of two string values, either of which could be null.
     *
     * @param s1 left-hand value
     * @param s2 right-hand value
     * @return order as for compareTo()
     */
    public static int compareStrings(final @Nullable Comparable<? super String> s1, final @Nullable String s2) {
        if (s1 == null) {
            return s2 == null ? 0 : -1;
        }
        if (s2 == null) {
            return 1;
        }
        return s1.compareTo(s2);
    }

    /**
     * Helper for comparators: generate the order of int values.
     * The result is based on the values of i1 and i2 if they differ.
     * If they are the same, use i3 and i4 instead.
     *
     * @param i1 left-hand value 1
     * @param i2 right-hand value 1
     * @param i3 left-hand value 2
     * @param i4 right-hand value 2
     * @return order as for compareTo()
     */
    public static int compareIntegers(final int i1, final int i2, final int i3, final int i4) {
        final int n = Integer.compare(i1, i2);
        if (n != 0) {
            return n;
        }
        return Integer.compare(i3, i4);
    }

    /**
     * Helper for comparators: generate the order of int/long values.
     * The result is based on the values of i1 and i2 if they differ.
     * If they are the same, use i3 and i4 instead, or i5 and i6 if i3 and i4 are also equal.
     *
     * @param i1 left-hand value 1
     * @param i2 right-hand value 1
     * @param i3 left-hand value 2
     * @param i4 right-hand value 2
     * @param i5 left-hand value 3
     * @param i6 right-hand value 3
     * @return order as for compareTo()
     */
    public static int compareIntegersAndLongs(final int i1, final int i2, final int i3, final int i4, final long i5, final long i6) {
        final int n1 = Integer.compare(i1, i2);
        if (n1 != 0) {
            return n1;
        }
        final int n2 = Integer.compare(i3, i4);
        if (n2 != 0) {
            return n2;
        }
        return Long.compare(i5, i6);
    }

    /**
     * Create a comparator that will deliver the reverse results of the argument one.
     *
     * @param comparator the argument comparator
     * @param <T> the type of objects it compares
     * @return the new comparator
     */
    public static <T> Comparator<T> reversedComparator(final Comparator<? super T> comparator) {
        return (o1, o2) -> comparator.compare(o2, o1);
    }

    /**
     * Format a time interval in ms as an informal string such as "in 3 hours".
     *
     * @param waitTime the time to wait
     * @return description of the time to wait
     */
    public static String getWaitTimeAsInformalString(final long waitTime) {
        if (waitTime <= 0) {
            return "now";
        }
        if (waitTime < HOUR * 2) {
            return String.format(Locale.ROOT, "in %d min", (waitTime+ MINUTE/2) / MINUTE);
        }
        if (waitTime < DAY * 2) {
            return String.format(Locale.ROOT, "in %d hours", (waitTime+ HOUR/2) / HOUR);
        }
        return String.format(Locale.ROOT, "in %d days", (waitTime+ DAY/2) / DAY);
    }

    /**
     * Format a time interval in ms as a short informal string such as "3h".
     *
     * @param waitTime the time to wait
     * @return description of the time to wait
     */
    public static String getShortWaitTimeAsInformalString(final long waitTime) {
        if (waitTime <= 0) {
            return "now";
        }
        if (waitTime < HOUR * 2) {
            return String.format(Locale.ROOT, "%dm", (waitTime+ MINUTE/2) / MINUTE);
        }
        if (waitTime < DAY * 2) {
            return String.format(Locale.ROOT, "%dh", (waitTime+ HOUR/2) / HOUR);
        }
        return String.format(Locale.ROOT, "in %dd", (waitTime+ DAY/2) / DAY);
    }

    /**
     * Create a Date instance that represents the start of the hour represented by the argument.
     *
     * @param date the date to check
     * @return the date with minute, second and millisecond set to 0
     */
    public static Date getTopOfHour(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Create a Date instance that represents the start of the hour represented by the argument.
     *
     * @param ts the timestamp to check
     * @return the date with minute, second and millisecond set to 0
     */
    public static Date getTopOfHour(final long ts) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(ts));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Check if value is a Boolean instance and that instance is true.
     *
     * @param value the value to check
     * @return true if it is
     */
    public static boolean isTrue(final @Nullable Object value) {
        return value instanceof Boolean && (boolean) value;
    }

    /**
     * Run some code that produces a non-null return value, while capturing and
     * logging any exceptions thrown.
     *
     * @param defaultValue the default value to return if the supplier throws an exception
     * @param supplier the supplier that produces the result, i.e. the code to run safely
     * @param <T> the type of the return value
     * @return the result from the supplier, or defaultValue if the supplier threw an exception
     */
    public static <T> T safe(final T defaultValue, final ThrowingSupplier<? extends T> supplier) {
        try {
            return requireNonNull(supplier.get());
        }
        catch (final Exception e) {
            LOGGER.uerr(e);
            return defaultValue;
        }
    }

    /**
     * Run some code that produces a non-null return value, while capturing and
     * logging any exceptions thrown. Variant that takes a Supplier to create a defaultValue.
     *
     * @param defaultValueSupplier the default value to return if the supplier throws an exception
     * @param supplier the supplier that produces the result, i.e. the code to run safely
     * @param <T> the type of the return value
     * @return the result from the supplier, or defaultValue if the supplier threw an exception
     */
    public static <T> T safe(final Supplier<T> defaultValueSupplier, final ThrowingSupplier<? extends T> supplier) {
        try {
            return requireNonNull(supplier.get());
        }
        catch (final Exception e) {
            LOGGER.uerr(e);
            return defaultValueSupplier.get();
        }
    }

    /**
     * A variation of safe() that can produce null values.
     *
     * @param supplier the supplier that produces the result, i.e. the code to run safely
     * @param <T> the type of the return value
     * @return the result from the supplier, or null if the supplier threw an exception
     */
    public static <T> @Nullable T safeNullable(final NullableThrowingSupplier<? extends T> supplier) {
        try {
            return supplier.get();
        }
        catch (final Exception e) {
            LOGGER.uerr(e);
            return null;
        }
    }

    /**
     * A variation of safe() that produces no return value.
     *
     * @param runnable the runnable that needs to be run, i.e. the code to run safely
     */
    public static void safe(final ThrowingRunnable runnable) {
        try {
            runnable.run();
        }
        catch (final Exception e) {
            LOGGER.uerr(e);
        }
    }

    /**
     * Run an AsyncTask with the supplied handler bodies.
     *
     * @param background run on the background thread, returns a result
     * @param progress run on the UI thread to report progress
     * @param post run on the UI thread to report the result
     * @param params the parameters for background
     * @param <Params> the type of the parameters
     * @param <Progress> the type of the progress values
     * @param <Result> the type of the result
     */
    @SafeVarargs
    public static <Params, Progress, Result> void runAsync(final DoInBackground<? super Params, Progress, Result> background,
                                                           final ObjectSupport.@Nullable OnProgressUpdate<? super Progress> progress,
                                                           final @Nullable OnPostExecute<? super Result> post,
                                                           final Params... params) {
        new AsyncTask<Params, Progress, Result>() {
            @SafeVarargs
            @SuppressWarnings("AnonymousClassVariableHidesContainingMethodVariable")
            @Override
            protected final @Nullable Result doInBackground(final Params... params) {
                return safeNullable(() -> {
                    //noinspection Convert2MethodRef
                    final ProgressPublisher<Progress> publisher = values -> publishProgress(values);
                    return background.doInBackground(publisher, params);
                });
            }

            @SafeVarargs
            @Override
            protected final void onProgressUpdate(final Progress... values) {
                if (progress != null) {
                    safe(() -> progress.onProgressUpdate(values));
                }
            }

            @Override
            protected void onPostExecute(final @Nullable Result result) {
                if (post != null) {
                    safe(() -> post.onPostExecute(result));
                }
            }
        }.execute(params);
    }

    /**
     * A Supplier variant that can throw exceptions.
     *
     * @param <T> the result type
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        /**
         * Supply a value of type T.
         *
         * @return the value, may not be null
         * @throws Exception if anything went wrong
         */
        @SuppressWarnings({"RedundantThrows", "RedundantSuppression"})
        T get() throws Exception;
    }

    /**
     * A Supplier variant that can throw exceptions and return null.
     *
     * @param <T> the result type
     */
    @FunctionalInterface
    public interface NullableThrowingSupplier<T> {
        /**
         * Supply a value of type T, possibly null.
         *
         * @return the value, may be null
         * @throws Exception if anything went wrong
         */
        @SuppressWarnings({"RedundantThrows", "RedundantSuppression"})
        @Nullable T get() throws Exception;
    }

    /**
     * A Runnable variant that can throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        /**
         * Run the action.
         *
         * @throws Exception if anything went wrong
         */
        @SuppressWarnings({"RedundantThrows", "RedundantSuppression"})
        void run() throws Exception;
    }

    /**
     * A Consumer variant for AsyncTask.publishProgress().
     *
     * @param <Progress> the type of progress values
     */
    @FunctionalInterface
    public interface ProgressPublisher<Progress> {
        /**
         * Publish progress.
         *
         * @param values the progress values
         * @throws Exception if anything went wrong
         */
        @SuppressWarnings({"unchecked", "RedundantThrows", "RedundantSuppression"})
        void progress(Progress... values) throws Exception;
    }

    /**
     * An interface for the work to do in doInBackground in an AsyncTask.
     *
     * @param <Params> the type of the parameters
     * @param <Progress> the type of the progress values
     * @param <Result> the type of the result
     */
    @FunctionalInterface
    public interface DoInBackground<Params, Progress, Result> {
        /**
         * Run the background operation. Same as AsyncTask.doInBackground, but it gets an explicit reference to a publisher that can publish progress.
         *
         * @param publisher the publisher that can process progress values
         * @param params the parameters
         * @return the result
         * @throws Exception if anything went wrong
         */
        @Nullable Result doInBackground(ProgressPublisher<Progress> publisher, Params[] params) throws Exception;
    }

    /**
     * An interface for the work to do in onProgressUpdate in an AsyncTask.
     *
     * @param <Progress> the type of the progress values
     */
    @FunctionalInterface
    public interface OnProgressUpdate<Progress> {
        /**
         * Report progress. Same as AsyncTask.onProgressUpdate, but it gets an explicit reference to the task.
         *
         * @param values the progress values
         */
        void onProgressUpdate(Progress[] values);
    }

    /**
     * An interface for the work to do in onPostExecute in an AsyncTask.
     *
     * @param <Result> the type of the result
     */
    @FunctionalInterface
    public interface OnPostExecute<Result> {
        /**
         * Report the result. Same as AsyncTask.onPostExecute.
         *
         * @param result the result
         */
        void onPostExecute(@Nullable Result result);
    }
}
