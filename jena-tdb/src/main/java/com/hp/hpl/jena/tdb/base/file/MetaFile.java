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

package com.hp.hpl.jena.tdb.base.file;

import java.io.ByteArrayOutputStream ;
import java.io.File ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.PrintStream ;
import java.util.Comparator ;
import java.util.Properties ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.jena.atlas.lib.* ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.sys.Names ;

/** Abstraction and many convenience operations on metadata. 
 * Metadata is recorded in Java properties style - not RDF - because it's relative to the file or context used.
 * Keys and values are always strings.
 */
public class MetaFile implements Sync, Closeable
{
    private static Comparator<String> comparator = new ComparatorKeys() ;
    private static Logger log = LoggerFactory.getLogger(MetaFile.class) ;
    private String metaFilename = null ;
    private Properties properties = null ;
    private String label = null ;
    private boolean changed = false ;
    private boolean closed = false ;
    
    /** Create a MetaFile
     *  
     * @param label     Convenience label.
     * @param fn        On disk filename @link{Names.mem} for in-memory
     */
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
            properties = new PropertiesSorted(comparator) ;
            if ( metaFilename != null )
                loadProperties() ;
        }
    }
    
    /** Does this metafile exist on disk? (In-memory MetaFiles always exist) */
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

    /** Test for the presence of a property */
    public boolean hasProperty(String key)
    {
        return _getProperty(key, null) != null ;
    }

    /** Get the property value or null. */
    public String getProperty(String key)
    {
        return _getProperty(key, null) ;
    }
    
    /** Get the property value or return supplied default. */
    public String getProperty(String key, String defaultString)
    {
        return _getProperty(key, defaultString) ;
    }

    /** Get the property value and parse as an integer */
    public int getPropertyAsInteger(String key)
    {
        return Integer.parseInt(_getProperty(key, null)) ;
    }
    
    /** Get the property value and parse as an integer or return default value. */
    public int getPropertyAsInteger(String key, int defaultValue)
    {
        String x = getProperty(key) ;
        if ( x == null )
            return defaultValue ;
        return Integer.parseInt(x) ;
    }

    /** Get property as a string and split on ",". */
    public String[] getPropertySplit(String key)
    {
        String str = getProperty(key) ;
        if ( str == null )
            return null ;
        return str.split(",") ;
    }
    
    /** Get property as a string and split on ",", using the default string if not present in the MetaFile. */
    public String[] getPropertySplit(String key, String defaultString)
    {
        String str = getProperty(key, defaultString) ;
        return str.split(",") ;
    }
    
    /** Set property */
    public void setProperty(String key, String value)
    {
        _setProperty(key, value) ;
    }
    
    /** Set property, turning integer into a string. */
    public void setProperty(String key, int value)
    {
        _setProperty(key, Integer.toString(value)) ;
    }
    
    /** Test whether a property has a value.  Null tests equal to not present. */
    public boolean propertyEquals(String key, String value)
    {
        return Lib.equal(getProperty(key), value) ;
    }

    /** Set property if not already set. */
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

    /** Check property has the value given - throw exception if not. */
    public void checkMetadata(String key, String expected)
    {
        String value = getProperty(key) ;
        
        if ( ! Lib.equal(value, value) )
            inconsistent(key, value, expected) ;
    }

    private static void inconsistent(String key, String actual, String expected) 
    {
        String msg = String.format("Inconsistent: key=%s value=%s expected=%s", 
                                   key, 
                                   (actual==null?"<null>":actual),
                                   (expected==null?"<null>":expected) ) ;
        throw new MetaFileException(msg) ; 
    }
    
    /** Clear all properties. */
    public void clear()
    {
        _clear() ;
    }

    // ---- All get/set access through these  operations
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
    
    /** Clear all properties. */
    private void _clear()
    {
        ensureInit() ;
        properties.clear() ;
        changedEvent() ;
    }

    private void changedEvent() { changed = true ; }
    // ----
    
    private boolean isMem() { return Names.isMem(metaFilename) ; }
    
    /** Write to backing file if changed */
    public void flush()
    {
        if ( log.isDebugEnabled() )
            log.debug("Flush metadata ("+changed+"): "+this.label) ;
        if ( ! changed )
            return ;
        
        
        if ( log.isDebugEnabled() )
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            PrintStream ps = new PrintStream(out) ;
            properties.list(ps) ;
            ps.flush() ;
            log.debug("\n"+out.toString()) ;
        }
        
        //properties.list(System.out) ;
        
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
            //  Copes with UTF-8 for Java5. 
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
        SortedSet<Object> x = new TreeSet<>() ;
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

    @Override
    public void sync()                  { flush() ; }
    
    @Override
    public void close()
    {
        flush() ;
        closed = true ;
        metaFilename = null ;
        properties = null ;

    }

    private static class ComparatorKeys implements Comparator<String>
    {
        @Override
        public int compare(String o1, String o2)
        {
            return - o1.compareTo(o2) ;
        }
        
    }
    
    private static class MetaFileException extends TDBException
    {
        MetaFileException(String msg) { super(msg) ; }
    }

}
