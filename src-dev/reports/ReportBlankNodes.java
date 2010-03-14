/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package reports;

import java.util.Iterator ;

import junit.framework.Assert ;
import org.junit.Test ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;


public class ReportBlankNodes {

    @Test
    public void testCreate() {
        Graph graph = TDBFactory.createGraph();
        Node blank1 = Node.createAnon();
        Node blank2 = Node.createAnon();
        graph.add(Triple.create(blank1, RDF.type.asNode(), OWL.Restriction.asNode()));
        graph.add(Triple.create(blank2, RDF.type.asNode(), OWL.Restriction.asNode()));
        ExtendedIterator<Triple> it = graph.find(Triple.createMatch(null, null, null));
        Triple t1 = it.next();
        Triple t2 = it.next();
        Assert.assertFalse(t1.getSubject().equals(t2.getSubject()));
        Assert.assertFalse(it.hasNext());
        TDB.sync(graph);
        graph.close();
    }

    
    @Test
    public void testLoadAfterRestart() {
        Graph graph = TDBFactory.createGraph();
        
        GraphTDB g = (GraphTDB)graph ;
        
        Iterator<Tuple<NodeId>> iter = g.getNodeTupleTable().find(NodeId.NodeIdAny, NodeId.NodeIdAny,NodeId.NodeIdAny) ;
        for ( ; iter.hasNext() ; )
            System.out.println(iter.next()) ;
        
        
        ExtendedIterator<Triple> it = graph.find(Triple.createMatch(null, null, null));
        Triple t1 = it.next();
        Triple t2 = it.next();
        Assert.assertFalse(t1.getSubject().equals(t2.getSubject()));
        Assert.assertFalse(it.hasNext());
        TDB.sync(graph);
        graph.close();
    }
}


/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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