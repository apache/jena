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

package tdb.examples;

import org.apache.jena.riot.RDFDataMgr ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab ;
import org.apache.jena.sparql.util.TypeNotUniqueException ;
import org.apache.jena.sparql.util.graph.GraphUtils ;
import org.apache.jena.tdb.assembler.VocabTDB ;

/** 
 * Examples of finding an assembler for a TDB model in a larger collection
 * of descriptions in a single file.
 */
public class ExTDB3
{
    public static void main(String... argv)
    {
        String assemblerFile = "Store/tdb-assembler.ttl" ;
        
        // Find a particular description in the file where there are several: 
        Model spec = RDFDataMgr.loadModel(assemblerFile) ;

        // Find the right starting point for the description in some way.
        Resource root = null ;

        if ( false )
            // If you know the Resource URI:
            root = spec.createResource("http://example/myChoiceOfURI" );
        else
        {
            // Alternatively, look for the a single resource of the right type. 
            try {
                // Find the required description - the file can contain descriptions of many different types.
                root = GraphUtils.findRootByType(spec, VocabTDB.tDatasetTDB) ;
                if ( root == null )
                    throw new JenaException("Failed to find a suitable root") ;
            } catch (TypeNotUniqueException ex)
            { throw new JenaException("Multiple types for: "+DatasetAssemblerVocab.tDataset) ; }
        }

        Dataset ds = (Dataset)Assembler.general.open(root) ;
    }
}
