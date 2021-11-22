package mwg.wb.common;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FixedSizeSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
    private final Spliterator<E> wrapped;
    private final long size;
    private final AtomicInteger cursor = new AtomicInteger(0);

    FixedSizeSpliterator(Spliterator<E> wrapped, int limitSize) {
        this(wrapped, size(wrapped, limitSize), wrapped.characteristics());
    }

    private FixedSizeSpliterator(Spliterator<E> wrapped, long size, int characteristics) {
        super(size, characteristics);
        this.wrapped = wrapped;
        this.size = size;
    }

    private static <E> long size(Spliterator<E> wrapped, long size) {
        long e = wrapped.estimateSize();
        if (e > size) {
            return size;
        }
        return e;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (cursor.getAndIncrement() > size) {
            return false;
        }
        return wrapped.tryAdvance(action);
    }

    @Override
    public long estimateSize() {
        long s = size(wrapped, size - cursor.get());
        return s > 0 ? s : 0;
    }

    @Override
    public Comparator<? super E> getComparator() {
        if (wrapped.hasCharacteristics(Spliterator.SORTED)) {
            return wrapped.getComparator();
        }
        return super.getComparator();
    }
}