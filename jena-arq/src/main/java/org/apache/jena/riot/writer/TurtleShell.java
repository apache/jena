/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for ad
 * ditional information
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

package org.apache.jena.riot.writer ;

import static org.apache.jena.riot.writer.WriterConst.GAP_P_O ;
import static org.apache.jena.riot.writer.WriterConst.GAP_S_P ;
import static org.apache.jena.riot.writer.WriterConst.INDENT_OBJECT ;
import static org.apache.jena.riot.writer.WriterConst.INDENT_PREDICATE ;
import static org.apache.jena.riot.writer.WriterConst.LONG_PREDICATE ;
import static org.apache.jena.riot.writer.WriterConst.LONG_SUBJECT ;
import static org.apache.jena.riot.writer.WriterConst.MIN_PREDICATE ;
import static org.apache.jena.riot.writer.WriterConst.OBJECT_LISTS ;
import static org.apache.jena.riot.writer.WriterConst.RDF_First ;
import static org.apache.jena.riot.writer.WriterConst.RDF_Nil ;
import static org.apache.jena.riot.writer.WriterConst.RDF_Rest ;
import static org.apache.jena.riot.writer.WriterConst.RDF_type ;
import static org.apache.jena.riot.writer.WriterConst.rdfNS ;

import java.util.* ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.riot.other.GLib ;
import org.apache.jena.riot.out.NodeFormatter ;
import org.apache.jena.riot.out.NodeFormatterTTL ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.RiotLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Base class to support the pretty forms of Turtle-related languages (Turtle,
 * TriG)
 */
public abstract class TurtleShell {
    protected final IndentedWriter out ;
    protected final NodeFormatter  nodeFmt ;
    protected final PrefixMap      prefixMap ;
    protected final String         baseURI ;

    protected TurtleShell(IndentedWriter out, PrefixMap pmap, String baseURI) {
        this.out = out ;
        if ( pmap == null )
            pmap = PrefixMapFactory.emptyPrefixMap() ;
        this.nodeFmt = new NodeFormatterTTL(baseURI, pmap, NodeToLabel.createScopeByDocument()) ;
        this.prefixMap = pmap ;
        this.baseURI = baseURI ;
    }

    protected void writeBase(String base) {
        RiotLib.writeBase(out, base) ;
    }

    protected void writePrefixes(PrefixMap prefixMap) {
        RiotLib.writePrefixes(out, prefixMap) ;
    }

    /** Write graph in Trutle syntax (or part of TriG) */
    protected void writeGraphTTL(Graph graph) {
        ShellGraph x = new ShellGraph(graph, null, null) ;
        x.writeGraph() ;
    }    

    /** Write graph in Trutle syntax (or part of TriG). graphName is null for default graph. */
    protected void writeGraphTTL(DatasetGraph dsg, Node graphName) {
        Graph g = (graphName == null || Quad.isDefaultGraph(graphName)) 
            ? dsg.getDefaultGraph()
            : dsg.getGraph(graphName) ; 
        ShellGraph x = new ShellGraph(g, graphName, dsg) ;
        x.writeGraph() ;
    }

    // Write one graph - using an inner object class to isolate
    // the state variables for writing a single graph.
    private final class ShellGraph {
        // Dataset (for writing graphs indatasets) -- may be null
        private final DatasetGraph          dsg ;  
        private final Collection<Node>      graphNames ; 
        private final Node                  graphName ;
        private final Graph                 graph ;
        
        // Blank nodes that have one incoming triple
        private final Set<Node>             nestedObjects ; 

        // Blank node subjects that are not referenced as objects or graph names
        // excluding unlnked lists. 
        private final Set<Node>             freeBnodes ;  

        // The head node in each well-formed list -> list elements
        private final Map<Node, List<Node>> lists ;   

        // List that do not have any incoming triples
        private final Map<Node, List<Node>> freeLists ; 

        // Lists that have more than one incoming triple
        private final Map<Node, List<Node>> nLinkedLists ; 

        // All nodes that are part of list structures.
        private final Collection<Node>      listElts ;  

