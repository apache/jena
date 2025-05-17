package org.apache.jena.sparql.exec.tracker;

import java.time.Instant;

public record StartRecord<T>(long id, Instant timestamp, T payload, Runnable abortAction) {}
