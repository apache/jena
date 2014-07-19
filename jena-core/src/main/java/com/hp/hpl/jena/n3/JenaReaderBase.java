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
 */
@SuppressWarnings("deprecation")
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
