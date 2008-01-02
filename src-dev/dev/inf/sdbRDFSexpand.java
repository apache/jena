/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemWriter;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

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
        ExtendedIterator iter = graph.find(null, property, null) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            tg.add(t.getSubject(), t.getObject()) ;
        }
        tg.expandReflexive() ;
        //tg.expand() ;
        return tg.asItem(tag) ;
    }
    
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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