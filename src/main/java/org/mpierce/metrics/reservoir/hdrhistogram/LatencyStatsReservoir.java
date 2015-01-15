package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.LatencyUtils.LatencyStats;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class LatencyStatsReservoir implements Reservoir {

    private final LatencyStats stats;

    @GuardedBy("this")
    private final Histogram runningTotals;

    @GuardedBy("this")
    @Nonnull
    private Histogram intervalHistogram;

    public LatencyStatsReservoir(LatencyStats stats) {
        this.stats = stats;
        intervalHistogram = stats.getIntervalHistogram();
        runningTotals = new Histogram(intervalHistogram.getNumberOfSignificantValueDigits());
    }

    @Override
    public int size() {
        return getSnapshot().size();
    }

    @Override
    public void update(long value) {
        stats.recordLatency(value);
    }

    @Override
    public Snapshot getSnapshot() {
        return new HistogramSnapshot(updateRunningTotals());
    }

    /**
     * @return a copy of the accumulated state
     */
    @Nonnull
    private synchronized Histogram updateRunningTotals() {
        stats.addIntervalHistogramTo(runningTotals);
        runningTotals.add(intervalHistogram);
        return runningTotals.copy();
    }
}
