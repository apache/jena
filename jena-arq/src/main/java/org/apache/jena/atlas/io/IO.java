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

package org.apache.jena.atlas.io;

import java.io.* ;
import java.nio.charset.Charset ;
import java.util.zip.GZIPInputStream ;
import java.util.zip.GZIPOutputStream ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.system.IRILib ;

import com.hp.hpl.jena.util.FileUtils ;

public class IO
{
    public static final int EOF = -1 ;
    public static final int UNSET = -2 ;
       
    /** Java name for UTF-8 encoding */
    public static final String encodingUTF8     = "utf-8" ;
    public static final String encodingAscii    = "ascii" ;
    
    private static Charset utf8 = null ;
    private static Charset     ascii         = null ;
    static {
        try {
            utf8 = Charset.forName(encodingUTF8) ;
            ascii = Charset.forName(encodingAscii) ;
        } catch (Throwable ex) { Log.fatal(FileUtils.class, "Failed to get charset", ex) ; }
    }
    
    /** Open an input stream to a file. 
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in  GZIPInputStream  
     */
    static public InputStream openFile(String filename) {
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
    static public InputStream openFileEx(String filename) throws IOException {
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
    
    /** Create a unbuffered reader that uses ASCII encoding */ 
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

    /** Create a writer that uses ASCII encoding */ 
    static public Writer asASCII(OutputStream out) {
        return new OutputStreamWriter(out, ascii.newEncoder());
    }

    /** Create a writer that uses UTF-8 encoding and is buffered. */ 
    static public Writer asBufferedUTF8(OutputStream out) {
        Writer w =  new OutputStreamWriter(out, utf8.newEncoder());
        return new BufferingWriter(w) ;
    }

    /** Open a file for output - may include adding gzip processing. */
    static public OutputStream openOutputFile(String filename)
    {
        try {
           return openOutputFileEx(filename) ;
        }
        catch (Exception ex) { IO.exception(null) ; return null ; }
    }
    
    /** Open an input stream to a file; do not mask IOExceptions. 
     * If the filename ends in .gz, wrap in GZIPOutputStream  
     * @param filename
     * @throws FileNotFoundException If the output can't be opened.
     * @throws IOException for bad gzip encoded data
     */
    static public OutputStream openOutputFileEx(String filename) throws FileNotFoundException,IOException
    {
        if ( filename == null || filename.equals("-") )
            return System.out ;
        if ( filename.startsWith("file:") )
        {
            filename = filename.substring("file:".length()) ;
            filename = IRILib.decode(filename) ;
        }
        OutputStream out = new FileOutputStream(filename) ;
        if ( filename.endsWith(".gz") )
            out = new GZIPOutputStream(out) ;
        return out ;
    }
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrap(Writer w) { 
        return Writer2.wrap(w) ;
    }
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrapUTF8(OutputStream out)        { return wrap(out, CharSpace.UTF8); } 
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrapASCII(OutputStream out)       { return wrap(out, CharSpace.ASCII) ; } 

    /** Wrap in a general writer interface */ 
    static public AWriter wrap(OutputStream out, CharSpace charSpace) {
        switch (charSpace) {
            case UTF8: return wrap(asUTF8(out)) ;
            case ASCII: return wrap(asASCII(out)) ;
        }
        return null ;
    }
    
    /** Create a print writer that uses UTF-8 encoding */ 
    static public PrintWriter asPrintWriterUTF8(OutputStream out) {
        return new PrintWriter(asUTF8(out)); 
    }

    public static void close(org.apache.jena.atlas.lib.Closeable resource) {
        resource.close() ;
    }

    public static void closeSilent(org.apache.jena.atlas.lib.Closeable resource) {
        try { resource.close(); } catch (Exception ex) { }
    }
    
    public static void close(java.io.Closeable resource) {
        if ( resource == null )
            return ;
        try { resource.close(); } catch (IOException ex) { exception(ex) ; }
    }
    
    public static void closeSilent(java.io.Closeable resource) {
        if ( resource == null )
            return ;
        try { resource.close(); } catch (IOException ex) { }
    }
    
    public static void close(AWriter resource) {
        if ( resource == null )
            return ;
        resource.close();
    }
    
    public static void closeSilent(AWriter resource) {
        if ( resource == null )
            return ;
        try { resource.close();  } catch (Exception ex) { }
    }

    public static void close(IndentedWriter resource) {
        if ( resource == null )
            return ;
        resource.close();
    }
    
    public static void closeSilent(IndentedWriter resource) {
        if ( resource == null )
            return ;
        try { resource.close();  } catch (Exception ex) { }
    }

    public static void exception(IOException ex) {
        throw new AtlasException(ex) ;
    }

    public static void exception(String msg, IOException ex) {
        throw new AtlasException(msg, ex) ;
    }
    
    public static void flush(OutputStream out) { 
        if ( out == null )
            return ;
        try { out.flush(); } catch (IOException ex) { exception(ex) ; }
    }
    
    public static void flush(Writer out) {
        if ( out == null )
            return ;
        try { out.flush(); } catch (IOException ex) { exception(ex) ; } 
    }

    public static void flush(AWriter out) {
        if ( out == null )
            return ;
        out.flush(); 
    }

    private static final int BUFFER_SIZE = 32*1024 ; 
    
    public static byte[] readWholeFile(InputStream in) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE)) {
            byte buff[] = new byte[BUFFER_SIZE] ;
            while (true) {
                int l = in.read(buff) ;
                if ( l <= 0 )
                    break ;
                out.write(buff, 0, l) ;
            }
            return out.toByteArray() ;
        }
        catch (IOException ex) {
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
        try ( InputStream in = new FileInputStream(filename) ) {
            return readWholeFileAsUTF8(in) ;
        }
    }

    /** Read a whole stream as UTF-8
     * 
     * @param in    InputStream to be read
     * @return      String
     * @throws IOException
     */
    public static String readWholeFileAsUTF8(InputStream in) throws IOException {
        // Don't buffer - we're going to read in large chunks anyway
        try ( Reader r = asUTF8(in) ) {
            return readWholeFileAsUTF8(r) ;
        }
    }
    
    /** Read a whole file as UTF-8
     * 
     * @param r
     * @return String The whole file
     * @throws IOException
     */
    
    // Private worker as we are trying to force UTF-8. 
    private static String readWholeFileAsUTF8(Reader r) throws IOException {
        try(StringWriter sw = new StringWriter(BUFFER_SIZE)) {
            char buff[] = new char[BUFFER_SIZE];
            for (;;)
            {
                int l = r.read(buff);
                if (l < 0)
                    break;
                sw.write(buff, 0, l);
            }
            return sw.toString();
        }
    }

    public static String uniqueFilename(String directory, String base, String ext) {
        File d = new File(directory) ;
        if ( !d.exists() )
            throw new IllegalArgumentException("Not found: " + directory) ;
        try {
            String fn0 = d.getCanonicalPath() + File.separator + base ;
            String fn = fn0 ;
            int x = 1 ;
            while (true) {
                if ( ext != null )
                    fn = fn + "."+ext ;
                File f = new File(fn) ;
                if ( ! f.exists() )
                    return fn ;
                fn = fn0 + "-" + (x++) ;
            }
        } catch (IOException e) {
            IO.exception(e) ;
            return null ;
        }
    }

}
