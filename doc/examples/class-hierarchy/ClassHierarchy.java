/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-Mar-2003
 * Filename           $RCSfile: ClassHierarchy.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-23 00:10:57 $
 *               by   $Author: ian_dickinson $
 *
 *****************************************************************************/

// Package
///////////////


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.io.PrintStream;
import java.util.*;


/**
 * <p>
 * Simple demonstration program to show how to list a hierarchy of classes
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: ClassHierarchy.java,v 1.1 2003-08-23 00:10:57 ian_dickinson Exp $
 */
public class ClassHierarchy {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    protected OntModel m_model;
    private Map m_anonIDs = new HashMap();
    private int m_anonCount = 0;
    


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /** Show the sub-class hierarchy encoded by the given model */
    public void showHierarchy( PrintStream out, OntModel m ) {
        for (Iterator i = rootClasses( m );  i.hasNext();  ) {
            showClass( out, (OntClass) i.next(), new ArrayList(), 0 );
        }
    }


    // Internal implementation methods
    //////////////////////////////////

    /** Present a class, then recurse down to the sub-classes.
     *  Use occurs check to prevent getting stuck in a loop
     */
    protected void showClass( PrintStream out, OntClass cls, List occurs, int depth ) {
        renderClassDescription( out, cls, depth );
        out.println();

        // recurse to the next level down
        if (cls.canAs( OntClass.class )  &&  !occurs.contains( cls )) {
            for (Iterator i = cls.listSubClasses( true );  i.hasNext(); ) {
                OntClass sub = (OntClass) i.next();

                // we push this expression on the occurs list before we recurse
                occurs.add( cls );
                showClass( out, sub, occurs, depth + 1 );
                occurs.remove( cls );
            }
        }
    }


    /**
     * <p>Render a description of the given class to the given output stream.</p>
     * @param out A print stream to write to
     * @param c The class to render
     */
    public void renderClassDescription( PrintStream out, OntClass c, int depth ) {
        indent( out, depth );
        
        if (c.isRestriction()) {
            renderRestriction( out, (Restriction) c.as( Restriction.class ) );
        }
        else {
            if (!c.isAnon()) {
                out.print( "Class " );
                renderURI( out, c.getModel(), c.getURI() );
                out.print( ' ' );
            }
            else {
                renderAnonymous( out, c, "class" );
            }
        }
    }

    /**
     * <p>Handle the case of rendering a restriction.</p>
     * @param out The print stream to write to
     * @param r The restriction to render
     */
    protected void renderRestriction( PrintStream out, Restriction r ) {
        if (!r.isAnon()) {
            out.print( "Restriction " );
            renderURI( out, r.getModel(), r.getURI() );
        }
        else {
            renderAnonymous( out, r, "restriction" );
        }
        
        out.print( " on property " );
        renderURI( out, r.getModel(), r.getOnProperty().getURI() );
    }
    
    /** Render a URI */
    protected void renderURI( PrintStream out, PrefixMapping prefixes, String uri ) {
        out.print( prefixes.usePrefix( uri ) );
    }
    
    /** Render an anonymous class or restriction */
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
    
    /** Generate the indentation */
    protected void indent( PrintStream out, int depth ) {
        for (int i = 0;  i < depth; i++) {
            out.print( "  " );
        }
    }

    /**
     * Answer an iterator over the classes we will use as the roots of the depicted
     * hierarchy.  We use named classes that either have Thing as a direct super-class,
     * or which have no declared super-classes.  The first condition is helpful if
     * using a reasoner, the second otherwise.
     * @param m A model
     * @return An iterator over the named class hierarchy roots in m
     */
    protected Iterator rootClasses( OntModel m ) {
        List roots = new ArrayList();
        
        for (Iterator i = m.listClasses();  i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            
            // too confusing to list all the restrictions as root classes 
            if (c.isAnon()) {
                continue;
            }
            
            if (c.hasSuperClass( m.getProfile().THING(), true ) ) {
                // this class is directly descended from Thing
                roots.add( c );
            }
            else if (c.getCardinality( m.getProfile().SUB_CLASS_OF() ) == 0 ) {
                // this class has no super-classes (can occur if we're not using the reasoner)
                roots.add( c );
            }
        }
        
        return roots.iterator();
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
