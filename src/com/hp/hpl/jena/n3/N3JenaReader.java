/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.rdf.model.*;
import java.net.* ;
import java.io.* ;
import com.hp.hpl.jena.shared.*;

// To do: buffereing version that does the whole file before adding statements (error catcher)

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaReader.java,v 1.14 2004-12-06 13:50:12 andy_seaborne Exp $
 */



public class N3JenaReader implements RDFReader
{
	RDFErrorHandler errorHandler = null ;
	N3toRDF converter = null ;
	
	public N3JenaReader() { converter = new N3toRDF() ; } 
	
	// Jena's Reader interface
	
	public void read(Model model, Reader r, String base) 
	{
		read(model, r, base, null) ;
	}

	public void read(Model model, java.lang.String url) 
	{
	      try {
        	URLConnection conn = new URL(url).openConnection();
        	String encoding = conn.getContentEncoding();
        	if ( encoding == null )
               read(model,new InputStreamReader(conn.getInputStream()), url, url);
        	else
               read(model,new InputStreamReader(conn.getInputStream(),encoding), url, url);
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
	
	public void read(Model model, Reader r, String base, String sourceName) 
	{
		// check reader is it charset safe ie not file reader
		//i.e. InputStreamReader

		try {
		    model.notifyEvent( GraphEvents.startRead );
            converter.setBase(base) ;
            converter.setModel(model);
			N3Parser p = new N3Parser(r, converter) ;
			p.parse() ;
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
	

	public void read(Model model, InputStream in, String base) 
	{
		read(model, in, base, null) ;
	}

	
	public void read(Model model, InputStream in, String base, String sourceName) 
	{
		try {
            model.notifyEvent( GraphEvents.startRead );
            converter.setBase(base) ;
            converter.setModel(model);
			N3Parser p = new N3Parser(in, converter) ;
			p.parse() ;
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
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
