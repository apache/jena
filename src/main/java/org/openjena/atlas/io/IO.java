/*
 * (c) Copyright 2009 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.Reader ;
import java.nio.charset.Charset ;
import java.util.zip.GZIPInputStream ;

import org.openjena.atlas.lib.AtlasException ;
import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.util.FileUtils ;

public class IO
{
    public static final int EOF = -1 ;
    public static final int UNSET = -2 ;
       
    /** Java name for UTF-8 encoding */
    public static final String encodingUTF8     = "utf-8" ;
    public static final String encodingAscii    = "ascii" ;
    
    private static Charset utf8 = null ;
    private static Charset ascii = null ;
    static {
        try {
            utf8 = Charset.forName(encodingUTF8) ;
            ascii = Charset.forName(encodingAscii) ;
        } catch (Throwable ex)
        {
            Log.fatal(FileUtils.class, "Failed to get charset", ex) ;
        }
    }
    
    /** Open an input stream to a file. 
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in  GZIPInputStream  
     */
    static public InputStream openFile(String filename)
    {
        try {
            if ( filename == null || filename.equals("-") )
                return System.in ;
            if ( filename.startsWith("file:") )
                filename = filename.substring("file:".length()) ;
            InputStream in = new FileInputStream(filename) ;
            if ( filename.endsWith(".gz") )
                in = new GZIPInputStream(in) ;
            return in ;
        }
        catch (Exception ex) { throw new AtlasException(ex) ; }
    }
    
    /** Open a UTF8 Reader for a file. 
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream  
     */
    static public Reader openFileUTF8(String filename)  { return openFileReader(filename, utf8) ; }

    /** Open an ASCII Reader for a file. 
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream  
     */
    static public Reader openFileASCII(String filename)  { return openFileReader(filename, ascii) ; }

    private static Reader openFileReader(String filename, Charset charset)
    {
        InputStream in = openFile(filename) ;
        return new InputStreamReader(in, charset) ;
    }

    public static void exception(IOException ex)
    {
        throw new AtlasException(ex) ;
    }
}

/*
 * (c) Copyright 2009 Talis Information Ltd
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