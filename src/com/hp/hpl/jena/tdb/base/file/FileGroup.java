/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file ;

import java.io.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.util.FileUtils;

/** A collection of files (often in the same directory, same basename, various extensions) */
public class FileGroup
{
    private static Logger log = LoggerFactory.getLogger(FileGroup.class) ;
    
    private Location location ;
    private String basename ;
//    // The properties of a FileGroup are an RDF model written in Turtle.
//    private Model properties ;
    private Properties properties ;
    private String metaFilename ;

    public FileGroup(String directory, String basename)
    {
        if ( ! new File(directory).isDirectory() )
            throw new TDBException("Not a directory: "+directory) ;
        
        this.location = new Location(directory) ;
        this.basename = basename ;
        this.metaFilename = location.absolute(basename, Names.metaData) ;
        this.properties = new Properties() ;
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