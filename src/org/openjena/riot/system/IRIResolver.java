/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.cache.Getter ;
import org.openjena.riot.RiotException ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIException ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.ViolationCodes ;

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
    
    private static IRIResolver globalResolver ; 
    public static IRIResolver get() { return globalResolver ; }
    
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
    static public String resolveFileURL(String filename) throws IRIException {
        IRI r = globalResolver.resolve(filename);
        if (!r.getScheme().equalsIgnoreCase("file")) 
        {
            // Pragmatic hack that copes with "c:"
            return resolveFileURL("./" + filename);
        }
        return r.toString();
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
    static public IRI resolve(String relStr, String baseStr)
            throws RiotException {
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
    static public String resolveString(String relStr, String baseStr)
            throws RiotException {
        return exceptions(resolveIRI(relStr, baseStr)).toString() ;
    }


    /*
     * No exception thrown by this method.
     */
    static private IRI resolveIRI(String relStr, String baseStr) {
        IRI i = iriFactory.create(relStr);
        if (i.isAbsolute())
            // removes excess . segments
            return globalResolver.getBaseIRI().create(i);

        IRI base = iriFactory.create(baseStr);

        if ("file".equalsIgnoreCase(base.getScheme()))
            return globalResolver.getBaseIRI().create(i);
        return base.create(i);
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
        return globalResolver.getBaseIRI() ;
    }

    /**
     * Choose a baseURI based on a suggestion
     * @return IRI (if relative, relative to current working directory).
     */
    @Deprecated
    static public IRI chooseBaseURI(String baseURI) {
        if (baseURI == null)
            return chooseBaseURI() ;
            baseURI = "file:.";
        if ( baseURI.startsWith("file:") )
            return globalResolver.resolveSilent(IRILib.filenameToIRI(baseURI)) ;
        else
            return get().resolveSilent(baseURI);
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
            public IRI get(String relURI) { return  base.resolve(relURI) ; }
        } ;
        // Not static - contains relative IRIs
        // Could split into absolute (statical, global cached) and relative.
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

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Systems Ltd.
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */