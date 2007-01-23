/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

@RunWith(Parameterized.class)
public class TestBulkUpdate {
	
	protected Model model;
	
	@Parameters public static Collection models()
	{
		Collection<Object[]> models = new ArrayList<Object[]>();
		
		//models.add(new Object[] { ModelPool.get().getIndexMySQL() } );
		//models.add(new Object[] { ModelPool.get().getIndexHSQL() });
		//models.add(new Object[] { ModelPool.get().getIndexPgSQL() });
		//models.add(new Object[] { ModelPool.get().getIndexDerby() });
		//models.add(new Object[] { ModelPool.get().getHashMySQL() } );
		//models.add(new Object[] { ModelPool.get().getHashHSQL() });
		//models.add(new Object[] { ModelPool.get().getHashPgSQL() });
		models.add(new Object[] { ModelPool.get().getHashDerby() });
		
		return models;
	}
	
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestBulkUpdate.class);
	} 
	
	public TestBulkUpdate(Model model)
	{
		this.model = model;
	}
	
	@Test public void loadOne()
	{
		model.removeAll(RDF.type, RDF.type, null);
		long size = model.size();
		model.add(RDF.type, RDF.type, "FOO");
		assertTrue("It's in there", model.contains(RDF.type, RDF.type, "FOO"));
		assertEquals("Added one triple", size + 1, model.size());
		model.remove(RDF.type, RDF.type, model.createLiteral("FOO"));
		assertEquals("Back to the start", size, model.size());
	}
	
	@Test public void loadFile()
	{
		Model toLoadAndRemove = FileManager.get().loadModel("testing/Data/data.ttl");
		
		long added = toLoadAndRemove.size();
		
		long size = model.size();
		
		model.add(toLoadAndRemove);
		
		assertEquals("Added all", size + added, model.size());
		
		model.add(RDF.type, RDF.type, RDF.type);
		
		assertTrue("Model contains <type,type,type>", model.contains(RDF.type, RDF.type, RDF.type));
		
		assertEquals("And another one", size + 14, model.size());
		
		model.remove(toLoadAndRemove);
		
		assertTrue("Model contains <type,type,type>", model.contains(RDF.type, RDF.type, RDF.type));
		
		assertEquals("Removed file", size + 1, model.size());
		
		model.remove(RDF.type, RDF.type, RDF.type);
		
		assertEquals("All removed", size, model.size());
	}
	
	@Test public void remove()
	{
		long size = model.size();
		
		model.add(RDF.nil, RDF.type, "ONE");
		model.add(RDF.nil, RDF.type, "TWO");
		model.add(RDF.nil, RDF.type, RDF.Alt);
		model.add(RDF.nil, RDF.type, RDF.first);
		
		assertEquals("All added ok", size + 4, model.size());
		
		model.removeAll(RDF.nil, RDF.type, null);
		
		assertEquals("Wild card removed all", size, model.size());
	}
	
	@Test public void dupeSuppressed()
	{
		long size = model.size();
		
		model.add(RDF.nil, RDF.type, RDF.first);
		model.add(RDF.nil, RDF.type, RDF.first);
		
		assertTrue("Model added only one item", model.size() == 1);
	}
	
	@Before public void format()
	{
		model.removeAll();
	}
	
	@AfterClass public static void closeModels()
	{
		ModelPool.get().closeAll();
	}

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
