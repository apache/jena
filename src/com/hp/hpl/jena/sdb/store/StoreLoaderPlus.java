package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.graph.Node;

public interface StoreLoaderPlus extends StoreLoader {
	public void addQuad(Node g, Node s, Node p, Node o);
	public void deleteQuad(Node g, Node s, Node p, Node o);
	public void addTuple(TableDesc t, Node... nodes);
	public void deleteTuple(TableDesc t, Node... nodes);
}
