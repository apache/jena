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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.arq.junit.manifest.AbstractManifestTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.TestSetupException;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.TestManifestUpdate_11 ;
import org.apache.jena.system.G;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.util.iterator.ClosableIterator ;
import org.apache.jena.vocabulary.RDFS ;

public class UpdateEvalTest extends AbstractManifestTest
{
    private final Creator<Dataset> creator;

    public UpdateEvalTest(ManifestEntry entry) {
        this(entry, ()->DatasetFactory.create());
    }

    public UpdateEvalTest(ManifestEntry entry, Creator<Dataset> maker) {
        super(entry);
        this.creator = maker;
    }

    @Override
    public void runTest() {
        Dataset input = getDataset(creator.create(), manifestEntry.getGraph(), manifestEntry.getAction()) ;
        Dataset output = getDataset(DatasetFactory.create(), manifestEntry.getGraph(), manifestEntry.getResult()) ;

        String updateFile = G.getOneSP(manifestEntry.getGraph(), manifestEntry.getAction(), TestManifestUpdate_11.request.asNode()).getURI();
        UpdateRequest request = UpdateFactory.read(updateFile, Syntax.syntaxARQ) ;
        UpdateAction.execute(request, input) ;
        boolean b = datasetSame(input, output, false) ;
        if ( ! b )
        {
            System.out.println("---- "+manifestEntry.getName()) ;
            System.out.println("---- Got: ") ;
            System.out.println(input.asDatasetGraph()) ;
            System.out.println("---- Expected") ;
            System.out.println(output.asDatasetGraph()) ;
            datasetSame(input, output, true) ;
            System.out.println("----------------------------------------") ;
        }

        assertTrue(b, "Datasets are different") ;
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

    static Dataset getDataset(Dataset ds, Graph graph, Node r)
    {
        List <Node> dftData = G.listSP(graph, r, TestManifestUpdate_11.data.asNode());
        for ( Node x : dftData ) {
            if ( ! x.isURI() )
                throw new TestSetupException("Not a URI for a default graph data file: "+x);
            SparqlTestLib.parser(x.getURI()).parse(ds);
        }

        List <Node> namedGraphs = G.listSP(graph, r, TestManifestUpdate_11.graphData.asNode());
        for ( Node x : namedGraphs ) {
            // An graphData entry can be a URI or a [ ut:graph , rdfs:label "" ]
            if ( x.isBlank() ) {
                if ( ! G.hasProperty(graph, x, TestManifestUpdate_11.graph.asNode()) )
                    throw new TestSetupException("No data for graphData") ;

                String fn = G.getOneSP(graph, x, TestManifestUpdate_11.graph.asNode()).getURI();
                String name = G.asString(G.getOneSP(graph, x, RDFS.Nodes.label));
                Model m = ModelFactory.createDefaultModel();
                SparqlTestLib.parser(fn).parse(m);
                ds.addNamedModel(name, m) ;
            }
            else
            {
                String uri = x.getURI() ;
                Model m = ModelFactory.createDefaultModel();
                SparqlTestLib.parser(uri).parse(m);
                ds.addNamedModel(uri, m) ;
            }
        }
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
