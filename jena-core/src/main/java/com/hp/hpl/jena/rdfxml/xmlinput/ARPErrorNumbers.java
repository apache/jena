/*
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

/*
 * ARPErrorNumbers.java
 *
 * Created on July 10, 2001, 8:34 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput;

/**
 * Error numbers used by ARP.
 */
public interface ARPErrorNumbers {
    /** Used as ErrorMode to not report an error.
     * @see ARPOptions#setErrorMode
     * 
     *
     */
    public int EM_IGNORE = 0;
    /** Used as ErrorMode to report warning and continue processing.
     * @see ARPOptions#setErrorMode
     */
    public int EM_WARNING = 1;
    /** Used as ErrorMode to report error, and not generate associated triples.
     * 
     * In the event of an error (i.e. a condition with this error mode), 
     * no further triples involving the resources and literals associated with the error are created. 
     * The precise definition of 'associated with' is deliberately 
     * undefined, and may change in future releases.
     * 
     * When the file includes.error error conditions the parsing is aborted immediately after such an error.
Otherwise, it is possible to see all the triples, including those involving resources and literals associated with any condition, by ensuring that the error mode of every error code is WARNING or IGNORE. (i.e. ARP optionally permits all errors to be downgraded to warnings, or to be ignored).
In this case, the precise rules which ARP uses to generate triples for ill-formed input are not defined by any standard and are subject to change with future releases.
For input involving no errors, ARP creates triples in accordance with the RDF/XML Syntax Revised Recommendation. 
     * @see ARPOptions#setErrorMode
     */
    public int EM_ERROR = 2;
    /** Used as ErrorMode to stop processing after reporting error.
     * @see ARPOptions#setErrorMode
     */
    public int EM_FATAL = 3;

    // All error numbers must be between 1 and 399.
    // Divide by 100 to get class.
    // Don't see->@see : some javadoc production then generates warning (incorrectly)
    /**
     An xml:lang attribute uses one or more of the extension
     facilities in RFC3066 or ISO639. 
     *In some way, the language specified is non-standard.
     *
     *In both default and strict modes this is ignored; a conservative application
     *in verbose mode may wish to raise a warning.
     * (W001)
     * see LanguageTagCodes#LT_PRIVATE_USE
     * see LanguageTagCodes#LT_LOCAL_USE
     * see LanguageTagCodes#LT_EXTRA
     */

    public int IGN_PRIVATE_XMLLANG = 1;

    /**
     Indicates that somewhere, other than in an XML Literal
     an xml:base attribute has been used.
     This is ignored in default and strict mode. (W002)
     @see #IGN_XMLBASE_SIGNIFICANT
     */
    public int IGN_XMLBASE_USED = 2;
    /**
     Indicates that somewhere, 
     an xml:base attribute has been used and
     changes the interpretation of some URI (either through a
     relative URI-reference or rdf:ID). (W003)
     This is ignored in default and strict mode.
     @see #IGN_XMLBASE_USED
     */
    public int IGN_XMLBASE_SIGNIFICANT = 3;
    
	/**
	 Indicates that no name is known for the current file being parsed.
	 * (W005)
	 */

	public int IGN_NO_BASE_URI_SPECIFIED = 5;


    /**
     
     * (W100)
     * @deprecated {@link #IGN_NO_BASE_URI_SPECIFIED}
     */

    @Deprecated
    public int WARN_XMLBASE_MISSING = 100;
    /**
     A standard rdf attribute like type or about is used 
     without a namespace qualifier. In default and strict mode ARP adds the
     *rdf: qualifier and reports a warning. (W101).
    */
    public int WARN_UNQUALIFIED_RDF_ATTRIBUTE = 101;
    /**
     *Some attribute that is not an RDF keyword is used in an 
       unqualified fashion. In default mode,  then the namespace of
       the enclosing element
       is used. 
       In strict mode this is an error. (W102).
   
     *
     */
    public int WARN_UNQUALIFIED_ATTRIBUTE = 102;
    /**
    An attribute name in the RDF namespace has been 
     used that is not a reserved RDF attribute name.
     *In default and strict modes, a statement is generated with the given name as property.
     * In default and strict modes this is a warning. (W103).
     **/
    public int WARN_UNKNOWN_RDF_ATTRIBUTE = 103;
    /**
    An element tag is not a qualified name. 
     In default mode, a resource or property is generated with a malformed URI.
     * Strict mode treats this as an error. (W104).
    */
    public int WARN_UNQUALIFIED_ELEMENT = 104;

