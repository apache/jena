package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.shared.Lock;

public interface LockTry extends Lock {

	boolean tryEnterCriticalSection(boolean readLockRequested);
}
