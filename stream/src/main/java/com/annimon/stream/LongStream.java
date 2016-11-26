package com.annimon.stream;

import com.annimon.stream.function.Function;
import com.annimon.stream.function.LongBinaryOperator;
import com.annimon.stream.function.LongConsumer;
import com.annimon.stream.function.LongFunction;
import com.annimon.stream.function.LongPredicate;
import com.annimon.stream.function.LongSupplier;
import com.annimon.stream.function.LongToDoubleFunction;
import com.annimon.stream.function.LongToIntFunction;
import com.annimon.stream.function.LongUnaryOperator;
import com.annimon.stream.function.ObjLongConsumer;
import com.annimon.stream.function.Supplier;
import com.annimon.stream.function.ToLongFunction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A sequence of {@code long}-valued elements supporting aggregate operations.
 *
 * @since 1.1.4
 * @see Stream
 */
@SuppressWarnings("WeakerAccess")
public final class LongStream {

    /**
     * Single instance for empty stream. It is safe for multi-thread environment because it has no content.
     */
    private static final LongStream EMPTY = new LongStream(new PrimitiveIterator.OfLong() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public long nextLong() {
            return 0L;
        }
    });

    /**
     * Returns an empty stream.
     *
     * @return the empty stream
     */
    public static LongStream empty() {
        return EMPTY;
    }

    /**
     * Creates a {@code LongStream} from {@code PrimitiveIterator.OfLong}.
     *
     * @param iterator  the iterator with elements to be passed to stream
     * @return the new {@code LongStream}
     * @throws NullPointerException if {@code iterator} is null
     */
    public static LongStream of(PrimitiveIterator.OfLong iterator) {
        Objects.requireNonNull(iterator);
        return new LongStream(iterator);
    }

    /**
     * Creates a {@code LongStream} from the specified values.
     *
     * @param values  the elements of the new stream
     * @return the new stream
     * @throws NullPointerException if {@code values} is null
     */
    public static LongStream of(final long... values) {
        Objects.requireNonNull(values);
        return new LongStream(new PrimitiveIterator.OfLong() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @Override
            public long nextLong() {
                return values[index++];
            }
        });
    }

    /**
     * Returns stream which contains single element passed as param
     *
     * @param t  element of the stream
     * @return the new stream
     */
    public static LongStream of(final long t) {
        return new LongStream(new PrimitiveIterator.OfLong() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index == 0;
            }

            @Override
            public long nextLong() {
                index++;
                return t;
            }
        });
    }

    /**
     * Returns a sequential ordered {@code LongStream} from {@code startInclusive}
     * (inclusive) to {@code endExclusive} (exclusive) by an incremental step of
     * {@code 1}.
     *
     * @param startInclusive the (inclusive) initial value
     * @param endExclusive the exclusive upper bound
     * @return a sequential {@code LongStream} for the range of {@code long}
     *         elements
     */
    public static LongStream range(final long startInclusive, final long endExclusive) {
        if (startInclusive >= endExclusive) {
            return empty();
        }
        return rangeClosed(startInclusive, endExclusive - 1);
    }

    /**
     * Returns a sequential ordered {@code LongStream} from {@code startInclusive}
     * (inclusive) to {@code endInclusive} (inclusive) by an incremental step of
     * {@code 1}.
     *
     * @param startInclusive the (inclusive) initial value
     * @param endInclusive the inclusive upper bound
     * @return a sequential {@code LongStream} for the range of {@code long}
     *         elements
     */
    public static LongStream rangeClosed(final long startInclusive, final long endInclusive) {
        if (startInclusive > endInclusive) {
            return empty();
        } else if (startInclusive == endInclusive) {
            return of(startInclusive);
        } else return new LongStream(new PrimitiveIterator.OfLong() {

            private long current = startInclusive;
            private boolean hasNext = current <= endInclusive;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public long nextLong() {
                if (current >= endInclusive) {
                    hasNext = false;
                    return endInclusive;
                }
                return current++;
            }
        });
    }

    /**
     * Creates a {@code LongStream} by elements that generated by {@code LongSupplier}.
     *
     * @param s  the {@code LongSupplier} for generated elements
     * @return a new infinite sequential {@code LongStream}
     * @throws NullPointerException if {@code s} is null
     */
    public static LongStream generate(final LongSupplier s) {
        Objects.requireNonNull(s);
        return new LongStream(new PrimitiveIterator.OfLong() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public long nextLong() {
                return s.getAsLong();
            }
        });
    }

    /**
     * Creates a {@code LongStream} by iterative application {@code LongUnaryOperator} function
     * to an initial element {@code seed}. Produces {@code LongStream} consisting of
     * {@code seed}, {@code f(seed)}, {@code f(f(seed))}, etc.
     *
     * <p> The first element (position {@code 0}) in the {@code LongStream} will be
     * the provided {@code seed}. For {@code n > 0}, the element at position
     * {@code n}, will be the result of applying the function {@code f} to the
     * element at position {@code n - 1}.
     *
     * <p>Example:
     * <pre>
     * seed: 1
     * f: (a) -&gt; a + 5
     * result: [1, 6, 11, 16, ...]
     * </pre>
     *
     * @param seed the initial element
     * @param f  a function to be applied to the previous element to produce a new element
     * @return a new sequential {@code LongStream}
     * @throws NullPointerException if {@code f} is null
     */
    public static LongStream iterate(final long seed, final LongUnaryOperator f) {
        Objects.requireNonNull(f);
        return new LongStream(new PrimitiveIterator.OfLong() {

            private long current = seed;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public long nextLong() {
                final long old = current;
                current = f.applyAsLong(current);
                return old;
            }
        });
    }

    /**
     * Concatenates two streams.
     *
     * <p>Example:
     * <pre>
     * stream a: [1, 2, 3, 4]
     * stream b: [5, 6]
     * result:   [1, 2, 3, 4, 5, 6]
     * </pre>
     *
     * @param a  the first stream
     * @param b  the second stream
     * @return the new concatenated stream
     * @throws NullPointerException if {@code a} or {@code b} is null
     */
    public static LongStream concat(final LongStream a, final LongStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        final PrimitiveIterator.OfLong it1 = a.iterator;
        final PrimitiveIterator.OfLong it2 = b.iterator;
        return new LongStream(new PrimitiveIterator.OfLong() {

            private boolean firstStreamIsCurrent = true;

            @Override
            public boolean hasNext() {
                if (firstStreamIsCurrent) {
                    if (it1.hasNext())
                        return true;

                    firstStreamIsCurrent = false;
                }
                return it2.hasNext();
            }

            @Override
            public long nextLong() {
                return firstStreamIsCurrent ? it1.nextLong() : it2.nextLong();
            }
        });
    }


    private final PrimitiveIterator.OfLong iterator;

    private LongStream(PrimitiveIterator.OfLong iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns internal {@code LongStream} iterator.
     *
     * @return internal {@code LongStream} iterator.
     */
    public PrimitiveIterator.OfLong iterator() {
        return iterator;
    }

    /**
     * Applies custom operator on stream.
     *
     * Transforming function can return {@code LongStream} for intermediate operations,
     * or any value for terminal operation.
     *
     * <p>Operator examples:
     * <pre><code>
     *     // Intermediate operator
     *     public class Zip implements Function&lt;LongStream, LongStream&gt; {
     *
     *         private final LongStream secondStream;
     *         private final LongBinaryOperator combiner;
     *
     *         public Zip(LongStream secondStream, LongBinaryOperator combiner) {
     *             this.secondStream = secondStream;
     *             this.combiner = combiner;
     *         }
     *
     *         &#64;Override
     *         public LongStream apply(LongStream firstStream) {
     *             final PrimitiveIterator.OfLong it1 = firstStream.iterator();
     *             final PrimitiveIterator.OfLong it2 = secondStream.iterator();
     *             return LongStream.of(new PrimitiveIterator.OfLong() {
     *                 &#64;Override
     *                 public boolean hasNext() {
     *                     return it1.hasNext() &amp;&amp; it2.hasNext();
     *                 }
     *
     *                 &#64;Override
     *                 public long nextLong() {
     *                     return combiner.applyAsLong(it1.nextLong(), it2.nextLong());
     *                 }
     *             });
     *         }
     *     }
     *
     *     // Intermediate operator based on existing stream operators
     *     public class SkipAndLimit implements UnaryOperator&lt;LongStream&gt; {
     *
     *         private final int skip, limit;
     *
     *         public SkipAndLimit(int skip, int limit) {
     *             this.skip = skip;
     *             this.limit = limit;
     *         }
     *
     *         &#64;Override
     *         public LongStream apply(LongStream stream) {
     *             return stream.skip(skip).limit(limit);
     *         }
     *     }
     *
     *     // Terminal operator
     *     public class LongSummaryStatistics implements Function&lt;LongStream, long[]&gt; {
     *         &#64;Override
     *         public long[] apply(LongStream stream) {
     *             long count = 0;
     *             long sum = 0;
     *             final PrimitiveIterator.OfLong it = stream.iterator();
     *             while (it.hasNext()) {
     *                 count++;
     *                 sum += it.nextLong();
     *             }
     *             return new long[] {count, sum};
     *         }
     *     }
     * </code></pre>
     *
     * @param <R> the type of the result
     * @param function  a transforming function
     * @return a result of the transforming function
     * @see Stream#custom(com.annimon.stream.function.Function)
     * @throws NullPointerException if {@code function} is null
     */
    public <R> R custom(final Function<LongStream, R> function) {
        Objects.requireNonNull(function);
        return function.apply(this);
    }

    /**
     * Returns a {@code Stream} consisting of the elements of this stream,
     * each boxed to an {@code Long}.
     *
     * <p>This is an lazy intermediate operation.
     *
     * @return a {@code Stream} consistent of the elements of this stream,
     *         each boxed to an {@code Long}
     */
    public Stream<Long> boxed() {
        return Stream.of(iterator);
    }

    /**
     * Returns {@code LongStream} with elements that satisfy the given predicate.
     *
     * <p> This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &gt; 2
     * stream: [1, 2, 3, 4, -8, 0, 11]
     * result: [3, 4, 11]
     * </pre>
     *
     * @param predicate  the predicate used to filter elements
     * @return the new stream
     */
    public LongStream filter(final LongPredicate predicate) {
        return new LongStream(new PrimitiveIterator.OfLong() {

            private long next;

            @Override
            public boolean hasNext() {
                while (iterator.hasNext()) {
                    next = iterator.next();
                    if (predicate.test(next)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public long nextLong() {
                return next;
            }
        });
    }

    /**
     * Returns {@code LongStream} with elements that does not satisfy the given predicate.
     *
     * <p> This is an intermediate operation.
     *
     * @param predicate  the predicate used to filter elements
     * @return the new stream
     */
    public LongStream filterNot(final LongPredicate predicate) {
        return filter(LongPredicate.Util.negate(predicate));
    }

    /**
     * Returns an {@code LongStream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * mapper: (a) -&gt; a + 5
     * stream: [1, 2, 3, 4]
     * result: [6, 7, 8, 9]
     * </pre>
     *
     * @param mapper  the mapper function used to apply to each element
     * @return the new stream
     * @see Stream#map(com.annimon.stream.function.Function)
     */
    public LongStream map(final LongUnaryOperator mapper) {
        return new LongStream(new PrimitiveIterator.OfLong() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public long nextLong() {
                return mapper.applyAsLong(iterator.nextLong());
            }
        });
    }

    /**
     * Returns a {@code Stream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * @param <R> the type result
     * @param mapper  the mapper function used to apply to each element
     * @return the new {@code Stream}
     */
    public <R> Stream<R> mapToObj(final LongFunction<? extends R> mapper) {
        return Stream.of(new LsaIterator<R>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R nextIteration() {
                return mapper.apply(iterator.nextLong());
            }
        });
    }

    /**
     * Returns an {@code IntStream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * @param mapper  the mapper function used to apply to each element
     * @return the new {@code IntStream}
     */
    public IntStream mapToInt(final LongToIntFunction mapper) {
        return IntStream.of(new PrimitiveIterator.OfInt() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public int nextInt() {
                return mapper.applyAsInt(iterator.nextLong());
            }
        });
    }

    /**
     * Returns an {@code DoubleStream} consisting of the results of applying the given
     * function to the elements of this stream.
     *
     * <p> This is an intermediate operation.
     *
     * @param mapper  the mapper function used to apply to each element
     * @return the new {@code DoubleStream}
     */
    public DoubleStream mapToDouble(final LongToDoubleFunction mapper) {
        return DoubleStream.of(new PrimitiveIterator.OfDouble() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public double nextDouble() {
                return mapper.applyAsDouble(iterator.nextLong());
            }
        });
    }

    /**
     * Returns a stream consisting of the results of replacing each element of
     * this stream with the contents of a mapped stream produced by applying
     * the provided mapping function to each element.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * mapper: (a) -&gt; [a, a + 5]
     * stream: [1, 2, 3, 4]
     * result: [1, 6, 2, 7, 3, 8, 4, 9]
     * </pre>
     *
     * @param mapper  the mapper function used to apply to each element
     * @return the new stream
     * @see Stream#flatMap(com.annimon.stream.function.Function)
     */
    public LongStream flatMap(final LongFunction<? extends LongStream> mapper) {
        return new LongStream(new PrimitiveIterator.OfLong() {

            private PrimitiveIterator.OfLong inner;

            @Override
            public boolean hasNext() {
                if (inner != null && inner.hasNext()) {
                    return true;
                }
                while (iterator.hasNext()) {
                    final long arg = iterator.next();
                    final LongStream result = mapper.apply(arg);
                    if (result == null) {
                        continue;
                    }
                    if (result.iterator.hasNext()) {
                        inner = result.iterator;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public long nextLong() {
                return inner.nextLong();
            }
        });
    }

    /**
     * Returns a stream consisting of the distinct elements of this stream.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stream: [1, 4, 2, 3, 3, 4, 1]
     * result: [1, 4, 2, 3]
     * </pre>
     *
     * @return the new stream
     */
    public LongStream distinct() {
        return boxed().distinct().mapToLong(UNBOX_FUNCTION);
    }

    /**
     * Returns a stream consisting of the elements of this stream in sorted order.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stream: [3, 4, 1, 2]
     * result: [1, 2, 3, 4]
     * </pre>
     *
     * @return the new stream
     */
    public LongStream sorted() {
        return new LongStream(new PrimitiveExtIterator.OfLong() {

            private int index = 0;
            private long[] array;

            @Override
            protected void nextIteration() {
                if (!isInit) {
                    array = toArray();
                    Arrays.sort(array);
                }
                hasNext = index < array.length;
                if (hasNext) {
                    next = array[index++];
                }
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream
     * in sorted order as determinated by provided {@code Comparator}.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * comparator: (a, b) -&gt; -a.compareTo(b)
     * stream: [1, 2, 3, 4]
     * result: [4, 3, 2, 1]
     * </pre>
     *
     * @param comparator  the {@code Comparator} to compare elements
     * @return the new {@code LongStream}
     */
    public LongStream sorted(Comparator<Long> comparator) {
        return boxed().sorted(comparator).mapToLong(UNBOX_FUNCTION);
    }

    /**
     * Samples the {@code LongStream} by emitting every n-th element.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * stepWidth: 3
     * stream: [1, 2, 3, 4, 5, 6, 7, 8]
     * result: [1, 4, 7]
     * </pre>
     *
     * @param stepWidth  step width
     * @return the new {@code LongStream}
     * @throws IllegalArgumentException if {@code stepWidth} is zero or negative
     * @see Stream#sample(int)
     */
    public LongStream sample(final int stepWidth) {
        if (stepWidth <= 0) throw new IllegalArgumentException("stepWidth cannot be zero or negative");
        if (stepWidth == 1) return this;
        return new LongStream(new PrimitiveIterator.OfLong() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public long nextLong() {
                final long result = iterator.nextLong();
                int skip = 1;
                while (skip < stepWidth && iterator.hasNext()) {
                    iterator.nextLong();
                    skip++;
                }
                return result;
            }
        });
    }

    /**
     * Performs provided action on each element.
     *
     * <p>This is an intermediate operation.
     *
     * @param action the action to be performed on each element
     * @return the new stream
     */
    public LongStream peek(final LongConsumer action) {
        return new LongStream(new PrimitiveIterator.OfLong() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public long nextLong() {
                long value = iterator.nextLong();
                action.accept(value);
                return value;
            }
        });
    }

    /**
     * Takes elements while the predicate is true.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &lt; 3
     * stream: [1, 2, 3, 4, 1, 2, 3, 4]
     * result: [1, 2]
     * </pre>
     *
     * @param predicate  the predicate used to take elements
     * @return the new {@code LongStream}
     */
    public LongStream takeWhile(final LongPredicate predicate) {
        return new LongStream(new PrimitiveExtIterator.OfLong() {

            @Override
            protected void nextIteration() {
                hasNext = iterator.hasNext()
                        && predicate.test(next = iterator.next());
            }
        });
    }

    /**
     * Drops elements while the predicate is true and returns the rest.
     *
     * <p>This is an intermediate operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a &lt; 3
     * stream: [1, 2, 3, 4, 1, 2, 3, 4]
     * result: [3, 4, 1, 2, 3, 4]
     * </pre>
     *
     * @param predicate  the predicate used to drop elements
     * @return the new {@code LongStream}
     */
    public LongStream dropWhile(final LongPredicate predicate) {
        return new LongStream(new PrimitiveExtIterator.OfLong() {

            @Override
            protected void nextIteration() {
                if (!isInit) {
                    // Skip first time
                    while (hasNext = iterator.hasNext()) {
                        next = iterator.next();
                        if (!predicate.test(next)) {
                            return;
                        }
                    }
                }

                hasNext = hasNext && iterator.hasNext();
                if (!hasNext) return;

                next = iterator.next();
            }
        });
    }

    /**
     * Returns a stream consisting of the elements of this stream, truncated
     * to be no longer than {@code maxSize} in length.
     *
     * <p> This is a short-circuiting stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * maxSize: 3
     * stream: [1, 2, 3, 4, 5]
     * result: [1, 2, 3]
     *
     * maxSize: 10
     * stream: [1, 2]
     * result: [1, 2]
     * </pre>
     *
     * @param maxSize  the number of elements the stream should be limited to
     * @return the new stream
     * @throws IllegalArgumentException if {@code maxSize} is negative
     */
    public LongStream limit(final long maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("maxSize cannot be negative");
        if (maxSize == 0) return LongStream.empty();
        return new LongStream(new PrimitiveIterator.OfLong() {

            private long index = 0;

            @Override
            public boolean hasNext() {
                return (index < maxSize) && iterator.hasNext();
            }

            @Override
            public long nextLong() {
                index++;
                return iterator.nextLong();
            }
        });
    }

    /**
     * Skips first {@code n} elements and returns {@code Stream} with remaining elements.
     * If this stream contains fewer than {@code n} elements, then an
     * empty stream will be returned.
     *
     * <p>This is a stateful intermediate operation.
     *
     * <p>Example:
     * <pre>
     * n: 3
     * stream: [1, 2, 3, 4, 5]
     * result: [4, 5]
     *
     * n: 10
     * stream: [1, 2]
     * result: []
     * </pre>
     *
     * @param n  the number of elements to skip
     * @return the new stream
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public LongStream skip(final long n) {
        if (n < 0) throw new IllegalArgumentException("n cannot be negative");
        if (n == 0) return this;
        return new LongStream(new PrimitiveIterator.OfLong() {

            private long skippedCount = 0;

            @Override
            public boolean hasNext() {
                while (iterator.hasNext()) {
                    if (skippedCount == n) break;
                    iterator.nextLong();
                    skippedCount++;
                }
                return iterator.hasNext();
            }

            @Override
            public long nextLong() {
                return iterator.nextLong();
            }
        });
    }

    /**
     * Performs an action for each element of this stream.
     *
     * <p>This is a terminal operation.
     *
     * @param action  the action to be performed on each element
     */
    public void forEach(LongConsumer action) {
        while (iterator.hasNext()) {
            action.accept(iterator.nextLong());
        }
    }

    /**
     * Performs a reduction on the elements of this stream, using the provided
     * identity value and an associative accumulation function, and returns the
     * reduced value.
     *
     * <p>The {@code identity} value must be an identity for the accumulator
     * function. This means that for all {@code x},
     * {@code accumulator.apply(identity, x)} is equal to {@code x}.
     * The {@code accumulator} function must be an associative function.
     *
     * <p>This is a terminal operation.
     *
     * <p>Example:
     * <pre>
     * identity: 0
     * accumulator: (a, b) -&gt; a + b
     * stream: [1, 2, 3, 4, 5]
     * result: 15
     * </pre>
     *
     * @param identity  the identity value for the accumulating function
     * @param accumulator  the accumulation function
     * @return the result of the reduction
     * @see #sum()
     * @see #min()
     * @see #max()
     */
    public long reduce(long identity, LongBinaryOperator accumulator) {
        long result = identity;
        while (iterator.hasNext()) {
            final long value = iterator.nextLong();
            result = accumulator.applyAsLong(result, value);
        }
        return result;
    }

    /**
     * Performs a reduction on the elements of this stream, using an
     * associative accumulation function, and returns an {@code OptionalLong}
     * describing the reduced value, if any.
     *
     * <p>The {@code accumulator} function must be an associative function.
     *
     * <p>This is a terminal operation.
     *
     * @param accumulator  the accumulation function
     * @return the result of the reduction
     * @see #reduce(com.annimon.stream.function.LongBinaryOperator)
     */
    public OptionalLong reduce(LongBinaryOperator accumulator) {
        boolean foundAny = false;
        long result = 0;
        while (iterator.hasNext()) {
            final long value = iterator.nextLong();
            if (!foundAny) {
                foundAny = true;
                result = value;
            } else {
                result = accumulator.applyAsLong(result, value);
            }
        }
        return foundAny ? OptionalLong.of(result) : OptionalLong.empty();
    }

    /**
     * Returns an array containing the elements of this stream.
     *
     * <p>This is a terminal operation.
     *
     * @return an array containing the elements of this stream
     */
    public long[] toArray() {
        SpinedBuffer.OfLong b = new SpinedBuffer.OfLong();
        forEach(b);
        return b.asPrimitiveArray();
    }

    /**
     * Collects elements to {@code supplier} provided container by applying the given accumulation function.
     *
     * <p>This is a terminal operation.
     *
     * @param <R> the type of the result
     * @param supplier  the supplier function that provides container
     * @param accumulator  the accumulation function
     * @return the result of collect elements
     * @see Stream#collect(com.annimon.stream.function.Supplier, com.annimon.stream.function.BiConsumer)
     */
    public <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator) {
        final R result = supplier.get();
        while (iterator.hasNext()) {
            final long value = iterator.nextLong();
            accumulator.accept(result, value);
        }
        return result;
    }

    /**
     * Returns the sum of elements in this stream.
     *
     * @return the sum of elements in this stream
     */
    public long sum() {
        long sum = 0;
        while (iterator.hasNext()) {
            sum += iterator.nextLong();
        }
        return sum;
    }

    /**
     * Returns an {@code OptionalLong} describing the minimum element of this
     * stream, or an empty optional if this stream is empty.
     *
     * <p>This is a terminal operation.
     *
     * @return the minimum element
     */
    public OptionalLong min() {
        return reduce(new LongBinaryOperator() {
            @Override
            public long applyAsLong(long left, long right) {
                return Math.min(left, right);
            }
        });
    }

    /**
     * Returns an {@code OptionalLong} describing the maximum element of this
     * stream, or an empty optional if this stream is empty.
     *
     * <p>This is a terminal operation.
     *
     * @return the maximum element
     */
    public OptionalLong max() {
        return reduce(new LongBinaryOperator() {
            @Override
            public long applyAsLong(long left, long right) {
                return Math.max(left, right);
            }
        });
    }

    /**
     * Returns the count of elements in this stream.
     *
     * <p>This is a terminal operation.
     *
     * @return the count of elements in this stream
     */
    public long count() {
        long count = 0;
        while (iterator.hasNext()) {
            iterator.nextLong();
            count++;
        }
        return count;
    }

    /**
     * Tests whether all elements match the given predicate.
     * May not evaluate the predicate on all elements if not necessary
     * for determining the result. If the stream is empty then
     * {@code false} is returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: true
     *
     * predicate: (a) -&gt; a == 5
     * stream: [5, 5, 5]
     * result: true
     * </pre>
     *
     * @param predicate  the predicate used to match elements
     * @return {@code true} if any elements of the stream match the provided
     *         predicate, otherwise {@code false}
     */
    public boolean anyMatch(LongPredicate predicate) {
        while (iterator.hasNext()) {
            if (predicate.test(iterator.nextLong()))
                return true;
        }
        return false;
    }

    /**
     * Tests whether all elements match the given predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result. If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: false
     *
     * predicate: (a) -&gt; a == 5
     * stream: [5, 5, 5]
     * result: true
     * </pre>
     *
     * @param predicate  the predicate used to match elements
     * @return {@code true} if either all elements of the stream match the
     *         provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean allMatch(LongPredicate predicate) {
        while (iterator.hasNext()) {
            if (!predicate.test(iterator.nextLong()))
                return false;
        }
        return true;
    }

    /**
     * Tests whether no elements match the given predicate.
     * May not evaluate the predicate on all elements if not necessary for
     * determining the result. If the stream is empty then {@code true} is
     * returned and the predicate is not evaluated.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3, 4, 5]
     * result: false
     *
     * predicate: (a) -&gt; a == 5
     * stream: [1, 2, 3]
     * result: true
     * </pre>
     *
     * @param predicate  the predicate used to match elements
     * @return {@code true} if either no elements of the stream match the
     *         provided predicate or the stream is empty, otherwise {@code false}
     */
    public boolean noneMatch(LongPredicate predicate) {
        while (iterator.hasNext()) {
            if (predicate.test(iterator.nextLong()))
                return false;
        }
        return true;
    }

    /**
     * Returns the first element wrapped by {@code OptionalLong} class.
     * If stream is empty, returns {@code OptionalLong.empty()}.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * @return an {@code OptionalLong} with first element
     *         or {@code OptionalLong.empty()} if stream is empty
     */
    public OptionalLong findFirst() {
        if (iterator.hasNext()) {
            return OptionalLong.of(iterator.nextLong());
        }
        return OptionalLong.empty();
    }

    /**
     * Returns the single element of stream.
     * If stream is empty, throws {@code NoSuchElementException}.
     * If stream contains more than one element, throws {@code IllegalStateException}.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * stream: []
     * result: NoSuchElementException
     *
     * stream: [1]
     * result: 1
     *
     * stream: [1, 2, 3]
     * result: IllegalStateException
     * </pre>
     *
     * @return single element of stream
     * @throws NoSuchElementException if stream is empty
     * @throws IllegalStateException if stream contains more than one element
     */
    public long single() {
        if (!iterator.hasNext()) {
            throw new NoSuchElementException("LongStream contains no element");
        }

        final long singleCandidate = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException("LongStream contains more than one element");
        }
        return singleCandidate;
    }

    /**
     * Returns the single element wrapped by {@code OptionalLong} class.
     * If stream is empty, returns {@code OptionalLong.empty()}.
     * If stream contains more than one element, throws {@code IllegalStateException}.
     *
     * <p>This is a short-circuiting terminal operation.
     *
     * <p>Example:
     * <pre>
     * stream: []
     * result: OptionalLong.empty()
     *
     * stream: [1]
     * result: OptionalLong.of(1)
     *
     * stream: [1, 2, 3]
     * result: IllegalStateException
     * </pre>
     *
     * @return an {@code OptionalLong} with single element
     *         or {@code OptionalLong.empty()} if stream is empty
     * @throws IllegalStateException if stream contains more than one element
     */
    public OptionalLong findSingle() {
        if (!iterator.hasNext()) {
            return OptionalLong.empty();
        }

        final long singleCandidate = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException("LongStream contains more than one element");
        }
        return OptionalLong.of(singleCandidate);
    }


    private static final ToLongFunction<Long> UNBOX_FUNCTION = new ToLongFunction<Long>() {
        @Override
        public long applyAsLong(Long t) {
            return t;
        }
    };
}
