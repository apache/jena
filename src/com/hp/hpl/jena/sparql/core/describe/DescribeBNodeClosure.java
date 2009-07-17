/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core.describe;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Closure;
import com.hp.hpl.jena.sparql.util.Context;

/** DescribeHandler that calculates the bNode closure.
 *  Takes all the statements of this resource, and for every object that is
 *  a bNode, it recursively includes its statements.
 * 
 * @author Andy Seaborne
 */

public class DescribeBNodeClosure implements DescribeHandler
{
    Model acc ;
    Dataset dataset ;
    
    public DescribeBNodeClosure() {}
    
    public void start(Model accumulateResultModel, Context cxt)
    {
        acc = accumulateResultModel ;
        this.dataset = (Dataset)cxt.get(ARQConstants.sysCurrentDataset) ;
    }

    private static Query query = QueryFactory.create("SELECT ?g { GRAPH ?g { ?s ?p ?o } }") ; 
    
    // Check all named graphs
    public void describe(Resource r)
    {
        // Default model.
        Closure.closure(otherModel(r, dataset.getDefaultModel()), false, acc) ;

        // Find all the named graphs in which this resource
        // occurs as a subject.  Faster than iterating in the
        // names of graphs in the case of very large numbers
        // of graphs, few of which contain the resource, in
        // some kind of persistent storage.
        
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        qsm.add("s", r) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset, qsm) ;
        ResultSet rs = qExec.execSelect() ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution qs = rs.next() ;
            String gName = qs.getResource("g").getURI() ;
            Model model =  dataset.getNamedModel(gName) ;
            Resource r2 = otherModel(r, model) ;
            Closure.closure(r2, false, acc) ;
        }
        
//        // Named graphs
//        for ( Iterator<String> iter = dataset.listNames() ; iter.hasNext() ; )
//        {
//            String name = iter.next();
//            Model model =  dataset.getNamedModel(name) ;
//            Resource r2 = otherModel(r, model) ;
//            Closure.closure(r2, false, acc) ;
//        }
        
        Closure.closure(r, false, acc) ;
    }

    private static Resource otherModel(Resource r, Model model)
    {
        if ( r.isURIResource() )
            return model.createResource(r.getURI()) ;
        if ( r.isAnon() )
            return model.createResource(r.getId()) ;
        // Literals do not need converting.
        return r ;
    }

    public void finish()
    { }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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