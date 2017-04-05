package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.shared.LockMRPlusSW;

public class LockTryMRPlusSW extends LockMRPlusSW implements LockTry {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean tryEnterCriticalSection(boolean readLockRequested) {
		return (readLockRequested ? true : tryLock());
	}
}
