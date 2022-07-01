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

package org.apache.jena.arq.junit.sparql.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.util.iterator.ClosableIterator ;
import org.apache.jena.vocabulary.RDFS ;

public class UpdateExecTest implements Runnable
{
    private final ManifestEntry testEntry;
    private final Creator<Dataset> creator;

    public UpdateExecTest(ManifestEntry entry) {
        this(entry, ()->DatasetFactory.create());
    }

    public UpdateExecTest(ManifestEntry entry, Creator<Dataset> maker) {
        this.testEntry = entry;
        this.creator = maker;
    }

    @Override
    public void run() {
        Dataset input = getDataset(creator.create(), testEntry.getAction()) ;
        Dataset output = getDataset(DatasetFactory.create(), testEntry.getResult()) ;

        String updateFile = testEntry
                .getAction()
                .getProperty(TestManifestUpdate_11.request)
                .getResource()
                .getURI() ;

        UpdateRequest request = UpdateFactory.read(updateFile, Syntax.syntaxARQ) ;
        UpdateAction.execute(request, input) ;
        boolean b = datasetSame(input, output, false) ;
        if ( ! b )
        {
            System.out.println("---- "+testEntry.getName()) ;
            System.out.println("---- Got: ") ;
            System.out.println(input.asDatasetGraph()) ;
            System.out.println("---- Expected") ;
            System.out.println(output.asDatasetGraph()) ;
            datasetSame(input, output, true) ;
            System.out.println("----------------------------------------") ;
        }

        assertTrue("Datasets are different", b) ;
    }

    private static boolean datasetSame(Dataset ds1, Dataset ds2, boolean verbose)
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

    static Dataset getDataset(Dataset ds, Resource r)
    {
        List<String> dftData = getAll(r, TestManifestUpdate_11.data) ;
        for ( String x : dftData )
            SparqlTestLib.parser(x).parse(ds);

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
                Model m = ModelFactory.createDefaultModel();
                SparqlTestLib.parser(fn).parse(m);
                ds.addNamedModel(name, m) ;
            }
            else
            {
                String x = gn.getURI() ;
                Model m = ModelFactory.createDefaultModel();
                SparqlTestLib.parser(x).parse(m);
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