    /**
     *The same name has been used for more than one rdf:ID or rdf:bagID,
     * in the context of the same xml:base (if any). 
      The default mode allows this with a warning. 
     This check is expensive in memory. When processing very large files,
     it may be sensible to switch the check off by using
     {@link ARPOptions#setErrorMode(int,int)} to ignore this condition.
     * (W105).
     @see #WARN_LEGAL_REUSE_OF_ID
    
     */

    public int WARN_REDEFINITION_OF_ID = 105;
    /**
     An unrecognised value for rdf:parseType has been found. 
     In strict mode, this error is ignored, and it is treated 
     as rdf:parseType="Literal", in default mode a warning is issued.
     * (W106)
    */
    public int WARN_UNKNOWN_PARSETYPE = 106;
    /**
     *A URI reference does not conform to the definition of RDF URI Reference. 
       Use Exception.getMessage() for details. 
       In default mode, the malformed URI is passed to the RDF 
       processing application; strict mode treats this as an error.
     * (W107)
    
     *
     */
    public int WARN_MALFORMED_URI = 107;
    /**
     *An ID symbol or other grammar production that should be an 
       XML name is malformed. In default mode, 
      the malformed string is passed to the RDF application. (W108)
     *Strict mode treats this as an error.
     *
     */
    public int WARN_BAD_NAME = 108;
    /**
     *A namespace has been declared with a relative URI.
      Such relative URI namespaces have been 
       <a href="http://www.w3.org/2000/09/xppa">deprecated</a>. 
       This often results in related {@link #WARN_RELATIVE_URI}
       warnings.
       (W109)
     *
     */
    public int WARN_RELATIVE_NAMESPACE_URI_DEPRECATED = 109;
   
    //   public int WARN_EMPTY_ABOUT_EACH                        =110;
    /**
     *No longer used.
     *(W111)
     * @deprecated
    
     */

    @Deprecated
    public int WARN_BAD_XML = 111;
    /**
     *
     *Should not happen. 
     *(W112)
     *@deprecated No longer used.
     */
    @Deprecated
    public int WARN_MINOR_INTERNAL_ERROR = 112;
    /**
     *An element is tagged rdf:XXX where XXX is not a recognised RDF element name. 
     *The typed node or property element construction is matched.
     *In both default and strict modes this is a warning. (W113).
     */
    public int WARN_UNKNOWN_RDF_ELEMENT = 113;
    /**
     * rdf:_NNN is being used in the typed node construction. 
     * 
     * In default mode this is a warning; in strict mode it is ignored.(W114)
     */
    public int WARN_RDF_NN_AS_TYPE = 114;
    /**
     * The value of an xml:lang attribute does not conform to the
       syntactic rules of RFC3066.
       In default mode this is a warning, in strict mode an error. (W115)
     */
    public int WARN_MALFORMED_XMLLANG = 115;
    /**
     * The value of an xml:lang attribute while syntactically conforming
       to RFC3066 does not conform with other rules, possibly through not
       being listed in ISO639 or ISO3166 or the IANA language tag
       registry. The databases used of these registries was a snapshot of
      July 24, 2001. If you are using language tags that postdate this snapshot,
      you should ignore this condition.
     *The use of a three letter tag instead of a two letter tag or use of
      the language tag "und" is also reported under this condition, see RFC3066.
     * 
       In default mode this is a warning, in strict mode an error. (W116)
     */
    public int WARN_BAD_XMLLANG = 116;
    /**
     * An attribute from the xml namespace has been used that is not
       recognised. (W118).
     * In default mode this is a warning; in strict mode it is ignored.
     */
    public int WARN_UNKNOWN_XML_ATTRIBUTE = 118;
    /**
     * An XML processing instruction occurred in RDF content. 
     * Such instructions are ignored, and are usually in error. (W119).
     * In default mode this is a warning; in strict mode it is ignored.
     */
    public int WARN_PROCESSING_INSTRUCTION_IN_RDF = 119;
    /**
     * No longer used. 
     * @see #WARN_REDEFINITION_OF_ID
     @deprecated Last supported in Jena 2.1 - too expensive.
     */
    @Deprecated
    public int WARN_LEGAL_REUSE_OF_ID = 120;
    /**
      * String Literals in RDF should not start with a composing char,
      * as defined by the CharacterModel working draft.
      *  This is particularly important if XML 1.1 compatibility is
      * required.
      *  This is a warning in both default and strict modes (W121).
      **/
    public int WARN_STRING_COMPOSING_CHAR = 121;
    /**
      * No longer used. (W122). 
      * 
      * @deprecated Superceded by the more general {@link #WARN_BAD_NAME} */
    @Deprecated
    public int WARN_QNAME_AS_ID = 122;

