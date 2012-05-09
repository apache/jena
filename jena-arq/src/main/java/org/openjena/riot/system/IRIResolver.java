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

package org.openjena.riot.system;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.cache.Getter ;
import org.openjena.riot.RiotException ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIException ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.ViolationCodes ;

/** Package up IRI reolver functionality. 
 */

public abstract class IRIResolver
{
    //static IRIFactory iriFactory = IRIFactory.jenaImplementation() ;
    //static IRIFactory iriFactory = IRIFactory.iriImplementation();
    
    /** The IRI checker setup - more than usual Jena but not full IRI. */
    public static final IRIFactory iriFactory = new IRIFactory();
    private static boolean showExceptions = true;
    static {
        // IRIFactory.iriImplementation() ...
        iriFactory.useSpecificationIRI(true);
        iriFactory.useSchemeSpecificRules("*",true);

        // Allow relative references for file: URLs.
        iriFactory.setSameSchemeRelativeReferences("file");

        //iriFactory.shouldViolation(false,true);

        // Moderate it -- allow unwise chars and any scheme name.
//        iriFactory.setIsError(ViolationCodes.UNWISE_CHARACTER,false);
//        iriFactory.setIsWarning(ViolationCodes.UNWISE_CHARACTER,false);

        iriFactory.setIsError(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
        iriFactory.setIsWarning(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
    }

    /** Check an IRI string (does not resolve it) */
    public static boolean checkIRI(String iriStr)
    {
        IRI iri = parseIRI(iriStr);
        return iri.hasViolation(false) ;
    }
    
    /** Parse an IRI (does not resolve it) */
    public static IRI parseIRI(String iriStr)
    {
        return iriFactory.create(iriStr);
    }

//    /**
//     * Resolve the relative URI str against the current global base.
//     * @param str
//     * @return IRI
//     */
//    public static IRI resolveGlobal(String str)
//    {
//        return globalResolver.resolve(str) ;
//    }
//    
//    /**
//     * Resolve the relative URI str against the current global base.
//     * @param str
//     * @return IRI
//     */
//    public static IRI resolveGlobalSilent(String str)
//    {
//        return globalResolver.resolveSilent(str) ;
//    }
//    
//    /**
//     * Resolve the relative URI str against the current global base.
//     * @param str
//     * @return String
//     */
//    public static String resolveGlobalToString(String str) {
//        return globalResolver.resolveToString(str) ;
//    }

    
    /**
     * The current working directory, as a string.
     */
    static private String globalBase = IRILib.filenameToIRI("./") ; // FileUtils.toURL(".").replace("/./", "/") ;
    static private Object globalResolverLock = new Object() ;
    private static IRIResolver globalResolver ; 
    //public static IRIResolver get() { return globalResolver ; }
    
    /**
     * The current global resolver based on the working directory
     */
    static {
        IRI cwd ;
        try {
            cwd = iriFactory.construct(globalBase);
        } catch (IRIException e) {
            System.err.println("Unexpected IRIException in initializer: "
                    + e.getMessage());
            cwd = iriFactory.create("file:///");
            e.printStackTrace(System.err) ;
        }
        globalResolver = IRIResolver.create(cwd) ;
    }
    
    /**
     * Turn a filename into a well-formed file: URL relative to the working
     * directory.
     * 
     * @param filename
     * @return String The filename as an absolute URL
     */
    static public String resolveFileURL(String filename) throws IRIException
    {
        synchronized(globalResolverLock)
        {
            IRI r = globalResolver.resolve(filename);
            if (!r.getScheme().equalsIgnoreCase("file")) 
            {
                // Pragmatic hack that copes with "c:"
                return resolveFileURL("./" + filename);
            }
            return r.toString();
        }
    }

    /**
     * Create resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * 
     * @param relStr
     * @param baseStr
     *            Can be null if relStr is absolute
     * @return An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public IRI resolve(String relStr, String baseStr) throws RiotException
    {
        return exceptions(resolveIRI(relStr, baseStr)) ;
    }

    /**
     * Create resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * 
     * @param relStr
     * @param baseStr
     *            Can be null if relStr is absolute
     * @return String An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public String resolveString(String relStr, String baseStr) throws RiotException
    {
        return exceptions(resolveIRI(relStr, baseStr)).toString() ;
    }

    /**
     * Create resolve a URI against a base. If baseStr is a relative file IRI
     * then it is first resolved against the current working directory.
     * 
     * @param relStr
     * @return String An absolute URI
     * @throws RiotException
     *             If result would not be legal, absolute IRI
     */
    static public String resolveString(String relStr) throws RiotException
    {
        return exceptions(resolveIRI(relStr)).toString() ;
    }


    /** Resolve an IRI against whatever is the base for this process 
     * (likely to be based on the current working directory of this process
     * at the time of initialization of this class).
     */
    public static IRI resolveIRI(String uriStr)
    {
        return exceptions(globalResolver.resolve(uriStr)) ;
    }
    
    /*
     * No exception thrown by this method.
     */
    static private IRI resolveIRI(String relStr, String baseStr)
    {
        synchronized(globalResolverLock)
        {
            IRI i = iriFactory.create(relStr);
            if (i.isAbsolute())
                // removes excess . segments
                return globalResolver.getBaseIRI().create(i);

            IRI base = iriFactory.create(baseStr);

            if ("file".equalsIgnoreCase(base.getScheme()))
                return globalResolver.getBaseIRI().create(i);
            return base.create(i);
        }
    }

    public static IRIResolver create()                  { return new IRIResolverNormal() ; }
    
    public static IRIResolver create(String baseStr)    { return new IRIResolverNormal(baseStr) ; }
    
    public static IRIResolver create(IRI baseIRI)       { return new IRIResolverNormal(baseIRI) ; } 

    // Used for N-triples, N-Quads
    public static IRIResolver createNoResolve()         { return new IRIResolverNoOp() ; } 

    /**
        To allow Eyeball to bypass IRI checking (because it's doing its own)
    */
    public static void suppressExceptions()
    { showExceptions = false; }

    /**
     * Choose a base URI based on the current directory
     * 
     * @return String Absolute URI
     */
    
    static public IRI chooseBaseURI() {
        synchronized(globalResolverLock)
        {
            return globalResolver.getBaseIRI() ;
        }
    }

    /**
     * Choose a baseURI based on a suggestion
     * @return IRI (if relative, relative to current working directory).
     */
    @Deprecated
    static public IRI chooseBaseURI(String baseURI)
    {
        synchronized (globalResolver)
        {
            if (baseURI == null) return chooseBaseURI() ;
            baseURI = "file:." ;
            if (baseURI.startsWith("file:")) 
                return globalResolver.resolveSilent(IRILib.filenameToIRI(baseURI)) ;
            else
                return globalResolver.resolveSilent(baseURI) ;
        }
    }

    public String getBaseIRIasString()
    { 
        IRI iri = getBaseIRI() ;
        if ( iri == null )
            return null ;
        return iri.toString();
    }
    protected abstract IRI getBaseIRI() ;
    
    /** Create a URI, resolving relative IRIs, and throw exception on bad a IRI */
    public abstract IRI resolve(String uriStr) ;
    /** Create a URI, resolving relative IRIs, but do not throw exception on bad a IRI */
    public abstract IRI resolveSilent(String uriStr) ;
    
    /** Resolving relative IRIs, return a string */
    public String resolveToString(String uriStr) { return resolve(uriStr).toString() ; }
    
    /** Resolving relative IRIs, return a string, but do not throw exception on bad a IRI */
    public String resolveToStringSilent(String uriStr) { return resolveSilent(uriStr).toString() ; }
    
    protected IRIResolver() {}
    
//    /**
//     * Print violations - convenience.
//     * @param iri
//     * @return iri
//     */
//    static private IRI exceptions2(IRI iri) {
//        if (showExceptions && iri.hasViolation(false)) {
//            try {
//                IRI iri2 = cwd.create(iri);
//                Iterator<Violation> vIter = iri2.violations(true) ;
//                for ( ; vIter.hasNext() ; )
//                {
//                    Violation v = vIter.next() ;
//                    System.err.println(v) ;
//                }
//            } catch (IRIException e) {
//                throw new RiotException(e);
//            }
//        }
//        return iri;
//    }
    
    /**
     * Throw any exceptions resulting from IRI.
     * @param iri
     * @return iri
     */
    private static IRI exceptions(IRI iri) {
        if ( !showExceptions ) return iri ;
        if ( ! iri.hasViolation(false) ) return iri ;
        String msg = iri.violations(false).next().getShortMessage() ;
        throw new RiotException(msg) ;
    }

    private static final int CacheSize = 1000 ;

    /** A resolver that does not resolve IRIs against base.  Can generate relative IRIs. **/ 
    static class IRIResolverNoOp extends IRIResolver
    {
        protected IRIResolverNoOp() {}

        final private Getter<String, IRI> getter = new Getter<String, IRI>() {
            @Override
            public IRI get(String relURI) { return  iriFactory.create(relURI) ; }
        } ;
        private Cache<String, IRI> resolvedIRIs = CacheFactory.createCache(getter, CacheSize) ;

        @Override
        protected IRI getBaseIRI()
        {
            return null ;
        }

        @Override
        public IRI resolve(String uriStr)
        {
            return iriFactory.create(uriStr) ;
        }

        @Override
        public IRI resolveSilent(String uriStr)
        {
            if ( resolvedIRIs != null && resolvedIRIs.containsKey(uriStr) ) 
                return resolvedIRIs.get(uriStr) ;
            IRI iri = iriFactory.create(uriStr) ;
            if ( resolvedIRIs != null )
                resolvedIRIs.put(uriStr, iri) ;
            return iri ;
        }

        // ??
//        @Override
//        public String resolveToString(String uriStr)
//        {
//            return uriStr ;
//        }
    }

    /** Resolving resolver **/   
    static class IRIResolverNormal extends IRIResolver
    {
        final private IRI base;
        // The cache.  Maybe this should be in Prologue.
        final private Getter<String, IRI> getter = new Getter<String, IRI>() {
            @Override
            public IRI get(String relURI) { return  base.resolve(relURI) ; }
        } ;
        // Not static - contains relative IRIs
        // Could split into absolute (static, global cached) and relative.
        private Cache<String, IRI> resolvedIRIs = CacheFactory.createCache(getter, CacheSize) ;


        /**
         * Construct an IRIResolver with base as the 
         * current working directory.
         *
         */
        public IRIResolverNormal() { this((String)null); }

        /**
         * Construct an IRIResolver with base determined
         * by the argument URI. If this is relative,
         * it is relative against the current working directory.
         * @param baseS
         * @throws RiotException If resulting base unparsable.
         */
        public IRIResolverNormal(String baseS) {
            if (baseS == null)
                base = chooseBaseURI();
            else
                base = globalResolver.resolveSilent(baseS) ;
        }

        public IRIResolverNormal(IRI baseIRI) {
            if (baseIRI == null)
                baseIRI = chooseBaseURI();
            base = baseIRI ;
        }

        /**
         * The base of this IRIResolver.
         * @return String
         */
        @Override
        public IRI getBaseIRI() {
            return base ;
        }

        /**
         * Resolve the relative URI against the base of
         * this IRIResolver.
         * @param relURI
         * @return the resolved IRI
         * @throws RiotException
         *             If resulting URI would not be legal, absolute IRI

         */
        @Override
        public IRI resolve(String relURI)
        {
            return exceptions(resolveSilent(relURI)) ;
        }

        /**
         * Resolve the relative URI against the base of
         * this IRIResolver.  
         * @param relURI
         * @return the resolved IRI - not checked for violations.
         */

        @Override
        public IRI resolveSilent(String relURI)
        {
            if ( resolvedIRIs != null && resolvedIRIs.containsKey(relURI) ) 
                return resolvedIRIs.get(relURI) ;
            IRI iri = base.resolve(relURI) ;
            if ( resolvedIRIs != null )
                resolvedIRIs.put(relURI, iri) ;
            return iri ;
        }
    }
}