        private ShellGraph(Graph graph, Node graphName, DatasetGraph dsg) {
            this.dsg = dsg ;
            this.graphName = graphName ;
            
            this.graphNames = (dsg != null) ? Iter.toSet(dsg.listGraphNodes()) : null ; 
            
            this.graph = graph ;
            this.nestedObjects = new HashSet<>() ;
            this.freeBnodes = new HashSet<>() ;

            this.lists = new HashMap<>() ;
            this.freeLists = new HashMap<>() ;
            this.nLinkedLists = new HashMap<>() ;
            this.listElts = new HashSet<>() ;

            
            // Must be in this order.
            findLists() ;
            findBNodesSyntax1() ;
            // Stop head of lists printed as triples going all the way to the
            // good part.
            nestedObjects.removeAll(listElts) ;
        }

        private ShellGraph(Graph graph) {
            this(graph, null, null) ;
        }

        // ---- Data access
        /** Get all the triples for the graph.find */
        private List<Triple> triples(Node s, Node p, Node o) {
            List<Triple> acc = new ArrayList<>() ;
            RiotLib.accTriples(acc, graph, s, p, o) ;
            return acc ;
        }

        /** Get exactly one triple or null for none or more than one. */
        private Triple triple1(Node s, Node p, Node o) {
            if ( dsg != null )
                return RiotLib.triple1(dsg, s, p, o) ;
            else
                return RiotLib.triple1(graph, s, p, o) ;
        }

        /** Get exactly one triple, or null for none or more than one. */
        private Triple triple1(DatasetGraph dsg, Node s, Node p, Node o) {
            Iterator<Quad> iter = dsg.find(Node.ANY, s, p, o) ;
            if ( !iter.hasNext() )
                return null ;
            Quad q = iter.next() ;
            if ( iter.hasNext() )
                return null ;
            return q.asTriple() ;
        }

        private long countTriples(Node s, Node p, Node o) {
            if ( dsg != null )
                return RiotLib.countTriples(dsg, s, p, o) ;
            else
                return RiotLib.countTriples(graph, s, p, o) ;
        }

        private ExtendedIterator<Triple> find(Node s, Node p, Node o) {
            return graph.find(s, p, o) ;
        }

        /** returns 0,1,2 (where 2 really means "more than 1") */
        private int inLinks(Node obj) {
            if ( dsg != null ) {
                Iterator<Quad> iter = dsg.find(Node.ANY, Node.ANY, Node.ANY, obj) ;
                return count012(iter) ;
            } else {
                ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, obj) ;
                try { return count012(iter) ; }
                finally { iter.close() ; }
            }
        }

        /** returns 0,1,2 (where 2 really means "more than 1") */
        private int occursAsSubject(Node subj) {
            if ( dsg != null ) {
                Iterator<Quad> iter = dsg.find(Node.ANY, subj, Node.ANY, Node.ANY) ;
                return count012(iter) ;
            } else {
                ExtendedIterator<Triple> iter = graph.find(subj, Node.ANY, Node.ANY) ;
                try { return count012(iter) ; }
                finally { iter.close() ; }
            }
        }
        
        private int count012(Iterator<? > iter) {
            if ( !iter.hasNext() )
                return 0 ;
            iter.next() ;
            if ( !iter.hasNext() )
                return 1 ;
            return 2 ;
        }

        /** Check whether a node is used only in the graph we're working on */ 
        private boolean containedInOneGraph(Node node) {
            if ( dsg == null )
                // Single graph
                return true ;
            
            if ( graphNames.contains(node) )
                // Used as a graph name.
                return false ;
            
            Iterator<Quad> iter = dsg.find(Node.ANY, node, Node.ANY, Node.ANY) ;
            if ( ! quadsThisGraph(iter) ) 
                return false ;

            iter = dsg.find(Node.ANY, Node.ANY, node, Node.ANY) ;
            if ( ! quadsThisGraph(iter) ) 
                return false ;
            
            iter = dsg.find(Node.ANY, Node.ANY, Node.ANY, node) ;
            if ( ! quadsThisGraph(iter) ) 
                return false ;
            return true ;
        }

