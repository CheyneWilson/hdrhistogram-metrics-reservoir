package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.SplittableRandom;

public class HdrHistogramReservoirBenchmark {

    @State(Scope.Group)
    public static class GroupState {
        final HdrHistogramReservoir reservoir = new HdrHistogramReservoir();
    }

    @State(Scope.Thread)
    public static class ThreadState {
        final SplittableRandom random = new SplittableRandom();
    }

    @Benchmark
    @Group("readWhileConcurrentRecording")
    @GroupThreads(2)
    public void recordConcurrentMeasurements(GroupState groupState, ThreadState threadState) {
        groupState.reservoir.update(threadState.random.nextLong(1_000_000_000));
    }

    @Benchmark
    @Group("readWhileConcurrentRecording")
    public Snapshot readSnapshotsForConcurrentMeasurements(GroupState groupState) throws InterruptedException {
        // don't really care about the performance of reading much as it's allocation-heavy and boring.
        // Just want to perturb writing now and then to be representative.
        Thread.sleep(100);
        return groupState.reservoir.getSnapshot();
    }

    @Benchmark
    @Group("readWhileSingleThreadedRecording")
    @GroupThreads(1)
    public void recordSingleThreadMeasurements(GroupState groupState, ThreadState threadState) {
        groupState.reservoir.update(threadState.random.nextLong(1_000_000_000));
    }

    @Benchmark
    @Group("readWhileSingleThreadedRecording")
    public Snapshot readSnapshotsForSingleThreadMeasurements(GroupState groupState) throws InterruptedException {
        Thread.sleep(100);
        return groupState.reservoir.getSnapshot();
    }

    @Benchmark
    public long baselineRandomGeneration(ThreadState threadState) throws InterruptedException {
        return threadState.random.nextLong(1_000_000_000);
    }
}

