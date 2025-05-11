package org.apache.jena.sparql.exec.tracker.todelete;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record FinishRecord<T>(StartRecord<T> start, Instant timestamp, Throwable throwable) {
//    public FinishRecord(StartRecord<T> start, Throwable throwable) {
//        this(start, Instant.now(), throwable);
//    }

    public FinishRecord(StartRecord<T> start, Instant timestamp, Throwable throwable) {
        this.start = Objects.requireNonNull(start);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.throwable = throwable;
    }

    public Duration duration() {
        return Duration.between(start.timestamp(), timestamp);
    }

    public boolean isSuccess() {
        return throwable == null;
    }
}
