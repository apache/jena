/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openjena.riot.system;

import java.io.FileReader ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.Reader ;
import java.net.URL ;
import java.net.URLConnection ;

import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
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

    @Override
    final
    public void read(Model model, Reader r, String base) 
	{ 
        checkReader(r) ;
        readImpl(model, makeTokenizer(r), base) ;
	}

    @Override
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

    @Override
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
