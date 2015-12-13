package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Test;

public class HdrHistogramReservoirTest extends HdrHistogramReservoirTestCase {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    Reservoir getReservoir() {
        return new HdrHistogramReservoir();
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
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

        final int numValues = 10_000;
        int numThreads = 4;

        for (int round = 0; round < 100; round++) {
            initReservoir();

            long[] allValues = new long[numThreads * numValues];

            final CountDownLatch latch = new CountDownLatch(numThreads);

            List<Future<long[]>> futures = new ArrayList<>();

            for (int i = 0; i < numThreads; i++) {
                futures.add(executorService.submit(
                        new Callable<long[]>() {
                            @Override
                            public long[] call() throws InterruptedException {

                                Random random = new Random();
                                latch.countDown();
                                latch.await();

                                long[] values = new long[numValues];
                                for (int j = 0; j < numValues; j++) {
                                    long randLong = random.nextInt(1_000_000_000);
                                    values[j] = randLong;
                                    r.update(randLong);
                                }

                                return values;
                            }
                        }

                ));
            }

            for (int i = 0; i < futures.size(); i++) {
                System.arraycopy(futures.get(i).get(), 0, allValues, i * numValues, numValues);
            }

            Arrays.sort(allValues);

            assertArrayFuzzyEquals(allValues, r.getSnapshot().getValues(), 0.01);
        }
    }

}
