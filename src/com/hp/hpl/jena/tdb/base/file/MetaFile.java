/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.lib.PropertyUtils;

import com.hp.hpl.jena.graph.query.Expression.Util;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.sys.Names;

/** Support for persistent metadata files */
public class MetaFile implements Sync, Closeable
{
    private static Logger log = LoggerFactory.getLogger(MetaFile.class) ;
    private String metaFilename = null ;
    private Properties properties = null ;
    private String label = null ;
    private boolean changed = false ;
    
    public MetaFile(String label, String fn)
    {
        this.label = label ;
        this.metaFilename = fn ;

        if ( fn == null || Names.isMem(fn) )
            // In-memory.
            return ;
        
        // Make absolute (current directory may change later)
        if ( ! fn.endsWith(Names.extMeta) )
            fn = fn+"."+Names.extMeta ;
        File f = new File(fn) ;
        this.metaFilename = f.getAbsolutePath() ;
        // Does not load the details yet.
        // JDI
        ensureInit() ; 
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
    
    public String getFilename()         { return metaFilename ; } 
    
    public boolean hasProperty(String key)
    {
        return _getProperty(key, null) != null ;
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
    
//    public String[] getPropertySplit(String key)
//    {
//        String str = getProperty(key) ;
//        if ( str == null )
//            return null ;
//        return str.split(",") ;
//    }
//    
//    public String[] getPropertySplit(String key, String defaultString)
//    {
//        String str = getProperty(key, defaultString) ;
//        return str.split(",") ;
//    }

    
    public void setProperty(String key, String value)
    {
        _setProperty(key, value) ;
    }
    
    public void setProperty(String key, int value)
    {
        _setProperty(key, Integer.toString(value)) ;
    }
    
    public boolean propertyEquals(String key, String value)
    {
        return Utils.equals(getProperty(key), value) ;
    }

    public void ensurePropertySet(String key, String expected)
    {
        getOrSetDefault(key, expected) ;
    }

    /** Get property or the default value - also set the default value if not present */
    public String getOrSetDefault(String key, String expected)
    {
        String x = getProperty(key) ;
        if ( x == null )
        {
            setProperty(key, expected) ;
            x = expected ;
        }
        return x ;
    }
    
    /** Check property is an expected value or set if missing */
    public void checkOrSetMetadata(String key, String expected)
    {
        String x = getProperty(key) ;
        if ( x == null )
        {
            setProperty(key, expected) ;
            return ; 
        }
        if ( x.equals(expected) )
            return ;
        
        inconsistent(key, x, expected) ; 
    }

    public void checkMetadata(String key, String expected)
    {
        String value = getProperty(key) ;
        
        if ( ! Utils.equals(value, value) )
            inconsistent(key, value, expected) ;
    }

    public static void inconsistent(String key, String actual, String expected) 
    {
        String msg = String.format("Inconsistent: key=%s value=%s expected=%s", 
                                   key, 
                                   (actual==null?"<null>":actual),
                                   (expected==null?"<null>":expected) ) ;
        throw new TDBException(msg) ; 
    }
    
    public void clear()
    {
        ensureInit() ;
        properties.clear() ;
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
        String str = label ;
        if ( str == null )
            str = metaFilename ;
        str = "Metadata: "+str ;

        try {
            PropertyUtils.storeToFile(properties, str, metaFilename) ;
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
            PropertyUtils.loadFromFile(properties, metaFilename) ;
        }
        catch (FileNotFoundException ex) {} 
        catch (IOException ex)
        {
            log.error("Failed to load properties: "+metaFilename, ex) ;
        }
    }
    
    /** Debugging */
    public void dump(PrintStream output)
    {
        output.println("Metafile: "+metaFilename) ;
        output.println("Label: "+label) ;
        output.println("Status: "+(changed?"changed":"unchanged")) ;
        
        if ( properties == null )
        {
            output.println("#<null>") ;
            return ;
        }
        // properties.list() ;
        SortedSet<Object> x = new TreeSet<Object>() ;
        x.addAll(properties.keySet()) ;
        
        for ( Object k : x )
        {
            String key = (String)k ;
            String value = properties.getProperty(key) ;
            output.print(key) ;
            output.print("=") ;
            output.print(value) ;
            output.println() ;
        }
    }

    //@Override
    public void sync(boolean force)
    { flush() ; }

    //@Override
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