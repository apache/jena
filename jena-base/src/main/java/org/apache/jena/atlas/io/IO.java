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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.StrUtils;

public class IO
{
    public static final int EOF = -1;
    public static final int UNSET = -2;

    /** Open an input stream to a file.
     * <p>
     * If the filename is null or "-", return System.in
     * If the filename ends in .gz, wrap in  GZIPInputStream
     * <p>
     * Throws {@link RuntimeIOException} on failure to open.
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
     * <p>
     * Throws {@link RuntimeIOException} on failure to open.
     */
    static public InputStream openFileBuffered(String filename) {
        InputStream in = openFile(filename);
        return ensureBuffered(in);
    }

    private static final String ext_gz = "gz";
    private static final String ext_bz2 = "bz2";
    private static final String ext_sz = "sz";

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
            filename = IRILib.decodeHex(filename);
        }
        InputStream in = new FileInputStream(filename);
        String ext = getExtension(filename);
        switch ( ext ) {
            case "":        return in;
            case ext_gz:    return new GZIPInputStream(in);
            case ext_bz2:   return new BZip2CompressorInputStream(in);
            case ext_sz:    return new SnappyCompressorInputStream(in);
        }
        return in;
    }

    // ---- Extracted from Apache CommonsIO : FilenameUtils (2.8.0) because of the drive letter handling.
    private static final int NOT_FOUND = -1;
    private static final String EMPTY_STRING = "";
    private static final String EXTENSION_SEPARATOR = ".";
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    private static int indexOfLastSeparator(final String fileName) {
        if (fileName == null) {
            return NOT_FOUND;
        }
        final int lastUnixPos = fileName.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = fileName.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    private static int indexOfExtension(final String fileName) throws IllegalArgumentException {
        if (fileName == null) {
            return NOT_FOUND;
        }
//        if (isSystemWindows()) {
//            // Special handling for NTFS ADS: Don't accept colon in the fileName.
//            final int offset = fileName.indexOf(':', getAdsCriticalOffset(fileName));
//            if (offset != -1) {
//                throw new IllegalArgumentException("NTFS ADS separator (':') in file name is forbidden.");
//            }
//        }
        final int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        final int lastSeparator = indexOfLastSeparator(fileName);
        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    private static String getExtension(final String fileName) {
        if (fileName == null) {
            return null;
        }
        final int index = indexOfExtension(fileName);
        if (index == -1) {
            return "";
        }
        return fileName.substring(index + 1);
    }

    // ---- Apache CommonsIO : FilenameUtils

    /**
     * The filename without any compression extension, or the original filename.
     * It tests for compression types handled by {@link #openFileEx}.
     */
    static public String filenameNoCompression(String filename) {
        String ext = getExtension(filename);
        switch ( ext ) {
            case EMPTY_STRING:
                return filename;
            case ext_gz:
            case ext_bz2:
            case ext_sz:
                // +1 for the "."
                return filename.substring(0, filename.length()-(ext.length()+1));
        }
        return filename;
    }

    /** Open a UTF8 Reader for a file.
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream
     */
    static public Reader openFileUTF8(String filename) {
        return openFileReader(filename, StandardCharsets.UTF_8);
    }

    /** Open an ASCII Reader for a file.
     * If the filename is null or "-", use System.in
     * If the filename ends in .gz, use GZIPInputStream
     */
    static public Reader openFileASCII(String filename) {
        return openFileReader(filename, StandardCharsets.US_ASCII);
    }

    private static Reader openFileReader(String filename, Charset charset)
    {
        InputStream in = openFile(filename);
        return new InputStreamReader(in, charset);
    }

    /** Create an unbuffered reader that uses UTF-8 encoding */
    static public Reader asUTF8(InputStream in) {
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    /** Create a unbuffered reader that uses ASCII encoding */
    static public Reader asASCII(InputStream in) {
        return new InputStreamReader(in, StandardCharsets.US_ASCII);
    }

    /** Create an buffered reader that uses UTF-8 encoding */
    static public BufferedReader asBufferedUTF8(InputStream in) {
        // Alway buffered - for readLine.
        return new BufferedReader(asUTF8(in), BUFSIZE_IN / 2);
    }

    /** Create a writer that uses UTF-8 encoding */
    static public Writer asUTF8(OutputStream out) {
        return new OutputStreamWriter(out, StandardCharsets.UTF_8);
    }

    /** Create a writer that uses ASCII encoding */
    static public Writer asASCII(OutputStream out) {
        return new OutputStreamWriter(out, StandardCharsets.US_ASCII);
    }

    /** Create a writer that uses UTF-8 encoding and is buffered. */
    static public Writer asBufferedUTF8(OutputStream out) {
        Writer w =  new OutputStreamWriter(out, StandardCharsets.UTF_8);
        return ensureBuffered(w);
    }

    /**
     * Open a file for output - may include adding gzip processing.
     * <p>
     * Throws {@link RuntimeIOException} on failure to open.
     */
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
            filename = IRILib.decodeHex(filename);
        }
        OutputStream out = new FileOutputStream(filename);
        String ext = getExtension(filename);
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

    public static boolean isEmptyDirectory(String directory) {
        Path path = Path.of(directory);
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            return !dirStream.iterator().hasNext();
        }
        catch (NotDirectoryException ex) { return false ; }
        catch (IOException ex) { IO.exception(ex); return false; }
    }

    public static boolean exists(String directory) {
        Path path = Path.of(directory);
        return Files.exists(path);
    }

    public static boolean isDirectory(String directory) {
        Path path = Path.of(directory);
        return Files.isDirectory(path);
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

    private static final int BUFSIZE_IN   = 128*1024 ;
    private static final int BUFSIZE_OUT  = 128*1024; ;
    private static final int WHOLE_FILE_BUFFER_SIZE = 32*1024;

    public static InputStream ensureBuffered(InputStream input) {
        if ( input instanceof BufferedInputStream )
            return input;
        if ( input instanceof ByteArrayInputStream )
            return input;
        return new BufferedInputStream(input, BUFSIZE_IN);
    }

    public static Reader ensureBuffered(Reader input) {
        if ( input instanceof BufferedReader )
            return input;
        if ( input instanceof StringReader )
            return input;
        return new BufferedReader(input, BUFSIZE_IN / 2);
    }

    public static OutputStream ensureBuffered(OutputStream output) {
        if ( output instanceof BufferedOutputStream )
            return output;
        if ( output instanceof ByteArrayOutputStream )
            return output;
        return new BufferedOutputStream(output, BUFSIZE_OUT);
    }

    public static Writer ensureBuffered(Writer output) {
        if ( output instanceof BufferedWriter )
            return output;
        if ( output instanceof StringWriter )
            return output;
        return new BufferedWriter(output, BUFSIZE_OUT / 2);
    }

    public static byte[] readWholeFile(InputStream in) {
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
     */

    public static String readWholeFileAsUTF8(String filename) {
        try ( InputStream in = new FileInputStream(filename) ) {
            return readWholeFileAsUTF8(in);
        } catch (IOException ex) {
            IO.exception(ex);
            return null;
        }
    }

    /** Read a whole stream as UTF-8
     *
     * @param in    InputStream to be read
     * @return      String
     */
    public static String readWholeFileAsUTF8(InputStream in) {
        // Don't buffer - we're going to read in large chunks anyway
        try ( Reader r = asUTF8(in) ) {
            return readWholeFileAsUTF8(r);
        } catch (IOException ex) {
            IO.exception(ex);
            return null;
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

    // Do nothing buffer.  Never read from this, it may be corrupt because it is shared.
    private static int SKIP_BUFFER_LEN = 64*1024;
    private static byte[] SKIP_BUFFER = null;
    /** Skip to the end of the InputStream, discarding input. */
    public static void skipToEnd(InputStream input) {
        if ( SKIP_BUFFER == null )
            // No harm in concurrent assignment.
            SKIP_BUFFER = new byte[SKIP_BUFFER_LEN];
        try {
            for(;;) {
                // Skip does not guarantee to go to end of file.
                long rLen = input.read(SKIP_BUFFER, 0, SKIP_BUFFER_LEN);
                if (rLen < 0) // EOF
                    break;
            }
        } catch (IOException ex) {}
    }
}
