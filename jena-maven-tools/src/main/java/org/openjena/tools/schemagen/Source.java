/*****************************************************************************
 * File:    Source.java
 * Project: schemagen
 * Created: 24 Mar 2010
 * By:      ian
 *
 * Copyright (c) 2010-11 Epimorphics Ltd. See LICENSE file for license terms.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;



// Imports
///////////////


/**
 * <p>Simple container object to hold the per-source configuration
 * values from the <code>pom.xml</code>.</p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Source
    extends SchemagenOptions
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/


    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    public String getFileName() {
        return getStringOption( OPT.INPUT );
    }

    /** @parameter expr="config-file" */
    public void setConfigFile( String arg ) {
        setOption( OPT.CONFIG_FILE, arg );
    }

    /** @parameter expr="no-comments" */
    public void setNoComments( String arg ) {
        setOption( OPT.NO_COMMENTS, trueArg( arg ) );
    }

    /** @parameter expr="input" */
    public void setInput( String arg ) {
        setOption( OPT.INPUT, arg );
    }

    /** @parameter expr="lang-daml" */
    public void setLangDaml( String arg ) {
        setOption( OPT.LANG_DAML, trueArg( arg ) );
    }

    /** @parameter expr="lang-owl" */
    public void setLangOwl( String arg ) {
        setOption( OPT.LANG_OWL, trueArg( arg ) );
    }

    /** @parameter expr="lang-rdfs" */
    public void setLangRdfs( String arg ) {
        setOption( OPT.LANG_RDFS, trueArg( arg ) );
    }

    /** @parameter expr="output" */
    public void setOutput( String arg ) {
        setOption( OPT.OUTPUT, arg );
    }

    /** @parameter expr="header" */
    public void setHeader( String arg ) {
        setOption( OPT.HEADER, arg );
    }

    /** @parameter expr="footer" */
    public void setFooter( String arg ) {
        setOption( OPT.FOOTER, arg );
    }

    /** @parameter expr="root" */
    public void setRoot( String arg ) {
        setOption( OPT.ROOT, arg );
    }

    /** @parameter expr="marker" */
    public void setMarker( String arg ) {
        setOption( OPT.MARKER, arg );
    }

    /** @parameter expr="package-name" */
    public void setPackageName( String arg ) {
        setOption( OPT.PACKAGENAME, arg );
    }

    /** @parameter expr="ontology" */
    public void setOntology( String arg ) {
        setOption( OPT.ONTOLOGY, trueArg( arg ) );
    }

    /** @parameter expr="classname" */
    public void setClassName( String arg ) {
        setOption( OPT.CLASSNAME, arg );
    }

    /** @parameter expr="classdec" */
    public void setClassDec( String arg ) {
        setOption( OPT.CLASSDEC, arg );
    }

    /** @parameter expr="namespace" */
    public void setNamespace( String arg ) {
        setOption( OPT.NAMESPACE, arg );
    }

    /** @parameter expr="declarations" */
    public void setDeclarations( String arg ) {
        setOption( OPT.DECLARATIONS, arg );
    }

    /** @parameter expr="property-section" */
    public void setPropertySection( String arg ) {
        setOption( OPT.PROPERTY_SECTION, arg );
    }

    /** @parameter expr="class-section" */
    public void setClassSection( String arg ) {
        setOption( OPT.CLASS_SECTION, arg );
    }

    /** @parameter expr="individuals-section" */
    public void setIndividualsSection( String arg ) {
        setOption( OPT.INDIVIDUALS_SECTION, arg );
    }

    /** @parameter expr="noproperties" */
    public void setNoProperties( String arg ) {
        setOption( OPT.NOPROPERTIES, trueArg( arg ) );
    }

    /** @parameter expr="noclasses" */
    public void setNoClasses( String arg ) {
        setOption( OPT.NOCLASSES, trueArg( arg ) );
    }

    /** @parameter expr="noindividuals" */
    public void setNoIndividuals( String arg ) {
        setOption( OPT.NOINDIVIDUALS, trueArg( arg ) );
    }

    /** @parameter expr="noheader" */
    public void setNoHeader( String arg ) {
        setOption( OPT.NOHEADER, trueArg( arg ) );
    }

    /** @parameter expr="prop-template" */
    public void setPropTemplate( String arg ) {
        setOption( OPT.PROP_TEMPLATE, arg );
    }

    /** @parameter expr="classttemplate" */
    public void setClassTemplate( String arg ) {
        setOption( OPT.CLASS_TEMPLATE, arg );
    }

    /** @parameter expr="individualttemplate" */
    public void setIndividualTemplate( String arg ) {
        setOption( OPT.INDIVIDUAL_TEMPLATE, arg );
    }

    /** @parameter expr="uc-names" */
    public void setUcNames( String arg ) {
        setOption( OPT.UC_NAMES, trueArg( arg ) );
    }

    /** @parameter expr="include" */
    public void setInclude( String arg ) {
        setOption( OPT.INCLUDE, arg );
    }

    /** @parameter expr="classname-suffix" */
    public void setClassNameSuffix( String arg ) {
        setOption( OPT.CLASSNAME_SUFFIX, arg );
    }

    /** @parameter expr="encoding" */
    public void setEncoding( String arg ) {
        setOption( OPT.ENCODING, arg );
    }

    /** @parameter expr="help" */
    public void setHelp( String arg ) {
        setOption( OPT.HELP, trueArg( arg ) );
    }

    /** @parameter expr="dos" */
    public void setDos( String arg ) {
        setOption( OPT.DOS, trueArg( arg ) );
    }

    /** @parameter expr="use-inf" */
    public void setUseInf( String arg ) {
        setOption( OPT.USE_INF, trueArg( arg ) );
    }

    /** @parameter expr="strict-individuals" */
    public void setStrictIndividuals( String arg ) {
        setOption( OPT.STRICT_INDIVIDUALS, trueArg( arg ) );
    }

    /** @parameter expr="include-source" */
    public void setIncludeSource( String arg ) {
        setOption( OPT.INCLUDE_SOURCE, trueArg( arg ) );
    }

    /** @parameter expr="no-strict" */
    public void setNoStrict( String arg ) {
        setOption( OPT.NO_STRICT, trueArg( arg ) );
    }


    /**
     * Return true if this source actually represents the default options
     * element
     *
     * @return True for the default options
     */
    public boolean isDefaultOptions() {
        return getFileName().equals( SchemagenMojo.DEFAULT_OPTIONS_ELEM );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    private boolean trueArg( String arg ) {
        return !"false".equals( arg );
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

