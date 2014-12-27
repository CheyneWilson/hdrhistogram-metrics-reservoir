A [Metrics](https://dropwizard.github.io/metrics/3.1.0/) [Reservoir](https://dropwizard.github.io/metrics/3.1.0/manual/core/#uniform-reservoirs) implementation backed by [HdrHistogram](http://hdrhistogram.org/).

Put it in your `build.gradle`, then use `HdrHistogramReservoir` when you create your `Histogram` instances:
```
compile 'org.mpierce.metrics.reservoir:hdrhistogram-metrics-reservoir:THE_LATEST_RELEASED_VERSION'
```

See http://hdrhistogram.org/ for why you want HdrHistogram as the underlying data structure, or watch [Gil Tene talking about how latency is hard to measure well](http://www.infoq.com/presentations/latency-pitfalls). The short version:

- It's very fast (tens of nanoseconds to record)
- It won't lose the outliers, which are the measurements you care about since latency is most assuredly not normally distributed
- It doesn't allocate (except for the occasional resize, or you can pre-size your histograms to not allocate at all)

Also, check out some [simple benchmarks of the various reservoir implementations](https://bitbucket.org/marshallpierce/metrics-reservoir-benchmark).