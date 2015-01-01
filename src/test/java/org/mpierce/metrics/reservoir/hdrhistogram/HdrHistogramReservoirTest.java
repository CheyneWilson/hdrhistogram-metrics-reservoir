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
        initReservoir();
    }

    private void initReservoir() {
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

        for (int i = 0; i < 1000; i++) {
            initReservoir();

            int count = 1000 + random.nextInt(10000);
            long[] expected = new long[count];
            for (int j = 0; j < count; j++) {
                long val = random.nextInt(1000_000_000);
                r.update(val);
                expected[j] = val;
            }

            Snapshot snapshot = r.getSnapshot();

            Arrays.sort(expected);

            assertArrayFuzzyEquals(expected, snapshot.getValues(), 0.01);
        }
    }

    @Test
    public void testConcurrentWrites() throws ExecutionException, InterruptedException {

        int numValues = 10_000;
        int numThreads = 4;

        for (int round = 0; round < 100; round++) {
            initReservoir();

            long[] allValues = new long[numThreads * numValues];

            CountDownLatch latch = new CountDownLatch(numThreads);

            List<Future<long[]>> futures = new ArrayList<>();

            for (int i = 0; i < numThreads; i++) {
                futures.add(executorService.submit(() -> {
                    SplittableRandom random = new SplittableRandom();
                    latch.countDown();
                    latch.await();

                    long[] values = new long[numValues];
                    for (int j = 0; j < numValues; j++) {
                        long randLong = random.nextLong(1_000_000_000);
                        values[j] = randLong;
                        r.update(randLong);
                    }

                    return values;
                }));
            }

            for (int i = 0; i < futures.size(); i++) {
                System.arraycopy(futures.get(i).get(), 0, allValues, i * numValues, numValues);
            }

            Arrays.sort(allValues);

            assertArrayFuzzyEquals(allValues, r.getSnapshot().getValues(), 0.01);
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
