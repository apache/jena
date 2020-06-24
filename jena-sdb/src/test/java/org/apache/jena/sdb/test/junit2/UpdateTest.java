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

package org.apache.jena.sdb.test.junit2;

import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestCase;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.util.iterator.ClosableIterator ;
import org.apache.jena.util.junit.TestUtils ;
import org.apache.jena.vocabulary.RDFS ;


public class UpdateTest extends TestCase
{
    private Resource action ;
    private Resource result ;
    
    private String updateFile ;
    private Dataset input ;
    private Dataset output ;
    private final String testURI;
    

    public static UpdateTest create(String testName, Resource entry, Resource action, Resource result)
    {
        if ( ! action.hasProperty(TestManifestUpdate_11.request) )
        {
            System.err.println("No request in action: "+testName) ;
            return null ;
        }
        return new UpdateTest(testName, entry, action, result) ;
        
    }
    private UpdateTest(String testName, Resource entry, Resource action, Resource result)
    {
        super(TestUtils.safeName(testName)) ;
        this.action = action ;
        this.result = result ;
        this.testURI = entry.getURI();
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
    public void setUp()
    {
        input = getDataset(action) ;
        output = getDataset(result) ;
    }
    
    @Override
    public void tearDown()
    {
//        if ( resetNeeded )
//            ARQ.setFalse(ARQ.strictGraph) ;
        input = null ;
        output = null ;
    }
    
    @Override
    protected void runTest()
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
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        // Growing. dataset.
        Dataset ds = DatasetFactory.wrap(dsg) ;
        
        List<String> dftData = getAll(r,  TestManifestUpdate_11.data) ;
        for ( String x : dftData )
            RDFDataMgr.read(ds.getDefaultModel(), x) ;
        
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
                Model m = RDFDataMgr.loadModel(fn);
                String name = gn.getProperty(RDFS.label).getString() ;
                ds.addNamedModel(name, m) ;
            }
            else
            {
                String x = gn.getURI() ;
                Model m = RDFDataMgr.loadModel(x) ;
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
