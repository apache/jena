package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Symbol;

public interface DatasetGraphShimWrite {

	static final IDFactory ID_FACTORY = IDFactory.valueOf(DatasetGraphShimWrite.class);
	
	static final Symbol SHIM_WRITE = Symbol.create(ID_FACTORY.suffix("shimWrite"));

	static DatasetGraphShimWrite RO = new DatasetGraphShimWrite() {
		
		@Override
		public void add(Node g, Node s, Node p, Node o) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void add(Quad quad) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void deleteAny(Node g, Node s, Node p, Node o) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void delete(Node g, Node s, Node p, Node o) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void delete(Quad quad) {
			throw new UnsupportedOperationException();
		}
	};
	
	void add(Quad quad);

	void add(Node g, Node s, Node p, Node o);

	void delete(Quad quad);

	void delete(Node g, Node s, Node p, Node o);

	void deleteAny(Node g, Node s, Node p, Node o);

}
