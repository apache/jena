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

package org.apache.jena.sparql.graph;

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Locale ;
import java.util.Set ;
import java.util.function.Predicate;

import org.apache.jena.graph.Capabilities ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.graph.impl.SimpleEventManager ;
import org.apache.jena.util.iterator.ClosableIterator ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * A version of Graph that does term equality only 
 */
public class GraphMemPlain extends GraphBase
{
    private Set<Triple> triples = new HashSet<>() ;
    
    public GraphMemPlain() {}
    
    @Override
	public Capabilities getCapabilities() {
		return gmpCapabilities;
	}
    
    @Override
    public void performAdd( Triple t )
    { triples.add(t) ; }

    @Override
    public void performDelete( Triple t ) 
    { triples.remove(t) ; }
    
    @Override
    public boolean graphBaseContains( Triple t ) 
    {
        if ( t.isConcrete() )
            return triples.contains( t ) ;
        
        ClosableIterator<Triple> it = find( t );
        try {
            for ( ; it.hasNext() ; )
            {
                Triple t2 = it.next() ;
                if ( tripleContained(t, t2) )
                    return true ;
            }
        } finally { it.close(); }
        return false ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m)
    {
        Iterator<Triple> iter = triples.iterator() ;
        return 
            SimpleEventManager.notifyingRemove( this, iter ) 
            .filterKeep ( new TripleMatchFilterEquality( m ) );
    }
    
    static boolean tripleContained(Triple patternTriple, Triple dataTriple)
    {
        return
            equalNode(patternTriple.getSubject(),   dataTriple.getSubject()) &&
            equalNode(patternTriple.getPredicate(), dataTriple.getPredicate()) &&
            equalNode(patternTriple.getObject(),    dataTriple.getObject()) ;
    }
    
    private static boolean equalNode(Node m, Node n)
    {
        // m should not be null unless .getMatchXXXX used to get the node.
        // Language tag canonicalization
        n = fixupNode(n) ;
        m = fixupNode(m) ;
        return (m==null) || (m == Node.ANY) || m.equals(n) ;
    }
    
    private static Node fixupNode(Node node)
    {
        if ( node == null || node == Node.ANY )
            return node ;

        // RDF says ... language tags should be canonicalized to lower case.
        if ( node.isLiteral() )
        {
            String lang = node.getLiteralLanguage() ;
            if ( lang != null && ! lang.equals("") )
                node = NodeFactory.createLiteral(node.getLiteralLexicalForm(), lang.toLowerCase(Locale.ROOT)) ;
        }
        return node ; 
    }
    
    static class TripleMatchFilterEquality implements Predicate<Triple>
    {
        final protected Triple tMatch;
    
        /** Creates new TripleMatchFilter */
        public TripleMatchFilterEquality(Triple tMatch) 
            { this.tMatch = tMatch; }
        
        @Override
        public boolean test(Triple t)
        {
            return tripleContained(tMatch, t) ;
        }
        
    }
    
    private static Capabilities gmpCapabilities = new Capabilities() {

		@Override
		public boolean sizeAccurate() {
			return true;
		}

		@Override
		public boolean addAllowed() {
			return true;
		}

		@Override
		public boolean addAllowed(boolean everyTriple) {
			return true;
		}

		@Override
		public boolean deleteAllowed() {
			return true;
		}

		@Override
		public boolean deleteAllowed(boolean everyTriple) {
			return true;
		}

		@Override
		public boolean iteratorRemoveAllowed() {
			return true;
		}

		@Override
		public boolean canBeEmpty() {
			return true;
		}

		@Override
		public boolean findContractSafe() {
			return true;
		}

		@Override
		public boolean handlesLiteralTyping() {
			return false;
		}
		
	};
}
