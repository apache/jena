package org.apache.jena.sparql.core.mosaic;

import java.util.concurrent.locks.ReentrantLock;

public class LockTryMRSW implements LockTry {

	protected final ReentrantLock reentrantLock;
	
	public LockTryMRSW() {
		super();
		reentrantLock = new ReentrantLock(true);
	}
	
	@Override
	public boolean tryEnterCriticalSection(boolean readLockRequested) {
		return (readLockRequested ? true : reentrantLock.tryLock());
	}
	
	@Override
	public void enterCriticalSection(boolean readLockRequested) {
		if (!readLockRequested) {
			reentrantLock.lock();
		}
	}

	@Override
	public void leaveCriticalSection() {
		if (reentrantLock.isHeldByCurrentThread()) {
			reentrantLock.unlock();
		}
	}

	@Override
	public String toString() {
		return reentrantLock.toString();
	}
}
