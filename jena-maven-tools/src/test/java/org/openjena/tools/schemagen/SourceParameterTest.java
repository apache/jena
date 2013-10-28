/*****************************************************************************
 * File:    SourceTest.java
 * Project: schemagen
 * Created: 10 May 2010
 * By:      ian
 *
 * Copyright (c) 2010-11 Epimorphics Ltd. See LICENSE file for license terms.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;


// Imports
///////////////

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import jena.schemagen.SchemagenOptions.OPT;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This test checks basic coverage of the options from schemagen: if more options are added,
 * without updating the option setters, this test should give a compile warning in @{@link #setParamValue(Source)}</p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
@RunWith( Parameterized.class )
public class SourceParameterTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Test parameters are formed from the schemagen options
     **/
    @Parameters
    public static Collection<Object[]> testParameters() {
        Collection<Object[]> params = new ArrayList<Object[]>();

        for (OPT opt: OPT.values()) {
            Object[] par = new Object[2];
            par[0] = opt;
            par[1] = opt.name();

            params.add( par );
        }

        return params;
    }


    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( SourceParameterTest.class );

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        //
    }

    /***********************************/
    /* Instance variables              */
    /***********************************/

    private OPT option;
    private String optionName;
    private Object expected;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public SourceParameterTest( OPT paramVal, String paramName ) {
        option = paramVal;
        optionName = paramName;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Test method for {@link org.openjena.tools.schemagen.Source#getFileName()}.
     */
    @Test
    public void testGetOption() {
        Source s = new Source();
        setParamValue( s );
        assertEquals( optionName, expected, s.getOption( option ).asLiteral().getValue() );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    protected void setParamValue( Source s ) {
        switch (option) {
            case INPUT:
                s.setInput( optionName );

                expected = optionName;
                break;

            case CLASS_SECTION:
                s.setClassSection( optionName );

                expected = optionName;
                break;

            case CLASSDEC:
                s.setClassDec( optionName );

                expected = optionName;
                break;

            case CLASSNAME:
                s.setClassName( optionName );

                expected = optionName;
                break;

            case CLASSNAME_SUFFIX:
                s.setClassNameSuffix( optionName );

                expected = optionName;
                break;

            case CLASS_TEMPLATE:
                s.setClassTemplate( optionName );

                expected = optionName;
                break;

            case CONFIG_FILE:
                s.setConfigFile( optionName );

                expected = optionName;
                break;

            case DECLARATIONS:
                s.setDeclarations( optionName );

                expected = optionName;
                break;

            case ENCODING:
                s.setEncoding( optionName );

                expected = optionName;
                break;

            case FOOTER:
                s.setFooter( optionName );

                expected = optionName;
                break;

            case HEADER:
                s.setHeader( optionName );

                expected = optionName;
                break;

            case INCLUDE:
                s.setInclude( optionName );

                expected = optionName;
                break;

            case INDIVIDUALS_SECTION:
                s.setIndividualsSection( optionName );

                expected = optionName;
                break;

            case INDIVIDUAL_TEMPLATE:
                s.setIndividualTemplate( optionName );

                expected = optionName;
                break;

            case MARKER:
                s.setMarker( optionName );

                expected = optionName;
                break;

            case NAMESPACE:
                s.setNamespace( optionName );

                expected = optionName;
                break;

            case OUTPUT:
                s.setOutput( optionName );

                expected = optionName;
                break;

            case PACKAGENAME:
                s.setPackageName( optionName );

                expected = optionName;
                break;

            case PROPERTY_SECTION:
                s.setPropertySection( optionName );

                expected = optionName;
                break;

            case PROP_TEMPLATE:
                s.setPropTemplate( optionName );

                expected = optionName;
                break;

            case ROOT:
                s.setRoot( optionName );

                expected = optionName;
                break;

            // Boolean options
            case DOS:
                s.setDos( optionName );

                expected = true;
                break;

            case HELP:
                s.setHelp( optionName );

                expected = true;
                break;

            case INCLUDE_SOURCE:
                s.setIncludeSource( optionName );

                expected = true;
                break;

            case LANG_DAML:
                s.setLangDaml( optionName );

                expected = true;
                break;

            case LANG_OWL:
                s.setLangOwl( optionName );

                expected = true;
                break;

            case LANG_RDFS:
                s.setLangRdfs( optionName );

                expected = true;
                break;

            case NOCLASSES:
                s.setNoClasses( optionName );

                expected = true;
                break;

            case NOHEADER:
                s.setNoHeader( optionName );

                expected = true;
                break;

            case NOINDIVIDUALS:
                s.setNoIndividuals( optionName );

                expected = true;
                break;

            case NOPROPERTIES:
                s.setNoProperties( optionName );

                expected = true;
                break;

            case NO_COMMENTS:
                s.setNoComments( optionName );

                expected = true;
                break;

            case NO_STRICT:
                s.setNoStrict( optionName );

                expected = true;
                break;

            case ONTOLOGY:
                s.setOntology( optionName );

                expected = true;
                break;

            case STRICT_INDIVIDUALS:
                s.setStrictIndividuals( optionName );

                expected = true;
                break;

            case UC_NAMES:
                s.setUcNames( optionName );

                expected = true;
                break;

            case USE_INF:
                s.setUseInf( optionName );

                expected = true;
                break;

        }
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

