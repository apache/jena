/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.util.Iterator;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.openjena.atlas.lib.cache.Getter ;


import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.util.FileUtils;

/** Package up IRI reolver functionality. 
 */

public class IRIResolver
{
    /**
     * An IRIFactory appropriately configuired.
     */
    static final IRIFactory factory = new IRIFactory(IRIFactory
            .jenaImplementation());
    static {
        factory.setSameSchemeRelativeReferences("file");
    }

    /**
     * The current working directory, as a string.
     */
    static private String globalBase = "http://localhost/LocalHostBase/" ;
    
    // Try to set the global base from the current directory.  
    // Security (e.g. Tomcat) may prevent this in which case we
    // use a common default set above.
    static {
        try { globalBase = FileUtils.toURL("."); }
        catch (Throwable th) {  }
    }
        
    /**
     * The current working directory, as an IRI.
     */
    static final IRI cwd;
    static {
        
        IRI cwdx;
        try {
            cwdx = factory.construct(globalBase);
        } catch (IRIException e) {
            System.err.println("Unexpected IRIException in initializer: "
                    + e.getMessage());
            cwdx = factory.create("file:///");
        }
        cwd = cwdx;
    }
    
    /**
     * Turn a filename into a well-formed file: URL relative to the working
     * directory.
     * 
     * @param filename
     * @return String The filename as an absolute URL
     */
    static public String resolveFileURL(String filename) throws IRIException {
        IRI r = cwd.resolve(filename);
        if (!r.getScheme().equalsIgnoreCase("file")) {
            return resolveFileURL("./" + filename);
        }
        return r.toString();
    }

    // The cache.  Maybe this should be in Prologue.
    private static final int CacheSize = 1000 ;
    final private Getter<String, IRI> getter = new Getter<String, IRI>() {
        public IRI get(String relURI) { return  base.resolve(relURI) ; }
    } ;
    
    // Not static - contains relative IRIs
    // Could split into absolute (satical, global cached) and relative.
    private Cache<String, IRI> resolvedIRIs = CacheFactory.createCache(getter, CacheSize) ;
    
    
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
    static public IRI resolve(String relStr, String baseStr)
            throws RiotException {
        return exceptions(resolveIRI(relStr, baseStr)) ;
    }

    /*
     * No exception thrown by this method.
     */
    static private IRI resolveIRI(String relStr, String baseStr) {
        IRI i = factory.create(relStr);
        if (i.isAbsolute())
            // removes excess . segments
            return cwd.create(i);

        IRI base = factory.create(baseStr);

        if ("file".equalsIgnoreCase(base.getScheme()))
            return cwd.create(base).create(i);
        return base.create(i);
    }

    final private IRI base;
    /**
     * Construct an IRIResolver with base as the 
     * current working directory.
     *
     */
    public IRIResolver() {
        this((String)null);
    }

    /**
     * Construct an IRIResolver with base determined
     * by the argument URI. If this is relative,
     * it is relative against the current working directory.
     * @param baseS
     * 
     * @throws RiotException
     *             If resulting base would not be legal, absolute IRI
     */
    public IRIResolver(String baseS) {
        if (baseS == null)
            base = chooseBaseURI();
        else
            base = exceptions(cwd.create(baseS));
    }
    
    public IRIResolver(IRI baseIRI) {
        if (baseIRI == null)
            baseIRI = chooseBaseURI();
        base = baseIRI ;
    }

    /**
     * The base of this IRIResolver.
     * @return String
     */
    public String getBaseIRI() {
        return base.toString();
    }

    /**
     * Resolve the relative URI against the base of
     * this IRIResolver.
     * @param relURI
     * @return the resolved IRI
     * @throws RiotException
     *             If resulting URI would not be legal, absolute IRI
    
     */
    public IRI resolve(String relURI) {
        return exceptions(resolveSilent(relURI)) ;
    }

    /**
     * Resolve the relative URI against the base of
     * this IRIResolver.  
     * @param relURI
     * @return the resolved IRI - not checked for violations.
     */
    
    public IRI resolveSilent(String relURI)
    {
        if ( resolvedIRIs != null && resolvedIRIs.containsKey(relURI) ) 
            return resolvedIRIs.get(relURI) ;
        IRI iri = base.resolve(relURI) ;
        if ( resolvedIRIs != null )
            resolvedIRIs.put(relURI, iri) ;
        return iri ;
    }
    
    /**
     * Print violations - convenience.
     * @param iri
     * @return iri
     */
    static private IRI exceptions2(IRI iri) {
        if (showExceptions && iri.hasViolation(false)) {
            try {
                IRI iri2 = cwd.create(iri);
                Iterator<Violation> vIter = iri2.violations(true) ;
                for ( ; vIter.hasNext() ; )
                {
                    Violation v = vIter.next() ;
                    System.err.println(v) ;
                }
            } catch (IRIException e) {
                throw new RiotException(e);
            }
        }
        return iri;
    }
    
    /**
     * Throw any exceptions resulting from IRI.
     * @param iri
     * @return iri
     */
    static private IRI exceptions(IRI iri) {
        if (showExceptions && iri.hasViolation(false)) {
            try {
                cwd.construct(iri);
            } catch (IRIException e) {
                throw new RiotException(e);
            }
        }
        return iri;
    }
    
    private static boolean showExceptions = true;

    /**
        To allow Eyeball to bypass IRI checking (because it's doing its own)
    */
    public static void suppressExceptions()
        { showExceptions = false; }
    
    /**
     * Resolve the relative URI str against the current
     * working directory.
     * @param str
     * @return IRI
     */
    public static IRI resolveGlobal(String str) {
        return exceptions(cwd.resolve(str)) ;
    }

    /**
     * Choose a base URI based on the current directory
     * 
     * @return String Absolute URI
     */

    static public IRI chooseBaseURI() {
        return chooseBaseURI(null);
    }

    /**
     * Choose a baseURI based on a suggestion
     * 
     * @return IRI (if relative, relative to current working directory).
     */

    static public IRI chooseBaseURI(String baseURI) {
        if (baseURI == null)
            baseURI = "file:.";
        return resolveGlobal(baseURI);
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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