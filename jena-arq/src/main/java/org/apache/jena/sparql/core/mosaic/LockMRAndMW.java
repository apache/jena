package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.shared.Lock;

public class LockMRAndMW implements Lock {

	@Override
	public void enterCriticalSection(boolean readLockRequested) {
	}

	@Override
	public void leaveCriticalSection() {
	}

}
