/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.*;

import org.apache.commons.logging.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.Lock; 
import com.hp.hpl.jena.shared.LockMRSW;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** A implementation of a DataSource, which is a Dataset,
 *  a set of a single unnamed graph and a number (zero or
 *  more) named graphs with graphs as Models. 
 * 
 * @author Andy Seaborne
 * @version $Id: DataSourceImpl.java,v 1.14 2007/02/05 17:11:39 andy_seaborne Exp $
 */

public class DataSourceImpl implements DataSource//, ModelGroup
{
    Model defaultModel = null ;
    Map namedModels = new HashMap() ;
    Lock lock = new LockMRSW() ;
    
    public DataSourceImpl() { this(GraphUtils.makeDefaultModel()) ; }
    
    // Clone
    public DataSourceImpl(Dataset Dataset)
    {
        if ( ! ( Dataset instanceof DataSourceImpl ) )
        {
            LogFactory.getLog(DataSourceImpl.class).fatal("Clone Dataset: only DataSourceImpl supported") ;
            return ;
        }            
        DataSourceImpl ds = (DataSourceImpl)Dataset ;
        namedModels.putAll(ds.namedModels) ;
        defaultModel = ds.defaultModel ;
    }

    // Clone
    public DataSourceImpl(DatasetGraph dataset)
    {
        cloneFromDatasetGraph(dataset) ;
    }

    
    public DataSourceImpl(Model m)
    {
        setDefaultModel(m) ;
    }
    
    // ---- Dataset interface
    
    public Model getDefaultModel() { return defaultModel ; } 
    
    public Model getNamedModel(String uri)
    {
        return (Model)namedModels.get(uri) ;
    }

    public boolean containsNamedModel(String uri) { return namedModels.containsKey(uri) ; } 
    
    public Iterator listNames()
    {
        return namedModels.keySet().iterator() ;
    }

    public void close()
    {
        Iterator iter = listNames() ;
        for ( ; iter.hasNext() ; )
        {
            Model m = (Model)iter.next() ;
            m.close() ;
        }

        namedModels.clear() ;
        
        if ( defaultModel != null )
            defaultModel.close() ;
    }
    
    // ---- DataSource interface
    
    public void setDefaultModel(Model model)
    {
        defaultModel = model ;  
    }


    public void addNamedModel(String uri, Model model) throws LabelExistsException
    {
        if ( namedModels.containsKey(uri))
            throw new LabelExistsException("Duplicate URI for named graph: "+uri) ;
        namedModels.put(uri, model) ;
    }

    public void removeNamedModel(String uri)
    {
        removeNamedGraph(uri) ;
    }


    public void removeNamedGraph(String uri)
    {
        namedModels.remove(uri) ;
    }


    public void replaceNamedModel(String uri, Model model)
    {
        removeNamedModel(uri) ;
        addNamedModel(uri, model) ;
    }

    public String toString()
    {
        String s = "{" ;
        if ( getDefaultModel() == null )
            s = s+"<null>" ;
        else
            s = s+"["+getDefaultModel().size()+"]" ;
        for ( Iterator iter = listNames() ; iter.hasNext() ; )
        {
            String name = (String)iter.next() ;
            s = s+", ("+name+", ["+getNamedModel(name).size()+"])" ;
        }
        s = s + "}" ;
        return s ;
    }

    public Lock getLock()
    {
        return lock ;
    }
    
    private void cloneFromDatasetGraph(DatasetGraph dataset)
    {
        if ( dataset.getDefaultGraph() != null )
            defaultModel = ModelFactory.createModelForGraph(dataset.getDefaultGraph()) ;
        
        Iterator iter = dataset.listNames() ;
        while(iter.hasNext())
        {
            String uri = (String)iter.next() ;
            Graph g = dataset.getNamedGraph(uri) ;
            if ( g == null )
                continue ;
            Model m = ModelFactory.createModelForGraph(g) ;
            addNamedModel(uri, m) ;
        }
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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