        /** Check whether an iterator of quads is all in the same graph (dataset assumed) */ 
        private boolean quadsThisGraph(Iterator<Quad> iter) {
            if ( ! iter.hasNext() )
                // Empty iterator
                return true ;
            Quad q = iter.next() ;
            Node gn = q.getGraph() ;

            // Test first quad - both default graph (various forms) or same named graph
            if ( isDefaultGraph(gn) ) {
                if ( ! isDefaultGraph(graphName) )
                    return false ;
            } else { 
                if ( ! Lib.equal(gn, graphName) )
                    // Not both same named graph
                    return false ;
            }
            // Check rest of iterator.
            for ( ; iter.hasNext() ; ) {
                Quad q2 = iter.next() ;
                if ( ! Lib.equal(gn, q2.getGraph()) )
                    return false ;    
            }
            return true ;
        }
        
        private boolean isDefaultGraph(Node node) {
            return node == null || Quad.isDefaultGraph(node) ;
        }
        
        /** Get triples with the same subject */
        private Collection<Triple> triplesOfSubject(Node subj) {
            return RiotLib.triplesOfSubject(graph, subj) ;
        }

        private Iterator<Node> listSubjects() {
            return GLib.listSubjects(graph) ;
        }

        // ---- Data access

        /** Find Bnodes that can written as []
         * Subject position (top level) - only used for subject position anywhere in the dataset
         * Object position (any level) - only used as object once anywhere in the dataset
         */
        private void findBNodesSyntax1() {
            Set<Node> rejects = new HashSet<>() ; // Nodes known not to meet the requirement.

            ExtendedIterator<Triple> iter = find(Node.ANY, Node.ANY, Node.ANY) ;
            try {
                for ( ; iter.hasNext() ; )
                {
                    Triple t = iter.next() ;
                    Node subj = t.getSubject() ;
                    Node obj = t.getObject() ;
                    if ( listElts.contains(subj) )  // In a list?
                        continue ;
                    if ( listElts.contains(obj) )  // In a list?
                        continue ;
                    
                    if ( subj.isBlank() )
                    {
                        // Blank node, not a list ...
                        int sConn = inLinks(subj) ;
                        if ( sConn == 0 && containedInOneGraph(subj) )  
                            // Not used as an object in this graph.
                            freeBnodes.add(subj) ;
                    }
                    
                    if ( ! obj.isBlank() )
                        continue ;
                    if ( rejects.contains(obj) )
                        continue ;

                    int connectivity = inLinks(obj) ;
                    if ( connectivity == 1 && containedInOneGraph(obj) ) {
                        // If not used in another graph (or as graph name)
                        nestedObjects.add(obj) ;
                    }
                    else
                        // Uninteresting object connected multiple times. 
                        rejects.add(obj) ;
                }
            } finally { iter.close() ; }
        }
        
        // --- Lists setup
        /*
         * Find all list heads and all nodes in well-formed lists. Return a
         * (list head -> Elements map), list elements)
         */
        private void findLists() {
            List<Triple> tails = triples(Node.ANY, RDF_Rest, RDF_Nil) ;
            for ( Triple t : tails ) {
                // Returns the elements, reversed.
                Collection<Node> listElts2 = new HashSet<>() ;
                Pair<Node, List<Node>> p = followTailToHead(t.getSubject(), listElts2) ;
                if ( p != null ) {
                    Node headElt = p.getLeft() ;
                    // Free standing/private
                    List<Node> elts = p.getRight() ;
                    long numLinks = countTriples(null, null, headElt) ;
                    if ( numLinks == 1 )
                        lists.put(headElt, elts) ;
                    else if ( numLinks == 0 )
                        // 0 connected lists
                        freeLists.put(headElt, elts) ;
                    else
                        // Two triples to this list.
                        nLinkedLists.put(headElt, elts) ;
                    listElts.addAll(listElts2) ;
                }
            }
        }

        // return head elt node, list of elements.
        private Pair<Node, List<Node>> followTailToHead(Node lastListElt, Collection<Node> listElts) {
            List<Node> listCells = new ArrayList<>() ;
            List<Node> eltsReversed = new ArrayList<>() ;
            List<Triple> acc = new ArrayList<>() ;
            Node x = lastListElt ;

            for ( ; ; ) {
                if ( !validListElement(x, acc) ) {
                    if ( listCells.size() == 0 )
                        // No earlier valid list.
                        return null ;
                    // Fix up to previous valid list cell.
                    x = listCells.remove(listCells.size() - 1) ;
                    break ;
                }

                Triple t = triple1(x, RDF_First, null) ;
                if ( t == null )
                    return null ;
                eltsReversed.add(t.getObject()) ;
                listCells.add(x) ;

                // Try to move up the list.
                List<Triple> acc2 = triples(null, null, x) ;
                long numRest = countTriples(null, RDF_Rest, x) ;
                if ( numRest != 1 ) {
                    // Head of well-formed list.
                    // Classified by 0,1,more links later.
                    listCells.add(x) ;
                    break ;
                }
                // numRest == 1
                int numLinks = acc2.size() ;
                if ( numLinks > 1 )
                    // Non-list links to x
                    break ;
                // Valid.
                Triple tLink = acc2.get(0) ;
                x = tLink.getSubject() ;
            }
            // Success.
            listElts.addAll(listCells) ;
            Collections.reverse(eltsReversed) ;
            return Pair.create(x, eltsReversed) ;
        }

