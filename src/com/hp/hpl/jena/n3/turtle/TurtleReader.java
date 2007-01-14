/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.turtle;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.BadURIException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;


public class TurtleReader implements RDFReader
{

    RDFErrorHandler errorHandler = null ;
    
    // Jena's Reader interface
    
    public void read(Model model, Reader r, String base) 
    {
        read(model, r, base, null) ;
    }

    public void read(Model model, String url) 
    {
          try {
            URLConnection conn = new URL(url).openConnection();
            String encoding = conn.getContentEncoding();
            if ( encoding == null )
               read(model, conn.getInputStream(), url, url);
            else
               read(model, new InputStreamReader(conn.getInputStream(),encoding), url, url);
        }
        catch (MalformedURLException ex)
        { 
            // parser should do better
            throw new BadURIException(ex.getMessage()) ; }
        
        catch (JenaException e)
        {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e) ;
        }
        catch (Exception ex)
        {
            if ( errorHandler == null ) throw new JenaException(ex) ;
            errorHandler.error(ex) ;
        }
    }
    
    public void read(Model model, Reader reader, String base, String sourceName) 
    {
        //System.err.println("Call to Turtle reader / Reader") ;
        readWorker(model, reader, base, sourceName) ;
    }

    public void read(Model model, InputStream in, String base) 
    {
        read(model, in, base, null) ;
    }

    
    public void read(Model model, InputStream in, String base, String sourceName) 
    {
        Reader reader = FileUtils.asUTF8(in) ;
        readWorker(model, reader, base, sourceName) ;
    }
    
    private void readWorker(Model model, Reader reader, String base, String sourceName)
    {
        try {
            if ( base == null )
                base = sourceName ;
            else if ( base.equals("") )
                ;
            
            if ( base != null )
                base = RelURI.resolve(base) ;
            
            model.notifyEvent( GraphEvents.startRead ) ;
            ParserTurtle p =  new ParserTurtle() ;
            p.parse(model.getGraph(), base, reader) ;
            // Finish done in finally block
        }
        catch (JenaException e)
        {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e) ;
        }
        catch (Exception ex)
        {
            if ( errorHandler == null ) throw new JenaException(ex) ;
            errorHandler.error(ex) ;
        }
        finally
        {
            model.notifyEvent( GraphEvents.finishRead );
        }
    }
    
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        RDFErrorHandler old = errorHandler ;
        errorHandler = errHandler ;
        return old ;
    }
    
    public Object setProperty(String propName, Object propValue)
    {
        return null ;
    }


}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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