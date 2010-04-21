/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.junit.Test ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.LabelExistsException ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.Lock ;

import static junit.framework.Assert.* ;

public abstract class TestDataset
{
    protected abstract Dataset create() ;
    
    @Test public void dataset_01()
    {
        Dataset ds = create() ;
        assertNotNull(ds.getDefaultModel()) ;
        assertNotNull(ds.asDatasetGraph()) ;
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
    
    @Test public void dataset_02()
    {
        Dataset ds = create() ;
        //
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