/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: DIGAdapter.java,v $
 * Revision           $Revision: 1.18 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:35 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.xml.SimpleXMLPath;
import com.hp.hpl.jena.vocabulary.*;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;


/**
 * <p>
 * An adapter class that mediates between a Jena InfGraph and a DIG reasoner process.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGAdapter.java,v 1.18 2004-12-07 09:56:35 andy_seaborne Exp $
 */
public class DIGAdapter 
{
    // Constants
    //////////////////////////////////

    /** DIG profile for 1.7 */
    public static final DIGProfile RACER_17_PROFILE = new DIGProfile() {
        public String getDIGNamespace()   {return "http://dl.kr.org/dig/lang"; }
        public String getSchemaLocation() {return "http://potato.cs.man.ac.uk/dig/level0/dig.xsd"; }
        public String getContentType()    {return "application/x-www-form-urlencoded";}
    };
    
    // switch codes for expression types
    private static final int UNION = 1;
    private static final int INTERSECTION = 2;
    private static final int COMPLEMENT = 3;
    private static final int ENUMERATED = 4;
    private static final int RESTRICTION = 5;

    /** Mark a bNode identifier */
    public static final String ANON_MARKER = "anon:";
    
    /** Well known concept URI's */
    public static final List KNOWN_CONCEPTS = Arrays.asList( new Object[] {OWL.Thing.getURI(), OWL.Nothing.getURI(), 
                                                                           DAML_OIL.Thing.getURI(), DAML_OIL.Thing.getURI() } );

    /** Well known integer type URI's, these we will translate into DIG integer attributes */
    public static final List XSD_INT_TYPES = Arrays.asList( new Object[] {
            XSDDatatype.XSDint.getURI(),
            XSDDatatype.XSDinteger.getURI(),
            XSDDatatype.XSDnonNegativeInteger.getURI(),
            XSDDatatype.XSDbyte.getURI(),
            XSDDatatype.XSDshort.getURI(),
            XSDDatatype.XSDlong.getURI(),
            XSDDatatype.XSDunsignedByte.getURI(),
            XSDDatatype.XSDunsignedLong.getURI(),
            XSDDatatype.XSDunsignedInt.getURI(),
            XSDDatatype.XSDunsignedShort.getURI(),
    } );
    
    
    // Static variables
    //////////////////////////////////

    /** Query ID counter */
    private static int s_queryID = 0;
    
    /** The table that represents the query translations we know about */
    protected static DIGQueryTranslator[] s_queryTable = {
        // subsumes when testing for subsumption between two known class expressions
        new DIGQuerySubsumesTranslator( RDFS.subClassOf.getURI() ),
        new DIGQuerySubsumesTranslator( DAML_OIL.subClassOf.getURI() ),
        
        // testing for disjoint between two known class expressions
        new DIGQueryDisjointTranslator( OWL.disjointWith.getURI() ),
        new DIGQueryDisjointTranslator( DAML_OIL.disjointWith.getURI() ),
        
        // ancestors and parents when testing for a named and variable node
        new DIGQueryAncestorsTranslator( RDFS.subClassOf.getURI(), true ),
        new DIGQueryAncestorsTranslator( RDFS.subClassOf.getURI(), false ),
        new DIGQueryAncestorsTranslator( DAML_OIL.subClassOf.getURI(), true ),
        new DIGQueryAncestorsTranslator( DAML_OIL.subClassOf.getURI(), false ),
        
        new DIGQueryParentsTranslator( ReasonerVocabulary.directSubClassOf.getURI(), true ),
        new DIGQueryParentsTranslator( ReasonerVocabulary.directSubClassOf.getURI(), false ),
        
        // the entire class hierarchy
        new DIGQueryClassHierarchyTranslator( RDFS.subClassOf.getURI() ),
        new DIGQueryClassHierarchyTranslator( DAML_OIL.subClassOf.getURI() ),
        
        // equivalent classes
        new DIGQueryEquivalentsTranslator( OWL.equivalentClass.getURI(), true ),
        new DIGQueryEquivalentsTranslator( OWL.equivalentClass.getURI(), false ),
        new DIGQueryEquivalentsTranslator( DAML_OIL.sameClassAs.getURI(), true ),
        new DIGQueryEquivalentsTranslator( DAML_OIL.sameClassAs.getURI(), false ),

        new DIGQueryIsEquivalentTranslator( OWL.equivalentClass.getURI() ),
        new DIGQueryIsEquivalentTranslator( DAML_OIL.sameClassAs.getURI() ),

        // rancestors and rparents when testing for a named and variable node
        new DIGQueryRoleAncestorsTranslator( RDFS.subPropertyOf.getURI(), true ),
        new DIGQueryRoleAncestorsTranslator( RDFS.subPropertyOf.getURI(), false ),
        new DIGQueryRoleAncestorsTranslator( DAML_OIL.subPropertyOf.getURI(), true ),
        new DIGQueryRoleAncestorsTranslator( DAML_OIL.subPropertyOf.getURI(), false ),
        
        new DIGQueryRoleParentsTranslator( ReasonerVocabulary.directSubPropertyOf.getURI(), true ),
        new DIGQueryRoleParentsTranslator( ReasonerVocabulary.directSubPropertyOf.getURI(), false ),
        
        // the entire role hierarchy
        new DIGQueryRoleHierarchyTranslator( RDFS.subPropertyOf.getURI() ),
        new DIGQueryRoleHierarchyTranslator( DAML_OIL.subPropertyOf.getURI() ),

        // all concepts query for [* rdf:type :Class]
        new DIGQueryAllConceptsTranslator( RDF.type.getURI(), RDFS.Class.getURI() ),
        new DIGQueryAllConceptsTranslator( RDF.type.getURI(), OWL.Class.getURI() ),
        new DIGQueryAllConceptsTranslator( RDF.type.getURI(), DAML_OIL.Class.getURI() ),
        
        // instances
        new DIGQueryInstancesTranslator( RDF.type.getURI() ),
        new DIGQueryInstancesTranslator( DAML_OIL.type.getURI() ),
        new DIGQueryTypesTranslator( RDF.type.getURI() ),
        new DIGQueryTypesTranslator( DAML_OIL.type.getURI() ),
        new DIGQueryInstanceTranslator( RDF.type.getURI() ),
        new DIGQueryInstanceTranslator( DAML_OIL.type.getURI() ),
        new DIGQueryDifferentFromTranslator( OWL.differentFrom.getURI() ),
        new DIGQueryDifferentFromTranslator( DAML_OIL.differentIndividualFrom.getURI() ),
        new DIGQueryRoleFillersTranslator(),
        new DIGQueryRoleFillerTranslator(),
        
        // specific type tests
        new DIGQueryIsConceptTranslator(),
        new DIGQueryIsRoleTranslator(),
        new DIGQueryIsIndividualTranslator(),
    };
    
    
    // Instance variables
    //////////////////////////////////

    /** The profile for the DIG interface this reasoner is interacting with. Defaults to Racer 1.7 */
    protected DIGProfile m_profile = RACER_17_PROFILE;
    
    /** The graph that contains the data we are uploading to the external DIG reasoner */
    protected OntModel m_sourceData;
    
    /** Counter for generating skolem names */
    private int m_skolemCounter = 0;
    
    /** The connection to the DIG reasoner */
    private DIGConnection m_connection;
    
    /** The set of known individual names from the DIG reasoner */
    protected Set m_indNames = new HashSet();
    
    /** Flag that is set to true once we have asked the remote reasoner for its list of individual names */
    protected boolean m_indNamesAsked = false;
    
