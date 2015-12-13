package org.mpierce.metrics.reservoir.hdrhistogram;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HdrHistogramResetOnSnapshotReservoirTest extends HdrHistogramReservoirTestCase {

    @Override
    Reservoir getReservoir() {
        return new HdrHistogramResetOnSnapshotReservoir();
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

    @Test
    public void testMinAfterReset() {
        r.update(1);
        r.update(2);
        r.update(3);

        Snapshot snapshot = r.getSnapshot();

        assertEquals(1, snapshot.getMin());
    }
}
