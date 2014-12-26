package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HdrHistogramReservoirTest {

    private HdrHistogramReservoir r;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void setUp() throws Exception {
        r = new HdrHistogramReservoir();
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

    @Test
    public void testConcurrentWrites() throws ExecutionException, InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);

        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executorService.submit((Callable<Void>) () -> {
                SplittableRandom random = new SplittableRandom();
                latch.countDown();

                for (int j = 0; j < 10_000_000; j++) {
                    r.update(random.nextLong(1_000_000_000));
                }

                return null;
            }));
        }

        for (Future<Void> future : futures) {
            future.get();
        }
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