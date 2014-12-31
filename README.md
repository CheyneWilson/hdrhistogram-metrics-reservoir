[![Build Status](https://semaphoreapp.com/api/v1/projects/60b0719a-f47d-447f-8f85-5ced5cca143e/317138/badge.png)](https://semaphoreapp.com/marshallpierce/hdrhistogram-metrics-reservoir)

A [Metrics](https://dropwizard.github.io/metrics/3.1.0/) [Reservoir](https://dropwizard.github.io/metrics/3.1.0/manual/core/#uniform-reservoirs) implementation backed by [HdrHistogram](http://hdrhistogram.org/).

The artifacts are [published to Maven Central](http://search.maven.org/#search|ga|1|hdrhistogram-metrics-reservoir). If you're using gradle, put this in your `build.gradle`:
```
compile 'org.mpierce.metrics.reservoir:hdrhistogram-metrics-reservoir:THE_LATEST_RELEASED_VERSION'
```

Then, use `HdrHistogramReservoir` when you create your `Histogram` instances.

See http://hdrhistogram.org/ for why you want HdrHistogram as the underlying data structure, or watch [Gil Tene talking about how latency is hard to measure well](http://www.infoq.com/presentations/latency-pitfalls), or read [this thread](https://groups.google.com/forum/#!msg/mechanical-sympathy/I4JfZQ1GYi8/ocuzIyC3N9EJ). The short version:

- It's very fast (tens of nanoseconds to record)
- It won't lose the outliers, which are the measurements you care about since latency is most assuredly not normally distributed
- It doesn't allocate (except for the occasional resize, or you can pre-size your histograms to not allocate at all)

Also, check out some [simple benchmarks of the various reservoir implementations](https://bitbucket.org/marshallpierce/metrics-reservoir-benchmark).
