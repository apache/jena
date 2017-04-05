package org.apache.jena.sparql.core.mosaic;

import java.util.function.Function;

import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class Metric extends Context {

	protected static final IDFactory ID_FACTORY = IDFactory.valueOf(Metric.class);
	
	public static final Symbol READ = Symbol.create(ID_FACTORY.suffix("read"));
	
	public static final Symbol WRITE = Symbol.create(ID_FACTORY.suffix("write"));
	
	public Integer getRead() {
		return get(READ);
	}
	
	public Integer getWrite() {
		return get(WRITE);
	}
	
	// TODO Add compute to Jena Context.
	@SuppressWarnings("unchecked")
	public <T> T compute(final Symbol key, final Function<T, T> f) {
//		return (T) compute(key, (k, v) -> {return f.apply((T) v);});
		return null;
	}
}
