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

package dev.inf;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemWriter ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class sdbRDFSexpand
{
    public static void main(String... args)
    {
        // Read model.
        Model m = FileManager.get().loadModel("D.ttl") ;

        // Need to worry about pure rdf:type stuff (unlinked classes 
        Item itemC = make(TAG_CLASS, RDFS.subClassOf.asNode(), m.getGraph()) ;
        
        IndentedWriter out = new IndentedWriter(System.out) ;
        ItemWriter.write(out, itemC, null) ;
        out.flush() ;
        //Item itemP = make(TAG_PROP, RDFS.subPropertyOf.asNode(), m.getGraph()) ;
    }
    
    static public final String TAG_PROP = "trans-property" ; 
    static public final String TAG_CLASS = "trans-class" ;

    static Item make(String tag, Node property, Graph graph)
    {
        TransGraphNode tg = new TransGraphNode() ;
        ExtendedIterator<Triple> iter = graph.find(null, property, null) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            tg.add(t.getSubject(), t.getObject()) ;
        }
        tg.expandReflexive() ;
        //tg.expand() ;
        return tg.asItem(tag) ;
    }
    
}
