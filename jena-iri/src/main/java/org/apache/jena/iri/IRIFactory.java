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

package org.apache.jena.iri;

import java.io.UnsupportedEncodingException;

import org.apache.jena.iri.impl.IRIFactoryImpl ;


/**
 * An IRIFactory is the entry point to this module.
 * It allows:
 * <ul>
 *  <li>The setting of conformance levels,
 *  particularly identifying which standard(s) to enforce
 *  <li>The creation of IRI objects, checking
 *  them against the selected standard(s)
 *  <li>The setting of optional behaviours, as 
 *  specified in the various standard(s)
 * </ul>
 * <p>
 * Any configuration of the factory to implement
 * particular standards, or to treat particular
 * violations as warnings or errors, must be
 * completed before using the construct or create methods.
 * </p>
 * <p>The easiest way to use this class is to use
 * one of the preconfigured factories:
 * {@link #semanticWebImplementation()}
 * {@link #iriImplementation()},
 * or
 * {@link #uriImplementation()}.
 * If none of these meets your application needs
 * then you have to configure your own factory.
 * </p>
 * 
 * <p>
 * When initializing a factory, the initialization
 * methods should be used in the following order:
 * </p>
 * <ol>
 * <li>Any of:
 * <ul>
 * <li>{@link #securityViolation(boolean, boolean)}
 * <li>
 *  {@link #dnsViolation(boolean, boolean)}
 *  <li>{@link #shouldViolation(boolean, boolean)}
 *  <li>{@link #mintingViolation(boolean, boolean)}
 *  </ul>
 *  <li>One or more of (note the effect is cumulative,
 *  all the used specifications will be enforced):
 *  <ul>
 *  <li>{@link #useSpecificationIRI(boolean)}
 *  <li>{@link #useSpecificationRDF(boolean)}
 *  <li>{@link #useSpecificationURI(boolean)}
 *  <li>{@link #useSpecificationXLink(boolean)}
 *  <li>{@link #useSpecificationXMLSchema(boolean)}
 *  <li>{@link #useSpecificationXMLSystemID(boolean)}
 *  </ul>
  <li>Any of the methods that invoke optional behaviour
  <ul>
  <li>{@link #allowUnwiseCharacters()}
  <li>{@link #setEncoding(String)}
  <li>{@link #setSameSchemeRelativeReferences(String)}
  </ul>
  <li>Any scheme specific initialization
  <ul>
  <li>{@link #useSchemeSpecificRules(String,boolean)}
  </ul>
 *  <li>Finally calls to
 *  <ul> 
 *  <li>{@link #setIsError(int, boolean)}
 *  <li>{@link #setIsWarning(int, boolean)}
 *  </ul>
 *  </ol>
 *  
 *  <p>
 *  It is possible to make these calls in different
 *  orders, but the resultant behaviour is likely
 *  to be more confusing, and may change in future releases.
 *  </p>
 *  <p>The other initialization methods (probably) do not
 *  have order dependencies.</p>
 */

