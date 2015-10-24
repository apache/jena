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

package org.apache.jena.riot.system ;

import java.io.PrintStream ;
import java.util.concurrent.Callable ;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIException ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.ViolationCodes ;
import org.apache.jena.iri.impl.PatternCompiler ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.SysRIOT ;

/** IRI handling */
public abstract class IRIResolver
{
    /** The IRI checker setup, focused on paring and languages.
     *  This is a clean version of jena-iri IRIFactory.iriImplementation(), 
     *  modified to allow unregistered schemes and allow <file:relative> IRIs.
     *  
     *  @see IRIFactory
     */
    public static final IRIFactory iriFactory        = new IRIFactory();
    private static boolean         showExceptions    = true;
    
    private static final boolean   ShowResolverSetup = false;
    static {
        // These two are from IRIFactory.iriImplementation() ...
        iriFactory.useSpecificationIRI(true) ;
        iriFactory.useSchemeSpecificRules("*", true) ;

        // Allow relative references for file: URLs.
        iriFactory.setSameSchemeRelativeReferences("file") ;

        // Convert "SHOULD" to warning (default is "error").
        // iriFactory.shouldViolation(false,true);

        if ( ShowResolverSetup ) {
            System.out.println("---- Default settings ----") ;
            printSetting(iriFactory) ;
        }
        
        setErrorWarning(iriFactory, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false) ;

        // Turn off?? (ignored in CheckerIRI.iriViolations anyway).
        // setErrorWarning(iriFactory, ViolationCodes.LOWERCASE_PREFERRED, false, false) ;
        // setErrorWarning(iriFactory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE, false, false) ;
        // setErrorWarning(iriFactory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED, false, false) ;
        
        // NFC tests are not well understood by general developers and these cause confusion.
        // See JENA-864
        //iriFactory.setIsError(ViolationCodes.NOT_NFC, false) ;
        //iriFactory.setIsError(ViolationCodes.NOT_NFKC, false) ;

        // ** Applies to various unicode blocks. 
        // setErrorWarning(iriFactory, ViolationCodes.COMPATIBILITY_CHARACTER, false, false) ;

        // This causes test failures.
        // The tests catch warnings and a warning is expected.
        // testing/RIOT/Lang/TurtleStd/turtle-eval-bad-02.ttl and 03 and TriG
        //   > as \u003C  and < \u003E 
        // Default is error=true, warning=false.
        // Test pass with error=false, warning=true.
        // setErrorWarning(iriFactory, ViolationCodes.UNWISE_CHARACTER, false, false) ;
        setErrorWarning(iriFactory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false) ;

        if ( ShowResolverSetup ) {
            System.out.println("---- After initialization ----") ;
            printSetting(iriFactory) ;
        }

    }

    // ---- Initialization support
    
