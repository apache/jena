/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.store.StoreLoaderPlus;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class TestStoreUpdateBase {
	
	Store store;
	StoreLoaderPlus loader;
	TableDescNodes nodeT;
	
	abstract Store getStore();
	
	protected int size(TableDesc desc) {
		return size(desc.getTableName());
	}
	
	protected int size(TableDescNodes desc) {
		return size(desc.getTableName());
	}
	
	protected int size(String name) {
		try {
			int size = -1;
			ResultSetJDBC result = store.getConnection().execQuery("SELECT COUNT(*) FROM " + name);
			if (result.get().next())
				size = result.get().getInt(1);
			result.close();
			return size;
		} catch (SQLException e) {
			throw new RuntimeException("Can't get size of table '" + name + "'", e);
		}
	}
	
	protected Node node(String str) {
		return NodeCreateUtils.create(str);
	}
	
	@Before public void init() throws SQLException {
		this.store = getStore();
		this.store.getConnection().getSqlConnection().setAutoCommit(true);
		this.loader = (StoreLoaderPlus) store.getLoader();
		this.nodeT = store.getNodeTableDesc();
	}
	
	@Test public void loadOneRemoveOne()
	{
		TableDesc desc = store.getTripleTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("B"), node("B"), node("C"));
		loader.finishBulkUpdate();
		assertEquals("Added one triple", 1, size(desc));
		assertEquals("Store added correct number of nodes", 2, size(nodeT));
		loader.startBulkUpdate();
		loader.deleteTuple(desc, node("B"), node("B"), node("C"));
		loader.finishBulkUpdate();
		assertEquals("Back to the start", 0, size(desc));
	}
	
	@Test public void loadOneRemoveOneQ()
	{
		TableDesc desc = store.getQuadTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("B"), node("B"), node("C"), node("D"));
		loader.finishBulkUpdate();
		assertEquals("Added one triple", 1, size(desc));
		assertEquals("Store added correct number of nodes", 3, size(nodeT));
		loader.startBulkUpdate();
		loader.deleteTuple(desc, node("B"), node("B"), node("C"), node("D"));
		loader.finishBulkUpdate();
		assertEquals("Back to the start", 0, size(desc));
	}
	
	@Test public void dupeSuppressed()
	{
		TableDesc desc = store.getTripleTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("F"), node("A"), node("F"));
		loader.addTuple(desc, node("F"), node("A"), node("F"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added only one item", 1, size(desc));
		assertEquals("Store added correct number of nodes", 2, size(nodeT));
		
		loader.startBulkUpdate();
		loader.addTuple(desc, node("G"), node("A"), node("F"));
		loader.finishBulkUpdate();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("G"), node("A"), node("F"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added only one item", 2, size(desc));
		assertEquals("Store added correct number of nodes", 3, size(nodeT));
	}
	
	@Test public void dupeSuppressedQ()
	{
		TableDesc desc = store.getQuadTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("F"), node("A"), node("F"), node("G"));
		loader.addTuple(desc, node("F"), node("A"), node("F"), node("G"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added only one item", 1, size(desc));
		assertEquals("Store added correct number of nodes", 3, size(nodeT));
		
		loader.startBulkUpdate();
		loader.addTuple(desc, node("G"), node("A"), node("F"), node("K"));
		loader.finishBulkUpdate();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("G"), node("A"), node("F"), node("K"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added only one item", 2, size(desc));
		assertEquals("Store added correct number of nodes", 4, size(nodeT));
	}
	
	@Test public void mixItUp()
	{
		TableDesc desc1 = store.getTripleTableDesc();
		TableDesc desc2 = store.getQuadTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc2, node("F"), node("A"), node("F"), node("G"));
		loader.addTuple(desc1, node("A"), node("F"), node("G"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added to triples", 1, size(desc1));
		assertEquals("Store added to quads", 1, size(desc2));
		assertEquals("Store added correct number of nodes", 3, size(nodeT));
		
		loader.startBulkUpdate();
		loader.addTuple(desc2, node("G"), node("A"), node("F"), node("K"));
		loader.finishBulkUpdate();
		loader.startBulkUpdate();
		loader.addTuple(desc1, node("G"), node("A"), node("F"));
		loader.finishBulkUpdate();
		
		assertEquals("Store added one to triples", 2, size(desc1));
		assertEquals("Store added one to quads", 2, size(desc2));
		assertEquals("Store added correct number of nodes", 4, size(nodeT));
		
		loader.startBulkUpdate();
		loader.addTuple(desc2, node("G"), node("A"), node("F"), node("G"));
		loader.deleteTuple(desc1, node("A"), node("F"), node("G"));
		loader.addTuple(desc1, node("B"), node("F"), node("G"));
		loader.deleteTuple(desc2, node("G"), node("A"), node("F"), node("G"));
		loader.deleteTuple(desc2, node("B"), node("A"), node("F"), node("G"));
		loader.addTuple(desc1, node("B"), node("B"), node("B"));
		loader.addTuple(desc2, node("B"), node("B"), node("B"), node("B"));
		loader.finishBulkUpdate();
		
		assertEquals("Store triple size correct", 3, size(desc1));
		assertEquals("Store quad size correct", 3, size(desc2));
		assertEquals("Store nodes node size correct", 5, size(nodeT));
	}
	
	@Test(expected = IllegalArgumentException.class) public void arityViolation()
	{
		TableDesc desc = store.getQuadTableDesc();
		
		loader.startBulkUpdate();
		loader.addTuple(desc, node("One"));
		loader.finishBulkUpdate();
	}
	
	@Test public void sizes() {
		TableDesc desc = store.getTripleTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("A"), node("A"), node("A"));
		loader.addTuple(desc, node("B"), node("B"), node("B"));
		loader.finishBulkUpdate();
		desc = store.getQuadTableDesc();
		loader.startBulkUpdate();
		loader.addTuple(desc, node("A"), node("A"), node("A"), node("A"));
		loader.addTuple(desc, node("B"), node("A"), node("A"), node("A"));
		loader.addTuple(desc, node("B"), node("B"), node("B"), node("B"));
		loader.addTuple(desc, node("B"), node("C"), node("C"), node("C"));
		loader.finishBulkUpdate();
		
		assertEquals("Triple size right", 2l, store.getSize());
		assertEquals("Quad size right", 1l, store.getSize(node("A")));
		assertEquals("Quad size (2) right", 3l, store.getSize(node("B")));
	}
	
	@Test public void rollback() {
		Model model = SDBFactory.connectDefaultModel(store);
		
		assertTrue("Initially empty", model.isEmpty());
		model.begin();
		model.add(RDF.type, RDF.type, RDF.type);
		assertTrue("Uncommited triple can be seen", model.contains(RDF.type, RDF.type, RDF.type));
		model.abort();
		assertTrue("Nothing was added, the add aborted", model.isEmpty());
		model.add(RDF.type, RDF.type, RDF.type);
		assertEquals("Model contains 1 triple", 1l, model.size());
		model.begin();
		model.remove(RDF.type, RDF.type, RDF.type);
		model.abort();
		assertEquals("Model still contains 1 triple", 1l, model.size());
	}
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
