/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            17-Nov-2003
 * Filename           $RCSfile: DigIdentifier.java,v $
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
import java.util.Iterator;


/**
 * <p>
 * A structure that presents identification information about the attached DIG reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DigIdentifier.java,v 1.1 2003-11-26 16:36:31 ian_dickinson Exp $)
 */
public interface DigIdentifier 
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the name of the attached reasoner, as a string.</p>
     * @return The name of the DIG reasoner.
     */
    public String getName();
    
    /**
     * <p>Answer the version string of the attached reasoner.</p>
     * @return The version string for the reasoner.
     */
    public String getVersion();
    
    /**
     * <p>Answer the message string from the DIG identifier element.</p>
     * @return The identification message
     */
    public String getMessage();
    
    /**
     * <p>Answer an iterator over the language elements that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG language
     * term that the attached reasoner supports.
     */
    public Iterator supportsLanguage();
    
    /**
     * <p>Answer an iterator over the TELL verbs that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG TELL
     * verb that the attached reasoner supports.
     */
    public Iterator supportsTell();
    
    /**
     * <p>Answer an iterator over the ASK verbs that this reasoner supports.</p>
     * @return An iterator, each element of which is a string denoting a DIG ASK
     * verb that the attached reasoner supports.
     */
    public Iterator supportsAsk();
}


/*
@footer@
*/
