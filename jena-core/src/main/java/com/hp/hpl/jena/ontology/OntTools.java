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

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.Filter;


/**
 * <p>
 * Some general utilities and algorithms to support developers working with the
 * general classes in the Jena ontology API. <strong>Warning</strong> these
 * utilities are <strong>experimental</strong>. Extensive testing has not yet
 * occurred (see <tt>com.hp.hpl.jena.ontology.impl.TestOntTools</tt> in the
 * test area for basic unit tests), 
 * and in particular performance testing has not been carried out yet. 
 * Users are advised to exercise caution before relying on these utilities in 
 * production code. Please send any comments or suggestions to the
 * <a href="http://tech.groups.yahoo.com/group/jena-dev">Jena support email list</a>.
 * </p>
 */
public class OntTools
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

//    static private Logger log = LoggerFactory.getLogger( OntTools.class );

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the lowest common ancestor of two classes in a given ontology. This
     * is the class that is farthest from the root concept (defaulting to
     * <code>owl:Thing</code> which is a super-class of both <code>u</code>
     * and <code>v</code>. The algorithm is based on
     * <a href="http://en.wikipedia.org/wiki/Tarjan's_off-line_least_common_ancestors_algorithm">Tarjan's
     * off-line LCA</a>. The current implementation expects that the given model:
     * </p>
     * <ul>
     * <li>is transitively closed over the <code>subClassOf</code> relation</li>
     * <li>can cheaply determine <em>direct sub-class</em> relations</li>
     * </ul>
     * <p>Both of these conditions are true of the built-in Jena OWL reasoners,
     * such as {@link OntModelSpec#OWL_MEM_MICRO_RULE_INF}, and external DL
     * reasoners such as Pellet.</p>
     *
     * @param m The ontology model being queried to find the LCA, which should conform
     * to the reasoner capabilities described above
     * @param u An ontology class
     * @param v An ontology class
     * @return The LCA of <code>u</code> and <code>v</code>
     * @exception JenaException if the language profile of the given model does not
     * define a top concept (e.g. <code>owl:Thing</code>)
     */
    public static OntClass getLCA( OntModel m, OntClass u, OntClass v ) {
        Resource root = m.getProfile().THING();
        if (root == null) {
            throw new JenaException( "The given OntModel has a language profile that does not define a generic root class (such as owl:Thing)" );
        }

        root = root.inModel( m );
        return getLCA( m, root.as( OntClass.class ), u, v );
    }

    /**
     * Answer the lowest common ancestor of two classes, assuming that the given
     * class is the root concept to start searching from. See {@link #getLCA(OntModel, OntClass, OntClass)}
     * for details.
     *
     * @param m The ontology model being queried to find the LCA, which should conform
     * to the reasoner capabilities described above
     * @param root The root concept, which will be the starting point for the algorithm
     * @param u An ontology class
     * @param v An ontology class
     * @return The LCA of <code>u</code> and <code>v</code>
     * @exception JenaException if the language profile of the given model does not
     * define a top concept (e.g. <code>owl:Thing</code>)
     */
    public static OntClass getLCA( OntModel m, OntClass root, OntClass u, OntClass v ) {
        // check some common cases first
        if (u.equals( root ) || v.equals( root )) {
            return root;
        }

        if (u.hasSubClass( v )) {
            return u;
        }

        if (v.hasSubClass( u )) {
            return v;
        }

        // not a common case, so apply Tarjan's LCA algorithm
        LCAIndex index = new LCAIndex();
        lca( root, u, v, index );
        return (OntClass) index.getLCA( u, v );
    }


    /**
     * <p>Answer the shortest path from the <code>start</code> resource to the <code>end</code> RDF node,
     * such that every step on the path is accepted by the given filter. A path is a {@link List}
     * of RDF {@link Statement}s. The subject of the first statement in the list is <code>start</code>,
     * and the object of the last statement in the list is <code>end</code>.</p>
     * <p>The <code>onPath</code> argument is a {@link Filter}, which accepts a statement and returns
     * true if the statement should be considered to be on the path. To search for an unconstrained
     * path, pass {@link Filter#any} as an argument. To search for a path whose predicates match a
     * fixed restricted set of property names, pass an instance of {@link PredicatesFilter}.</p>
     * <p>If there is more than one path of minimal length from <code>start</code> to <code>end</code>,
     * this method returns an arbitrary one. The algorithm is blind breadth-first search,
     * with loop detection.</p>
     *
     * @param m The model in which we are seeking a path
     * @param start The starting resource
     * @param end The end, or goal, node
     * @param onPath A filter which determines whether a given statement can be considered part
     * of the path
     * @return A path, consisting of a list of statements whose first subject is <code>start</code>,
     * and whose last object is <code>end</code>, or null if no such path exists.
     */
    public static Path findShortestPath( Model m, Resource start, RDFNode end, Filter<Statement> onPath ) {
        List<Path> bfs = new LinkedList<>();
        Set<Resource> seen = new HashSet<>();

        // initialise the paths
        for (Iterator<Statement> i = m.listStatements( start, null, (RDFNode) null ).filterKeep( onPath ); i.hasNext(); ) {
            bfs.add( new Path().append( i.next() ) );
        }

        // search
        Path solution = null;
        while (solution == null && !bfs.isEmpty()) {
            Path candidate = bfs.remove( 0 );

            if (candidate.hasTerminus( end )) {
                solution = candidate;
            }
            else {
                Resource terminus = candidate.getTerminalResource();
                if (terminus != null) {
                    seen.add( terminus );

                    // breadth-first expansion
                    for (Iterator<Statement> i = terminus.listProperties().filterKeep( onPath ); i.hasNext(); ) {
                        Statement link = i.next();

                        // no looping allowed, so we skip this link if it takes us to a node we've seen
                        if (!seen.contains( link.getObject() )) {
                            bfs.add( candidate.append( link ) );
                        }
                    }
                }
            }
        }

        return solution;
    }


    /**
     * Answer a list of the named hierarchy roots of a given {@link OntModel}. This
     * will be similar to the results of {@link OntModel#listHierarchyRootClasses()},
     * with the added constraint that every member of the returned iterator will be a
     * named class, not an anonymous class expression. The named root classes are
     * calculated from the root classes, by recursively replacing every anonymous class
     * with its direct sub-classes. Thus it can be seen that the values in the list
     * consists of the shallowest fringe of named classes in the hierarchy.
     * @param m An ontology model
     * @return A list of classes whose members are the named root classes of the
     * class hierarchy in <code>m</code>
     */
    public static List<OntClass> namedHierarchyRoots( OntModel m ) {
        List<OntClass> nhr = new ArrayList<>();         // named roots
        List<OntClass> ahr = new ArrayList<>();         // anon roots

        // do the initial partition of the root classes
        partitionByNamed( m.listHierarchyRootClasses(), nhr, ahr );

        // now push the fringe down until we have only named classes
        while (!ahr.isEmpty()) {
            OntClass c = ahr.remove( 0 );
            partitionByNamed( c.listSubClasses( true ), nhr, ahr );
        }

        return nhr;
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Compute the LCA disjoint set at <code>cls</code>, noting that we are
     * searching for the LCA of <code>uCls</code> and <code>vCls</code>.
     * @param cls The class we are testing (this is 'u' in the Wiki article)
     * @param uCls One of the two classes we are searching for the LCA of. We
     * have simplified the set P of pairs to the unity set {uCls,vCls}
     * @param vCls One of the two classes we are searching for the LCA of. We
     * have simplified the set P of pairs to the unity set {uCls,vCls}
     * @param index A data structure mapping resources to disjoint sets (since
     * we can't side-effect Jena resources), and which is used to record the
     * LCA pairs
     */
    protected static DisjointSet lca( OntClass cls, OntClass uCls, OntClass vCls, LCAIndex index ) {
//        log.debug( "Entering lca(), cls = " + cls );
        DisjointSet clsSet = index.getSet( cls );
        if (clsSet.isBlack()) {
            // already visited
            return clsSet;
        }

        // not visited yet
        clsSet.setAncestor( clsSet );

        // for each child of cls
        for (Iterator<OntClass> i = cls.listSubClasses( true ); i.hasNext(); ) {
            OntClass child = i.next();

            if (child.equals( cls ) || child.equals( cls.getProfile().NOTHING() )) {
                // we ignore the reflexive case and bottom
                continue;
            }

            // compute the LCA of the sub-tree
            DisjointSet v = lca( child, uCls, vCls, index );

            // union the two disjoint sets together
            clsSet.union( v );

            // propagate the distinguished member
            clsSet.find().setAncestor( clsSet );
        }

        // this node is done
        clsSet.setBlack();

        // are we inspecting one of the elements we're interested in?
        if (cls.equals( uCls )) {
            checkSolution( uCls, vCls, index );
        }
        else if (cls.equals( vCls )) {
            checkSolution( vCls, uCls, index );
        }

        return clsSet;
    }

    /**
     * Check to see if we have found a solution to the problem.
     * TODO: we could throw an exception to simulate a non-local exit
     * here, since we've assumed that P is the unity set.
     * @param uCls
     * @param vCls
     * @param index
     */
    protected static void checkSolution( OntClass uCls, OntClass vCls, LCAIndex index ) {
        DisjointSet vSet = index.getSet( vCls );
        DisjointSet uSet = index.getSet( uCls );

        if (vSet != null && vSet.isBlack() && !vSet.used() &&
            uSet != null && uSet.isBlack() && !uSet.used()) {
            vSet.setUsed();
            uSet.setUsed();
//            log.debug( "Found LCA: u = " + uCls + ", v = " + vCls  );
            OntClass lca = (OntClass) vSet.find().getAncestor().getNode();
//            log.debug( "Found LCA: lca = " + lca );
            index.setLCA( uCls, vCls, lca );
        }

    }

    /**
     * Partition the members of an iterator into two lists, according to whether
     * they are named or anonymous classes
     * @param i An iterator to partition
     * @param named A list of named classes
     * @param anon A list of anonymous classes
     */
    protected static void partitionByNamed( Iterator<? extends OntClass> i, List<OntClass> named, List<OntClass> anon ) {
        while (i.hasNext()) {
            OntClass c = i.next();
            boolean ignore = false;

            // duplicate check: we ignore this class if we've already got it
            if (named.contains( c )) {
                ignore = true;
            }

            // subsumption check: c must have only anon classes or Thing
            // as super-classes to still qualify as a root class
            Resource thing = c.getProfile().THING();
            for (Iterator<OntClass> j = c.listSuperClasses(); !ignore && j.hasNext(); ) {
                OntClass sup = j.next();
                if (!((thing != null && sup.equals( thing )) ||
                      sup.isAnon() ||
                      sup.equals( c )))
                {
                    ignore = true;
                }
            }

            if (!ignore) {
                // place the class in the appropriate partition
                (c.isAnon() ? anon : named).add( c );
            }
        }
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * A simple representation of disjoint sets
     */
    public static class DisjointSet
    {
        /** The resource this set represents */
        private Resource m_node;

        /** The parent set in a union */
        private DisjointSet m_parent;

        /** Heuristic used to build balanced unions */
        private int m_rank;

        /** The link to the distinguished member set */
        private DisjointSet m_ancestor;

        /** Set to true when the node has been processed */
        private boolean m_black = false;

        /** Set to true when we've inspected a black set, since the result is only
         * correct just after both of the sets for u and v have been marked black */
        private boolean m_used = false;

        public DisjointSet( Resource node ) {
            m_node = node;
            m_rank = 0;
            m_parent = this;
        }

        public Resource getNode() {
            return m_node;
        }

        public DisjointSet getParent() {
            return m_parent;
        }

        public void setParent( DisjointSet parent ) {
            m_parent = parent;
        }

        public int getRank() {
            return m_rank;
        }

        public void incrementRank() {
            m_rank++;
        }

        public DisjointSet getAncestor() {
            return m_ancestor;
        }

        public void setAncestor( DisjointSet anc ) {
            m_ancestor = anc;
        }

        public void setBlack() {
            m_black = true;
        }

        public boolean isBlack() {
            return m_black;
        }

        public boolean used() {
            return m_used;
        }

        public void setUsed() {
            m_used = true;
        }

        /**
         * The find operation collapses the pointer to the root parent, which is
         * one of Tarjan's standard optimisations.
         * @return The representative of the union containing this set
         */
        public DisjointSet find() {
            DisjointSet root;
            if (getParent() == this) {
                // the representative of the set
                root = this;
            }
            else {
                // otherwise, seek the representative of my parent and save it
                root = getParent().find();
                setParent( root );
            }

            return root;
        }

        /**
         * The union of two sets
         * @param y
         */
        public void union( DisjointSet y ) {
            DisjointSet xRoot = find();
            DisjointSet yRoot = y.find();

            if (xRoot.getRank() > yRoot.getRank()) {
                yRoot.setParent( xRoot );
            }
            else if (yRoot.getRank() > xRoot.getRank()) {
                xRoot.setParent( yRoot );
            }
            else if (xRoot != yRoot) {
                yRoot.setParent( xRoot );
                xRoot.incrementRank();
            }
        }

        /**
         * @see java.lang.Object#toString()
         * @return A string representation of this set for debugging
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append( "DisjointSet{node=" );
            buf.append( m_node );
            buf.append( ",anc=" );
            buf.append( (getAncestor() == this) ? "self" : (getAncestor() == null ? "null" : getAncestor().toShortString()) );
            buf.append( ",parent=" );
            buf.append( (getParent() == this) ? "self" : (getParent() == null ? "null" : getParent().toShortString()) );
            buf.append( ",rank=" );
            buf.append( getRank() );
            buf.append( m_black ? ",black" : ",white" );
            buf.append( "}");

            return buf.toString();
        }

        public String toShortString() {
            StringBuilder buf = new StringBuilder();
            buf.append( "DisjointSet{node=" );
            buf.append( m_node );
            buf.append( ",parent=" );
            buf.append( (getParent() == this) ? "self" : (getParent() == null ? "null" : getParent().toShortString()) );
            buf.append( "...}" );

            return buf.toString();
        }
    }

    /**
     * Simple data structure mapping RDF nodes to disjoint sets, and
     * pairs of resources to their LCA.
     */
    public static class LCAIndex
    {
        private Map<Resource, DisjointSet> m_setIndex = new HashMap<>();
        private Map<Resource, Map<Resource, Resource>> m_lcaIndex = new HashMap<>();

        public Resource getLCA( Resource u, Resource v ) {
            Map<Resource, Resource> map = m_lcaIndex.get( u );
            Resource lca = (map == null) ? null : (Resource) map.get( v );

            if (lca == null) {
                map = m_lcaIndex.get( v );
                lca = (map == null) ? null : (Resource) map.get( u );
            }

            return lca;
        }

        public void setLCA( Resource u, Resource v, Resource lca ) {
            Map<Resource, Resource> uMap = m_lcaIndex.get( u );
            if (uMap == null) {
                uMap = new HashMap<>();
                m_lcaIndex.put( u, uMap );
            }
            uMap.put( v, lca );
        }

        public DisjointSet getSet( Resource r ) {
            DisjointSet s = m_setIndex.get( r );
            if (s == null) {
//                log.debug( "Generating new set for " + r );
                s = new DisjointSet( r );
                m_setIndex.put( r, s );
            }
            else {
//                log.debug( "Retrieving old set for " + r );

            }
            return s;
        }
    }

    /**
     * A path is an application of {@link java.util.List} containing only {@link Statement}
     * objects, and in which for all adjacent elements <code>S<sub>i-1</sub></code>
     * and  <code>S<sub>i</sub></code>, where <code>i &gt; 0</code>, it is true that:
     * <code><pre>S<sub>i-1</sub>.getObject().equals( S<sub>i</sub>.getSubject() )</pre></code>
     */
    public static class Path extends ArrayList<Statement>
    {
        public Path() {
            super();
        }

        public Path( Path basePath ) {
            super( basePath );
        }

        public Statement getStatement( int i ) {
            return get( i );
        }

        /** Answer a new Path whose elements are this Path with <code>s</code> added at the end */
        public Path append( Statement s ) {
            Path newPath = new Path( this );
            newPath.add( s );
            return newPath;
        }

        /** Answer true if the last link on the path has object equal to <code>n</code>  */
        public boolean hasTerminus( RDFNode n ) {
            return n != null && n.equals( getTerminal() );
        }

        /** Answer the RDF node at the end of the path, if defined, or null */
        public RDFNode getTerminal() {
            return size() > 0 ? get( size() - 1 ).getObject() : null;
        }

        /** Answer the resource at the end of the path, if defined, or null */
        public Resource getTerminalResource() {
            RDFNode n = getTerminal();
            return (n != null && n.isResource()) ? (Resource) n : null;
        }
    }

    /**
     * A filter which accepts statements whose predicate matches one of a collection
     * of predicates held by the filter object.
     */
    public static class PredicatesFilter extends Filter<Statement>
    {
        public Collection<Property> m_preds;

        /** Accept statements with any predicate from <code>preds</code> */
        public PredicatesFilter( Collection<Property> preds ) {
            m_preds = preds;
        }

        /** Accept statements with any predicate from <code>preds</code> */
        public PredicatesFilter( Property[] preds ) {
            m_preds = new HashSet<>();
            for ( Property pred : preds )
            {
                m_preds.add( pred );
            }
        }

        /** Accept statements with predicate <code>pred</code> */
        public PredicatesFilter( Property pred ) {
            m_preds = new HashSet<>();
            m_preds.add( pred );
        }

        @Override public boolean accept( Statement s ) {
            return m_preds.contains( s.getPredicate() );
        }
    }
}
