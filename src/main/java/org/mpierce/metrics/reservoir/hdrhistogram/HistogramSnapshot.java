package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

final class HistogramSnapshot extends Snapshot {
    private final Histogram histogram;

    HistogramSnapshot(Histogram histogram) {
        this.histogram = histogram;
    }

    @Override
    public double getValue(double quantile) {
        return histogram.getValueAtPercentile(quantile * 100.0);
    }

    @Override
    public long[] getValues() {
        long[] vals = new long[64];
        int i = 0;

        for (HistogramIterationValue value : histogram.recordedValues()) {
            long val = value.getValueIteratedTo();

            for (int j = 0; j < value.getCountAddedInThisIterationStep(); j++) {
                vals[i] = val;

                if (i == vals.length - 1) {
                    // we've filled up this array; double it
                    long[] oldVals = vals;
                    vals = new long[vals.length * 2];
                    System.arraycopy(oldVals, 0, vals, 0, oldVals.length);
                }

                i++;
            }
        }

        // trim
        long[] trimmed = new long[i];
        System.arraycopy(vals, 0, trimmed, 0, i);

        return trimmed;
    }

    @Override
    public int size() {
        return (int) histogram.getTotalCount();
    }

    @Override
    public long getMax() {
        return histogram.getMaxValue();
    }

    @Override
    public double getMean() {
        return histogram.getMean();
    }

    @Override
    public long getMin() {
        return histogram.getMinValue();
    }

    @Override
    public double getStdDev() {
        return histogram.getStdDeviation();
    }

    @Override
    public void dump(OutputStream output) {
        try (PrintWriter p = new PrintWriter(new OutputStreamWriter(output, UTF_8))) {
            for (HistogramIterationValue value : histogram.recordedValues()) {
                for (int j = 0; j < value.getCountAddedInThisIterationStep(); j++) {
                    p.printf("%d%n", value.getValueIteratedTo());
                }
            }
        }
    }
}