public class IRIFactory extends IRIFactoryImpl 
  implements  
  IRIFactoryI {
    
    static {
        // call static initializers
        new ViolationCodes.Initialize();
    }

    static private IRIFactory jenaFactory;
    static private IRIFactory theSemWebFactory;
    static private IRIFactory theURIFactory;
    static private IRIFactory theIRIFactory;
    /**
     * This factory implements RFC 3987
     * <a href="http://www.apps.ietf.org/rfc/rfc3987.html">
     * Internationalized Resource Identifiers (IRIs)
     * </a>.
     * @return An implementation of RFC 3987
     */
    static public IRIFactory iriImplementation() {
        return theIRIFactory;
    }
    

    /**
     * This factory implements RFC 3986
     * <a href="http://www.apps.ietf.org/rfc/rfc3986.html">
     * Uniform Resource Identifier (URI): Generic Syntax
     * </a>.
     * @return An implementation of RFC 3986
     */
    static public IRIFactory uriImplementation() {
        return theURIFactory;
    }
    

    /** 
     * This factory is a conservative implementation
     * appropriate for Semantic Web applications.
     * It implements: 
     * RDF URI Reference (essential), 
     * IRI (needed for SPARQL) and 
     * XLink locator (ensures that only legal XML characters are
     * included, allowing RDF/XML usage).
     * In addition, {@link ViolationCodes#NON_INITIAL_DOT_SEGMENT}
     * is treated as an error (since 
     * any IRI with this condition cannot be 
     * serialized in RDF/XML, which resolves
     * all IRIs against the inscope base, and hence uses
     * the remove_dot_segments algorithm).
     * This should ensure that any IRI that is
     * not in error, can be used 
     * interoperably in RDF/XML, SPARQL, N3
     * and N-Triple.
     * @return A factory suitable for Semantic Web applications.
     */
    static public IRIFactory semanticWebImplementation() {
        return theSemWebFactory;
    }
    /** 
     * For use by Jena team only.
     * This method reflects the current IRI support
     * in Jena, which is a moving target at present.
     * @return A factory suitable for Jena.
     */
    static public IRIFactory jenaImplementation() {
        return jenaFactory;
    }
    private boolean usingSpecXMLSchema = false;

    /**
     * Create a new IRIFactory without
     * any conformance settings.
     * To check for errors, you must call one or more
     * of {@link #useSpecificationIRI(boolean)},
     * {@link #useSpecificationRDF(boolean)},
     * {@link #useSpecificationURI(boolean)},
     * {@link #useSpecificationXMLSchema(boolean)},
     * {@link #useSpecificationXLink(boolean)}
     * or
     * {@link #useSpecificationXMLSystemID(boolean)}.
     *
     */
    public IRIFactory() {
        /*
         * Resets this factory to:
         * <ul>
         * <li>ignore all errors,
         * <li>have no scheme specific rules,
         * </ul>
         * */
        // TODO copy port numbers into violations.xml
//        registerScheme("ftp",21); //,H,Q);   
//        registerScheme("http",80); // ,H,U);
//        registerScheme("gopher",70);
//        registerScheme("mailto",OPAQUE);
//        registerScheme("news",OPAQUE);
//        registerScheme("nntp",119);
//        registerScheme("telnet",23);
//        registerScheme("wais",210);
//        registerScheme("file",GENERIC_NO_PORT); //,H,Q|P|U);
//        registerScheme("prospero",1525);
//        registerScheme("z39.50s",210); // 210
//        registerScheme("z39.50r",210);
//        registerScheme("cid",UNKNOWN_SYNTAX);
//        registerScheme("mid",UNKNOWN_SYNTAX);
//        registerScheme("vemmi",575);
//        registerScheme("service",UNKNOWN_SYNTAX);
//        registerScheme("imap",143);
//        registerScheme("nfs",UNKNOWN_SYNTAX);
//        registerScheme("acap",UNKNOWN_SYNTAX);
//        registerScheme("rtsp",UNKNOWN_SYNTAX);
//        registerScheme("tip",UNKNOWN_SYNTAX);
//        registerScheme("pop",UNKNOWN_SYNTAX);
//        registerScheme("data",UNKNOWN_SYNTAX);
//        registerScheme("dav",UNKNOWN_SYNTAX);
//        registerScheme("opaquelocktoken",UNKNOWN_SYNTAX);
//        registerScheme("sip",UNKNOWN_SYNTAX);
//        registerScheme("sips",UNKNOWN_SYNTAX);
//        registerScheme("tel",UNKNOWN_SYNTAX);
//        registerScheme("fax",UNKNOWN_SYNTAX);
//        registerScheme("modem",UNKNOWN_SYNTAX);
//        registerScheme("ldap",UNKNOWN_SYNTAX);
//        registerScheme("https",443);
//        registerScheme("soap.beep",605);
//        registerScheme("soap.beeps",UNKNOWN_SYNTAX);
//        registerScheme("xmlrpc.beep",602);
//        registerScheme("xmlrpc.beeps",UNKNOWN_SYNTAX);
//        registerScheme("urn",OPAQUE);
//        registerScheme("go",UNKNOWN_SYNTAX);
//        registerScheme("h323",UNKNOWN_SYNTAX);
//        registerScheme("ipp",UNKNOWN_SYNTAX);
//        registerScheme("tftp",69);
//        registerScheme("mupdate",UNKNOWN_SYNTAX);
//        registerScheme("pres",UNKNOWN_SYNTAX);
//        registerScheme("im",UNKNOWN_SYNTAX);
//        registerScheme("mtqp",UNKNOWN_SYNTAX);
//        registerScheme("iris.beep",UNKNOWN_SYNTAX);
//        registerScheme("dict",UNKNOWN_SYNTAX);
//        registerScheme("snmp",161);
//        registerScheme("crid",UNKNOWN_SYNTAX);
//        registerScheme("tag",UNKNOWN_SYNTAX);
//        registerScheme("dns",UNKNOWN_SYNTAX);
//        registerScheme("info",UNKNOWN_SYNTAX);
//        registerScheme("afs",UNKNOWN_SYNTAX);
//        registerScheme("tn3270",UNKNOWN_SYNTAX);
//        registerScheme("mailserver",UNKNOWN_SYNTAX);

         
    }
    /**
     * Create a new IRIFactory with
     * the same conformance settings as the 
     * template factory.
     * These can then be modified before using
     * the new factory.
     *
     */
    public IRIFactory(IRIFactory template) {
        super(template);
        this.usingSpecXMLSchema = template.usingSpecXMLSchema;
    }
//    /**
//     * Create a new IRIFactory with user specified
//     * conformance behaviour.
//     *
//     */
//    public IRIFactory(int recsExceptions,  int recsWarnings) {
////        setConformance(recsExceptions, recsWarnings);
////        setExceptionMask(exceptions,warnings);
//    }
    
//    static final private IRIFactory theFactory = new IRIFactory();
//    static public IRIFactory defaultFactory() {
//        return theFactory;
//    }    
//    public void setConformance(int recsForExceptions,
//                               int recsForWarnings) {
//        exceptions = recsToMask(recsForExceptions);
//        warnings = recsToMask(recsForWarnings);
//    }
//    public void setExceptionMask(long exceptions_, long warnings_) {
//        exceptions = exceptions_;
//        warnings = warnings_;
//    }
    
    // choice point: IRIs are heavy weight objects
    // use String s for long term storage, and reparse them
//    public void setCompact(boolean compact) {
//        
//    }
//    public void setComponentCaching(boolean caching){
//        
//    }
    
    
//    public IRI emptyIRI() {
//        return new IRIImpl(this);
//    }
    
    /**
     * Allows scheme:relative-path as a relative
     * reference against a base URI from the same scheme.
     * 
     * <p>
     * Sets the behaviour of the relative
     * reference resolution algorithm to be the
     * backward compatible mode described
     * in the URI specification:
     * </p>
     * <blockquote>
<pre>
-- A non-strict parser may ignore a scheme in the reference
-- if it is identical to the base URI's scheme.
--
   if ((not strict) and (R.scheme == Base.scheme)) then
         undefine(R.scheme);
   endif;
</pre>
     * 
     * </blockquote>
     * @param scheme The scheme to enable this behaviour for, or "*" for all schemes
     */
    @Override
    public void setSameSchemeRelativeReferences(String scheme) {
        super.setSameSchemeRelativeReferences(scheme);
    }
    /**
     * Allows the unwise characters as optionally 
     * permitted by RFC 3987 (IRI).
     * <blockquote>
Systems accepting IRIs MAY also deal with the 
printable characters in US-ASCII that are not allowed 
in URIs, namely "&lt;", "&gt;", '"', space, "{", "}", "|", 
"\", "^", and "`", in step 2 above. If these characters 
are found but are not converted, then the conversion 
SHOULD fail. Please note that the number sign ("#"), 
the percent sign ("%"), and the square bracket 
characters ("[", "]") are not part of the above list
     * </blockquote>
     *<p>This method is intended to be used
     *with factories that are using the IRI
     *specification.
     *The unwise characters are treated as minting
     *warnings after this method is called.
     *This method does not override any setting
     *from {@link #useSpecificationXMLSchema(boolean)}
     *concerning {@link ViolationCodes#DOUBLE_WHITESPACE}.
     *</p>
     */
    public void allowUnwiseCharacters() {
        boolean warning = getAsErrors(MINTING)||getAsWarnings(MINTING);
        setIsError(UNWISE_CHARACTER,false);
        setIsError(WHITESPACE,false);
        setIsWarning(UNWISE_CHARACTER,warning);
        setIsWarning(WHITESPACE,warning);
        if (!usingSpecXMLSchema ) {
            setIsError(DOUBLE_WHITESPACE,false);
            setIsWarning(DOUBLE_WHITESPACE,warning);
        }
    }
    
    /**
     * The character constraints on the query component
     * of an IRI are weaker than on other components.
     * It is not clear how much weaker.
     * Calling this method with <code>restrict=false</code>
     * removes (all?) restrictions, calling this method
     * with  <code>restrict=true</code> adds restrictions,
     * specifically disallowing private use codepoints.
     * @param restrict True to make the query component checking stricter, false to make the query component checking more lenient
     */
    public void setQueryCharacterRestrictions(boolean restrict){
        // TODO setQueryCharacterRestrictions
    	throw new UnsupportedOperationException("unimplemented");
    }
    /**
     * Sets the character encoding to use
     * for decoding and encoding to percent escape sequences.
     * UTF-8 is always used for the hostname.
     * <p>
     * Using this method does not conform with
     * the IRI specification, or XLink, XML system identifiers,
     * RDF URI references, or XML Schema anyURI.
     * This method is conformant with the URI specification.
     *</p>
     *
     *@throws IllegalStateException If this factory has already been used to create an IRI.
     @throws UnsupportedEncodingException If the encoding is not supported.
     */
    @Override
    public void setEncoding(String enc) throws UnsupportedEncodingException {
       super.setEncoding(enc);
    }
    
    
    /**
     * Create an IRI from the given components.
     * Performs whatever percent escaping
     * or punycode encoding is necessary
     * to make this IRI legal for this factory
     * (if possible).
     * Omitted components are passed as null.
     * 
     * @param scheme Schema
     * @param userInfo User Info
     * @param host Will be encoded using punycode, if necessary.
     * @param port May be 0 for no port.
     * @param path  May not be null
     * @param query Query string
     * @param fragment Fragment
     * @return An IRI with the given components.
     @see #setEncoding(String)
     */
    
    public IRI create(
            String scheme,
            String userInfo,
            String host,
            int port,
            String path,
            String query,
            String fragment
            ) {
        // TODO create/7
        return null;
    }
    

    /**
     * Create an IRI from the given components.
     * Performs whatever percent escaping is necessary
     * to make this IRI legal for this factory
     * (if possible).
     * Omitted components are passed as null.
     * Use {@link #create(String, String, String, int, String, String, String)}
     * when the authority is a DNS hostname, even if 
     * both the user information and the port are unspecified;
     * this version uses percent escaping as opposed to punycode
     * for the authority. DNS hostnames should be
     * escaped in punycode (if necessary).
     * @param scheme Scheme
     * @param authority Will be percent escaped if necessary
     * @param path  May not be null
     * @param query Query
     * @param fragment Fragment
     * @return An IRI with the given components.
     @see #setEncoding(String)
     */
    public IRI create(
            String scheme,
            String authority,
            String path,
            String query,
            String fragment
            ) {
        // TODO create/5
        return null;
    }
    
    /**
     * Create an IRI from the given components.
     * Performs whatever percent escaping
     * or punycode encoding is necessary
     * to make this IRI legal for this factory
     * (if possible).
     * Omitted components are passed as null.
     * @param scheme Scheme
     * @param userInfo user info
     * @param host Will be encoded using punycode, if necessary.
     * @param port May be 0 for no port.
     * @param path  May not be null
     * @param query Query string
     * @param fragment Fragment
     * @return An IRI with the given components.
     * * @throws IRIException If the resulting IRI
     *    has unfixable errors, e.g. non-ascii chars in the scheme name
     
     @see #setEncoding(String)
     */
    public IRI construct(
            String scheme,
            String userInfo,
            String host,
            int port,
            String path,
            String query,
            String fragment
            ) throws IRIException {
        return throwAnyErrors(create( scheme,
                userInfo,
                host,
                port,
                path,
                query,
                fragment
                ));
    }
    
    /**
     * Create an IRI from the given components.
     * Performs whatever percent escaping is necessary
     * to make this IRI legal for this factory
     * (if possible).
     * Omitted components are passed as null.
     * Use {@link #construct(String, String, String, int, String, String, String)}
     * when the authority is a DNS hostname, even if 
     * both the user information and the port are unspecified;
     * this version uses percent escaping as opposed to punycode
     * for the authority. DNS hostnames should be
     * escaped in punycode (if necessary).
     * @param scheme Scheme
     * @param authority Will be percent escaped if necessary
     * @param path  May not be null
     * @param query Query string
     * @param fragment Fragment
     * @return An IRI with the given components.
    
     * @throws IRIException If the resulting IRI
     *    has unfixable errors, e.g. non-ascii chars in the scheme name
     
     @see #setEncoding(String)
     */
    public IRI construct(
            String scheme,
            String authority,
            String path,
            String query,
            String fragment
            ) throws IRIException {
        return throwAnyErrors(create( scheme,
                authority,
                path,
                query,
                fragment
                ));
    }
    
    /**
     * Is condition #<code>code</code> being treated as an error.
     * @param code A condition code from {@link ViolationCodes}.
     * 
     */
    public boolean isError(int code) {
        return (errors & (1l<<code))!=0;
    }

    /**
     * Is condition #<code>code</code>
     * being treated as a warning.
     * @param code A condition code from {@link ViolationCodes}.
     */
    public boolean isWarning(int code) {
        return (warnings & (1l<<code))!=0;
    }

    /**
     * Set condition #<code>code</code>
     * to be treated as an error; or clear it as an error condition.
     * <p>
     * Care must be taken when using this to clear the error behaviour
     * on a code documented 
     * in {@link ViolationCodes}
     * as having SHOULD force:
     * see the documentation at {@link #shouldViolation(boolean, boolean)},
     * concerning the necessary steps.
     * Using this method  with <code>code</code>
     * being one that is documented as having MUST force
     * will result in non-conformant behaviour.
     * </p>
     * @param code A condition code from {@link ViolationCodes}.
     * @param set True to set this as an error, false to clear.
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void setIsError(int code, boolean set) {

        initializing();
        if (set) {
            errors |= (1l<<code);
            setIsWarning(code,false);
        } else
            errors &= ~(1l<<code);
    }

    /**
     * Set condition #<code>code</code>
     * to be treated as a warning; 
     * or clear it as a warning condition.
     * Setting a code to be a warning, clears it from
     * being an error. Care must be taken
     * if the <code>code</code> is one that is documented
     * in {@link ViolationCodes} has having SHOULD or MUST
     * force, since ignoring any resulting warning may
     * result in a nonconformant application.
     * @param code A condition code from {@link ViolationCodes}.
     * @param set True to set this as a warning, false to clear.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void setIsWarning(int code, boolean set) {

        initializing();
        
        if (set) { 
            warnings |= (1l<<code);
            setIsError(code,false); 
        } else
            warnings &= ~(1l<<code);
    }
    
    /**
     * Is condition #<code>code</code> being ignored.
     * @param code A condition code from {@link ViolationCodes}.
     */
    public boolean ignoring(int code) {
        return !(isError(code)||isWarning(code));
    }
    
    
    
    /**
     * The factory will check for violations of RFC 3986, URI.
     * Non-ascii input will result in warnings or errors.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void useSpecificationURI(boolean asErrors){
        useSpec("URI",asErrors);
    } 
    /**
     * The factory will check for violations of RFC 3987, IRI.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void useSpecificationIRI(boolean asErrors){
        useSpec("IRI",asErrors);
    }

    /**
     * The factory will check for violations of RDF URI Reference.
     * Note: relative IRIs are prohibited.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void useSpecificationRDF(boolean asErrors){
        useSpec("RDF",asErrors);
    }
    

    /**
     * The factory will check for violations of XML Schema anyURI.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     
     */
    public void useSpecificationXMLSchema(boolean asErrors){
        usingSpecXMLSchema = true;

        useSpec("Schema",asErrors);
    }

    /**
     * The factory will check for violations of XML constraints on system ID.
     * Note: fragments are prohibited.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void useSpecificationXMLSystemID(boolean asErrors){
     
        useSpec("XML",asErrors);
    }
    /**
     * The factory will check for violations of XLink locator: <code>href</code> value.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void useSpecificationXLink(boolean asErrors){
        useSpec("XLink",asErrors);
    }
    /**
     * The factory will treat
     * violations of "SHOULD" force statements
     * from the specifications it is enforcing as errors,
     * or warnings, or not at all. (Default is error)
     * 
     * <p>
     * From <a href="http://www.apps.ietf.org/rfc/rfc2119.html#sec-3">RFC 2119</a>
     * <em>the full implications must be understood and carefully weighed before</em>
     * calling this method with <code>isError=false</code>. 
     * Thus, you MUST have read and understood
     * the specifications that you are configuring the factory
     * to use, before switching SHOULDs to warnings or to be
     * ignored.
     * An easier path, is to understand a specific error code,
     * with SHOULD force,
     * and then use {@link #setIsError(int, boolean)}, and 
     * {@link #setIsWarning(int, boolean)} to modify the behaviour
     * of that error code only. The prerequisite for modifiying a
     * single error code to be ignored, or to be treated as a warning
     * is to have understood the full implications of that condition,
     * rather than of all the SHOULD force statements within all
     * the specifications being used.
     * </p>
     * 
     * 
     * @param isError  If true, treat violations of SHOULDs as errors.
     * @param isWarning If true, treat violations of SHOULDs as warnings.
     * @throws IllegalArgumentException if <code>isError &amp;&amp; isWarning</code>.
     
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    public void shouldViolation(boolean isError,boolean isWarning){
        setViolation(SHOULD,isError,isWarning);
    }
    /**
     * The factory will treat
     * violations of statements
     * from the specifications 
     * flagged as security issues,
     * including weak heuristical suggestions,
     * it is enforcing as errors,
     * or warnings, or not at all. (Default is error)
     * @param isError  If true, treat security violations as errors.
     * @param isWarning If true, treat security violations as warnings.
     * @throws IllegalArgumentException if <code>isError &amp;&amp; isWarning</code>.
     */
    public void securityViolation(boolean isError,boolean isWarning){
        setViolation(SECURITY,isError,isWarning);
    }
    /**
     * The factory will treat
     * violations of statements
     * from the implemented scheme specifications 
     * it is enforcing as errors,
     * or warnings, or not at all. (Default is error)
     * You must also call {@link #useSchemeSpecificRules(String)}
     * to enable particular enforcement of particular schemes.
     * This method primarily permits the scheme specific rules
     * to be treated as warnings.
     * @param isError  If true, treat scheme violations as errors.
     * @param isWarning If true, treat scheme violations as warnings.
     * @throws IllegalArgumentException if <code>isError &amp;&amp; isWarning</code>.
     */
//    public void schemeViolation(boolean isError,boolean isWarning){
//        setViolation(SCHEME_SPECIFIC,isError,isWarning);
//    }
    /**
     * The factory will treat
     * violations of statements
     * from the specifications 
     * flagged as DNS issues,
     * including Internationalized Domain Name issues,
     * it is enforcing as errors,
     * or warnings, or not at all. (Default is error)
     * 
     * 
     * @param isError  If true, treat DNS violations as errors.
     * @param isWarning If true, treat DNS violations as warnings.
     * @throws IllegalArgumentException if <code>isError &amp;&amp; isWarning</code>.
     */
    public void dnsViolation(boolean isError,boolean isWarning){
        setViolation(DNS,isError,isWarning);
    }
    /**
     * The factory will treat
     * violations of statements
     * from the specifications concerning
     * creating new IRIs it is enforcing as errors,
     * or warnings, or not at all. (Default is warning).
     * A sample phrase indicating the intent is this one
     * from RFC 3986:
     * <blockquote>
     * An implementation should accept uppercase letters as equivalent to lowercase in scheme names (e.g., allow "HTTP" as well as 
     * "http") for the sake of robustness but 
     * should only produce lowercase scheme names for 
     * consistency.
     * </blockquote>
     * @param isError  If true, treat violations of minting force statements as errors.
     * @param isWarning If true, treat violations of  minting force statements as warnings.
     * @throws IllegalArgumentException if <code>isError &amp;&amp; isWarning</code>.
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     
     */
    public void mintingViolation(boolean isError,boolean isWarning){
        setViolation(MINTING,isError,isWarning);
    }
    
//    
    /* *
     * Adds a scheme to the list of known schemes.
     * The <code>port</code> argument either
     * associates this scheme with a default port number,
     * and hence with the generic syntax;
     * or is one of the constants
     * {@link #GENERIC_NO_PORT},
     * {@link #GENERIC_UNKNOWN_PORT},
     * {@link #OPAQUE},
     * {@link #UNKNOWN_SYNTAX}
     * to indicate what restrictions if any should
     * be used.
     * @param scheme The scheme name to register
     * @param port The port associated with the scheme, or an appropriate constant.
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
//    public void registerScheme(String scheme, int port){
//        
//    }
    /**
     * Use the rules for a given scheme,
     * or use all known scheme specific rules.
     * Only a few sets of scheme specific rules are implemented.
     * In the table below:
     * <dl>
     * <dt>
     * Partial
     * </dt>
     * <dd>indicates that some but not all
     * of the
     * scheme specific restrictions on the IRI are implemented.
     * </dd>
     * <dt>
     * component
     * </dt>
     * <dd>means that the scheme prohibits or requires
     * one or more components of the IRI; and only
     * these checks are performed.</dd>
     * </dl>
     * </table>
     * <p>The currently implemented schemes are:</p>
     * <table>
     * <tr><th>Scheme</th><th>Level of implementation</th></tr>
     * <tr><td>none</td><td></td></tr>
     * </table> 
     * @param scheme The scheme name or "*" to use all implemented scheme specific rules.
     * @param asErrors If true, then violations are treated as errors; if false violations are treated as warnings.
     * @throws IllegalStateException If this factory has already been used to create an IRI.
     */
    @Override
    public void useSchemeSpecificRules(String scheme, boolean asErrors){
        super.useSchemeSpecificRules(scheme,asErrors);
    }
    
    static {
        theIRIFactory = new IRIFactory();
        theIRIFactory.useSpecificationIRI(true);
        theIRIFactory.useSchemeSpecificRules("*",true);
        theIRIFactory.create("");
        
        jenaFactory = new IRIFactory();
//        jenaFactory.dnsViolation(false,false);
//        jenaFactory.setSameSchemeRelativeReferences("file");
        jenaFactory.shouldViolation(false,false);
        jenaFactory.securityViolation(false,false);
        jenaFactory.useSpecificationRDF(false);
        jenaFactory.setIsError(UNREGISTERED_IANA_SCHEME,false);
        jenaFactory.setIsWarning(UNREGISTERED_IANA_SCHEME,false);
        jenaFactory.setIsError(CONTROL_CHARACTER,false);
        jenaFactory.setIsWarning(CONTROL_CHARACTER,false);
//        jenaFactory.setIsError(PORT_SHOULD_NOT_BE_WELL_KNOWN,false);
//        jenaFactory.setIsWarning(PORT_SHOULD_NOT_BE_WELL_KNOWN,false);
        jenaFactory.useSchemeSpecificRules("http",true);
        jenaFactory.create("");
        
        theURIFactory = new IRIFactory();
        theURIFactory.useSpecificationURI(true);
        theURIFactory.useSchemeSpecificRules("*",true);
        theURIFactory.create("");
        
        theSemWebFactory = new IRIFactory();
        theSemWebFactory.useSpecificationRDF(true);
        theSemWebFactory.useSpecificationIRI(true);
        theSemWebFactory.useSpecificationXLink(true);
        theSemWebFactory.useSchemeSpecificRules("*",true);
        theSemWebFactory.setIsError(NON_INITIAL_DOT_SEGMENT,true);
        theSemWebFactory.create("");
        
    }

    /**
     * This <em>globally</em> sets the {@link #jenaImplementation}; use with care.
     * This should be used before any calls to {@link #jenaImplementation}; 
     * it does not modify the factory returned by any previous calls, but subsequent
     * calls to {@link #jenaImplementation} will return the new value.
     * @param jf The new Jena Factory
     */
	public static void setJenaImplementation(IRIFactory jf) {
		jenaFactory = jf;
	}
    /**
     * This <em>globally</em> sets the {@link #iriImplementation}; use with care.
     * This should be used before any calls to {@link #iriImplementation}; 
     * it does not modify the factory returned by any previous calls, but subsequent
     * calls to {@link #iriImplementation} will return the new value.
     * @param iriF The new IRI Factory
     */
	public static void setIriImplementation(IRIFactory iriF) {
		theIRIFactory = iriF;
	}
    /**
     * This <em>globally</em> sets the {@link #uriImplementation}; use with care.
     * This should be used before any calls to {@link #uriImplementation}; 
     * it does not modify the factory returned by any previous calls, but subsequent
     * calls to {@link #uriImplementation} will return the new value.
     * @param uriF The new URI Factory
     */
	public static void setUriImplementation(IRIFactory uriF) {
		theURIFactory = uriF;
	}
    /**
     * This <em>globally</em> sets the {@link #semanticWebImplementation}; use with care.
     * This should be used before any calls to {@link #semanticWebImplementation}; 
     * it does not modify the factory returned by any previous calls, but subsequent
     * calls to {@link #semanticWebImplementation} will return the new value.
     * @param sw The new IRI Factory
     */
	public static void setSemanticWebImplementation(IRIFactory sw) {
		theSemWebFactory = sw;
	}

}
