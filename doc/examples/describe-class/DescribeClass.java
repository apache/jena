/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Jul-2003
 * Filename           $RCSfile: DescribeClass.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-23 00:10:40 $
 *               by   $Author: ian_dickinson $
 *
 *****************************************************************************/

// Package
///////////////

// Imports
///////////////
import java.io.PrintStream;
import java.util.*;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;



/**
 * <p>
 * Simple example of describing the basic attributes of a OWL, DAML or RDFS class
 * using the ontology API.  This is not meant as a definitive solution to the problem,
 * but as an illustration of one approach to solving the problem. This example should
 * be adapted as necessary to provide a given application with the means to render
 * a class description in a readable form. 
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DescribeClass.java,v 1.1 2003-08-23 00:10:40 ian_dickinson Exp $
 */
public class DescribeClass {
    // Constants
    //////////////////////////////////



    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    private Map m_anonIDs = new HashMap();
    private int m_anonCount = 0;
    
    
    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Describe the given ontology class in texttual form. The description
     * produced has the following form (approximately):
     * <pre>
     * Class foo:Bar 
     *    is a sub-class of foo:A, ex:B
     *    is a super-class of ex:C
     * </pre>
     * </p>
     * 
     * @param out The print stream to write the description to
     * @param cls The ontology class to describe
     */
    public void describeClass( PrintStream out, OntClass cls ) {
        renderClassDescription( out, cls );
        out.println();
        
        // sub-classes
        for (Iterator i = cls.listSuperClasses( true ); i.hasNext(); ) {
            out.print( "  is a sub-class of " );
            renderClassDescription( out, (OntClass) i.next() );
            out.println();
        }

        // super-classes
        for (Iterator i = cls.listSubClasses( true ); i.hasNext(); ) {
            out.print( "  is a super-class of " );
            renderClassDescription( out, (OntClass) i.next() );
            out.println();
        }
    }

    /**
     * <p>Render a description of the given class to the given output stream.</p>
     * @param out A print stream to write to
     * @param c The class to render
     */
    public void renderClassDescription( PrintStream out, OntClass c ) {
        if (c.isRestriction()) {
            renderRestriction( out, (Restriction) c.as( Restriction.class ) );
        }
        else {
            if (!c.isAnon()) {
                out.print( "Class " );
                renderURI( out, prefixesFor( c ), c.getURI() );
                out.print( ' ' );
            }
            else {
                renderAnonymous( out, c, "class" );
            }
        }
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Handle the case of rendering a restriction.</p>
     * @param out The print stream to write to
     * @param r The restriction to render
     */
    protected void renderRestriction( PrintStream out, Restriction r ) {
        if (!r.isAnon()) {
            out.print( "Restriction " );
            renderURI( out, prefixesFor( r ), r.getURI() );
        }
        else {
            renderAnonymous( out, r, "restriction" );
        }
        
        out.print( ' ' );
        
        renderRestrictionElem( out, "on property", r.getOnProperty() );

        if (r.isAllValuesFromRestriction()) {
            renderRestrictionElem( out, "all values from", r.asAllValuesFromRestriction().getAllValuesFrom() );        
        }
        if (r.isSomeValuesFromRestriction()) {
            renderRestrictionElem( out, "some values from", r.asSomeValuesFromRestriction().getSomeValuesFrom() );        
        }
        if (r.isHasValueRestriction()) {
            renderRestrictionElem( out, "has value", r.asHasValueRestriction().getHasValue() );        
        }
    }

    protected void renderRestrictionElem( PrintStream out, String desc, RDFNode value ) {
        out.print( desc );
        out.print( " " );
        renderValue( out, value );
        out.print( ",  " );
    }

    protected void renderValue( PrintStream out, RDFNode value ) {
        if (value instanceof Resource) {
            Resource r = (Resource) value;
            if (r.isAnon()) {
                renderAnonymous( out, r, "resource" );
            }
            else {
                renderURI( out, r.getModel(), r.getURI() ); 
            }
        }
        else {
            out.print( value );
        }
    }

    protected void renderURI( PrintStream out, PrefixMapping prefixes, String uri ) {
        out.print( prefixes.usePrefix( uri ) );
    }
    
    protected PrefixMapping prefixesFor( Resource n ) {
        return n.getModel().getGraph().getPrefixMapping();
    }
    
    protected void renderAnonymous( PrintStream out, Resource anon, String name ) {
        String anonID = (String) m_anonIDs.get( anon.getId() );
        if (anonID == null) {
            anonID = "a-" + m_anonCount++;
            m_anonIDs.put( anon.getId(), anonID );
        }
        
        out.print( "Anonymous ");
        out.print( name );
        out.print( " with ID " );
        out.print( anonID );
    }
        
    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

