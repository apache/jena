/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.util.FileUtils;

/** Support for persistent metadata files */
public class MetaBase implements Sync, Closeable
{
    // Replaces MetaFile???
    
    // The magic name "--mem--" means in-memory only.
    private static Logger log = LoggerFactory.getLogger(MetaBase.class) ;
    private String metaFilename = null ;
    private Properties properties = null ;
    private String label = null ;
    private boolean changed = false ;
    
    //Must call init(,) afterwards.
    protected MetaBase()
    {}
    
    public MetaBase(String label, String fn)
    {
        this.label = label ;
        this.metaFilename = fn ;
        if ( fn != null )
            initMetaFile(label, fn) ;
    }
    
    protected void initMetaFile(String label, String fn)
    {
        close() ;
        this.label = label ;
        if ( fn.equals(Names.memName) )
            return ;
        
        // Make absolute (current directy may change later)
        if ( ! fn.endsWith(Names.extMeta) )
            fn = fn+"."+Names.extMeta ;
        File f = new File(fn) ;
        this.metaFilename = f.getAbsolutePath() ;
        // Does not load the details.
    }
    
    private void ensureInit()
    { 
        if ( properties == null )
        {
            properties = new Properties() ;
            if ( metaFilename != null )
                loadProperties() ;
        }
    }
    
    public boolean existsMetaData()
    {
        if ( isMem() )
            return true ;
        File f = new File(metaFilename) ;
        if ( f.isDirectory() )
            log.warn("Metadata file clashes with a directory") ;
        return f.exists() && f.isFile() ;
    }
    
    public String getProperty(String key)
    {
        return _getProperty(key, null) ;
    }
    
    public String getProperty(String key, String defaultString)
    {
        return _getProperty(key, defaultString) ;
    }

    public int getPropertyAsInteger(String key)
    {
        return Integer.parseInt(_getProperty(key, null)) ;
    }
    
    public void setProperty(String key, String value)
    {
        _setProperty(key, value) ;
    }
    
    public void setProperty(String key, int value)
    {
        _setProperty(key, Integer.toString(value)) ;
    }

    // All get/set access through these two operations
    private String _getProperty(String key, String dft)
    {
        ensureInit() ;
        return properties.getProperty(key, dft) ;
    }
    
    private void _setProperty(String key, String value)
    {
        ensureInit() ;
        properties.setProperty(key, value) ;
        changedEvent() ;
    }
    
    private void changedEvent() { changed = true ; }
 
    private boolean isMem() { return Names.isMem(metaFilename) ; }
    
    public void flush()
    {
        if ( ! changed )
            return ;
        
        saveProperties() ;
        changed = false ;
    }

    private void saveProperties()
    {
        if ( isMem() )
            return ;
        try {
            FileOutputStream fos = new FileOutputStream(metaFilename) ;
            Writer w = FileUtils.asUTF8(fos) ;
            w = new BufferedWriter(w) ;
            String str = label ;
            if ( str != null )
                str = metaFilename ;
            properties.store(w, "File set: "+str) ;
        } 
        catch (IOException ex)
        {
            log.error("Failed to store properties: "+metaFilename, ex) ;
        }
    }

    
    private void loadProperties()
    {
        if ( isMem() )
        {
            properties = new Properties() ;
            return ;
        }
        
        if ( properties == null )
            properties = new Properties() ;
        
        // if ( metaFilename == null )
        InputStream in = null ;
        try { 
            in = new FileInputStream(metaFilename) ;
            Reader r = FileUtils.asBufferedUTF8(in) ;
            properties.load(r) ;
        }
        catch (FileNotFoundException ex) {} 
        catch (IOException ex)
        {
            log.error("Failed to load properties: "+metaFilename, ex) ;
        }
    }

    @Override
    public void sync(boolean force)
    { flush() ; }

    @Override
    public void close()
    {
        flush() ;
        metaFilename = null ;
        properties = null ;

    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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