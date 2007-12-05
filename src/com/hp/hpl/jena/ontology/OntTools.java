/*****************************************************************************
 * Source code metadata
 *
 * Author    ijd
 * Package   Jena2
 * Created   Nov 30, 2007
 * Filename  OntTools.java
 *
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.impl.test.TestOntTools;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;


/**
 * <p>
 * Some general utilities and algorithms to support developers working with the
 * general classes in the Jena ontology API. <strong>Warning</strong> these
 * utilities are <strong>experimental</strong>. Extensive testing has not yet
 * occurred (see {@link TestOntTools} for basic unit tests), and in particular
 * performance testing has not been carried out yet. Users are advised to exercise
 * caution before relying on these utilities in production code. Please send
 * any comments or suggestions to the
 * <a href="http://tech.groups.yahoo.com/group/jena-dev">Jena support email list</a>.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 */
public class OntTools
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    static private Log log = LogFactory.getLog( OntTools.class );

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the lowest common ancestor of two classes in a given ontology. This
     * is the class that is furthest from the root concept (defaulting to
     * <code>owl:Thing</code> which is a super-class of both <code>u</code>
     * and <code>v</code>. The algorithm is based on
     * <a href="http://en.wikipedia.org/wiki/Tarjan's_off-line_least_common_ancestors_algorithm">Tarjan's
     * off-line LCA</a>. The current implementation expects that the given model:
     * </p>
     * <ul>
     * <li>is transitively closed over the <code>subClassOf</code> relation</li>
     * <li>can cheaply determine <em>direct sub-class<em> relations</li>
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

        root = (Resource) root.inModel( m );
        return getLCA( m, (OntClass) root.as( OntClass.class ), u, v );
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
        log.debug( "Entering lca(), cls = " + cls );
        DisjointSet clsSet = index.getSet( cls );
        if (clsSet.isBlack()) {
            // already visited
            return clsSet;
        }

        // not visited yet
        clsSet.setAncestor( clsSet );

        // for each child of cls
        for (Iterator i = cls.listSubClasses( true ); i.hasNext(); ) {
            OntClass child = (OntClass) i.next();

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
            log.debug( "Found LCA: u = " + uCls + ", v = " + vCls  );
            OntClass lca = (OntClass) vSet.find().getAncestor().getNode();
            log.debug( "Found LCA: lca = " + lca );
            index.setLCA( uCls, vCls, lca );
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
         * @return
         * @see java.lang.Object#toString()
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
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
            StringBuffer buf = new StringBuffer();
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
     * @author ijd
     *
     */
    public static class LCAIndex
    {
        private Map m_setIndex = new HashMap();
        private Map m_lcaIndex = new HashMap();

        public Resource getLCA( Resource u, Resource v ) {
            Map map = (Map) m_lcaIndex.get( u );
            Resource lca = (map == null) ? null : (Resource) map.get( v );

            if (lca == null) {
                map = (Map) m_lcaIndex.get( v );
                lca = (map == null) ? null : (Resource) map.get( u );
            }

            return lca;
        }

        public void setLCA( Resource u, Resource v, Resource lca ) {
            Map uMap = (Map) m_lcaIndex.get( u );
            if (uMap == null) {
                uMap = new HashMap();
                m_lcaIndex.put( u, uMap );
            }
            uMap.put( v, lca );
        }

        public DisjointSet getSet( Resource r ) {
            DisjointSet s = (DisjointSet) m_setIndex.get( r );
            if (s == null) {
                log.debug( "Generating new set for " + r );
                s = new DisjointSet( r );
                m_setIndex.put( r, s );
            }
            else {
                log.debug( "Retrieving old set for " + r );

            }
            return s;
        }
    }
}