    /**
      * No longer used. (W123)
      * 
     * @deprecated WG decision on <a href=
     * 
"http://www.w3.org/2001/sw/RDFCore/20030123-issues/#williams-01"
      >williams-01</a>.
      **/
    @Deprecated
    public int WARN_URI_COMPOSING_CHAR = 123;

    /**
      * Some xmlns declaration has a non-URI as its right hand side.
      * This currently permits non-ASCII characters, awaiting clarification from
      * the namespace editor.  This is a warning in default mode and an error in
      * strict mode (W124). */
    public int WARN_BAD_NAMESPACE_URI = 124;
    /**
     * The value of an xml:lang attribute has been deprecated by IANA (W117). In strict and default mode this is a warning.
     */
    public int WARN_DEPRECATED_XMLLANG = 117;
    /**
     * This is an internal only field, not intended for public use.
     * In particular, specifically setting the error mode for this
     * error should be avoided.
     * Errors with this error code cannot happen. (W125)
     **/
    public int WARN_IN_STRICT_MODE = 125;

    /**
    * The SAX Parser generated a warning. Treated as a warning in both default
    * and strict modes. (W126)
    */
    public int WARN_SAX_WARNING = 126;
    
    /**
     * Within RDF, it is not permitted to define an
     *  XML namespace that has a namespace URI with the
     * RDF namespace URI as a proper prefix. (W127).
     * A warning in default mode, an error in strict mode.
     */
    public int WARN_BAD_RDF_NAMESPACE_URI = 127;

    /**
     * Within RDF, it is not permitted to define an
     *  XML   namespace that has a namespace URI with the  XML
     * namespace URI as a proper prefix. (W128). A warning in default mode,an
     * error in strict mode.
     */
    public int WARN_BAD_XML_NAMESPACE_URI = 128;
    
    /**
     * ARP was called with an InputSteamReader or a FileReader which used
     * an encoding differnt from that in the XML declaration. The usual fix
     * is to use an InputStream or a FileInputStream instead. (W129).  
     * A warning in default mode, an error in strict mode.
     * @see #ERR_ENCODING_MISMATCH
     */
    public int WARN_ENCODING_MISMATCH = 129;
	/**
	 * A base URI was required but "" was given. The 
	 * RDF/XML input includes a relative URI, an rdf:ID or
	 * some other same document reference. (W130).
	 */
	public int WARN_RESOLVING_URI_AGAINST_EMPTY_BASE = 130;

	/**
		* String Literals in RDF should be in Unicode Normal Form C
		* 
		* *  (W131).
		**/
	   public int WARN_STRING_NOT_NORMAL_FORM_C = 131;
	   
	   /**
	    * The character encoding in the XML declaration is not
	    * fully supported. In particular, advice about
	    * the IANA registry entry, if any, is not available.
	    * This warning is only believed to be applicable to
	    * Java 1.4. Java 1.5 has more extensive support for
	    * this information, and so upgrading from Java 1.4 to Java 1.5
	    * is a plausible response to this warning.
	    * (W132).
	    */
	   public int WARN_UNSUPPORTED_ENCODING = 132;

	   /**
	    * The character encoding in the XML declaration is not
	    * registered with IANA. Hence the RDF/XML file
	    * is not appropriate for use on the Web, but only
	    * for private interactions, between parties agreeing
	    * on the character encoding.
	    * It may not be possible to read this file with superficially
	    * similar Jena installations, let alone with other RDF systems.
	    * (W133).
	    */
	   public int WARN_NON_IANA_ENCODING = 133;

	   /**
	    * The encoding in the XML declaration is an alias
	    * registered with IANA. Better interoperability
	    * is likely by replacing the encoding declaration
	    * with the canonical IANA name, provided in the warning
	    * message.
	    * (W134).
	    */
	   public int WARN_NONCANONICAL_IANA_NAME =134;
       
       /**
        * It seems likely that the namespace for rdf: has a typo in it.
        * (W135)
        */
        public int WARN_NOT_RDF_NAMESPACE = 135;
        
        /**
         *A URI reference which is a relative reference
         *has been used either as the starting base URI
         *or as the outcome of a URI resolution somehow.
         *In strict mode this is an error.
         *(W136)
         */
        public int WARN_RELATIVE_URI = 136;
        