        /** Return the triples of the list element, or null if invalid list */
        private boolean validListElement(Node x, List<Triple> acc) {
            Triple t1 = triple1(x, RDF_Rest, null) ; // Which we came up to get
                                                     // here :-(
            if ( t1 == null )
                return false ;
            Triple t2 = triple1(x, RDF_First, null) ;
            if ( t2 == null )
                return false ;
            long N = countTriples(x, null, null) ;
            if ( N != 2 )
                return false ;
            acc.add(t1) ;
            acc.add(t2) ;
            return true ;
        }

        // ----

        private void writeGraph() {
            Iterator<Node> subjects = listSubjects() ;
            boolean somethingWritten = writeBySubject(subjects) ;

            // Write remainders
            // 1 - Shared lists
            // 2 - Free standing lists

            if ( !nLinkedLists.isEmpty() )
                somethingWritten = writeNLinkedLists(somethingWritten) ;

            if ( !freeLists.isEmpty() )
                somethingWritten = writeFreeLists(somethingWritten) ;

        }

        // Write lists that are shared objects
        private boolean writeNLinkedLists(boolean somethingWritten) {
            // Print carefully - need a label for the first cell.
            // So we write out the first element of the list in triples, then
            // put
            // the remainer as a pretty list
            for ( Node n : nLinkedLists.keySet() ) {
                if ( somethingWritten )
                    out.println() ;
                somethingWritten = true ;

                List<Node> x = nLinkedLists.get(n) ;
                writeNode(n) ;

                if ( out.getCol() > LONG_SUBJECT )
                    println() ;
                else
                    gap(GAP_S_P) ;
                out.incIndent(INDENT_PREDICATE) ;
                // ----
                // DRY writeCluster.
                out.pad() ;
                writeNode(RDF_First) ;
                print(" ") ;
                writeNode(x.get(0)) ;
                print(" ;") ;
                println() ;
                writeNode(RDF_Rest) ;
                print("  ") ;
                x = x.subList(1, x.size()) ;
                list(x) ;
                print(" .") ;
                out.decIndent(INDENT_PREDICATE) ;
                println() ;
            }
            return somethingWritten ;
        }

        // Write free standing lists - ones where the head is not an object of
        // some other triple.
        // Turtle does not (... ) . so write as a predicateObjectList for one
        // element.
        private boolean writeFreeLists(boolean somethingWritten) {
            // out.println("# Free standing lists") ;
            // Write free lists.
            for ( Node n : freeLists.keySet() ) {
                if ( somethingWritten )
                    out.println() ;
                somethingWritten = true ;

                List<Node> x = freeLists.get(n) ;
                // Print first element for the [ ... ]
                out.print("[ ") ;

                writeNode(RDF_First) ;
                print(" ") ;
                writeNode(x.get(0)) ;
                print(" ; ") ;
                writeNode(RDF_Rest) ;
                print(" ") ;
                x = x.subList(1, x.size()) ;
                // Print remainder.
                list(x) ;
                out.println(" ] .") ;
            }
            return somethingWritten ;
        }

        // return true if did write something.
        private boolean writeBySubject(Iterator<Node> subjects) {
            boolean first = true ;
            for ( ; subjects.hasNext() ; ) {
                Node subj = subjects.next() ;
                if ( nestedObjects.contains(subj) )
                    continue ;

                if ( listElts.contains(subj) )
                    continue ;
                if ( !first )
                    out.println() ;
                first = false ;
                if ( freeBnodes.contains(subj) ) {
                    // Write in "[....]" form.
                    nestedObject(subj) ;
                    out.println(" .") ;
                    continue ;
                }

                Collection<Triple> cluster = triplesOfSubject(subj) ;
                writeCluster(subj, cluster) ;
            }
            return !first ;
        }

