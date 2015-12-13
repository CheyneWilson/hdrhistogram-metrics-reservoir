[![Build Status](https://semaphoreapp.com/api/v1/projects/60b0719a-f47d-447f-8f85-5ced5cca143e/317138/badge.png)](https://semaphoreapp.com/marshallpierce/hdrhistogram-metrics-reservoir)

A [Metrics](https://dropwizard.github.io/metrics/3.1.0/) [Reservoir](https://dropwizard.github.io/metrics/3.1.0/manual/core/#uniform-reservoirs) implementation backed by [HdrHistogram](http://hdrhistogram.org/).

The artifacts are [published to Bintray](https://bintray.com/marshallpierce/maven/org.mpierce.metrics.reservoir%3Ahdrhistogram-metrics-reservoir/view). If you're using gradle, use the `jcenter()` repository and put this in your `build.gradle` `dependencies` block:
```
compile 'org.mpierce.metrics.reservoir:hdrhistogram-metrics-reservoir:THE_LATEST_RELEASED_VERSION'
```

Then, use the following reservoir implementations when you create your `Histogram` or `Timer` instances:

- `HdrHistogramReservoir`, a reservoir that accumulates its internal state forever
- `HdrHistogramResetOnSnapshotReservoir`, a reservoir that resets its internal state on each snapshot (which is how reporters get information from reservoirs). See [this article](http://taint.org/2014/01/16/145944a.html) for when that is useful.

See http://hdrhistogram.org/ for why you want HdrHistogram as the underlying data structure, or watch [Gil Tene talking about how latency is hard to measure well](http://www.infoq.com/presentations/latency-pitfalls), or read [this thread](https://groups.google.com/forum/#!msg/mechanical-sympathy/I4JfZQ1GYi8/ocuzIyC3N9EJ). The short version:

- It's very fast (tens of nanoseconds to record)
- It won't lose the outliers, which are the measurements you care about since latency is most assuredly not normally distributed
- It doesn't allocate (except for the occasional resize, or you can pre-size your histograms to not allocate at all)

Also, check out some [simple benchmarks of the various reservoir implementations](https://bitbucket.org/marshallpierce/metrics-reservoir-benchmark).
