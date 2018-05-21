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

package arq.examples.riot;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.riot.RDFParser ;
import org.apache.jena.riot.out.SinkTripleOutput ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFBase ;
import org.apache.jena.riot.system.SyntaxLabels ;
import org.apache.jena.sparql.vocabulary.FOAF ;

/** Example of using RIOT : extract only certain properties from a parser run */ 
public class ExRIOT_4
{
    public static void main(String...argv)
    {
        String filename = "data.ttl" ;

        // This is the heart of N-triples printing ... outoput is heavily buffered
        // so the FilterSinkRDF called flush at the end of parsing.
        Sink<Triple> output = new SinkTripleOutput(System.out, null, SyntaxLabels.createNodeToLabel()) ;
        StreamRDF filtered = new FilterSinkRDF(output, FOAF.name, FOAF.knows) ;
        
        // Call the parsing process.
        RDFParser.source(filename).parse(filtered);
    }
    
    static class FilterSinkRDF extends StreamRDFBase
    {
        private final Node[] properties ;
        // Where to send the filtered triples.
        private final Sink<Triple> dest ;

        FilterSinkRDF(Sink<Triple> dest, Property... properties)
        {
            this.dest = dest ;
            this.properties = new Node[properties.length] ;
            for ( int i = 0 ; i < properties.length ; i++ ) 
                this.properties[i] = properties[i].asNode() ;
        }

        @Override
        public void triple(Triple triple)
        {
            for ( Node p : properties )
            {
                if ( triple.getPredicate().equals(p) )
                    dest.send(triple) ;
            }
        }
        
        @Override
        public void finish()
        {
            // Output may be buffered.
            dest.flush() ;
        }
    }
}
 