/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            17-Nov-2003
 * Filename           $RCSfile: DIGProfile.java,v $
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
 * Encapsulates the multiple extant versions of the DIG protocol, which have 
 * different expectations as to namespaces, XML encodings, and other variables. This
 * allows us to parameterise the DIG interface to different DIG enabled tools.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGProfile.java,v 1.1 2003-11-26 16:36:31 ian_dickinson Exp $)
 */
public interface DIGProfile 
{
    // Constants
    //////////////////////////////////

    /* DIG verbs and other constants */
    
    /** The DIG verb to get the identification information on the reasoner */
    public static final String GET_IDENTIFIER = "getIdentifier";

    /** The DIG attribute denoting the version string of a reasoner */
    public static final String VERSION = "version";
    
    /** The DIG attribute denoting the version message string of a reasoner */
    public static final String MESSAGE = "message";
    
    /** The DIG element denoting the capabilities of the reasoner */
    public static final String SUPPORTS = "supports";
    
    /** The DIG element denoting the language capabilities of the reasoner */
    public static final String LANGUAGE = "language";
    
    /** The DIG element denoting the ask capabilities of the reasoner */
    public static final String ASK = "ask";
    
    /** The DIG element denoting the tell capabilities of the reasoner */
    public static final String TELL = "tell";
    
    // tell language
    
    public static final String DEFCONCEPT       = "defconcept";
    public static final String DEFROLE          = "defrole";
    public static final String DEFFEATURE       = "deffeature";
    public static final String DEFATTRIBUTE     = "defattribute";
    public static final String DEFINDIVIDUAL    = "defindividual";
    public static final String IMPLIESC         = "impliesc";
    public static final String EQUALC           = "equalc";
    public static final String DISJOINT         = "disjoint";
    public static final String IMPLIESR         = "impliesr";
    public static final String EQUALR           = "equalr";
    public static final String DOMAIN           = "domain";
    public static final String RANGE            = "range";
    public static final String RANGEINT         = "rangeint";
    public static final String RANGESTRING      = "rangestring";
    public static final String TRANSITIVE       = "transitive";
    public static final String FUNCTIONAL       = "functional";
    public static final String INSTANCEOF       = "instanceof";
    public static final String RELATED          = "related";
    public static final String VALUE            = "value";
   
    // concept language
    
    public static final String TOP              = "top";
    public static final String BOTTOM           = "bottom";
    public static final String CATOM            = "catom";
    public static final String AND              = "and";
    public static final String OR               = "or";
    public static final String NOT              = "not";
    public static final String SOME             = "some";
    public static final String ALL              = "all";
    public static final String ATMOST           = "atmost";
    public static final String ATLEAST          = "atleast";
    public static final String ISET             = "iset";
    public static final String DEFINED          = "defined";
    public static final String STRINGMIN        = "stringmin";
    public static final String STRINGMAX        = "stringmax";
    public static final String STRINGEQUALS     = "stringequals";
    public static final String STRINGRANGE      = "stringrange";
    public static final String INTMIN           = "intmin";
    public static final String INTMAX           = "intmax";
    public static final String INTEQUALS        = "intequals";
    public static final String INTRANGE         = "intrange";
    public static final String RATOM            = "ratom";
    public static final String FEATURE          = "feature";
    public static final String INVERSE          = "inverse";
    public static final String ATTRIBUTE        = "attribute";
    public static final String CHAIN            = "chain";
    public static final String INDIVIDUAL       = "individual";
    public static final String NUM              = "num";
    public static final String IVAL             = "ival";
    public static final String SVAL             = "sval";
    
    
    // attributes
    
    public static final String NAME             = "name";
    public static final String VAL              = "val";
    public static final String MIN              = "min";
    public static final String MAX              = "max";
    
    
    // External signature methods
    //////////////////////////////////

    /** Answer the root namespace for this version of the DIG protocol */
    public String getDIGNamespace();
    
    /** Answer the location of the DIG schema for this version of the DIG protocol */
    public String getSchemaLocation();
    
    /** Answer the HTTP Content-Type of a DIG request (e.g. text/xml) */
    public String getContentType();
}


/*
@footer@
*/
