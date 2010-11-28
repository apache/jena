/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class RIOT
{
    /** IRI for ARQ */  
    public static final String riotIRI = "http://openjena.org/#riot" ;

    /** The product name */   
    public static final String NAME = "RIOT";
    
    /** The root package name for RIOT */   
    public static final String PATH = "org.openjena.riot";

    public static final String VERSION = "ARQ/"+ARQ.VERSION ;
    public static final String BUILD_DATE = ARQ.BUILD_DATE ;
    
//    static private String metadataLocation = "com/hp/hpl/jena/sparql/arq-properties.xml" ;
//
//    static private Metadata metadata = new Metadata(metadataLocation) ;
//    
//    /** The full name of the current version */   
//    public static final String VERSION = metadata.get(PATH+".version", "unset") ;
//   
//    /** The date and time at which this release was built */   
//    public static final String BUILD_DATE = metadata.get(PATH+".build.datetime", "unset") ;
    
    static boolean initialized = false ;
    public static synchronized void init()
    {
        if ( initialized ) return ;
        initialized = true ;
        
        String NS = RIOT.PATH ;
        SystemInfo sysInfo2 = new SystemInfo(RIOT.riotIRI, RIOT.VERSION, RIOT.BUILD_DATE) ;
        ARQMgt.register(NS+".system:type=SystemInfo", sysInfo2) ;
        SystemARQ.registerSubSystem(sysInfo2) ;
        
        SysRIOT.wireIntoJena() ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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