/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.util.DatasetUtils;

// TODO Integrate this
//   use in FROM/FROM NAMED and change Query class.
//   use in DatasetUtils
//   use in tests

public class DatasetDesc
{
    private List<String> defaultGraphURIs = new ArrayList<String>() ;
    private List<String> namedGraphURIs = new ArrayList<String>() ;
    
    public DatasetDesc() {}
    public void addDefaultGraphURI(String uri) { defaultGraphURIs.add(uri) ; }
    public void addNamedGraphURI(String uri) { namedGraphURIs.add(uri) ; }
    
    public List<String> getDefaultGraphURIs() { return defaultGraphURIs ; }
    public List<String> getNamedGraphURIs() { return namedGraphURIs ; }
    
    public Iterator<String> eachDefaultGraphURI() { return defaultGraphURIs.iterator() ; }
    public Iterator<String> eachNamedGraphURI() { return namedGraphURIs.iterator() ; }
    
    public Dataset create() { return DatasetUtils.createDataset(this) ; }
    public DatasetGraph createDatasetGraph() { return DatasetUtils.createDatasetGraph(this) ; }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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