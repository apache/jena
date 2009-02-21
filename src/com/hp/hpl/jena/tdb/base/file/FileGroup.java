/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file ;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.FileUtils;

import com.hp.hpl.jena.tdb.sys.Names;

/** Naming, access and metadata management to a collection of related files
 *  (same directory, same basename within directory, various extensions).
 */
public class FileGroup
{
    private static Logger log = LoggerFactory.getLogger(FileGroup.class) ;
    
    private Location location ;
    private String basename ;
    // For now, Java properties file.
//    // The properties of a FileGroup are an RDF model written in Turtle.
//    private Model properties ;
    private Properties properties ;
    private String metaFilename ;

    public FileGroup(String directory, String basename)
    {
        this(new Location(directory), basename) ;
    }
    
    public FileGroup(Location directory, String basename)
    {
        this.location = directory ;
        this.basename = basename ;
        this.metaFilename = location.absolute(basename, Names.metaData) ;
        this.properties = new Properties() ;
        loadProperties() ;
    }
    
    private void loadProperties()
    {
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

    public String getProperty(String key)
    {
        return properties.getProperty(key, null) ;
    }
    
    public void setProperty(String key, String value)
    {
        properties.setProperty(key, value) ;
    }
    
    public RandomAccessFile openReadOnly(String ext)
    {
        return open(ext, "r") ;
    }
    
    public RandomAccessFile open(String ext)
    {
        return open(ext, "rw") ;
    }
        
    public void flush()
    {
        try {
            FileOutputStream fos = new FileOutputStream(metaFilename) ;
            Writer w = FileUtils.asUTF8(fos) ;
            w = new BufferedWriter(w) ;
            properties.store(w, "File group: "+basename) ;
        } 
        catch (IOException ex)
        {
            log.error("Failed to store properties: "+metaFilename, ex) ;
        }
    }
    
    public String filename(String ext)
    {
        return location.getPath(basename, ext) ;
    }
    
    public RandomAccessFile open(String ext, String mode)
    {
        // "rwd" - Syncs only the file contents
        // "rws" - Syncs the file contents and metadata
        // "rw" -
        try {
            RandomAccessFile out = new RandomAccessFile(filename(ext), mode) ;
            return out ;
        } catch (IOException ex) { throw new FileException("Failed to open file", ex) ; } 
    }
    
    public FileChannel openChannel(String ext)
    {
        RandomAccessFile out = open(ext, "rw") ;
        return out.getChannel() ;
    }
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */