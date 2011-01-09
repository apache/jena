/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.io.StringWriter ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.web.TypedStream ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.shared.WrappedIOException ;
import com.hp.hpl.jena.util.FileUtils ;

public class StreamManager
{
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
        if ( mapper == null )
            return filenameOrURI ; 
            
        String uri = mapper.altMapping(filenameOrURI, null) ;
    
        if ( uri == null )
        {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: "+filenameOrURI) ;
            uri = filenameOrURI ;
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: "+filenameOrURI+" => "+uri) ;
        }
        return uri ;
    }

    /** Slurp up a whole file */
    public String readWholeFileAsUTF8(InputStream in)
    {
        try {
            Reader r = FileUtils.asBufferedUTF8(in) ;
            StringWriter sw = new StringWriter(1024);
            char buff[] = new char[1024];
            while (true) {
                int l = r.read(buff);
                if (l <= 0)
                    break;
                sw.write(buff, 0, l);
            }
            r.close();
            sw.close();
            return sw.toString();
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex) ;
        }
    }

    /** Slurp up a whole file: map filename as necessary */
    public String readWholeFileAsUTF8(String filename)
    {
        InputStream in = open(filename) ;
        if ( in == null )
            throw new NotFoundException("File not found: "+filename) ;
        return readWholeFileAsUTF8(in) ;
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
        for (Locator loc : handlers)
        {
            TypedStream in = loc.open(filenameOrURI) ;
            if ( in != null )
            {
                if ( log.isDebugEnabled() )
                    log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
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