        // Common subject
        // Used by the blocks writer as well.
        private void writeCluster(Node subject, Collection<Triple> cluster) {
            if ( cluster.isEmpty() )
                return ;
            writeNode(subject) ;

            if ( out.getCol() > LONG_SUBJECT )
                out.println() ;
            else
                gap(GAP_S_P) ;
            out.incIndent(INDENT_PREDICATE) ;
            out.pad() ;
            writePredicateObjectList(cluster) ;
            out.decIndent(INDENT_PREDICATE) ;
            // End of cluster.
            print(" .") ;
            println() ;
        }

        // Writing predciate-object lists.
        // We group the cluster by predicate and within each group
        // we print:
        // literals, then simple objects, then pretty objects

        private void writePredicateObjectList(Collection<Triple> cluster) {
            Map<Node, List<Node>> pGroups = groupByPredicates(cluster) ;
            Collection<Node> predicates = pGroups.keySet() ;

            // Find longest predicate URI
            int predicateMaxWidth = RiotLib.calcWidth(prefixMap, baseURI, predicates, MIN_PREDICATE, LONG_PREDICATE) ;

            boolean first = true ;

            if ( !OBJECT_LISTS ) {
                for ( Node p : predicates ) {
                    for ( Node o : pGroups.get(p) ) {
                        writePredicateObject(p, o, predicateMaxWidth, first) ;
                        first = false ;
                    }
                }
                return ;
            }

            for ( Node p : predicates ) {
                List<Node> rdfLiterals = new ArrayList<>() ; // Literals in the
                                                             // group
                List<Node> rdfSimpleNodes = new ArrayList<>() ; // Non-literals,
                                                                // printed
                List<Node> rdfComplexNodes = new ArrayList<>() ; // Non-literals,
                                                                 // printed ()
                                                                 // or
                                                                 // []-embedded

                for ( Node o : pGroups.get(p) ) {
                    if ( o.isLiteral() ) {
                        rdfLiterals.add(o) ;
                        continue ;
                    }
                    if ( isPrettyNode(o) ) {
                        rdfComplexNodes.add(o) ;
                        continue ;
                    }
                    rdfSimpleNodes.add(o) ;
                }

                if ( rdfLiterals.size() != 0 ) {
                    writePredicateObjectList(p, rdfLiterals, predicateMaxWidth, first) ;
                    first = false ;
                }
                if ( rdfSimpleNodes.size() != 0 ) {
                    writePredicateObjectList(p, rdfSimpleNodes, predicateMaxWidth, first) ;
                    first = false ;
                }

                for ( Node o : rdfComplexNodes ) {
                    writePredicateObject(p, o, predicateMaxWidth, first) ;
                    first = false ;
                }
            }
        }

        private void writePredicateObject(Node p, Node obj, int predicateMaxWidth, boolean first) {
            writePredicate(p, predicateMaxWidth, first) ;
            out.incIndent(INDENT_OBJECT) ;
            writeNodePretty(obj) ;
            out.decIndent(INDENT_OBJECT) ;
        }

        private void writePredicateObjectList(Node p, List<Node> objects, int predicateMaxWidth, boolean first) {
            writePredicate(p, predicateMaxWidth, first) ;
            out.incIndent(INDENT_OBJECT) ;
            boolean firstObject = true ;
            for ( Node o : objects ) {
                if ( !firstObject )
                    out.print(" , ") ;
                else
                    firstObject = false ;
                writeNode(o) ;
                // writeNodePretty(obj) ;
            }
            out.decIndent(INDENT_OBJECT) ;
        }

        /** Write a predicate - jump to next line if deemed long */
        private void writePredicate(Node p, int predicateMaxWidth, boolean first) {
            if ( first )
                first = false ;
            else {
                print(" ;") ;
                println() ;
            }
            int colPredicateStart = out.getAbsoluteIndent() ;

            if ( !prefixMap.contains(rdfNS) && RDF_type.equals(p) )
                print("a") ;
            else
                writeNode(p) ;
            int colPredicateFinish = out.getCol() ;
            int wPredicate = (colPredicateFinish - colPredicateStart) ;

            if ( wPredicate > LONG_PREDICATE )
                println() ;
            else {
                out.pad(predicateMaxWidth) ;
                // out.print(' ', predicateMaxWidth-wPredicate) ;
                gap(GAP_P_O) ;
            }
        }

