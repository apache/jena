/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.junit;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.riot.checker.CheckerLiterals ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
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
    public void setUpTest() throws Exception
    {
        super.setUpTest() ;
        // Turn parser warnings off for the test data. 
        oldWarningFlag = CheckerLiterals.WarnOnBadLiterals ;
        //CheckerLiterals.WarnOnBadLiterals = false ;
        input = getDataset(action) ;
        output = getDataset(result) ;
    }
    
    @Override
    public void tearDownTest() throws Exception
    {
//        if ( resetNeeded )
//            ARQ.setFalse(ARQ.strictGraph) ;
        CheckerLiterals.WarnOnBadLiterals = oldWarningFlag ;
        input = null ;
        output = null ;
        super.tearDownTest() ;
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
        Dataset ds = DatasetFactory.create(dsg) ;
        
        
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
        List<String> l = new ArrayList<>() ;
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
