/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: DIGAdapter.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-01 22:40:06 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.io.PrintWriter;
import java.util.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

import org.w3c.dom.*;


/**
 * <p>
 * An adapter class that mediates between a Jena InfGraph and a DIG reasoner process.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGAdapter.java,v 1.1 2003-12-01 22:40:06 ian_dickinson Exp $
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

    /** Mark a bNode identifier */
    public static final String ANON_MARKER = "anon:";
    

    // Static variables
    //////////////////////////////////

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
    
    
    // Constructors
    //////////////////////////////////

    public DIGAdapter( OntModelSpec spec, Graph source ) {
        this( spec, source, DIGConnectionPool.getInstance().allocate() );
    }
    
    
    public DIGAdapter( OntModelSpec spec, Graph source, DIGConnection connection ) {
        m_connection = connection;
        
        // we wrap the given graph in a suitable ontology model
        m_sourceData = ModelFactory.createOntologyModel( spec, ModelFactory.createModelForGraph( source ) );
        
        // don't do the .as() checking, since we know we're not using the reasoner
        m_sourceData.setStrictMode( false );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * Set a configuration parameter for the reasoner. Parameters can identified
     * by URI and can also be set when the Reasoner instance is created by specifying a
     * configuration in RDF.
     * 
     * @param parameterUri the property identifying the parameter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(Property parameterUri, Object value) {
        // no parameters
    }


    /**
     * <p>Answer the DIG profile for the DIG interface this reasoner is attached to.</p>
     * @return A profile detailing the parameters of the DIG variant this reasoner is interacting with.
     */
    public DIGProfile getProfile() {
        return m_profile;
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
        if (true) { // TODO remove debug
            System.err.println( "Sending");
            getConnection().serialiseDocument( kbDIG, new PrintWriter( System.err ) );
            System.err.println();
            System.err.println( " .. sent");
        }
                
        Document response = getConnection().sendDigVerb( kbDIG, getProfile() );
        return !getConnection().warningCheck( response );
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
    }
    
    
    /**
     * <p>Answer this adapter's connection to the database.</p>
     * @return The DIG connector this adapter is using
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
    
    
    // Internal implementation methods
    //////////////////////////////////

    
    /**
     * <p>In Dig, defXXX elements are required to introduce all named entities,
     * such as concepts and roles.  This method collects such definitions and
     * adds the defXXX elements as children of the tell element.</p>
     */
    protected void addNamedEntities( Element tell ) {
        // first we collect the named entities
        HashSet roles = new HashSet();
        HashSet attributes = new HashSet();
        HashSet concepts = new HashSet();
        HashSet individuals = new HashSet();
        
        addAll( m_sourceData.listNamedClasses(), concepts );
        addAll( m_sourceData.listDatatypeProperties(), attributes );
        addAll( m_sourceData.listIndividuals(), individuals );
        
        collectRoleProperties(roles);
        
        // collect the DIG definitions at the beginning of the document
        addNamedDefs( tell, concepts.iterator(), DIGProfile.DEFCONCEPT );
        addNamedDefs( tell, roles.iterator(), DIGProfile.DEFROLE );
        addNamedDefs( tell, attributes.iterator(), DIGProfile.DEFATTRIBUTE);
        addNamedDefs( tell, individuals.iterator(), DIGProfile.DEFINDIVIDUAL );
    }
    

    /** Add all object properties (roles) to the given collection */
    protected void collectRoleProperties( Collection roles ) {
        addAll( m_sourceData.listObjectProperties(), roles );
        addAll( m_sourceData.listInverseFunctionalProperties(), roles );
        addAll( m_sourceData.listSymmetricProperties(), roles );
        addAll( m_sourceData.listTransitiveProperties(), roles );
    }


    /**
     * <p>Add the named definitions from the given iterator to the tell document we are building.</p>
     * @param tell The document being built
     * @param i An iterator over resources
     * @param defType The type of DIG element we want to build
     */
    protected void addNamedDefs( Element tell, Iterator i, String defType ) {
        while (i.hasNext()) {
            RDFNode concept = (Resource) i.next();
            if (concept instanceof Resource && !((Resource) concept).isAnon()) {
                addNamedElement( tell, defType, ((Resource) concept).getURI() );
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
    
    
    protected void translateClasses( Element tell ) {
        translateSubClassAxioms( tell );
        translateClassEquivalences( tell );
        translateClassDisjointAxioms( tell );
    }
    
    /**
     * <p>Translate the sub-class axioms in the source model into DIG
     * impliesc axioms</p>
     * @param tell The node representing the DIG tell verb 
     */
    protected void translateSubClassAxioms( Element tell ) {
        StmtIterator i = m_sourceData.listStatements( null, m_sourceData.getProfile().SUB_CLASS_OF(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element impliesc = addElement( tell, DIGProfile.IMPLIESC );
            translateClass( impliesc, sc.getSubject(), true );
            translateClass( impliesc, sc.getResource(), true );
        }
    }
    
    
    /**
     * <p>Translate the class equivalence axioms in the source model into DIG
     * equalsc axioms.</p>
     * @param tell The node representing the DIG tell verb
     */
    protected void translateClassEquivalences( Element tell ) {
        // first we do stated equivalences
        StmtIterator i = m_sourceData.listStatements( null, m_sourceData.getProfile().EQUIVALENT_CLASS(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element impliesc = addElement( tell, DIGProfile.EQUALC );
            translateClass( impliesc, sc.getSubject(), true );
            translateClass( impliesc, sc.getResource(), true );
        }
        
        // now the implicit equivalences
        translateImplicitClassEquivalences( tell, m_sourceData.getProfile().INTERSECTION_OF(), INTERSECTION );
        translateImplicitClassEquivalences( tell, m_sourceData.getProfile().UNION_OF(), UNION );
        translateImplicitClassEquivalences( tell, m_sourceData.getProfile().COMPLEMENT_OF(), COMPLEMENT );
        translateImplicitClassEquivalences( tell, m_sourceData.getProfile().ONE_OF(), ENUMERATED );
    }
    
    
    /**
     * <p>A named owl:class with a class-construction axiom directly attached is implicitly
     * an equivalence axiom with the anonymous class that has the given class construction.</p>
     * @param tell The node representing the DIG tell verb
     * @param p A property that will require an implicit equivalence to be made explicit
     * in a correct translation to DIG
     */
    protected void translateImplicitClassEquivalences( Element tell, Property p, int classExprType ) {
        StmtIterator i = m_sourceData.listStatements( null, p, (RDFNode) null ); 
        while (i.hasNext()) {
            OntClass cls = (OntClass) i.nextStatement().getSubject().as( OntClass.class );
            
            if (!cls.isAnon()) {
                Element impliesc = addElement( tell, DIGProfile.EQUALC );
                translateClass( impliesc, cls, true );
                
                switch (classExprType) {
                    case UNION:          translateUnionClass( impliesc, cls );        break;
                    case INTERSECTION:   translateIntersectionClass( impliesc, cls ); break;
                    case COMPLEMENT:     translateComplementClass( impliesc, cls );   break;
                    case ENUMERATED:     translateEnumeratedClass( impliesc, cls );   break;
                }
            }
        }
    }
    
    
    protected void translateClassDisjointAxioms( Element tell ) {
        StmtIterator i = m_sourceData.listStatements( null, m_sourceData.getProfile().DISJOINT_WITH(), (RDFNode) null ); 
        while (i.hasNext()) {
            Statement sc = i.nextStatement();
            Element impliesc = addElement( tell, DIGProfile.DISJOINT );
            translateClass( impliesc, sc.getSubject(), true );
            translateClass( impliesc, sc.getResource(), true );
        }
    }
    
    
    /**
     * <p>Translate a given class resource into a DIG concept description, as a child
     * of the given expression element</p>
     * @param expr The parent expression element
     * @param c The concept resource
     * @param allowCAtom If true, named classes will be represented by a &lt;catom&gt; element.
     */
    protected void translateClass( Element expr, Resource c, boolean allowCAtom ) {
        if (c.equals( m_sourceData.getProfile().THING())) {
            // this is TOP in DIG
            addElement( expr, DIGProfile.TOP );
            return;
        }
        else if (c.equals( m_sourceData.getProfile().NOTHING())) {
            // this is BOTTOM in DIG
            addElement( expr, DIGProfile.BOTTOM );
            return;
        }
        
        OntClass cls = (OntClass) c.as( OntClass.class );
        
        if (allowCAtom && !cls.isAnon()) {
            // a named class is represented as a catom element
            Element catom = addElement( expr, DIGProfile.CATOM );
            catom.setAttribute( DIGProfile.NAME, cls.getURI() );
        }
        else if (cls.isComplementClass()) {
            translateComplementClass(expr, cls);
        }
        else if (cls.isIntersectionClass()) {
            translateIntersectionClass(expr, cls);
        }
        else if (cls.isUnionClass()) {
            translateUnionClass(expr, cls);
        }
        else if (cls.isEnumeratedClass()) {
            translateEnumeratedClass(expr, cls);
        }
        else if (cls.isRestriction()) {
            // an anonymous restriction
            Restriction r = cls.asRestriction();
            
            if (r.isAllValuesFromRestriction()) {
                // all values from restriction translates to a DIG <all>R E</all> axiom
                Element all = addElement( expr, DIGProfile.ALL );
                addNamedElement( all, DIGProfile.RATOM, r.getOnProperty().getURI() );
                translateClass( all, r.asAllValuesFromRestriction().getAllValuesFrom(), true );
            }
            else if (r.isSomeValuesFromRestriction()) {
                // some values from restriction translates to a DIG <some>R E</some> axiom
                Element some = addElement( expr, DIGProfile.SOME );
                addNamedElement( some, DIGProfile.RATOM, r.getOnProperty().getURI() );
                translateClass( some, r.asSomeValuesFromRestriction().getSomeValuesFrom(), true );
            }
            else if (r.isHasValueRestriction()) {
                // special case
                translateHasValueRestriction( expr, r.asHasValueRestriction() );
            }
            else if (r.isMinCardinalityRestriction()) {
                // unqualified, so we make the qualification class TOP
                translateCardinalityRestriction( expr, r.asMinCardinalityRestriction().getMinCardinality(), r, 
                                                 DIGProfile.ATLEAST, m_sourceData.getProfile().THING() );
            }
            else if (r.isMaxCardinalityRestriction()) {
                // unqualified, so we make the qualification class TOP
                translateCardinalityRestriction( expr, r.asMaxCardinalityRestriction().getMaxCardinality(), r, 
                                                 DIGProfile.ATMOST, m_sourceData.getProfile().THING() );
            }
            else if (r.isCardinalityRestriction()) {
                // we model a cardinality restriction as the intersection of min and max resrictions
                Element and = addElement( expr, DIGProfile.AND );
                
                // unqualified, so we make the qualification class TOP
                translateCardinalityRestriction( and, r.asCardinalityRestriction().getCardinality(), r, 
                                                 DIGProfile.ATMOST, m_sourceData.getProfile().THING() );
                translateCardinalityRestriction( and, r.asCardinalityRestriction().getCardinality(), r, 
                                                 DIGProfile.ATLEAST, m_sourceData.getProfile().THING() );
            }
            // TODO qualified cardinality restrictions
        }
    }
    
    
    /** Translate an enumerated class to an iset element */
    protected void translateEnumeratedClass(Element expr, OntClass cls) {
        // an anonymous enumeration of class expressions
        Element iset = addElement( expr, DIGProfile.ISET );
        for (Iterator i = cls.asEnumeratedClass().listOneOf(); i.hasNext(); ) {
            addNamedElement( iset, DIGProfile.INDIVIDUAL, ((Resource) i.next()).getURI() );
        }
    }


    /** Translate a complement class to a not element */
    protected void translateComplementClass(Element expr, OntClass cls) {
        // an anonymous complement of another class expression
        Element not = addElement( expr, DIGProfile.NOT );
        translateClass( not, cls.asComplementClass().getOperand(), true );
    }


    /** Translate an intersection class to an and element */
    protected void translateIntersectionClass(Element expr, OntClass cls) {
        // an anonymous intersection of class expressions
        Element or = addElement( expr, DIGProfile.AND );
        translateClassList( or, cls.asIntersectionClass().getOperands() );
    }

 
    /** Translate an union class to an or element */
    protected void translateUnionClass(Element expr, OntClass cls) {
        // an anonymous intersection of class expressions
        Element or = addElement( expr, DIGProfile.OR );
        translateClassList( or, cls.asUnionClass().getOperands() );
    }


    /**
     * <p>Translate a cardinality restriction, with qualification</p>
     * @param parent The parent element
     * @param card The cardinality value
     * @param r The restriction we are translating
     * @param exprName The restriction type (e.g. mincardinality)
     * @param qualType The qualification class
     */
    private void translateCardinalityRestriction( Element parent, int card, Restriction r, String exprName, Resource qualType ) {
        Element restrict = addElement( parent, exprName );
        restrict.setAttribute( DIGProfile.NUM, Integer.toString( card ) );
        addNamedElement( restrict, DIGProfile.RATOM, r.getOnProperty().getURI() );
        translateClass( restrict, qualType, true );
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
    protected void translateClassList( Element expr, RDFList operands ) {
        for (Iterator i = operands.iterator(); i.hasNext(); ) {
            translateClass( expr, (Resource) i.next(), true );
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
            
            if (p.equals( m_sourceData.getProfile().DIFFERENT_FROM())) {
                translateDifferentIndividuals( expr, ind, (Individual) s.getResource().as( Individual.class ) );
            }
            else if (p.equals( m_sourceData.getProfile().SAME_AS())) {
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
            addNamedElement( inst, DIGProfile.INDIVIDUAL, getIndividualID( ind ) );
            addNamedElement( inst, DIGProfile.CATOM, type.getURI() );
        }
    }
    
    
    /** Translate an object property into a DIG related element */
    protected void translateInstanceRole( Element expr, Individual ind, OntProperty p, Individual obj) {
        Element related = addElement( expr, DIGProfile.RELATED );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getIndividualID( ind ) );
        addNamedElement( related, DIGProfile.RATOM, p.getURI() );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getIndividualID( obj ) );
    }


    /** Translate a datatype property into a DIG value element */
    protected void translateInstanceAttrib( Element expr, Individual ind, OntProperty p, Literal obj ) {
        Element related = addElement( expr, DIGProfile.VALUE );
        addNamedElement( related, DIGProfile.INDIVIDUAL, getIndividualID( ind ) );
        addNamedElement( related, DIGProfile.ATTRIBUTE, p.getURI() );
        
        if (isIntegerType( obj.getDatatype() )) {
            Element ival = addElement( related, DIGProfile.IVAL );
            ival.appendChild( expr.getOwnerDocument().createTextNode( obj.getLexicalForm() ) );
        }
        else {
            Element sval = addElement( related, DIGProfile.SVAL );
            sval.appendChild( expr.getOwnerDocument().createTextNode( obj.getLexicalForm() ) );
        }
    }
    
    /** Translate differentFrom(i0, i1) we assert disjoint( iset(i0), iset(i1) ) */
    protected void translateDifferentIndividuals( Element expr, Individual ind, Individual other ) {
        Element disjoint = addElement( expr, DIGProfile.DISJOINT );
        Element iset0 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset0, DIGProfile.INDIVIDUAL, getIndividualID( ind ) );
        Element iset1 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset1, DIGProfile.INDIVIDUAL, getIndividualID( other ) );
    }
    
    
    /** Translate sameAs(i0, i1) we assert equalc( iset(i0), iset(i1) ) */
    protected void translateSameIndividuals( Element expr, Individual ind, Individual other ) {
        Element disjoint = addElement( expr, DIGProfile.EQUALC );
        Element iset0 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset0, DIGProfile.INDIVIDUAL, getIndividualID( ind ) );
        Element iset1 = addElement( disjoint, DIGProfile.ISET );
        addNamedElement( iset1, DIGProfile.INDIVIDUAL, getIndividualID( other ) );
    }
    
    
    /** Translate all of the roles (ObjectProperties) in the KB */
    protected void translateRoles( Element expr ) {
        Set roles = new HashSet();
        collectRoleProperties( roles );
        
        for (Iterator i = roles.iterator(); i.hasNext(); ) {
            translateRole( expr, (ObjectProperty) ((Property) i.next()).as( ObjectProperty.class ) );
        }
    }
    
    /** Translate the various axioms that can apply to roles */
    protected void translateRole( Element expr, ObjectProperty role ) {
        translateBinaryPropertyAxioms( expr, role.getURI(), DIGProfile.IMPLIESR, role.listSuperProperties(), DIGProfile.RATOM );
        translateBinaryPropertyAxioms( expr, role.getURI(), DIGProfile.EQUALR, role.listEquivalentProperties(), DIGProfile.RATOM );
        translateDomainRangeAxioms( expr, role.getURI(), DIGProfile.DOMAIN, role.listDomain(), DIGProfile.RATOM );
        translateDomainRangeAxioms( expr, role.getURI(), DIGProfile.RANGE, role.listRange(), DIGProfile.RATOM );
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
            translateAttribute( expr, (DatatypeProperty) ((Property) i.next()).as( DatatypeProperty.class ) );
        }
    }
    
    /** Attributes (datatype properties) have fewer axiom choices than roles */
    protected void translateAttribute( Element expr, DatatypeProperty attrib ) {
        translateBinaryPropertyAxioms( expr, attrib.getURI(), DIGProfile.IMPLIESR, attrib.listSuperProperties(), DIGProfile.ATTRIBUTE);
        translateBinaryPropertyAxioms( expr, attrib.getURI(), DIGProfile.EQUALR, attrib.listEquivalentProperties(), DIGProfile.ATTRIBUTE );
        translateDomainRangeAxioms( expr, attrib.getURI(), DIGProfile.DOMAIN, attrib.listDomain(), DIGProfile.ATTRIBUTE );
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
    protected void translateDomainRangeAxioms( Element expr, String propURI, String axiomType, Iterator i, String propType ) {
        while (i.hasNext()) {
            Element drAxiom = addElement( expr, axiomType );
            addNamedElement( drAxiom, propType, propURI );
            translateClass( drAxiom, (Resource) i.next(), true );
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
        for (Iterator i = m_sourceData.listAllDifferent(); i.hasNext(); ) {
            AllDifferent ad = (AllDifferent) ((Resource) i.next()).as( AllDifferent.class );
            translateAllDifferent( expr, ad.getDistinctMembers() );
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
        
        return  typeURI != null &&
                (typeURI.equals( XSDDatatype.XSDint.getURI() ) ||
                 typeURI.equals( XSDDatatype.XSDinteger.getURI() ) ||
                 typeURI.equals( XSDDatatype.XSDnonNegativeInteger.getURI() ));
    }
    
    /** Answer a skolem constant, using the given name as a root */
    private String getSkolemName( String root ) {
        return "skolem(" + root + "," + m_skolemCounter++ + ")";
        
    }
    
    
    /** Answer an identifier for an individual, named or bNode */
    private String getIndividualID( Individual ind ) {
        if (ind.isAnon()) {
            return ANON_MARKER + ind.getId().toString();
        }
        else {
            return ind.getURI();
        }
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
    (c) Copyright Hewlett-Packard Company 2002-2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
