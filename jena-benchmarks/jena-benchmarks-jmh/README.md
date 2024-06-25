# jena-benchmarks-jmh

This module contains benchmarks implemented using the Java Microbenchmark Harness (jmh) and JUnit.

## Troubleshooting

* `Unable to find the resource: /META-INF/BenchmarkList`: If you encounter this error while attempting to run the benchmark JUnit tests from the IDE of your choice then it means that the jmh annotation processor has not yet been run. The processor should run as part of the usual java compilation (via the `maven-compiler-plugin`). You can manually force the build by running `mvn -Pdev clean install` on the `jena-benchmarks-jmh` module from the command line.
