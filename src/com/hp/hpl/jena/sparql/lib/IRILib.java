/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.ViolationCodes ;

public class IRILib
{
    // Code from the IRI library v0.8

    /* -----------
    static {
        // call static initializers
        new ViolationCodes.Initialize();
    }

    static private IRIFactory jenaFactory;
    static private IRIFactory theSemWebFactory;
    static private IRIFactory theURIFactory;
    static private IRIFactory theIRIFactory;

    static {
        theIRIFactory = new IRIFactory();
        theIRIFactory.useSpecificationIRI(true);
        theIRIFactory.useSchemeSpecificRules("*",true);
        theIRIFactory.create("");


        jenaFactory = new IRIFactory();
        //            jenaFactory.dnsViolation(false,false);
        //            jenaFactory.setSameSchemeRelativeReferences("file");
        jenaFactory.shouldViolation(false,false);
        jenaFactory.securityViolation(false,false);
        jenaFactory.useSpecificationRDF(false);
        jenaFactory.setIsError(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
        jenaFactory.setIsWarning(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
        jenaFactory.setIsError(ViolationCodes.CONTROL_CHARACTER,false);
        jenaFactory.setIsWarning(ViolationCodes.CONTROL_CHARACTER,false);
        //            jenaFactory.setIsError(PORT_SHOULD_NOT_BE_WELL_KNOWN,false);
        //            jenaFactory.setIsWarning(PORT_SHOULD_NOT_BE_WELL_KNOWN,false);
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
        theSemWebFactory.setIsError(ViolationCodes.NON_INITIAL_DOT_SEGMENT,true);
        theSemWebFactory.create("");
    }
    
    private static IRIFactory iriFactoryJena = IRIFactory.jenaImplementation() ;
    private static IRIFactory iriFactoryIRI  = IRIFactory.iriImplementation();
    ---- */
    
    
    /** The IRI checker setup - more than usual Jena but not full IRI. */
    public static IRIFactory iriFactory = new IRIFactory();
    static {
        // IRIFactory.iriImplementation() ...
        iriFactory.useSpecificationIRI(true);
        iriFactory.useSchemeSpecificRules("*",true);

        //iriFactory.shouldViolation(false,true);

        // Moderate it -- allow unwise chars and any scheme name.
        iriFactory.setIsError(ViolationCodes.UNWISE_CHARACTER,false);
        iriFactory.setIsWarning(ViolationCodes.UNWISE_CHARACTER,false);

        iriFactory.setIsError(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
        iriFactory.setIsWarning(ViolationCodes.UNREGISTERED_IANA_SCHEME,false);
    }
    
    public static boolean checkIRI(String iriStr)
    {
        IRI iri = parseIRI(iriStr);
        return iri.hasViolation(false) ;
    }
    
    public static IRI parseIRI(String iriStr)
    {
        return iriFactory.create(iriStr);
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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