        /**
         * After 10000 rdf:ID attributes have been
         * read, ARP no longer checks for
         * {@link #WARN_REDEFINITION_OF_ID}.
         * This warning is to inform the user that
         * ARP behaviour has changed during parsing.
         * 
         */
        public int WARN_BIG_FILE = 137;
        
    
    /** Should not happen. 
     (E200)
    @deprecated No longer used.
     */
    @Deprecated
    public int ERR_INTERNAL_ERROR = 200;
    /** The attributes or element tags contravene the RDF grammar. 
     (XML syntax errors are not reported with this mechanism, 
     but as {@link org.xml.sax.SAXParseException SAXParseException}'s).
     The detailed error message indicates the nature of the contravention.
     Future releases may specialize these codes, it is better to
     use {@link ParseException#isSyntaxError()} to check for syntax errors.
     (E201).
     */
    public int ERR_SYNTAX_ERROR = 201;
    /** Non-white character data has occurred where the RDF grammar 
     does not permit it. This is a special case of ERR_SYNTAX_ERROR, 
     which is detected differently.
    (E202)
     */
    public int ERR_NOT_WHITESPACE = 202;
    /** rdf:aboutEach may only occur in a top-level obj expansion,
     * either an rdf:Description or a typed node. This is a special
     * case of ERR_SYNTAX_ERROR, which is detected differently. (E203).
     * @deprecated No longer occurs. Any use of rdf:aboutEach is a syntax error.
     *
     */

    @Deprecated
    public int ERR_ABOUTEACH_NOT_TOPLEVEL = 203;
    /**
     * rdf:li is being used in the typed node construction. 
     * (E204)
     */
    public int ERR_LI_AS_TYPE = 204;
    /**
     *An element is tagged rdf:XXX where XXX is an RDF attribute name.
     * (E205)
     */
    public int ERR_BAD_RDF_ELEMENT = 205;
    /**
    An attribute name in the RDF namespace has been 
     used that is reserved as an RDF name, but not as an attribute.
     These are rdf:Description, rdf:aboutEach, rdf:aboutEachPrefix.
     The latter two are deprecated. (E206).
     **/
    public int ERR_BAD_RDF_ATTRIBUTE = 206;
    /**
     * No longer used.
     * @see #WARN_STRING_NOT_NORMAL_FORM_C
     @deprecated See 2nd Last Call docs
     * 
     * *  (E207).
     **/
    @Deprecated
    public int ERR_STRING_NOT_NORMAL_FORM_C = 207;
    /**
     * No longer used.(E208).
     * @deprecated WG decision on <a href=
     * 
"http://www.w3.org/2001/sw/RDFCore/20030123-issues/#williams-01"
      >williams-01</a>.
   */
    @Deprecated
    public int ERR_URI_NOT_NORMAL_FORM_C = 208;
    /**
    * The SAX Parser generated an error. 
    * Treated as an error in both default and strict modes. (E209)
    */
    public int ERR_SAX_ERROR = 209;
    

    /**
     * ARP was called with an InputSteamReader or a FileReader which used
     * an encoding differnt from that in the XML declaration. Moreover, this
     * was detected as probably significant (i.e. the document includes
     * characters outside the ascii range). The usual fix is to use an
     * InputStream or a FileInputStream instead. (E210).
     * @see #WARN_ENCODING_MISMATCH
     */
    public int ERR_ENCODING_MISMATCH = 210;
    
    /**
     * A base URI was required but not given. The 
     * RDF/XML input includes a relative URI, an rdf:ID or
     * some other same document reference. (E211).
     */
    public int ERR_RESOLVING_URI_AGAINST_NULL_BASE = 211;
    /**
     * The document claimed to be UTF-8 but was not.
     * It probably needs an xml declaration with
     * an encoding attribute. (E212).
     */
    public int ERR_UTF_ENCODING = 212;/**
     * An IOException occurred. (E213).
     */
    public int ERR_GENERIC_IO = 213;
    
    /**
     * Cannot resolve a relative URI, because base URI is malformed.
     * The base URI, specified in the API call, or
     * with an xml:base was malformed,
     * (see {@link #WARN_MALFORMED_URI}).
     * A relative URI needs to be resolved against it,
     * and this cannot be done correctly. (E214)
     */
    public int ERR_RESOLVING_AGAINST_MALFORMED_BASE = 214;
    /**
     * Cannot resolve a relative URI, because base URI is relative.
     * The base URI, specified in the API call, or
     * with an xml:base was relative {@link #WARN_RELATIVE_URI}.
     * A relative URI needs to be resolved against it,
     * resulting in a relative URI. (E215)
     */
    public int ERR_RESOLVING_AGAINST_RELATIVE_BASE = 215;

      /**   No longer used. (E300)
       * @deprecated Not used.
    **/
    @Deprecated
    public int ERR_UNABLE_TO_RECOVER = 300;

    /**   The SAX Parser generated a.error error. 
     * Resetting this mode is not supported. 
     * Treated as a.error error in both
     * default and strict modes. (E301) */
    public int ERR_SAX_FATAL_ERROR = 301;
    

    /**   The Thread was interrupted. (E302) */
    public int ERR_INTERRUPTED = 302;

}
