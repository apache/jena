/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            17-Nov-2003
 * Filename           $RCSfile: DigReasonerException.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-11-26 16:36:31 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import com.hp.hpl.jena.shared.JenaException;


/**
 * <p>
 * Exception indicating that a problem has arisen in the interface to the DIG
 * reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DigReasonerException.java,v 1.1 2003-11-26 16:36:31 ian_dickinson Exp $)
 */
public class DigReasonerException 
    extends JenaException
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /** 
     * Create a new exception with the given message 
     */
    public DigReasonerException( String message ) {
        super( message );
    }
    
    
    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
@footer@
*/
