/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tools.schemagen;


// Imports
///////////////

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import jena.schemagen.SchemagenOptions.OPT;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <p>This test checks basic coverage of the options from schemagen: if more options are added,
 * without updating the option setters, this test should give a compile warning in @{@link #setParamValue(Source)}</p>
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
        Collection<Object[]> params = new ArrayList<>();

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
     * @throws SchemagenOptionsConfigurationException
     */
    @Test
    public void testGetOption() throws SchemagenOptionsConfigurationException {
        Source s = new Source();
        setParamValue( s );
        SchemagenOptions so = new SchemagenOptions(null, s);
        assertEquals( optionName, expected, so.getOption( option ).asLiteral().getValue() );
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
                
            case DATATYPES_SECTION:
                s.setDatatypesSection( optionName );

                expected = optionName;
                break;

            case DATATYPE_TEMPLATE:
                s.setDatatypeTemplate( optionName );

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
                s.setDos( true );

                expected = true;
                break;

            case HELP:
                s.setHelp( true );

                expected = true;
                break;

            case INCLUDE_SOURCE:
                s.setIncludeSource( true );

                expected = true;
                break;

            case LANG_DAML:
                s.setLangDaml( true );

                expected = true;
                break;

            case LANG_OWL:
                s.setLangOwl( true );

                expected = true;
                break;

            case LANG_RDFS:
                s.setLangRdfs( true );

                expected = true;
                break;

            case NOCLASSES:
                s.setNoClasses( true );

                expected = true;
                break;
                
            case NODATATYPES:
                s.setNoDatatypes( true );

                expected = true;
                break;
            case NOHEADER:
                s.setNoHeader( true );

                expected = true;
                break;

            case NOINDIVIDUALS:
                s.setNoIndividuals( true );

                expected = true;
                break;

            case NOPROPERTIES:
                s.setNoProperties( true );

                expected = true;
                break;

            case NO_COMMENTS:
                s.setNoComments( true );

                expected = true;
                break;

            case NO_STRICT:
                s.setNoStrict( true );

                expected = true;
                break;

            case ONTOLOGY:
                s.setOntology( true );

                expected = true;
                break;

            case STRICT_INDIVIDUALS:
                s.setStrictIndividuals( true );

                expected = true;
                break;

            case UC_NAMES:
                s.setUcNames( true );

                expected = true;
                break;

            case USE_INF:
                s.setUseInf( true );

                expected = true;
                break;

        }
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

