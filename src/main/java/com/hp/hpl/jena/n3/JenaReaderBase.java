/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.rdf.model.*;
import java.net.* ;
import java.io.* ;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileUtils;

/** Abstract class that sorts out input streams, readers and base URIs, to call a
 * single worker function with model, UTF8 reader and visated base  
 *   
 * @author		Andy Seaborne
 * @version 	$Id: JenaReaderBase.java,v 1.2 2009-07-15 11:39:50 andy_seaborne Exp $
 */

public abstract class JenaReaderBase implements RDFReader
{
	protected RDFErrorHandler errorHandler = null ;
	
	public JenaReaderBase() {}

    @Override
    final
    public void read(Model model, Reader r, String base) 
	{ 
        checkReader(r) ;
        readImpl(model, r, base) ;
	}

    @Override
    final
	public void read(Model model, java.lang.String url) 
	{
	      try {
        	URLConnection conn = new URL(url).openConnection();
        	String encoding = conn.getContentEncoding();
        	if ( encoding == null )
               read(model, new InputStreamReader(conn.getInputStream(), FileUtils.encodingUTF8), url);
        	else
            {
                LoggerFactory.getLogger(this.getClass()).warn("URL content is not UTF-8") ;
                read(model, new InputStreamReader(conn.getInputStream(),encoding), url);
            }
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
	}

    @Override
    final
    public void read(Model model, InputStream in, String base) 
	{
        readImpl(model, FileUtils.asBufferedUTF8(in), base) ;
    }
	
    @Override
    final
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
	{
		RDFErrorHandler old = errorHandler ;
		errorHandler = errHandler ;
		return old ;
	}
    
    @Override
    final
	public Object setProperty(String propName, Object propValue)
	{ return null ; }

    protected void checkReader(Reader r)
    {
        if ( r instanceof FileReader )
        {
            FileReader f = (FileReader)r ;
            if ( f.getEncoding().equalsIgnoreCase(FileUtils.encodingUTF8) )
                LoggerFactory.getLogger(this.getClass()).warn("FileReader is not UTF-8") ;
        }
    }

    private void readImpl(Model model, Reader reader, String base)
    {
        // The reader has been checked, if possible, by now or
        // constructed correctly by code here. 
        if ( base != null )
            base = IRIResolver.resolveGlobal(base) ;
        try {
            model.notifyEvent( GraphEvents.startRead );
            readWorker(model, reader,  base) ;
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

    protected abstract void readWorker(Model model, Reader reader, String base) throws Exception;
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
