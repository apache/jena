/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.web.TypedStream ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Operations to open streams, indirecting via a LocationMapper.
 * Inckludes filename to IRI, handling ".gz" and "-" 
 *  
 *  */ 
public class StreamManager
{
    // Need to combine with IO to do the .gz and "-" things.
    
    private static Logger log = LoggerFactory.getLogger(StreamManager.class) ;
    
    public static boolean logAllLookups = true ; 
    
    protected List<Locator> handlers = new ArrayList<Locator>() ;
    protected LocationMapper mapper = null ;
    
    /** Open a file using the locators of this FileManager */
    public InputStream open(String filenameOrURI)
    {
        if ( log.isDebugEnabled())
            log.debug("open("+filenameOrURI+")") ;
        
        String uri = mapURI(filenameOrURI) ;
        
        if ( log.isDebugEnabled() && ! uri.equals(filenameOrURI) )
            log.debug("open: mapped to "+uri) ;
        
        return openNoMap(uri) ;
    }

    /** Apply the mapping of a filename or URI */
    public String mapURI(String filenameOrURI)
    {
        // XXX NO
        // allow any string (e.g. relative filename - made absolute in LocatorFile)
        
        String uriFilename = IRILib.filenameToIRI(filenameOrURI) ;
        
        
        if ( mapper == null )
            return uriFilename ; 
        
        String uri = mapper.altMapping(uriFilename, null) ;
    
        if ( uri == null )
        {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: "+filenameOrURI) ;
            uri = uriFilename ;
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: "+filenameOrURI+" => "+uri) ;
        }
        return uri ;
    }

    /** Open a file using the locators of this FileManager 
     *  but without location mapping */ 
    public InputStream openNoMap(String filenameOrURI)
    {
        TypedStream in = openNoMapOrNull(filenameOrURI) ;
        if ( in == null )
            return null ;
//        if ( in == null )
//            throw new NotFoundException(filenameOrURI) ;
        return in.getInput() ;
    }

    /** Open a file using the locators of this FileManager 
     *  but without location mapping.
     *  Return null if not found
     */ 
    
    public TypedStream openNoMapOrNull(String filenameOrURI)
    {
        String uriFilename = IRILib.filenameToIRI(filenameOrURI) ;
        for (Locator loc : handlers)
        {
            TypedStream in = loc.open(uriFilename) ;
            if ( in != null )
            {
                if ( log.isDebugEnabled() )
                    log.debug("Found: "+uriFilename+" ("+loc.getName()+")") ;
                return in ;
            }
        }
        return null; 
    }

}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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