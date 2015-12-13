package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class HdrHistogramReservoirTestCase {

    Reservoir r;

    @Before
    public void setUp() {
        initReservoir();
    }

    /**
     * Some tests need to re-initialize state, so we make a method for it
     */
    void initReservoir() {
        r = getReservoir();
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
    public void testMinValueOnNewReservoir() {
        assertEquals(0, r.getSnapshot().getMin());
    }

    @Test
    public void testMaxValueOnNewReservoir() {
        assertEquals(0, r.getSnapshot().getMax());
    }

    @Test
    public void testMinValueOn1Sample() {
        r.update(5);

        assertEquals(5, r.getSnapshot().getMin());
    }

    @Test
    public void testMaxValueOn1Sample() {
        r.update(5);

        assertEquals(5, r.getSnapshot().getMax());
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

    abstract Reservoir getReservoir();
}
