/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.openjena.riot.RIOT ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class Fuseki
{
    // External log : operations, etc.
    static public String PATH = "org.openjena.fuseki" ;
    static public String FusekiIRI = "http://openjena.org/Fuseki" ;
    
    static public String PagesPublish = "pages-publish" ;
    static public String PagesAll =     "pages-update" ;
    
    //static private String metadataDevLocation = "org/openjena/fuseki/fuseki-properties-dev.xml" ;
    static private String metadataLocation = "org/openjena/fuseki/fuseki-properties.xml" ;
    static private Metadata metadata = initMetadata() ;
    private static Metadata initMetadata()
    {
        Metadata m = new Metadata() ;
        //m.addMetadata(metadataDevLocation) ;
        m.addMetadata(metadataLocation) ;
        return m ;
    }
    
    static public String NAME = "Fuseki" ;
    static public String VERSION = metadata.get(PATH+".version", "development") ;
    static public String BUILD_DATE = metadata.get(PATH+".build.datetime", "unknown") ; // call Date if unavailable.
    public static String serverHttpName     = NAME+" ("+VERSION+")" ;    
    
    // Log for operations
    public static String requestLogName = PATH+".Fuseki" ;
    public static Logger requestLog = LoggerFactory.getLogger(PATH+".Fuseki") ;
    public static String serverLogName = PATH+".Server" ;
    public static Logger serverLog = LoggerFactory.getLogger(PATH+".Server") ;
    // Log for general server messages.
    
    private static boolean initialized = false ;
    public static void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        ARQ.init() ;
        SystemInfo sysInfo = new SystemInfo(FusekiIRI, VERSION, BUILD_DATE) ;
        ARQMgt.register(PATH+".system:type=SystemInfo", sysInfo) ;
        SystemARQ.registerSubSystem(sysInfo) ;
        RIOT.init() ;
    }
  
    // Force a call to init.
    static { init() ; }
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