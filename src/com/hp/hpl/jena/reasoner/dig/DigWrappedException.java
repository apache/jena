/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            17-Nov-2003
 * Filename           $RCSfile: DigWrappedException.java,v $
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

/**
 * <p>
 * An exception type that wraps a checked exception from the DIG interface as a Jena (runtime)
 * exception.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DigWrappedException.java,v 1.1 2003-11-26 16:36:31 ian_dickinson Exp $)
 */
public class DigWrappedException 
    extends DigReasonerException
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    private Throwable m_ex;
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a DIG exception that wraps a deeper exception from the DIG interface.</p>
     * @param ex An exception or other error to be wrapped
     */
    public DigWrappedException( Throwable ex ) {
        super( "DIG wrapped exception: " + ex.getMessage() );
        m_ex = ex;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the exception that this exception is wrapping.</p>
     * @return The underlying, or wrapped, exception
     */
    public Throwable getWrappedException() {
        return m_ex;
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
@footer@
*/
