package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Recorder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class HdrHistogramReservoirTest {

    private HdrHistogramReservoir r;

    @Before
    public void setUp() throws Exception {
        r = new HdrHistogramReservoir(new Recorder(5));
    }

    @Test
    public void testSnapshotSize() {
        r.update(1);
        r.update(2);
        r.update(3);

        Snapshot snapshot = r.getSnapshot();

        assertEquals(3, snapshot.size());
    }

    @Test
    public void testSnapshotValues() {

        int count = 1000;
        long[] expected = new long[count];
        for (int i = 0; i < count; i++) {
            r.update(i);
            expected[i] = i;
        }

        Snapshot snapshot = r.getSnapshot();

        assertArrayEquals(expected, snapshot.getValues());
    }

    @Test
    public void testSnapshotRandomValues() {
        Random random = new Random();

        long seed = random.nextLong();
        random.setSeed(seed);
        System.out.println("Using seed " + seed);

        int count = 1000;
        long[] expected = new long[count];
        for (int i = 0; i < count; i++) {
            long val = random.nextInt(1000_000_000);
            r.update(val);
            expected[i] = val;
        }

        Snapshot snapshot = r.getSnapshot();

        Arrays.sort(expected);

        assertArrayEquals(expected, snapshot.getValues());
    }
}