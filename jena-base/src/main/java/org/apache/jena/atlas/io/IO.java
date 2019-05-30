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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.StrUtils;

public class IO
{
    public static final int EOF = -1;
    public static final int UNSET = -2;
    
    // Buffer size.  Larger than Java's default.
    private static final int BUFFER_SIZE = 128*1024;
       
    private static Charset utf8  = StandardCharsets.UTF_8;
    private static Charset ascii = StandardCharsets.US_ASCII;
    
    /** Open an input stream to a file. 
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in  GZIPInputStream  
     */
    static public InputStream openFile(String filename) {
        try { return openFileEx(filename); }
        catch (IOException ex) { IO.exception(ex); return null; }
    }
    
    /**
     * Open an input stream to a file and buffer it. If the filename is null or "-",
     * return System.in If the filename ends in .gz, wrap in GZIPInputStream.
     * If using this {@code InputStream} with an {@code InputStreamReader}
     * (e.g. to get UTF-8), there is no need to buffer the {@code InputStream}.
     * Instead, buffer the {@code Reader}. 
     */
    static public InputStream openFileBuffered(String filename) {
        try {
            InputStream in = openFileEx(filename);
            return new BufferedInputStream(in, BUFFER_SIZE);
        } catch (IOException ex) { IO.exception(ex); return null; }
    }
    
    /** Open an input stream to a file; do not mask IOExceptions. 
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in GZIPInputStream  
     * @param filename
     * @throws FileNotFoundException 
     * @throws IOException
     */
    static public InputStream openFileEx(String filename) throws IOException, FileNotFoundException {
        if ( filename == null || filename.equals("-") )
            return System.in;
        if ( filename.startsWith("file:") )
        {
            filename = filename.substring("file:".length());
            filename = IRILib.decode(filename);
        }
        InputStream in = new FileInputStream(filename);
        String ext = FilenameUtils.getExtension(filename);
        switch ( ext ) {
            case "":        return in;
            case "gz":      return new GZIPInputStream(in);
            case "bz2":     return new BZip2CompressorInputStream(in);
            case "sz":      return new SnappyCompressorInputStream(in);
        }
        return in;
    }

    private static String[] extensions = { "gz", "bz2", "sz" }; 
    
    /** The filename without any compression extension, or the original filename.
     *  It tests for compression types handled by {@link #openFileEx}.
     */
    static public String filenameNoCompression(String filename) {
        if ( FilenameUtils.isExtension(filename, extensions) ) {
            return FilenameUtils.removeExtension(filename);
        }
        return filename;
    }
    
    /** Open a UTF8 Reader for a file. 
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream  
     */
    static public Reader openFileUTF8(String filename)  { return openFileReader(filename, utf8); }

    /** Open an ASCII Reader for a file. 
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream  
     */
    static public Reader openFileASCII(String filename)  { return openFileReader(filename, ascii); }

    private static Reader openFileReader(String filename, Charset charset)
    {
        InputStream in = openFile(filename);
        return new InputStreamReader(in, charset);
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
        return new BufferedReader(asUTF8(in));
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
        return new BufferingWriter(w);
    }

    /** Open a file for output - may include adding gzip processing. */
    static public OutputStream openOutputFile(String filename) {
        try { return openOutputFileEx(filename); }
        catch (IOException ex) { IO.exception(ex); return null; }
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
            return System.out;
        if ( filename.startsWith("file:") )
        {
            filename = filename.substring("file:".length());
            filename = IRILib.decode(filename);
        }
        OutputStream out = new FileOutputStream(filename);
        String ext = FilenameUtils.getExtension(filename);
        switch ( ext ) {
            case "":        return out;
            case "gz":      return new GZIPOutputStream(out);
            case "bz2":     return new BZip2CompressorOutputStream(out);
            case "sz":      throw new UnsupportedOperationException("Snappy output");
        }
        return out;
    }
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrap(Writer w)                    { return Writer2.wrap(w); }
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrapUTF8(OutputStream out)        { return wrap(asUTF8(out)); } 
    
    /** Wrap in a general writer interface */ 
    static public AWriter wrapASCII(OutputStream out)       { return wrap(asASCII(out)); } 

    /** Create a print writer that uses UTF-8 encoding */ 
    static public PrintWriter asPrintWriterUTF8(OutputStream out) {
        return new PrintWriter(asUTF8(out)); 
    }

    public static void close(org.apache.jena.atlas.lib.Closeable resource) {
        resource.close();
    }

    public static void closeSilent(org.apache.jena.atlas.lib.Closeable resource) {
        try { resource.close(); } catch (Exception ex) { }
    }
    
    public static void close(java.io.Closeable resource) {
        if ( resource == null )
            return;
        try { resource.close(); } catch (IOException ex) { exception(ex); }
    }
    
