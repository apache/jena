package org.apache.jena.sparql.core.mosaic;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.util.Symbol;

public class Topology {

	protected static final Map<Symbol, Metric> ENTRIES;
	
	public static Symbol keyFor(final Class<? extends DatasetGraph> c) {
		return Symbol.create(c.getName());
	}
	
	static {
		final Map<Symbol, Metric> entries = new HashMap<>();
		
		entries.put(keyFor(DatasetGraph.class), new Metric());
		
		entries.put(keyFor(DatasetGraphInMemory.class), new Metric());
		
//		entries.put(keyFor(DatasetGraphTransaction.class), new Metric());
		
//		entries.put(keyFor(DatasetGraphThriftClient.class), new Metric());
		
		ENTRIES = entries;
	}
	
	protected final Map<Symbol, Metric> entries;
	
	protected final Metric metric = new Metric();

	public Topology() {
		super();
		this.entries = new HashMap<>();
		entries.putAll(ENTRIES);
	}
	
	public Metric getMetric() {
		return metric;
	}
}
