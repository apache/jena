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

package org.openjena.atlas.io;

import java.io.BufferedReader ;
import java.io.ByteArrayOutputStream ;
import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.PrintWriter ;
import java.io.Reader ;
import java.io.StringWriter ;
import java.io.Writer ;
import java.nio.charset.Charset ;
import java.util.zip.GZIPInputStream ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.IRILib ;
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
           return openFileEx(filename) ;
        }
        catch (Exception ex) { throw new AtlasException(ex) ; }
    }
    
    /** Open an input stream to a file; do not mask IOExceptions. 
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in  GZIPInputStream  
     * @param filename
     * @throws FileNotFoundException 
     */
    static public InputStream openFileEx(String filename) throws IOException
    {
        if ( filename == null || filename.equals("-") )
            return System.in ;
        if ( filename.startsWith("file:") )
        {
            filename = filename.substring("file:".length()) ;
            filename = IRILib.decode(filename) ;
        }
        InputStream in = new FileInputStream(filename) ;
        if ( filename.endsWith(".gz") )
            in = new GZIPInputStream(in) ;
        return in ;
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

    /** Create an unbuffered reader that uses UTF-8 encoding */ 
    static public Reader asUTF8(InputStream in)
    {
        return new InputStreamReader(in, utf8.newDecoder());
    }
    
    /** Create a reader that uses ASCII encoding */ 
    static public Reader asASCII(InputStream in)
    {
        return new InputStreamReader(in, ascii.newDecoder());
    }
    
    /** Create an buffered reader that uses UTF-8 encoding */ 
    static public BufferedReader asBufferedUTF8(InputStream in) {
        return new BufferedReader(asUTF8(in)) ;
    }

    /** Create a writer that uses UTF-8 encoding */ 
    static public Writer asUTF8(OutputStream out) {
        return new OutputStreamWriter(out, utf8.newEncoder());
    }

    /** Create a print writer that uses UTF-8 encoding */ 

    static public PrintWriter asPrintWriterUTF8(OutputStream out) {
        return new PrintWriter(asUTF8(out)); 
    }
    
    public static void close(java.io.Closeable resource)
    {
        try { resource.close(); } catch (IOException ex) { exception(ex) ; }
    }
    
    public static void exception(IOException ex)
    {
        throw new AtlasException(ex) ;
    }

    public static void flush(OutputStream out)
    { try { out.flush(); } catch (IOException ex) { exception(ex) ; } }
    
    public static void flush(Writer out)
    { try { out.flush(); } catch (IOException ex) { exception(ex) ; } }

    private static final int BUFFER_SIZE = 8*1024 ; 
    
    public static byte[] readWholeFile(InputStream in) 
    {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE) ;
            byte buff[] = new byte[BUFFER_SIZE];
            while (true)
            {
                int l = in.read(buff);
                if (l <= 0)
                    break;
                out.write(buff, 0, l);
            }
            out.close();
            return out.toByteArray() ;
        } catch (IOException  ex)
        {
            exception(ex) ;
            return null ;
        }
    }
    
    /** Read a whole file as UTF-8
     * @param filename
     * @return String
     * @throws IOException
     */
    
    public static String readWholeFileAsUTF8(String filename) throws IOException {
        InputStream in = new FileInputStream(filename) ;
        return readWholeFileAsUTF8(in) ;
    }

    /** Read a whole stream as UTF-8
     * 
     * @param in    InputStream to be read
     * @return      String
     * @throws IOException
     */
    public static String readWholeFileAsUTF8(InputStream in) throws IOException
    {
        // Don't buffer - we're going to read in larg chunks anyway
        Reader r = asUTF8(in) ;
        return readWholeFileAsUTF8(r) ;
    }
    
    /** Read a whole file as UTF-8
     * 
     * @param r
     * @return String The whole file
     * @throws IOException
     */
    
    // Private worker as we are trying to force UTF-8. 
    private static String readWholeFileAsUTF8(Reader r) throws IOException
    {
        StringWriter sw = new StringWriter(BUFFER_SIZE);
        char buff[] = new char[BUFFER_SIZE];
        for (;;)
        {
            int l = r.read(buff);
            if (l < 0)
                break;
            sw.write(buff, 0, l);
        }
        sw.close();
        return sw.toString();  
    }

}