    public static void closeSilent(java.io.Closeable resource) {
        if ( resource == null )
            return;
        try { resource.close(); } catch (IOException ex) { }
    }
    
    public static void close(AWriter resource) {
        if ( resource == null )
            return;
        resource.close();
    }
    
    public static void closeSilent(AWriter resource) {
        if ( resource == null )
            return;
        try { resource.close();  } catch (Exception ex) { }
    }

    public static void close(IndentedWriter resource) {
        if ( resource == null )
            return;
        resource.close();
    }
    
    public static void closeSilent(IndentedWriter resource) {
        if ( resource == null )
            return;
        try { resource.close();  } catch (Exception ex) { }
    }

    /** Throw a RuntimeIOException - this function is guaranteed not to return normally */
    public static void exception(String message) {
        throw new RuntimeIOException(message);
    }

    /** Throw a RuntimeIOException - this function is guaranteed not to return normally */
    public static void exception(IOException ex) {
        throw new RuntimeIOException(ex);
    }

    /** Throw a RuntimeIOException - this function is guaranteed not to return normally */
    public static void exception(String msg, IOException ex) {
        throw new RuntimeIOException(msg, ex);
    }
    
    public static void flush(OutputStream out) { 
        if ( out == null )
            return;
        try { out.flush(); } catch (IOException ex) { exception(ex); }
    }
    
    public static void flush(Writer out) {
        if ( out == null )
            return;
        try { out.flush(); } catch (IOException ex) { exception(ex); } 
    }

    public static void flush(AWriter out) {
        if ( out == null )
            return;
        out.flush(); 
    }

    public static byte[] readWholeFile(InputStream in) {
        final int WHOLE_FILE_BUFFER_SIZE = 32*1024; 
        try(ByteArrayOutputStream out = new ByteArrayOutputStream(WHOLE_FILE_BUFFER_SIZE)) {
            byte buff[] = new byte[WHOLE_FILE_BUFFER_SIZE];
            while (true) {
                int l = in.read(buff);
                if ( l <= 0 )
                    break;
                out.write(buff, 0, l);
            }
            return out.toByteArray();
        }
        catch (IOException ex) {
            exception(ex);
            return null;
        }
    }
    
    /** Read a whole file as UTF-8
     * @param filename
     * @return String
     * @throws IOException
     */
    
    public static String readWholeFileAsUTF8(String filename) throws IOException {
        try ( InputStream in = new FileInputStream(filename) ) {
            return readWholeFileAsUTF8(in);
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
            return readWholeFileAsUTF8(r);
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
        final int WHOLE_FILE_BUFFER_SIZE = 32*1024; 
        try(StringWriter sw = new StringWriter(WHOLE_FILE_BUFFER_SIZE)) {
            char buff[] = new char[WHOLE_FILE_BUFFER_SIZE];
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

    /** Write a string to a file as UTF-8. The file is closed after the operation.
     * @param filename
     * @param content String to be written
     * @throws IOException
     */

    public static void writeStringAsUTF8(String filename, String content) throws IOException {
        try ( OutputStream out = IO.openOutputFileEx(filename) ) {
            writeStringAsUTF8(out, content);
            out.flush();
        }
    }

    /** Write a string into an {@link OutputStream} as UTF-8.
     *
     * @param out       OutputStream destination.
     * @param content   String to be written
     * @throws  IOException
     */
    public static void writeStringAsUTF8(OutputStream out, String content) throws IOException {
        Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        w.write(content);
        w.flush();
        // Not close.
    }

    /** String to ByteBuffer as UTF-8 bytes */
    public static ByteBuffer stringToByteBuffer(String str) {
        byte[] b = StrUtils.asUTF8bytes(str);
        return ByteBuffer.wrap(b);
    }

    /** ByteBuffer to String */
    public static String byteBufferToString(ByteBuffer bb) {
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return StrUtils.fromUTF8bytes(b);
    }
    
    public static String uniqueFilename(String directory, String base, String ext) {
        File d = new File(directory);
        if ( !d.exists() )
            throw new IllegalArgumentException("Not found: " + directory);
        try {
            String fn0 = d.getCanonicalPath() + File.separator + base;
            String fn = fn0;
            int x = 1;
            while (true) {
                if ( ext != null )
                    fn = fn + "."+ext;
                File f = new File(fn);
                if ( ! f.exists() )
                    return fn;
                fn = fn0 + "-" + (x++);
            }
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }
    
    /** Delete everything from a {@code Path} start point, including the path itself.
     * This function works on files or directories.
     * This function does not follow symbolic links.
     */  
    public static void deleteAll(Path start) {
        // Walks down the tree and delete directories on the way backup.
        try { 
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        }
        catch (IOException ex) { IO.exception(ex); return; }
    }
}
