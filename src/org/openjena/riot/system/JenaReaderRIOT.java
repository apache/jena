/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package org.openjena.riot.system;

import java.io.FileReader ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.Reader ;
import java.net.URL ;
import java.net.URLConnection ;

import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.GraphEvents ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.FileUtils ;

/** Abstract class that sorts out input streams, readers and base URIs, to call a
 * single worker function with model, UTF8 reader and visited base  
 */

public abstract class JenaReaderRIOT implements RDFReader
{
	protected RDFErrorHandler errorHandler = null ;
	
	protected JenaReaderRIOT() {}

    final
    public void read(Model model, Reader r, String base) 
	{ 
        checkReader(r) ;
        readImpl(model, makeTokenizer(r), base) ;
	}

    final
	public void read(Model model, String url) 
	{
        // See AFS/dev.ContentNeg
        try {
            URLConnection conn = new URL(url).openConnection();
            String encoding = conn.getContentEncoding();

            // Dispatch on MIME type.
            // Inc .gz streams.

            if ( encoding == null )
                read(model, new InputStreamReader(conn.getInputStream(), FileUtils.encodingUTF8), url);
            else
            {
                if ( ! encoding.equalsIgnoreCase("UTF-8") )
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

    final
    public void read(Model model, InputStream in, String base) 
	{
        readImpl(model, makeTokenizer(in), base) ;
    }

    /** Turn an InputStream into a Tokenizer, default behaviour is UTF-8. */   
    protected Tokenizer makeTokenizer(InputStream in)
    {
        return TokenizerFactory.makeTokenizerUTF8(in) ;
    }
    
    /** Turn a Reader into a PeekReader, default behaviour is an UTF-8 Peekreader. */   
    protected Tokenizer makeTokenizer(Reader r)
    {
        return TokenizerFactory.makeTokenizer(r) ;
    }
    
    final
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
	{
		RDFErrorHandler old = errorHandler ;
		errorHandler = errHandler ;
		return old ;
	}
    
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

    private void readImpl(Model model, Tokenizer tokenizer, String base)
    {
        try {
            model.notifyEvent( GraphEvents.startRead );
            readWorker(model, tokenizer,  base) ;
        }
        catch (JenaException e)
        {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e) ;
        }
        //catch (NullPointerException ex) { ex.printStackTrace(System.err) ;  throw new JenaException(ex) ; }
        catch (Exception ex)
        {
            //ex.printStackTrace(System.err) ;
            if ( errorHandler == null ) throw new JenaException(ex) ;
            errorHandler.error(ex) ;
        }
        finally
        {
            model.notifyEvent( GraphEvents.finishRead );
        }
    }

    // Called after decisions have been made.
    // readWorker responsible for closing tokenizer.
    protected abstract void readWorker(Model model, Tokenizer tokenizer, String base) throws Exception;
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
