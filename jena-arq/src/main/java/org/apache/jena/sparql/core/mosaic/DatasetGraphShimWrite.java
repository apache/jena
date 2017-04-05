package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public interface DatasetGraphShimWrite {

	public void add(Quad quad);

	public void add(Node g, Node s, Node p, Node o);

	public void delete(Quad quad);

	public void delete(Node g, Node s, Node p, Node o);

	public void deleteAny(Node g, Node s, Node p, Node o);

}
