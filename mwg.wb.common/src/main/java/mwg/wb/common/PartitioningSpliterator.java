package mwg.wb.common;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class PartitioningSpliterator<E> extends Spliterators.AbstractSpliterator<Spliterator<E>> {
	private final Spliterator<E> wrapped;
	private final int partitionSize;

	public PartitioningSpliterator(Spliterator<E> wrapped, int partitionSize) {
		super(wrapped.estimateSize(), wrapped.characteristics() & ~Spliterator.SORTED);
		this.wrapped = wrapped;
		this.partitionSize = partitionSize;
	}

	@Override
	public boolean tryAdvance(Consumer<? super Spliterator<E>> action) {
		Spliterator<E> s = new FixedSizeSpliterator<>(wrapped, partitionSize);
		action.accept(s);
		return wrapped.estimateSize() != 0;
	}

	@Override
	public long estimateSize() {
		long e = wrapped.estimateSize();
		if (e == Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		long remainder = e % partitionSize;
		if (remainder == 0) {
			return e / partitionSize;
		}
		return e / partitionSize + 1;
	}
}