    /** The set of known concept names from the DIG reasoner */
    protected Set m_conceptNames = new HashSet();
    
    /** Flag that is set to true once we have asked the remote reasoner for its list of concept names */
    protected boolean m_conceptNamesAsked = false;
    
    /** The set of known role names from the DIG reasoner */
    protected Set m_roleNames = new HashSet();
    
    /** Flag that is set to true once we have asked the remote reasoner for its list of role names */
    protected boolean m_roleNamesAsked = false;
    
    /** Model containing axiom statements */
    protected Model m_axioms = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a DIG adapter for the given source data graph, which is encoding an
     * ontology in a language represented by the given model spec. Allocates a new 
     * DIG connection using the default connection URL (<code>http://localhost:8081</code>).</p>
     * @param spec An ont model spec encoding the ontology language of the source graph
     * @param source The graph that contains the source data on which the DIG reasoner 
     * will operate
     */
    public DIGAdapter( OntModelSpec spec, Graph source ) {
        this( spec, source, DIGConnectionPool.getInstance().allocate(), null );
    }
    
    
    /**
     * <p>Construct a DIG adapter for the given source data graph, which is encoding an
     * ontology in a language represented by the given model spec.</p>
     * @param spec An ont model spec encoding the ontology language of the source graph
     * @param source The graph that contains the source data on which the DIG reasoner 
     * will operate
     * @param connection A pre-configured DIG connection to use to communicate with the 
     * external reasoner
     * @param axioms A model containing axioms appropriate to the ontology language
     * this adapter is processing. May be null.
     */
    public DIGAdapter( OntModelSpec spec, Graph source, DIGConnection connection, Model axioms ) {
        m_connection = connection;
        m_axioms = axioms;
        
        // we wrap the given graph in a suitable ontology model
        m_sourceData = ModelFactory.createOntologyModel( spec, ModelFactory.createModelForGraph( source ) );
        
        // don't do the .as() checking, since we know we're not using the reasoner
        m_sourceData.setStrictMode( false );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the DIG profile for the DIG interface this reasoner is attached to.</p>
     * @return A profile detailing the parameters of the DIG variant this reasoner is interacting with.
     */
    public DIGProfile getProfile() {
        return m_profile;
    }
    

    /**
     * <p>Set the profile specifying the variable parts of the DIG profile that are being
     * used in this instance.</p>
     * @param profile The new DIG profile
     */
    public void setProfile( DIGProfile profile ) {
        m_profile = profile;
    }
    
    
    /**
     * <p>Answer the ontology language profile we're assuming in this reasoner.</p>
     * @return The ontology language via the language profile
     */
    public Profile getOntLanguage() {
        return m_sourceData.getProfile();
    }
    
    
    /**
     * <p>Answer the DIG identification structure we obtain by querying the attached reasoner.</p>
     * @return An object containing the results of querying the reasoner for its identity
     * and capabilities
     */
    public DIGIdentifier getDigIdentifier() {
        Document getIDVerb = getConnection().createDigVerb( DIGProfile.GET_IDENTIFIER, getProfile() );
        return new DigIdentifierImpl( getConnection().sendDigVerb( getIDVerb, getProfile() ) );
    }
    
    
    /**
     * <p>Upload the entire contents of the local knowledge base (OWL/DAML model)
     * to the DIG reasoner, using a single large TELL verb.</p>
     * @return True if the ontology model was uploaded to DIG without any warnings.  Recent warnings
     * are available via {@link #getRecentWarnings}
     * @exception DigReasonerException If the upload fails for any reason. The error message from
     * the DIG reasoner will be returned.
     */
    public boolean uploadKB() {
        // ensure first that we have a KB identifier
        getConnection().bindKB( false, getProfile() );
        
        // now tell the existing KB contents
        Document kbDIG = translateKbToDig();
                
        Document response = getConnection().sendDigVerb( kbDIG, getProfile() );
        return !getConnection().warningCheck( response );
    }
    
    
    
    /**
     * <p>Answer an iterator over any recent warnings returned from from the remote DIG reasoner.</p>
     * @return An iterator over any warnings; if there are no warnings the return 
     * value will be an iterator that returns <code>hasNext()</code> = false.
     */
    public Iterator getRecentWarnings() {
        return getConnection().getWarnings();
    }
    
    
    /**
     * <p>Answer an XML document that contains the DIG translation of the local graph, wrapped
     * as a tell verb</p>
     * @return An XML document containing the tell verb
     */
    public Document translateKbToDig() {
        Document tell = getConnection().createDigVerb( DIGProfile.TELLS, getProfile() );
        Element root = tell.getDocumentElement();
        
        addNamedEntities( root );
        translateClasses( root );
        translateRoles( root );
        translateAttributes( root );
        translateIndividuals( root );
        translateAllDifferentAxioms( root );
        
        return tell;
    }
    
    
    /**
     * <p>Clear the old contents of the DIG knowledge base</p>
     */
    public void resetKB() {
        getConnection().bindKB( true, getProfile() );
        
        // reset the name caches
        m_indNames.clear();
        m_indNamesAsked = false;
        m_conceptNames.clear();
        m_conceptNamesAsked = false;
        m_roleNames.clear();
        m_roleNamesAsked = false;
    }
    
    
    /**
     * <p>Answer this adapter's connection to the database.</p>
     * @return The DIG connector this adapter is using, or null if the connection has 
     * been closed.
     */
    public DIGConnection getConnection() {
        return m_connection;
    }
    
    
    /**
     * <p>Close this adapter, and release the connector to the external DIG KB.</p>
     */
    public void close() {
        getConnection().release();
        m_connection = null;
    }
    
    
    /**
     * <p>Basic pattern lookup interface - answer an iterator over the triples
     * matching the given pattern.  Where possible, this query will first be
     * given to the external reasoner, with the local graph used to generate
     * supplemental bindings.</p> 
     * @param pattern a TriplePattern to be matched against the data
     * @return An ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */ 
    public ExtendedIterator find( TriplePattern pattern ) {
        DIGQueryTranslator tr = getQueryTranslator( pattern, null );
        
        ExtendedIterator remote = (tr == null) ? null : tr.find( pattern, this );
        
        com.hp.hpl.jena.graph.Node pSubj = normaliseNode( pattern.getSubject() );
        com.hp.hpl.jena.graph.Node pPred = normaliseNode( pattern.getPredicate() );
        com.hp.hpl.jena.graph.Node pObj = normaliseNode( pattern.getObject() );
        ExtendedIterator local = m_sourceData.getGraph().find( pSubj, pPred, pObj );
        
        // if we have a remote iterator, prepend to the local one and drop duplicates
        ExtendedIterator i = (remote == null) ? local : remote.andThen( local );
        
        // add the axioms if specified 
        i = (m_axioms == null) ? i : i.andThen( m_axioms.getGraph().find( pSubj, pPred, pObj ) );
        
        // make sure we don't have duplicates
        return UniqueExtendedIterator.create( i );
    }


    /**
     * <p>Basic pattern lookup interface - answer an iterator over the triples
     * matching the given (S,P,O) pattern, given also some premises for the 
     * query.  Where possible, this query will first be
     * given to the external reasoner, with the local graph used to generate
     * supplemental bindings.</p> 
     * @param pattern a TriplePattern to be matched against the data
     * @param premises A model containing additional premises for the find query,
     * typically used to allow the subject and/or object to be an expression
     * rather than just a simple node
     * @return An ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */ 
    public ExtendedIterator find( TriplePattern pattern, Model premises ) {
        DIGQueryTranslator tr = getQueryTranslator( pattern, premises );
        
        if (tr == null) {
            LogFactory.getLog( getClass() ).debug( "Could not find DIG query translator for " + pattern );
        }
        
        ExtendedIterator remote = (tr == null) ? null : tr.find( pattern, this, premises );
        
        com.hp.hpl.jena.graph.Node pSubj = normaliseNode( pattern.getSubject() );
        com.hp.hpl.jena.graph.Node pPred = normaliseNode( pattern.getPredicate() );
        com.hp.hpl.jena.graph.Node pObj = normaliseNode( pattern.getObject() );
        ExtendedIterator local = m_sourceData.getGraph().find( pSubj, pPred, pObj );
        
        // if we have a remote iterator, prepend to the local one and drop duplicates
        ExtendedIterator i = (remote == null) ? local : remote.andThen( local );
        
        // add the axioms if specified 
        i = (m_axioms == null) ? i : i.andThen( m_axioms.getGraph().find( pSubj, pPred, pObj ) );
        
        // make sure we don't have duplicates
        return UniqueExtendedIterator.create( i );
    }


    /**
     * <p>Answer the query translator that matches the given pattern, if any</p>
     * @param pattern The triple pattern that has been received
     * @param premises A model containing the premises to a query (e.g. a class expression)
     * @return A DIG translator that can translate this pattern to a DIG query,
     * or null if no matches.
     */
    public DIGQueryTranslator getQueryTranslator( TriplePattern pattern, Model premises ) {
        for (int i = 0;  i < s_queryTable.length;  i++) {
            DIGQueryTranslator dqt = s_queryTable[i];
            
            if (s_queryTable[i].trigger( pattern, this, premises )) {
                return s_queryTable[i];
            }
        }
        
        return null;
    }


    /**
     * <p>Answer the graph of local (source) data.</p>
     * @return The graph containing the local source data.
     */
    public Graph getGraph() {
        return m_sourceData.getGraph();
    }

    
    /** 
     * <p>Answer an identifier for a resource, named or bNode</p>
     * @param r A resource
     * @return A unique identifier for the resource as a string, which will either
     * be the resource URI for named resources, or a unique ID string for bNodes
     */
    public String getResourceID( Resource r ) {
        return getNodeID( r.asNode() );
    }


    /** 
     * <p>Answer an identifier for a node, named or anon</p>
     * @param n An RDF node
     * @return A unique identifier for the node as a string, which will either
     * be the resource URI for named nodes, or a unique ID string for bNodes
     */
    public String getNodeID( com.hp.hpl.jena.graph.Node n ) {
        if (n.isBlank()) {
            return ANON_MARKER + n.getBlankNodeId().toString();
        }
        else {
            return n.getURI();
        }
    }


    /**
     * <p>Add a DIG reference to the class identifed in the source graph by the given Jena
     * graph Node to the given XML element.  If the class is a named class, this will be
     * a <code>&lt;catom&gt;</code> element, otherwise it will be a class description axiom.
     * Assumes that the instance variable <code>m_sourceData</code> provides the statements that
     * further define the class if it is a description not a name. 
     * </p>
     * @param elem The parent XML element to which the class description will be attached
     * @param node An RDF graph node representing a class we wish to describe.
     */
    public void addClassDescription( Element elem, com.hp.hpl.jena.graph.Node node ) {
        addClassDescription( elem, node, m_sourceData );
    }
    
    
    /**
     * <p>Add a DIG reference to the class identifed in the source graph by the given Jena
     * graph Node to the given XML element.  If the class is a named class, this will be
     * a <code>&lt;catom&gt;</code> element, otherwise it will be a class description axiom.
     * </p>
     * @param elem The parent XML element to which the class description will be attached
     * @param node An RDF graph node representing a class we wish to describe.
     * @param sourceData A model containing the statements about the given class description
     * resource
     */
    public void addClassDescription( Element elem, com.hp.hpl.jena.graph.Node node, Model sourceData ) {
        Model m = (sourceData == null) ? m_sourceData : sourceData;
        addClassDescription( elem, (Resource) m.getRDFNode( node ), m );
    }
    
    
    /**
     * <p>Add a DIG reference to the class identifed in the source graph by the given Jena
     * resource to the given XML element.  If the class is a named class, this will be
     * a <code>&lt;catom&gt;</code> element, otherwise it will be a class description axiom.</p>
     * @param elem The parent XML element to which the class description will be attached
     * @param res An RDF resource representing a class we wish to describe.
     * @param sourceData A model containing the statements about the given class description
     * resource
     */
    public void addClassDescription( Element elem, Resource res, Model sourceData ) {
        // ensure we have a resource from the source data model
        Resource cls = (res.getModel() != sourceData) ? sourceData.getResource( res.getURI() ) : res;
        
        if (!cls.isAnon() || m_conceptNames.contains( getNodeID( cls.getNode() ))) {
            // a named class, or an already known bNode
            translateClassIdentifier( elem, cls );
        }
        else {
            // a new bNode introducing a class expression
            translateClassDescription( elem, (OntClass) cls.as( OntClass.class ), sourceData );
        }
    }
    
    
    /**
     * <p>Answer true if the given node corresponds to one of the individuals known to
     * the DIG reasoner.</p>
     * @param node A node to test
     * @return True if <code>node</code> is a known individual
     */
    public boolean isIndividual( com.hp.hpl.jena.graph.Node node ) {
        return node.isConcrete() && !node.isLiteral() && getKnownIndividuals().contains( getNodeID( node ) );
    }    


    /**
     * <p>Answer true if the given node corresponds to one of the roles known to
     * the DIG reasoner.</p>
     * @param node A node to test
     * @param premises A model defining premises that may encode more information about
     * node, or may be null
     * @return True if <code>node</code> is a known role
     */
    public boolean isRole( com.hp.hpl.jena.graph.Node node, Model premises ) {
        return node.isConcrete() && 
               (getKnownRoles().contains( getNodeID( node ) ) ||
                ((premises != null) &&
                isPremisesRole( node, premises )) );
    }    


    /**
     * <p>Answer true if the given node corresponds to one of the concepts known to
     * the DIG reasoner.</p>
     * @param node A node to test
     * @param premises A model defining premises that may encode more information about
     * node, or may be null
     * @return True if <code>node</code> is a known concept
     */
    public boolean isConcept( com.hp.hpl.jena.graph.Node node, Model premises ) {
        return node.isConcrete() && !node.isLiteral() && 
               (getKnownConcepts().contains( getNodeID( node ) ) ||
                ((premises != null) && isPremisesClass( node, premises )) ||
                KNOWN_CONCEPTS.contains( getNodeID( node ) ));
    }
    
    
    /**
     * <p>Answer the ontology language specification for the source model underlying
     * this DIG adapter.</p>
     * @return The ontology model spec
     */
    public OntModelSpec getSourceSpecification() {
        return m_sourceData.getSpecification();
    }
    
    
    /**
     * <p>Create a new element to represent a query, adding to it a unique query
     * ID.</p>
     * @param query The query document
     * @param elemName The string name of the query element
     * @return The new query element
     */
    public Element createQueryElement( Document query, String elemName ) {
        Element qElem = addElement( query.getDocumentElement(), elemName );
        qElem.setAttribute( DIGProfile.ID, "q" + s_queryID++ );
        return qElem;
    }


    // Internal implementation methods
    //////////////////////////////////

    
    /**
     * <p>In Dig, defXXX elements are required to introduce all named entities,
     * such as concepts and roles.  This method collects such definitions and
     * adds the defXXX elements as children of the tell element.</p>
     * @param tell The XML element, typically &lt;tells&gt;, to which to attach the 
     * declarations
     */
    protected void addNamedEntities( Element tell ) {
        // first we collect the named entities
        HashSet roles = new HashSet();
        HashSet attributes = new HashSet();
        HashSet concepts = new HashSet();
        HashSet individuals = new HashSet();
        
        addAll( m_sourceData.listClasses(), concepts );
        addAll( m_sourceData.listDatatypeProperties(), attributes );
        addAll( m_sourceData.listIndividuals(), individuals );
        
        collectRoleProperties( roles );
        
        // collect the DIG definitions at the beginning of the document
        addNamedDefs( tell, concepts.iterator(), DIGProfile.DEFCONCEPT, m_conceptNames );
        addNamedDefs( tell, roles.iterator(), DIGProfile.DEFROLE, m_roleNames );
        addNamedDefs( tell, attributes.iterator(), DIGProfile.DEFATTRIBUTE, null);
        addNamedDefs( tell, individuals.iterator(), DIGProfile.DEFINDIVIDUAL, m_indNames );
    }
    

    /** Add all object properties (roles) to the given collection */
    protected void collectRoleProperties( Collection roles ) {
        addAll( m_sourceData.listObjectProperties(), roles );
        addAll( m_sourceData.listInverseFunctionalProperties(), roles );
        addAll( m_sourceData.listTransitiveProperties(), roles );
        
        // not present in DAML
        if (m_sourceData.getProfile().SYMMETRIC_PROPERTY() != null) {
            addAll( m_sourceData.listSymmetricProperties(), roles );
        }
    }


    /**
     * <p>Add the named definitions from the given iterator to the tell document we are building.</p>
     * @param tell The document being built
     * @param i An iterator over resources
     * @param defType The type of DIG element we want to build
     * @param nameCollection Optional set of names of this type of entity to collect
     */
    protected void addNamedDefs( Element tell, Iterator i, String defType, Set nameCollection ) {
        while (i.hasNext()) {
            RDFNode n = (Resource) i.next();
            if (n instanceof Resource) {
                String id = getNodeID( n.asNode() );
                addNamedElement( tell, defType, getNodeID( n.asNode() ) );
                
                // a named concept, role, etc is being defined
                if (nameCollection != null) {
                    nameCollection.add( id );
                }
            }
        }
    }


    /**
     * <p>Answer a element with the given element name, 
     * and with a attribute 'name' with the given uri as name.<p>
     * @param parent The parent node to add to
     * @param elemName The element name, eg defconcept
     * @param uri The URI of the definition
     * @return A named element
     */
    protected Element addNamedElement( Element parent, String elemName, String uri ) {
        Element elem = addElement( parent, elemName );
        elem.setAttribute( DIGProfile.NAME, uri );

        return elem;
    }
    
    
    /** Add to the given element a child element with the given name */
    protected Element addElement( Element parent, String childName ) {
        Element child = parent.getOwnerDocument().createElement( childName );
        return (Element) parent.appendChild( child );
    }
    
    
    /** Add iterator contents to collection */
    private void addAll( Iterator i, Collection c ) {
        for (; i.hasNext(); c.add( i.next() ) );
    }
    
    
    /**
     * <p>Translate all of the classes in the current KB into descriptions
     * using the DIG concept language, and attach the axioms generated
     * to the given element.</p>
     * @param tell The XML element, typically &lt;tells&gt;, to which
     * to attach the generated translations.
     */
    protected void translateClasses( Element tell ) {
        translateSubClassAxioms( tell );
        translateClassEquivalences( tell );
        translateClassDisjointAxioms( tell );
        
        translateRestrictions( tell );
        
        // now the implicit equivalences
        translateClassExpressions( tell, getOntLanguage().INTERSECTION_OF(), INTERSECTION );
        translateClassExpressions( tell, getOntLanguage().UNION_OF(), UNION );
        translateClassExpressions( tell, getOntLanguage().COMPLEMENT_OF(), COMPLEMENT );
        translateClassExpressions( tell, getOntLanguage().ONE_OF(), ENUMERATED );
    }
    
    /**
     * <p>Translate the sub-class axioms in the source model into DIG
     * impliesc axioms</p>
     * @param tell The node representing the DIG tell verb 
     */
    protected void translateSubClassAxioms( Element tell ) {
        StmtIterator i = m_sourceData.listStatements( null, getOntLanguage().SUB_CLASS_OF(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element impliesc = addElement( tell, DIGProfile.IMPLIESC );
            addClassDescription( impliesc, sc.getSubject(), m_sourceData );
            addClassDescription( impliesc, sc.getResource(), m_sourceData );
        }
    }
    
    
    /**
     * <p>Translate the class equivalence axioms in the source model into DIG
     * equalsc axioms.</p>
     * @param tell The node representing the DIG tell verb
     */
    protected void translateClassEquivalences( Element tell ) {
        // first we do stated equivalences
        StmtIterator i = m_sourceData.listStatements( null, getOntLanguage().EQUIVALENT_CLASS(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element equalc = addElement( tell, DIGProfile.EQUALC );
            addClassDescription( equalc, sc.getSubject(), m_sourceData );
            addClassDescription( equalc, sc.getResource(), m_sourceData );
        }
    }
    
    
    /**
     * <p>Translate class expressions, such as union classes, intersection classes, etc, into the DIG.</p>
     * concept language. The translations are attached to the given tell node.</p>
     * @param tell The node representing the DIG tell verb
     * @param p A property that will require an implicit equivalence to be made explicit
     * in a correct translation to DIG
     * @param classExprType Denotes the type of class expression we are translating
     */
    protected void translateClassExpressions( Element tell, Property p, int classExprType ) {
        translateClassExpressions( tell, m_sourceData.listStatements( null, p, (RDFNode) null ), classExprType, m_sourceData );
    }
    
    
    /**
     * <p>Translate the restrictions in the source model into the DIG concept language.</p>
     * @param tell The node representing the DIG tell verb
     */
    protected void translateRestrictions( Element tell ) {
        translateClassExpressions( tell, 
                                   m_sourceData.listStatements( null, RDF.type, getOntLanguage().RESTRICTION() ), 
                                   RESTRICTION, m_sourceData );
    }
    
    
    /**
     * <p>A named owl:class with a class-construction axiom directly attached is implicitly
     * an equivalence axiom with the anonymous class that has the given class construction.</p>
     * @param tell The node representing the DIG tell verb
     * @param i A statement iterator whose subjects denote the class expressions to be translated
     * @param classExprType Denotes the type of class expression we are translating
     */
    protected void translateClassExpressions( Element tell, StmtIterator i, int classExprType, Model source ) {
        while (i.hasNext()) {
            OntClass cls = (OntClass) i.nextStatement().getSubject().as( OntClass.class );
            
            Element equalc = addElement( tell, DIGProfile.EQUALC );
            addClassDescription( equalc, cls, source );
            
            switch (classExprType) {
                case UNION:          translateUnionClass( equalc, cls, source );        break;
                case INTERSECTION:   translateIntersectionClass( equalc, cls, source ); break;
                case COMPLEMENT:     translateComplementClass( equalc, cls, source );   break;
                case ENUMERATED:     translateEnumeratedClass( equalc, cls, source );   break;
                case RESTRICTION:    translateRestrictionClass( equalc, cls, source );   break;
            }
        }
    }
    
    
    /**
     * <p>Translate a node representing a class expression (presumed anonymous, though
     * this is not tested) into the appropriate DIG class axiom.</p>
     * @param parent The XML node that will be the parent of the class description axiom
     * @param classDescr An OntClass representing the class expression to be translated
     */
    protected void translateClassDescription( Element parent, OntClass classDescr, Model source ) {
        if (classDescr.isUnionClass()) {
            translateUnionClass( parent, classDescr, source );
        }
        else if (classDescr.isIntersectionClass()) {
            translateIntersectionClass( parent, classDescr, source );
        }
        else if (classDescr.isComplementClass()) {
            translateComplementClass( parent, classDescr, source );
        }
        else if (classDescr.isEnumeratedClass()) {
            translateEnumeratedClass( parent, classDescr, source );
        }
        else if (classDescr.isRestriction()) {
            translateRestrictionClass( parent, classDescr, source );
        }
    }
    
    
    /**
     * <p>Translate any statements from the KB that indicates disjointness between
     * two classes.</p>
     * @param tell The XML element representing the tell verb we will attach the
     * translations to.
     */
    protected void translateClassDisjointAxioms( Element tell ) {
        StmtIterator i = m_sourceData.listStatements( null, getOntLanguage().DISJOINT_WITH(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element impliesc = addElement( tell, DIGProfile.DISJOINT );
            addClassDescription( impliesc, sc.getSubject(), m_sourceData );
            addClassDescription( impliesc, sc.getResource(), m_sourceData );
        }
    }
    
    
    /**
     * <p>Translate a given class resource into a DIG concept description, as a child
     * of the given expression element</p>
     * @param expr The parent expression element
     * @param c The concept resource
     */
    protected void translateClassIdentifier( Element expr, Resource c ) {
        if (c.equals( getOntLanguage().THING())) {
            // this is TOP in DIG
            addElement( expr, DIGProfile.TOP );
            return;
        }
        else if (c.equals( getOntLanguage().NOTHING())) {
            // this is BOTTOM in DIG
            addElement( expr, DIGProfile.BOTTOM );
            return;
        }
        else {
            // a named class is represented as a catom element
            Element catom = addElement( expr, DIGProfile.CATOM );
            String digConceptName = getNodeID( c.asNode() );
            catom.setAttribute( DIGProfile.NAME, digConceptName );
        }
    }
    
    
    /**
     * <p>Translate a given restriction resource into a DIG concept description, as a child
     * of the given expression element</p>
     * @param expr The parent expression element
     * @param c The restriction concept resource
     */
    protected void translateRestrictionClass( Element expr, Resource c, Model source ) {
        Restriction r = (Restriction) c.as( Restriction.class );
        
        if (r.isAllValuesFromRestriction()) {
            // all values from restriction translates to a DIG <all>R E</all> axiom
            Element all = addElement( expr, DIGProfile.ALL );
            addNamedElement( all, DIGProfile.RATOM, r.getOnProperty().getURI() );
            addClassDescription( all, r.asAllValuesFromRestriction().getAllValuesFrom(), source );
        }
        else if (r.isSomeValuesFromRestriction()) {
            // some values from restriction translates to a DIG <some>R E</some> axiom
            Element some = addElement( expr, DIGProfile.SOME );
            addNamedElement( some, DIGProfile.RATOM, r.getOnProperty().getURI() );
            addClassDescription( some, r.asSomeValuesFromRestriction().getSomeValuesFrom(), source );
        }
        else if (r.isHasValueRestriction()) {
            // special case
            translateHasValueRestriction( expr, r.asHasValueRestriction() );
        }
        else if (r.isMinCardinalityRestriction()) {
            // unqualified, so we make the qualification class TOP
            translateCardinalityRestriction( expr, r.asMinCardinalityRestriction().getMinCardinality(), r, 
                                             DIGProfile.ATLEAST, getOntLanguage().THING(), source );
        }
        else if (r.isMaxCardinalityRestriction()) {
            // unqualified, so we make the qualification class TOP
            translateCardinalityRestriction( expr, r.asMaxCardinalityRestriction().getMaxCardinality(), r, 
                                             DIGProfile.ATMOST, getOntLanguage().THING(), source );
        }
        else if (r.isCardinalityRestriction()) {
            // we model a cardinality restriction as the intersection of min and max resrictions
            Element and = addElement( expr, DIGProfile.AND );
            
            // unqualified, so we make the qualification class TOP
            translateCardinalityRestriction( and, r.asCardinalityRestriction().getCardinality(), r, 
                                             DIGProfile.ATMOST, getOntLanguage().THING(), source );
            translateCardinalityRestriction( and, r.asCardinalityRestriction().getCardinality(), r, 
                                             DIGProfile.ATLEAST, getOntLanguage().THING(), source );
        }
        // TODO qualified cardinality restrictions
    }
    
    
    /** Translate an enumerated class to an iset element */
    protected void translateEnumeratedClass(Element expr, OntClass cls, Model source ) {
        // an anonymous enumeration of class expressions
        Element iset = addElement( expr, DIGProfile.ISET );
        for (Iterator i = cls.asEnumeratedClass().listOneOf(); i.hasNext(); ) {
            RDFNode n = (RDFNode) i.next();
            
            if (n instanceof Resource) {
                addNamedElement( iset, DIGProfile.INDIVIDUAL, ((Resource) n).getURI() );
            }
            else {
                LogFactory.getLog( getClass() ).warn( "DIG language cannot yet represent enumerations of concrete literals: " + ((Literal) n).getLexicalForm() );
                //translateLiteral( (Literal) n, iset );
            }
        }
    }


    /** Translate a complement class to a not element */
    protected void translateComplementClass(Element expr, OntClass cls, Model source ) {
        // an anonymous complement of another class expression
        Element not = addElement( expr, DIGProfile.NOT );
        addClassDescription( not, cls.asComplementClass().getOperand(), source );
    }


    /** Translate an intersection class to an and element */
    protected void translateIntersectionClass(Element expr, OntClass cls, Model source) {
        // an anonymous intersection of class expressions
        Element or = addElement( expr, DIGProfile.AND );
        translateClassList( or, cls.asIntersectionClass().getOperands(), source );
    }

 
    /** Translate an union class to an or element */
    protected void translateUnionClass(Element expr, OntClass cls, Model source) {
        // an anonymous intersection of class expressions
        Element or = addElement( expr, DIGProfile.OR );
        translateClassList( or, cls.asUnionClass().getOperands(), source );
    }


    /**
     * <p>Translate a cardinality restriction, with qualification</p>
     * @param parent The parent element
     * @param card The cardinality value
     * @param r The restriction we are translating
     * @param exprName The restriction type (e.g. mincardinality)
     * @param qualType The qualification class
     */
    private void translateCardinalityRestriction( Element parent, int card, Restriction r, String exprName, Resource qualType, Model source ) {
        Element restrict = addElement( parent, exprName );
        restrict.setAttribute( DIGProfile.NUM, Integer.toString( card ) );
        addNamedElement( restrict, DIGProfile.RATOM, r.getOnProperty().getURI() );
        addClassDescription( restrict, qualType, source );
    }


    /**
     * <p>Translate a has value restriction to DIG form.  This is slightly tricky, because there is no
     * direct translation in the DIG concept language.  We translate a has value restriction with an
     * individual value to a existential restriction of the singleton concept.  We translate a has
     * value restriction with a datatype value either to an exists restriction on an integer
     * equality or a string equality, depending on the value.</p>
     * @param expr The parent expression node
     * @param r The has value restriction to translate
     */
    protected void translateHasValueRestriction( Element expr, HasValueRestriction r ) {
        RDFNode value = r.getHasValue();
        Property p = r.getOnProperty();
        
        // we must chose whether to use the concrete domain construction or the individual domain
        if (value instanceof Literal) {
            // int or string domain?
            Literal lit = (Literal) value;
            boolean intDomain = isIntegerType( lit.getDatatype() );
            
            // encode as <intequals val="x"> or <stringequals val="x"> 
            Element eq =  addElement( expr, (intDomain ? DIGProfile.INTEQUALS : DIGProfile.STRINGEQUALS ) );
            eq.setAttribute( DIGProfile.VAL, lit.getLexicalForm() );
            
            addNamedElement( eq, DIGProfile.ATTRIBUTE, p.getURI() );
        }
        else {
            // we model hasValue as an existential restriction on a very small set of possible values!
            Element some = addElement( expr, DIGProfile.SOME );
            addNamedElement( some, DIGProfile.RATOM, p.getURI() );

            // we want the set of one individual
            Element iset = addElement( some, DIGProfile.ISET );
            addNamedElement( iset, DIGProfile.INDIVIDUAL, ((Resource) value).getURI() );
        }
    }

    /**
     * <p>Translate a list of class descriptions into DIG concept descriptions 
     */
    protected void translateClassList( Element expr, RDFList operands, Model source ) {
        for (Iterator i = operands.iterator(); i.hasNext(); ) {
            addClassDescription( expr, (Resource) i.next(), source );
        }
    }
    
    
    /** Translate the individuals in the KB to DIG form */
    protected void translateIndividuals( Element expr ) {
        for (Iterator i = m_sourceData.listIndividuals(); i.hasNext(); ) {
            translateIndividual( expr, (Resource) i.next() );
        }
    }
    
    /** Translate the various axioms pertaining to an individual */
    protected void translateIndividual( Element expr, Resource r ) {
        Individual ind = (Individual) r.as( Individual.class );
        translateInstanceTypes( expr, ind );
        
        for (StmtIterator i = ind.listProperties(); i.hasNext(); ) {
            Statement s = i.nextStatement();
            OntProperty p = (OntProperty) s.getPredicate().as( OntProperty.class );
            
            if (p.equals( getOntLanguage().DIFFERENT_FROM())) {
                translateDifferentIndividuals( expr, ind, (Individual) s.getResource().as( Individual.class ) );
            }
            else if (p.equals( getOntLanguage().SAME_AS())) {
                translateSameIndividuals( expr, ind, (Individual) s.getResource().as( Individual.class ) );
            }
            else if (p.isObjectProperty() ||
                     p.isTransitiveProperty() ||
                     p.isSymmetricProperty() ||
                     p.isInverseFunctionalProperty()) {
                translateInstanceRole( expr, ind, p, (Individual) s.getResource().as( Individual.class ) );
            }
            else if (p.isDatatypeProperty()) {
                translateInstanceAttrib( expr, ind, p, s.getLiteral() );
            }
        }
    }
    
    
    /** The rdf:type of each individual becomes a DIG instanceof element */
    protected void translateInstanceTypes( Element expr, Individual ind ) {
        for (Iterator i = ind.listRDFTypes( true );  i.hasNext(); ) {
            Resource type = (Resource) i.next();
            Element inst = addElement( expr, DIGProfile.INSTANCEOF );
            addNamedElement( inst, DIGProfile.INDIVIDUAL, getResourceID( ind ) );
            addClassDescription( inst, (OntClass) type.as( OntClass.class ), m_sourceData );
        }
    }
    
    
    /** Translate an object property into a DIG related element */
    protected void translateInstanceRole( Element expr, Individual ind, OntProperty p, Individual obj) {
        Element related = addElement( expr, DIGProfile.RELATED );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getResourceID( ind ) );
        addNamedElement( related, DIGProfile.RATOM, p.getURI() );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getResourceID( obj ) );
    }


    /** Translate a datatype property into a DIG value element */
    protected void translateInstanceAttrib( Element expr, Individual ind, OntProperty p, Literal obj ) {
        Element related = addElement( expr, DIGProfile.VALUE );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getResourceID( ind ) );
        addNamedElement( related, DIGProfile.ATTRIBUTE, p.getURI() );
        
        translateLiteral( obj, related);
    }
    
    /** Translate an RDF literal to an IVAL or SVAL element */
    protected void translateLiteral( Literal lit, Element parent ) {
        if (isIntegerType( lit.getDatatype() )) {
            Element ival = addElement( parent, DIGProfile.IVAL );
            ival.appendChild( parent.getOwnerDocument().createTextNode( lit.getLexicalForm() ) );
        }
        else {
            Element sval = addElement( parent, DIGProfile.SVAL );
            sval.appendChild( parent.getOwnerDocument().createTextNode( lit.getLexicalForm() ) );
        }
    }


    /** Translate differentFrom(i0, i1) we assert disjoint( iset(i0), iset(i1) ) */
    protected void translateDifferentIndividuals( Element expr, Individual ind, Individual other ) {
        Element disjoint = addElement( expr, DIGProfile.DISJOINT );
        Element iset0 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset0, DIGProfile.INDIVIDUAL, getResourceID( ind ) );
        Element iset1 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset1, DIGProfile.INDIVIDUAL, getResourceID( other ) );
    }
    
    
    /** Translate sameAs(i0, i1) we assert equalc( iset(i0), iset(i1) ) */
    protected void translateSameIndividuals( Element expr, Individual ind, Individual other ) {
        Element disjoint = addElement( expr, DIGProfile.EQUALC );
        Element iset0 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset0, DIGProfile.INDIVIDUAL, getResourceID( ind ) );
        Element iset1 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset1, DIGProfile.INDIVIDUAL, getResourceID( other ) );
    }
    
    
    /** Translate all of the roles (ObjectProperties) in the KB */
    protected void translateRoles( Element expr ) {
        Set roles = new HashSet();
        collectRoleProperties( roles );
        
        for (Iterator i = roles.iterator(); i.hasNext(); ) {
            translateRole( expr, (ObjectProperty) ((Property) i.next()).as( ObjectProperty.class ), m_sourceData );
        }
    }
    
    /** Translate the various axioms that can apply to roles */
    protected void translateRole( Element expr, ObjectProperty role, Model source ) {
        translateBinaryPropertyAxioms( expr, role.getURI(), DIGProfile.IMPLIESR, role.listSuperProperties(), DIGProfile.RATOM );
        translateBinaryPropertyAxioms( expr, role.getURI(), DIGProfile.EQUALR, role.listEquivalentProperties(), DIGProfile.RATOM );
        translateDomainRangeAxioms( expr, role.getURI(), DIGProfile.DOMAIN, role.listDomain(), DIGProfile.RATOM, source );
        translateDomainRangeAxioms( expr, role.getURI(), DIGProfile.RANGE, role.listRange(), DIGProfile.RATOM, source );
        translateInverseAxioms( expr, role, DIGProfile.RATOM );
        
        if (role.isTransitiveProperty()) {
            translateUnaryPropertyAxiom( expr, role.getURI(), DIGProfile.TRANSITIVE, DIGProfile.RATOM );
        }
        if (role.isFunctionalProperty()) {
            translateUnaryPropertyAxiom( expr, role.getURI(), DIGProfile.FUNCTIONAL, DIGProfile.RATOM );
        }
        if (role.isInverseFunctionalProperty()) {
            translateInverseFunctionalAxiom( expr, role, DIGProfile.RATOM );
        }
        if (role.isSymmetricProperty()) {
            translateInverseAxiom( expr, role, DIGProfile.RATOM, role );
        }
    }

    /** Translate all of the attribute (datatype properties) in the KB */    
    protected void translateAttributes( Element expr ) {
        for (Iterator i = m_sourceData.listDatatypeProperties(); i.hasNext(); ) {
            translateAttribute( expr, (DatatypeProperty) ((Property) i.next()).as( DatatypeProperty.class ), m_sourceData );
        }
    }
    
    /** Attributes (datatype properties) have fewer axiom choices than roles */
    protected void translateAttribute( Element expr, DatatypeProperty attrib, Model source ) {
        translateBinaryPropertyAxioms( expr, attrib.getURI(), DIGProfile.IMPLIESR, attrib.listSuperProperties(), DIGProfile.ATTRIBUTE);
        translateBinaryPropertyAxioms( expr, attrib.getURI(), DIGProfile.EQUALR, attrib.listEquivalentProperties(), DIGProfile.ATTRIBUTE );
        translateDomainRangeAxioms( expr, attrib.getURI(), DIGProfile.DOMAIN, attrib.listDomain(), DIGProfile.ATTRIBUTE, source );
        translateAttribRangeAxioms( expr, attrib.getURI(), attrib.listRange(), DIGProfile.ATTRIBUTE );
        
        if (attrib.isFunctionalProperty()) {
            translateUnaryPropertyAxiom( expr, attrib.getURI(), DIGProfile.FUNCTIONAL, DIGProfile.ATTRIBUTE );
        }
    }
    
    /** Helper method for binary axioms each argument of which is an ratom element */
    protected void translateBinaryPropertyAxioms( Element expr, String propURI, String axiomType, Iterator i, String propType ) {
        while (i.hasNext()) {
            Property prop = (Property) i.next();
            Element binaryAxiom = addElement( expr, axiomType );
            addNamedElement( binaryAxiom, propType, propURI );
            addNamedElement( binaryAxiom, propType, prop.getURI() );
        }
    }
    
    /** Helper method for unary axioms, the argument of which is an ratom element */
    protected void translateUnaryPropertyAxiom( Element expr, String propURI, String axiomType, String propType ) {
        Element unaryAxiom = addElement( expr, axiomType );
        addNamedElement( unaryAxiom, propType, propURI );
    }
    
    /** Domain and range are translated as dig domain and range elements */
    protected void translateDomainRangeAxioms( Element expr, String propURI, String axiomType, Iterator i, String propType, Model source ) {
        while (i.hasNext()) {
            Element drAxiom = addElement( expr, axiomType );
            addNamedElement( drAxiom, propType, propURI );
            addClassDescription( drAxiom, (Resource) i.next(), source );
        }
    }
    
    /** Concrete ranges have special treatment*/
    protected void translateAttribRangeAxioms( Element expr, String propURI, Iterator i, String propType ) {
        while (i.hasNext()) {
            Resource type = (Resource) i.next();
            RDFDatatype dt = TypeMapper.getInstance().getTypeByName( type.getURI() );
            
            Element drAxiom = addElement( expr, isIntegerType( dt ) ? DIGProfile.RANGEINT : DIGProfile.RANGESTRING );
            addNamedElement( drAxiom, propType, propURI );
        }
    }
    
    /** Axioms for all of the inverses of a property */
    protected void translateInverseAxioms( Element expr, ObjectProperty p, String propType ) {
        for (Iterator i = p.listInverse(); i.hasNext(); ) {
            translateInverseAxiom(expr, p, propType, (Property) i.next() );
        }
    }
    
    /** Translate inverseOf as equality between the role and the inverse of the named inverse role */
    protected void translateInverseAxiom( Element expr, Property p, String propType, Property inv ) {
        Element equalr = addElement( expr, DIGProfile.EQUALR );
        addNamedElement( equalr, propType, p.getURI() );
        Element inverse = addElement( equalr, DIGProfile.INVERSE );
        addNamedElement( inverse, propType, inv.getURI() );
    }


    /** To translate an inverse functional property, we must introduce a new skolem constant for the inverse role */
    protected void translateInverseFunctionalAxiom( Element expr, ObjectProperty role, String propType ) {
        // we need a skolem name for the inverse property
        String skolemName = getSkolemName( role.getLocalName() );
        
        // first we make the skolem role functional
        addNamedElement( expr, DIGProfile.DEFROLE, skolemName );
        Element functional = addElement( expr, DIGProfile.FUNCTIONAL );
        addNamedElement( functional, propType, skolemName );
        
        // then we make its inverse equal to role
        Element equalr = addElement( expr, DIGProfile.EQUALR );
        addNamedElement( equalr, propType, role.getURI() );
        Element inverse = addElement( equalr, DIGProfile.INVERSE );
        addNamedElement( inverse, propType, skolemName );
    }
    
    
    /** Translate all of the AllDifferent axioms in the KB */
    protected void translateAllDifferentAxioms( Element expr ) {
        if (m_sourceData.getProfile().ALL_DIFFERENT() != null) {
            for (Iterator i = m_sourceData.listAllDifferent(); i.hasNext(); ) {
                AllDifferent ad = (AllDifferent) ((Resource) i.next()).as( AllDifferent.class );
                translateAllDifferent( expr, ad.getDistinctMembers() );
            }
        }
    }
    
    
    /** Translate a single AllDifferent declaration as a set of pair-wise disjoints */
    protected void translateAllDifferent( Element expr, RDFList diffMembers ) {
        List dm = diffMembers.asJavaList();
        
        for (int i = 0;  i < dm.size(); i++) {
            Individual ind0 = (Individual) ((Resource) dm.get(i)).as( Individual.class );
            
            for (int j = i+1; j < dm.size(); j++) {
                Individual ind1 = (Individual) ((Resource) dm.get(j)).as( Individual.class );
                translateDifferentIndividuals( expr, ind0, ind1 );
            }
        }
    }
    
    
    /**
     * <p>Answer true if the given RDF datatype represents an integer value</p>
     */
    private boolean isIntegerType( RDFDatatype type ) {
        String typeURI = (type != null) ? type.getURI() : null;
        return  typeURI != null && XSD_INT_TYPES.contains( typeURI );
    }

    
    /** Answer a skolem constant, using the given name as a root */
    private String getSkolemName( String root ) {
        return "skolem(" + root + "," + m_skolemCounter++ + ")";
        
    }
    

    
    /**
     * <p>Answer an iterator of the individual names known to the DIG reasoner, from the cache if possible.</p>
     * @return An iterator of the known individual names
     */
    protected Set getKnownIndividuals() {
        if (!m_indNamesAsked) {
            m_indNames.addAll( collectNamedTerms( DIGProfile.ALL_INDIVIDUALS,
                                                  new String[] {DIGProfile.INDIVIDUAL_SET, DIGProfile.INDIVIDUAL} ) );
            m_indNamesAsked = true;
        }
        
        return m_indNames;
    }
    
    
    /**
     * <p>Answer an iterator of the concept names known to the DIG reasoner, from the cache if possible.</p>
     * @return An iterator of the known concept names
     */
    protected Set getKnownConcepts() {
        if (!m_conceptNamesAsked) {
            m_conceptNames.addAll( collectNamedTerms( DIGProfile.ALL_CONCEPT_NAMES,
                                                new String[] {DIGProfile.CONCEPT_SET, DIGProfile.SYNONYMS, DIGProfile.CATOM} ) );
            m_conceptNamesAsked = true;
        }
        
        return m_conceptNames;
    }
    
    
    /**
     * <p>Answer an iterator of the role names known to the DIG reasoner, from the cache if possible.</p>
     * @return An iterator of the known role names
     */
    protected Set getKnownRoles() {
        if (!m_roleNamesAsked) {
            m_roleNames.addAll( collectNamedTerms( DIGProfile.ALL_ROLE_NAMES,
                                             new String[] {DIGProfile.ROLE_SET, DIGProfile.SYNONYMS, DIGProfile.RATOM} ) );
            m_roleNamesAsked = true;
        }
        
        return m_roleNames;
    }
    
    
    /**
     * <p>Answer an iterator of named terms known to the DIG reasoner, from the cache if possible.</p>
     * @param queryType The query verb for the ask
     * @param path A list of element names to extract the term names from the returned document
     * @return An iterator of the known names of a particular type
     */
    protected Set collectNamedTerms( String queryType, String[] path ) {
        Set names = new HashSet();
        
        // query the DIG ks for the currently known individuals
        Document query = getConnection().createDigVerb( DIGProfile.ASKS, getProfile() );
        createQueryElement( query, queryType );
        Document response = getConnection().sendDigVerb( query, getProfile() );

        // build the path to extract the names        
        SimpleXMLPath p = new SimpleXMLPath( true );
        for (int j = 0;  j < path.length;  j++) {
            p.appendElementPath( path[j] );
        }
        p.appendAttrPath( DIGProfile.NAME );
                             
        // collect them into a cached set
        addAll( p.getAll( response ), names );
        
        return names;
    }
    
    
    /** Check whether the given node represents a class in the premises */
    private boolean isPremisesClass( com.hp.hpl.jena.graph.Node node, Model premises ) {
        RDFNode rdfNode = premises.getRDFNode( node );
        Profile oProf = getOntLanguage();
        
        if (rdfNode instanceof Resource) {
            Resource r = (Resource) rdfNode;
            Resource any = null;
            
            return 
                ((oProf.CLASS() != null)            && premises.contains( r, RDF.type, oProf.CLASS())       ) ||
                ((oProf.RESTRICTION() != null)      && premises.contains( r, RDF.type, oProf.RESTRICTION()) ) ||
                ((oProf.SUB_CLASS_OF() != null)     && premises.contains( r, oProf.SUB_CLASS_OF(), any )    ) ||
                ((oProf.SUB_CLASS_OF() != null)     && premises.contains( any, oProf.SUB_CLASS_OF(), r )    ) ||
                ((oProf.UNION_OF() != null)         && premises.contains( r, oProf.SUB_CLASS_OF(), any )    ) ||
                ((oProf.INTERSECTION_OF() != null)  && premises.contains( r, oProf.SUB_CLASS_OF(), any )    ) ||
                ((oProf.COMPLEMENT_OF() != null)    && premises.contains( r, oProf.SUB_CLASS_OF(), any )    ) ||
                ((oProf.DISJOINT_WITH() != null)    && premises.contains( r, oProf.DISJOINT_WITH(), any )   ) ||
                ((oProf.EQUIVALENT_CLASS() != null) && premises.contains( r, oProf.EQUIVALENT_CLASS(), any ));
        }
        
        // by default it is not a class
        return false;
    }
    
    /** Check whether the given node represents a class in the premises */
    private boolean isPremisesRole( com.hp.hpl.jena.graph.Node node, Model premises ) {
        RDFNode rdfNode = premises.getRDFNode( node );
        Profile oProf = getOntLanguage();
        
        if (rdfNode instanceof Resource) {
            Resource r = (Resource) rdfNode;
            Resource any = null;
            
            return 
                ((oProf.PROPERTY() != null)                    && premises.contains( r, RDF.type, oProf.PROPERTY())                   ) ||
                ((oProf.OBJECT_PROPERTY() != null)             && premises.contains( r, RDF.type, oProf.OBJECT_PROPERTY())            ) ||
                ((oProf.DATATYPE_PROPERTY() != null)           && premises.contains( r, RDF.type, oProf.DATATYPE_PROPERTY())          ) ||
                ((oProf.TRANSITIVE_PROPERTY() != null)         && premises.contains( r, RDF.type, oProf.TRANSITIVE_PROPERTY())        ) ||
                ((oProf.FUNCTIONAL_PROPERTY() != null)         && premises.contains( r, RDF.type, oProf.FUNCTIONAL_PROPERTY())        ) ||
                ((oProf.INVERSE_FUNCTIONAL_PROPERTY() != null) && premises.contains( r, RDF.type, oProf.INVERSE_FUNCTIONAL_PROPERTY())) ||
                ((oProf.SYMMETRIC_PROPERTY() != null)          && premises.contains( r, RDF.type, oProf.SYMMETRIC_PROPERTY())         ) ||
                ((oProf.SUB_PROPERTY_OF() != null)             && premises.contains( r, oProf.SUB_PROPERTY_OF(), any )                ) ||
                ((oProf.SUB_PROPERTY_OF() != null)             && premises.contains( any, oProf.SUB_PROPERTY_OF(), r )                ) ||
                ((oProf.INVERSE_OF() != null)                  && premises.contains( r, oProf.INVERSE_OF (), any )                    ) ||
                ((oProf.INVERSE_OF() != null)                  && premises.contains( any, oProf.INVERSE_OF (), r )                    );
        }
        
        // by default it is not a class
        return false;
    }
    
    /** Normalise any variables to Node.ANY */
    private com.hp.hpl.jena.graph.Node normaliseNode( com.hp.hpl.jena.graph.Node n ) {
        return n.isConcrete() ? n : com.hp.hpl.jena.graph.Node.ANY;
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================



    /** Encapsulates the identification information from a DIG reasoner */
    private class DigIdentifierImpl
        implements DIGIdentifier
    {
        private Document m_id;
        
        private DigIdentifierImpl( Document id ) {
            m_id = id;
        }
        
        public String getName()             {return m_id.getDocumentElement().getAttribute( DIGProfile.NAME ); }
        public String getVersion()          {return m_id.getDocumentElement().getAttribute( DIGProfile.VERSION ); }
        public String getMessage()          {return m_id.getDocumentElement().getAttribute( DIGProfile.MESSAGE ); }

        public Iterator supportsLanguage()  {return supports( DIGProfile.LANGUAGE ); }
        public Iterator supportsTell()      {return supports( DIGProfile.TELL ); }
        public Iterator supportsAsk()       {return supports( DIGProfile.ASK ); }
        
        private Iterator supports( String support ) {
            Element supports = getChild( m_id.getDocumentElement(), DIGProfile.SUPPORTS );
            return childElementNames( getChild( supports, support ) );
        }

        /** Answer an iterator of the child node names for a given node */
        private Iterator childElementNames( Element node ) {
            ArrayList l = new ArrayList();
            NodeList nl = node.getChildNodes();
            
            for (int i = 0;  i < nl.getLength();  i++) {
                org.w3c.dom.Node n = nl.item(i);
                
                // ignore whitespace text etc
                if (n instanceof Element) {
                    l.add( n.getNodeName() );
                }
            }
            
            return l.iterator();
        }
        
        /** Answer the first named child node */
        private Element getChild( Element node, String name ) {
            return (Element) node.getElementsByTagName( name ).item( 0 );
        }
    }
}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
