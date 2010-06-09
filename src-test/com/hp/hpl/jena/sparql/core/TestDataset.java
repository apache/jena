/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import static junit.framework.Assert.assertEquals ;
import static junit.framework.Assert.assertFalse ;
import static junit.framework.Assert.assertNotNull ;
import static junit.framework.Assert.assertNull ;
import static junit.framework.Assert.assertTrue ;

import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;

public abstract class TestDataset
{
    protected abstract DataSource create() ;
    
    static Model model1 = ModelFactory.createDefaultModel() ;
    static Model model2 = ModelFactory.createDefaultModel() ;
    
    static Resource s1 = model1.createResource("s1") ;
    static Resource s2 = model1.createResource("s2") ;

    static Property p1 = model1.createProperty("p1") ;
    static Property p2 = model1.createProperty("p2") ;
    
    static Resource o1 = model1.createResource("o1") ;
    static Resource o2 = model1.createResource("o2") ;
    
    static {
        model1.add(s1, p1, o1) ;
        model2.add(s2, p2, o2) ;
    }
    
    /*
    public Model getDefaultModel() ;
    public Model getNamedModel(String uri) ;
    public boolean containsNamedModel(String uri) ;
    public Iterator<String> listNames() ;
    public Lock getLock() ;
    public DatasetGraph asDatasetGraph() ; 
    public void close() ;

    public void  setDefaultModel(Model model) ;
    public void  addNamedModel(String uri, Model model) throws LabelExistsException ;
    public void  removeNamedModel(String uri) ;
    public void  replaceNamedModel(String uri, Model model) ;
     */
    
    @Test public void dataset_01()
    {
        Dataset ds = create() ;
        assertNotNull(ds.getDefaultModel()) ;
        assertNotNull(ds.asDatasetGraph()) ;
    }
    
    @Test public void dataset_02()
    {
        Dataset ds = create() ;
        ds.getDefaultModel().add(s1,p1,o1) ;
        assertTrue(model1.isIsomorphicWith(ds.getDefaultModel())) ;
    }

    @Test public void datasource_01()
    {
        DataSource ds = create() ;
        ds.setDefaultModel(model2) ;
        assertTrue(model2.isIsomorphicWith(ds.getDefaultModel())) ;
    }

    @Test public void datasource_02()
    {
        String graphName = "http://example/" ;
        DataSource ds = create() ;
        ds.addNamedModel(graphName, model1) ;
        assertTrue(ds.containsNamedModel(graphName)) ;
        
        List<String> x = Iter.toList(ds.listNames()) ;
        assertEquals(1, x.size()) ;
        assertEquals(graphName, x.get(0)) ;
        
        assertFalse(model1.isIsomorphicWith(ds.getDefaultModel())) ;
        Model m = ds.getNamedModel(graphName) ;

        assertNotNull(m) ;
        assertTrue(model1.isIsomorphicWith(m)) ;
        
        ds.removeNamedModel(graphName) ;
        Model m2 = ds.getNamedModel(graphName) ;
        assertNull(m2) ;
    }

    @Test public void datasource_03()
    {
        String graphName = "http://example/" ;
        DataSource ds = create() ;
        ds.addNamedModel(graphName, model1) ;
        ds.replaceNamedModel(graphName, model2) ;
        assertTrue(ds.containsNamedModel(graphName)) ;
        
        List<String> x = Iter.toList(ds.listNames()) ;
        assertEquals(1, x.size()) ;
        assertEquals(graphName, x.get(0)) ;
        
        assertFalse(model1.isIsomorphicWith(ds.getNamedModel(graphName))) ;
        assertTrue(model2.isIsomorphicWith(ds.getNamedModel(graphName))) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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