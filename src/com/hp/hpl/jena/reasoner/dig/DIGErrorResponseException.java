/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            28-Nov-2003
 * Filename           $RCSfile: DIGErrorResponseException.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-01 22:40:07 $
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
 * An exception that encapsulates an error response from the DIG server, including 
 * error number and message.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGErrorResponseException.java,v 1.1 2003-12-01 22:40:07 ian_dickinson Exp $)
 */
public class DIGErrorResponseException 
    extends DIGReasonerException
{
    // Constants
    //////////////////////////////////

    public static final int GENERAL_UNSPECIFIED_ERROR = 100;
    public static final int UNKNOWN_REQUEST = 101;
    public static final int MAFORMED_REQUEST = 102;
    public static final int UNSUPPORTED_OPERATION = 103;
    
    public static final int CANNOT_CREATE_NEW_KB = 201;
    public static final int MALFORMED_KB_URI = 202;
    public static final int UNKNOWN_OR_STALE_KB_URI = 203;
    public static final int KB_RELEASE_ERROR = 204;
    public static final int MISSING_URI = 205;
    
    public static final int GENERAL_TELL_ERROR = 301;
    public static final int UNSUPPORTED_TELL_OPERATION = 302;
    public static final int UNKNOWN_TELL_OPERATION = 303;

    public static final int GENERAL_ASK_ERROR = 401;
    public static final int UNSUPPORTED_ASK_OPERATION = 402;
    public static final int UNKNOWN_ASK_OPERATION = 403;
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The msg attribute from the DIG error message */
    private String m_msgAttr;
    
    /** The DIG error code */
    private int m_errorCode;
    
    
    // Constructors
    //////////////////////////////////

    public DIGErrorResponseException( String msg, String msgAttr, int errorCode ) {
        super( "DIG error: " + msg );
        m_msgAttr = msgAttr;
        m_errorCode = errorCode;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the error code sent back by DIG.  Well known error codes are listed
     * as constants exported from this class.</p>
     * @return The DIG error code; the value of the <code>code</code> attribute in
     * the error response returned by the reasoner.
     */
    public int getErrorCode() {
        return m_errorCode;
    }
    
    
    /**
     * <p>Answer the error message sent back by DIG.</p>
     * @return The DIG error message; the value of the <code>msg</code> attribute
     * in the error response returned by the reasoner
     */
    public String getMessage() {
        return m_msgAttr;
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