        private Map<Node, List<Node>> groupByPredicates(Collection<Triple> cluster) {
            SortedMap<Node, List<Node>> x = new TreeMap<>(compPredicates) ;
            for ( Triple t : cluster ) {
                Node p = t.getPredicate() ;
                if ( !x.containsKey(p) )
                    x.put(p, new ArrayList<Node>()) ;
                x.get(p).add(t.getObject()) ;
            }

            return x ;
        }

        private int countPredicates(Collection<Triple> cluster) {
            Set<Node> x = new HashSet<>() ;
            for ( Triple t : cluster ) {
                Node p = t.getPredicate() ;
                x.add(p) ;
            }
            return x.size() ;
        }

        private void nestedObject(Node node) {
            Collection<Triple> x = triplesOfSubject(node) ;

            if ( x.isEmpty() ) {
                print("[] ") ;
                return ;
            }

            int pCount = countPredicates(x) ;

            if ( pCount == 1 ) {
                print("[ ") ;
                out.incIndent(2) ;
                writePredicateObjectList(x) ;
                out.decIndent(2) ;
                print(" ]") ;
                return ;
            }

            // Two or more.
            int indent0 = out.getAbsoluteIndent() ;
            int here = out.getCol() ;
            out.setAbsoluteIndent(here) ;
            print("[ ") ;
            out.incIndent(2) ;
            writePredicateObjectList(x) ;
            out.decIndent(2) ;
            if ( true ) {
                println() ; // Newline for "]"
                print("]") ;
            } else { // Compact
                print(" ]") ;
            }
            out.setAbsoluteIndent(indent0) ;
        }

        // Write a list
        private void list(List<Node> elts) {
            if ( elts.size() == 0 ) {
                out.print("()") ;
                return ;
            }

            out.print("(") ;
            for ( Node n : elts ) {
                out.print(" ") ;
                writeNodePretty(n) ;
            }

            out.print(" )") ;
        }

        private boolean isPrettyNode(Node n) {
            // Order matters? - one connected objects may include list elements.
            if ( lists.containsKey(n) )
                return true ;
            if ( nestedObjects.contains(n) )
                return true ;
            if ( RDF_Nil.equals(n) )
                return true ;
            return false ;
        }

        // --> write S or O??
        private void writeNodePretty(Node obj) {
            // Order matters? - one connected objects may include list elements.
            if ( lists.containsKey(obj) )
                list(lists.get(obj)) ;
            else if ( nestedObjects.contains(obj) )
                nestedObject(obj) ;
            else if ( RDF_Nil.equals(obj) )
                out.println("()") ;
            else
                writeNode(obj) ;
        }
    }

    // Order of properties.
    // rdf:type ("a")
    // RDF and RDFS
    // Other.
    // Sorted by URI.

    private static final class ComparePredicates implements Comparator<Node> {
        private static int classification(Node p) {
            if ( p.equals(RDF_type) )
                return 0 ;

            if ( p.getURI().startsWith(RDF.getURI()) || p.getURI().startsWith(RDFS.getURI()) )
                return 1 ;

            return 2 ;
        }

        @Override
        public int compare(Node t1, Node t2) {
            int class1 = classification(t1) ;
            int class2 = classification(t2) ;
            if ( class1 != class2 ) {
                // Java 1.7
                // return Integer.compare(class1, class2) ;
                if ( class1 < class2 )
                    return -1 ;
                if ( class1 > class2 )
                    return 1 ;
                return 0 ;
            }
            String p1 = t1.getURI() ;
            String p2 = t2.getURI() ;
            return p1.compareTo(p2) ;
        }
    }

    private static Comparator<Node> compPredicates = new ComparePredicates() ;

    protected final void writeNode(Node node) {
        nodeFmt.format(out, node) ;
    }

    private void print(String x) {
        out.print(x) ;
    }

    private void gap(int gap) {
        out.print(' ', gap) ;
    }

    // flush aggressively (debugging)
    private void println() {
        out.println() ;
        // out.flush() ;
    }
}
