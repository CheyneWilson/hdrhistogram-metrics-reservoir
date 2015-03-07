package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramReservoirTest.assertArrayFuzzyEquals;

public class HdrHistogramResetOnSnapshotReservoirTest {
    private HdrHistogramResetOnSnapshotReservoir r;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void setUp() throws Exception {
        r = new HdrHistogramResetOnSnapshotReservoir();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
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

        assertArrayFuzzyEquals(expected, snapshot.getValues(), 0.01);
    }

    @Test
    public void testResetsOnSnapshot() {
        r.update(1);
        r.update(2);
        r.update(3);

        Snapshot snapshot = r.getSnapshot();

        assertEquals(3, snapshot.size());

        assertEquals(0, r.getSnapshot().size());
    }
}