    /** Set the error/warnign state of a violation code.
     * @param factory   IRIFactory
     * @param code      ViolationCodes constant
     * @param isError   Whether it is to be treated an error.
     * @param isWarning Whether it is to be treated a warning.
     */
    private static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsError(code, isError);
        factory.setIsWarning(code, isWarning);
    }
    
    private static void printSetting(IRIFactory factory) {
        PrintStream ps = System.out ;
        printErrorWarning(ps, iriFactory, ViolationCodes.UNREGISTERED_IANA_SCHEME) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.NOT_NFC) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.NOT_NFKC) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.UNWISE_CHARACTER) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.COMPATIBILITY_CHARACTER) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.LOWERCASE_PREFERRED) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.PERCENT_ENCODING_SHOULD_BE_UPPERCASE) ;
        printErrorWarning(ps, iriFactory, ViolationCodes.SCHEME_PATTERN_MATCH_FAILED) ;
        ps.println() ;
    }
    
    private static void printErrorWarning(PrintStream ps, IRIFactory factory, int code) {
        String x = PatternCompiler.errorCodeName(code);
        ps.printf("%-40s : E:%-5s W:%-5s\n", x, factory.isError(code), factory.isWarning(code)) ;
    }

    // ---- System-wide operations.
    
    /** Check an IRI string (does not resolve it) */
    public static boolean checkIRI(String iriStr) {
        IRI iri = parseIRI(iriStr) ;
        return iri.hasViolation(false) ;
    }

    /** Check an IRI string (does not resolve it) - throw exception if not good */
    public static void validateIRI(String iriStr) throws IRIException {
        parseIRIex(iriStr) ;
    }

    /** Parse an IRI (does not resolve it) */
    public static IRI parseIRI(String iriStr) {
        return iriFactory.create(iriStr) ;
    }

    /** Parse an IRI (does not resolve it) - throws exception on a bad IRI */
    public static IRI parseIRIex(String iriStr) throws IRIException {
        return iriFactory.construct(iriStr) ;
    }

    /**
     * The current working directory, as a string.
     */
    static private String      globalBase         = IRILib.filenameToIRI("./") ;

    // The global resolver may be accessed by multiple threads
    // Other resolvers are not thread safe.
    
    private static IRIResolver globalResolver ;

    /**
     * The current global resolver based on the working directory
     */
    static {
        IRI cwd ;
        try {
            cwd = iriFactory.construct(globalBase) ;
        } catch (IRIException e) {
            System.err.println("Unexpected IRIException in initializer: " + e.getMessage()) ;
            cwd = iriFactory.create("file:///") ;
            e.printStackTrace(System.err) ;
        }
        globalResolver = new IRIResolverSync(IRIResolver.create(cwd)) ;
    }

    /**
     * Turn a filename into a well-formed file: URL relative to the working
     * directory.
     * 
     * @param filename
     * @return String The filename as an absolute URL
     */
    static public String resolveFileURL(String filename) throws IRIException {
        IRI r = globalResolver.resolve(filename) ;
        if (!r.getScheme().equalsIgnoreCase("file")) {
            // Pragmatic hack that copes with "c:"
            return resolveFileURL("./" + filename) ;
        }
        return r.toString() ;
    }

    /**
     * Resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * 
     * @param relStr
     * @param baseStr
     *            Can be null if relStr is absolute
     * @return An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public IRI resolve(String relStr, String baseStr) throws RiotException {
        return exceptions(resolveIRI(relStr, baseStr)) ;
    }

    /**
     * Resolve a URI against a base.
     * 
     * @param relStr
     * @param baseStr
     *            Can be null if relStr is absolute
     * @return String An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public String resolveString(String relStr, String baseStr) throws RiotException {
        return exceptions(resolveIRI(relStr, baseStr)).toString() ;
    }

    /**
     * Resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * If it is an absolute URI, it is normalized.
     * 
     * @param uriStr
     * @return String An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public String resolveString(String uriStr) throws RiotException {
        return exceptions(resolveIRI(uriStr)).toString() ;
    }

    /**
     * Resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * If it is an absolute URI, it is normalized.
     * 
     * @param uriStr
     * @return String An absolute URI
     */
    static public String resolveStringSilent(String uriStr) throws RiotException {
        return globalResolver.resolveSilent(uriStr).toString() ;
    }

    /**
     * Resolve an IRI against whatever is the base for this process (likely to
     * be based on the current working directory of this process at the time of
     * initialization of this class).
     */
    public static IRI resolveIRI(String uriStr) {
        return exceptions(globalResolver.resolve(uriStr)) ;
    }

    /*
     * No exception thrown by this method.
     */
    static private IRI resolveIRI(String relStr, String baseStr) {
        IRI i = iriFactory.create(relStr) ;
        if (i.isAbsolute())
            // removes excess . segments
            return globalResolver.getBaseIRI().create(i) ;

        IRI base = iriFactory.create(baseStr) ;

        if ("file".equalsIgnoreCase(base.getScheme()))
            return globalResolver.getBaseIRI().create(i) ;
        return base.create(i) ;
    }

    public static IRIResolver create() {
        return new IRIResolverNormal() ;
    }

    public static IRIResolver create(String baseStr) {
        return new IRIResolverNormal(baseStr) ;
    }

    public static IRIResolver create(IRI baseIRI) {
        return new IRIResolverNormal(baseIRI) ;
    }

    // Used for N-triples, N-Quads
    public static IRIResolver createNoResolve() {
        return new IRIResolverNoOp() ;
    }

    /**
     * To allow Eyeball to bypass IRI checking (because it's doing its own)
     */
    public static void suppressExceptions() {
        showExceptions = false ;
    }

    /**
     * Choose a base URI based on the current directory
     * 
     * @return String Absolute URI
     */

    static public IRI chooseBaseURI() {
        return globalResolver.getBaseIRI() ;
    }

    public String getBaseIRIasString() {
        IRI iri = getBaseIRI() ;
        if (iri == null)
            return null ;
        return iri.toString() ;
    }

    /**
     * The base of this IRIResolver.
     * 
     * @return String
     */
    protected abstract IRI getBaseIRI() ;

    /**
     * Resolve a relative URI against the base of this IRIResolver
     * or normalize an absolute URI.
     * 
     * @param uriStr
     * @return the resolved IRI
     * @throws RiotException
     *             If resulting URI would not be legal, absolute IRI
     */
    public IRI resolve(String uriStr) {
        return exceptions(resolveSilent(uriStr)) ;
    }

    /**
     * Create a URI, resolving relative IRIs, 
     * normalize an absolute URI, 
     * but do not throw exception on a bad IRI.
     * 
     * @param uriStr
     * @return the resolved IRI
     * @throws RiotException
     *             If resulting URI would not be legal, absolute IRI
     */
    public abstract IRI resolveSilent(String uriStr) ;

    /** Resolving relative IRIs, return a string */
    public String resolveToString(String uriStr) {
        return resolve(uriStr).toString() ;
    }

    /**
     * Resolving relative IRIs, return a string, but do not throw exception on
     * bad a IRI
     */
    public String resolveToStringSilent(String uriStr) {
        return resolveSilent(uriStr).toString() ;
    }

    protected IRIResolver()
    {}

    /**
     * Throw any exceptions resulting from IRI.
     * 
     * @param iri
     * @return iri
     */
    private static IRI exceptions(IRI iri) {
        if (!showExceptions)
            return iri ;
        if (!iri.hasViolation(false))
            return iri ;
        String msg = iri.violations(false).next().getShortMessage() ;
        throw new RiotException(msg) ;
    }

    private static final int CacheSize = 1000 ;

    /**
     * A resolver that does not resolve IRIs against base. 
     * This can generate relative IRIs.
     **/
    static class IRIResolverNoOp extends IRIResolver
    {
        protected IRIResolverNoOp()
        {}

        private Cache<String, IRI> resolvedIRIs = CacheFactory.createCache(CacheSize) ;

        @Override
        protected IRI getBaseIRI() {
            return null ;
        }

        @Override
        public IRI resolveSilent(final String uriStr) {
            if ( resolvedIRIs == null ) 
                return iriFactory.create(uriStr) ;
            Callable<IRI> filler = () -> iriFactory.create(uriStr) ;
            IRI iri = resolvedIRIs.getOrFill(uriStr, filler) ;
            return iri ;
        }

        @Override
        public String resolveToString(String uriStr) {
            return uriStr ;
        }
    }

    /** Resolving resolver **/
    static class IRIResolverNormal extends IRIResolver
    {
        final private IRI          base ;
        // Not static - contains relative IRIs
        // Could split into absolute (static, global cached) and relative.
        private Cache<String, IRI> resolvedIRIs = CacheFactory.createCache(CacheSize) ;

        /**
         * Construct an IRIResolver with base as the current working directory.
         */
        public IRIResolverNormal() {
            this((String)null);
        }

        /**
         * Construct an IRIResolver with base determined by the argument URI. If
         * this is relative, it is relative against the current working
         * directory.
         * 
         * @param baseStr
         * @throws RiotException
         *             If resulting base unparsable.
         */
        public IRIResolverNormal(String baseStr) {
            if ( baseStr == null )
                base = chooseBaseURI();
            else
                base = globalResolver.resolveSilent(baseStr);
        }

        public IRIResolverNormal(IRI baseIRI) {
            if ( baseIRI == null )
                baseIRI = chooseBaseURI();
            base = baseIRI;
        }

        @Override
        protected IRI getBaseIRI() {
            return base ;
        }

        @Override
        public IRI resolveSilent(String uriStr) {
            if ( resolvedIRIs == null )
                return resolveSilentCache(uriStr) ;
            else
                return resolveSilentNoCache(uriStr) ;
        }
        
        private IRI resolveSilentNoCache(String uriStr) {
            IRI x = IRIResolver.iriFactory.create(uriStr) ;
            if ( SysRIOT.StrictAbsURINoNormalization ) {
                // Always process "file:", even in strict mode.
                // file: is widely used in irregular forms.
                if ( x.isAbsolute() && ! uriStr.startsWith("file:") )
                    return x ;
            }
            return base.create(x) ;
        }

        private IRI resolveSilentCache(final String uriStr) {
            Callable<IRI> filler = () -> resolveSilentNoCache(uriStr) ;
            return resolvedIRIs.getOrFill(uriStr, filler) ;
        }
    }
    
    /** Thread safe wrapper for an IRIResolver */
    static class IRIResolverSync extends IRIResolver
    {
        private final IRIResolver other ;

        IRIResolverSync(IRIResolver other) { this.other = other ; }
        @Override
        synchronized
        protected IRI getBaseIRI() {
            return other.getBaseIRI() ;
        }

        @Override
        synchronized
        public IRI resolve(String uriStr) {
            return other.resolve(uriStr) ;
        }

        @Override
        synchronized
        public IRI resolveSilent(String uriStr) {
            return other.resolveSilent(uriStr) ;
        }
    }
}
