A [Metrics](https://dropwizard.github.io/metrics/3.1.0/) [Reservoir](https://dropwizard.github.io/metrics/3.1.0/manual/core/#uniform-reservoirs) implementation backed by [HdrHistogram](http://hdrhistogram.org/).

See http://hdrhistogram.org/ for why you want HdrHistogram as the underlying data structure. The short version:
- It's very fast
- It won't lose outliers
- It doesn't allocate