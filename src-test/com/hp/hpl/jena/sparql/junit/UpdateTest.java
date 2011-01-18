/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.riot.checker.CheckerLiterals ;

import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.core.DataSourceImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.junit.TestUtils ;
import com.hp.hpl.jena.vocabulary.RDFS ;


public class UpdateTest extends EarlTestCase
{
    private Resource action ;
    private Resource result ;
    
    private String updateFile ;
    private Dataset input ;
    private Dataset output ;

    public static UpdateTest create(String testName, EarlReport earl, Resource entry, Resource action, Resource result)
    {
        if ( ! action.hasProperty(TestManifestUpdate_11.request) )
        {
            System.err.println("No request in action: "+testName) ;
            return null ;
        }
        return new UpdateTest(testName, earl, entry, action, result) ;
        
    }
    private UpdateTest(String testName, EarlReport earl, Resource entry, Resource action, Resource result)
    {
        super(TestUtils.safeName(testName), entry.getURI(), earl) ;
        this.action = action ;
        this.result = result ;
        /*
         *  mf:action [ ut:query <insert-data-spo1.rq> ; 
                ut:data <empty.ttl> 
              ] ;
    mf:result [ ut:result ut:success ; 
                ut:data  <spo.ttl>
              ] .

         */
        
        updateFile = action.getProperty(TestManifestUpdate_11.request).getResource().getURI() ;
    }
    
    private boolean oldWarningFlag  ;
    @Override
    protected void setUp() throws Exception
    {
        super.setUp() ;
        // Turn parser warnings off for the test data. 
        oldWarningFlag = CheckerLiterals.WarnOnBadLiterals ;
        //CheckerLiterals.WarnOnBadLiterals = false ;
        input = getDataset(action) ;
        output = getDataset(result) ;
    }
    
    @Override
    protected void tearDown() throws Exception
    {
//        if ( resetNeeded )
//            ARQ.setFalse(ARQ.strictGraph) ;
        CheckerLiterals.WarnOnBadLiterals = oldWarningFlag ;
        input = null ;
        output = null ;
        super.tearDown() ;
    }
    
    @Override
    protected void runTestForReal() throws Throwable
    {
        try {
            UpdateRequest request = UpdateFactory.read(updateFile, Syntax.syntaxSPARQL_11) ;
            UpdateAction.execute(request, input) ;
            boolean b = datasetSame(input, output, false) ;
            if ( ! b )
            {
                System.out.println("---- "+getName()) ;
                System.out.println("---- Got: ") ;
                System.out.println(input.asDatasetGraph()) ;
                System.out.println("---- Expected") ;
                System.out.println(output.asDatasetGraph()) ;
                datasetSame(input, output, true) ;
                System.out.println("----------------------------------------") ;
            }

            assertTrue("Datasets are different", b) ;
        } catch (RuntimeException ex)
        {
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
    
    
    
    private boolean datasetSame(Dataset ds1, Dataset ds2, boolean verbose)
    {
        List<String> names1 = Iter.toList(ds1.listNames()) ;
        List<String> names2 = Iter.toList(ds2.listNames()) ;
        
        if ( ! names1.equals(names2) )
        {
            if ( verbose )
            {
                System.out.println("Different named graphs") ;
                System.out.println("  "+names1) ;
                System.out.println("  "+names2) ;
            }
            return false ;
        }
        if ( !ds1.getDefaultModel().isIsomorphicWith(ds2.getDefaultModel()) )
        {
            if ( verbose )
                System.out.println("Default graphs differ") ;
            return false ;
        }
        
        for ( String gn : names1 )
        {
            Model m1 = ds1.getNamedModel(gn) ;
            Model m2 = ds2.getNamedModel(gn) ;
            if ( ! m1.isIsomorphicWith(m2) )
            {
                if ( verbose )
                    System.out.println("Different on named graph "+gn) ;
                return false ;
            }
        }
        return true ;
    }

    static Dataset getDataset(Resource r)
    {
        //DataSource ds = DatasetFactory.create() ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        // Growing. dataset.
        DataSource ds = DataSourceImpl.wrap(dsg) ;
        
        
        List<String> dftData = getAll(r,  TestManifestUpdate_11.data) ;
        for ( String x : dftData )
            FileManager.get().readModel(ds.getDefaultModel(), x) ;
        
        ClosableIterator<Statement> cIter =  r.listProperties(TestManifestUpdate_11.graphData) ;
        for ( ; cIter.hasNext() ; )
        {
            // An graphData entry can be a URI or a [ ut ... ; rdfs:label "foo" ] ;
            Statement stmt = cIter.next() ;
            Resource gn = stmt.getResource() ;
            if ( gn.isAnon() )
            {
                if ( ! gn.hasProperty(TestManifestUpdate_11.graph) )
                    System.err.println("No data for graphData") ;
                
                String fn = gn.getProperty(TestManifestUpdate_11.graph).getResource().getURI() ;
                String name = gn.getProperty(RDFS.label).getString() ;
                Model m = FileManager.get().loadModel(fn) ;
                ds.addNamedModel(name, m) ;
            }
            else
            {
                String x = gn.getURI() ;
                Model m = FileManager.get().loadModel(x) ;
                ds.addNamedModel(x, m) ;
            }
        }
        cIter.close() ;
        return ds ;
    }

    static List<String> getAll(Resource r, Property p)
    {
        List<String> l = new ArrayList<String>() ;
        ClosableIterator<Statement> cIter =  r.listProperties(p) ;
        for ( ; cIter.hasNext() ; )
        {
            Statement stmt = cIter.next() ;
            String df = stmt.getObject().asResource().getURI() ;
            l.add(df) ;
        }
        cIter.close() ;
        return l ;
    }
    
    
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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