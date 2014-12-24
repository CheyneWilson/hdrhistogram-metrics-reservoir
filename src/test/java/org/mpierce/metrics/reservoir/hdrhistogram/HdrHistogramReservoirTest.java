package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HdrHistogramReservoirTest {

    private HdrHistogramReservoir r;

    @Before
    public void setUp() throws Exception {
        r = new HdrHistogramReservoir();
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

        assertArrayFuzzyEquals(expected, snapshot.getValues(), 0.01);
    }

    /**
     * Fuzzy array equality where the fuzz permissible scales with the expected value.
     *
     * @param expected expected
     * @param actual   actual
     * @param fuzz     actual[i] must be within expected[i] * fuzz
     */
    static void assertArrayFuzzyEquals(long[] expected, long[] actual, double fuzz) {
        assertEquals("length", expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {

            long e = expected[i];
            long a = actual[i];
            long delta = Math.abs(e - a);
            assertTrue("index " + i + " expected " + e + " actual " + a, delta <= e * fuzz);
        }
    }
}