package org.apache.jena.sparql.core;

import org.apache.jena.sparql.core.pergraph.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ BasicTest.class, FindPatternsTest.class, FindTest.class, LockTest.class, TransactionLifecycleTest.class,
		ViewTest.class, MultithreadingTest.class })
public class TS_DatasetGraphPerGraphLocking {}
