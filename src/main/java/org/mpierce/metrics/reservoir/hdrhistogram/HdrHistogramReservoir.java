package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class HdrHistogramReservoir implements Reservoir {

    private final Recorder recorder;

    @GuardedBy("this")
    private final Histogram runningTotals = new Histogram(5);

    /**
     * If non-null, use as destination for getIntervalHistogram.
     */
    @Nullable
    @GuardedBy("this")
    private Histogram intervalHistogram;

    public HdrHistogramReservoir(Recorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public int size() {
        // This appears to be infrequently called, so not keeping a separate counter just for this.
        return getSnapshot().size();
    }

    @Override
    public void update(long value) {
        recorder.recordValue(value);
    }

    @Override
    public Snapshot getSnapshot() {
        return new HistogramSnapshot(updateRunningTotals());
    }

    /**
     * @return a copy of the accumulated state
     */
    private synchronized Histogram updateRunningTotals() {
        Histogram dest = null;
        if (intervalHistogram != null) {
            dest = intervalHistogram;
            intervalHistogram = null;
        }

        Histogram result = recorder.getIntervalHistogram(dest);
        runningTotals.add(result);
        intervalHistogram = result;
        return runningTotals.copy();
    }
}
