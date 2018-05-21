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

package org.apache.jena.tdb2.sys;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function ;
import java.util.regex.Pattern;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  PathX
 */
public class IOX {
    
    public static Path currentDirectory = Paths.get(".");
    
    /** A Consumer that can throw {@link IOException}. */
    public interface IOConsumer<X> {
        void actionEx(X arg) throws IOException;
    }

    /** Convert an {@link IOException} into a {@link RuntimeIOException}.
     * <p>
     * Idiom:
     * <pre>
     *     catch(IOException ex) { throw new exception(ex); } 
     * </pre>
     * @param ioException
     * @return RuntimeIOException
     */
    public static RuntimeIOException exception(IOException ioException) {
        return new RuntimeIOException(ioException);
    }
    
    /** Convert an {@link IOException} into a {@link RuntimeIOException}.
     * <p>
     * Idiom:
     * <pre>
     *     catch(IOException ex) { throw new exception("Oh dear", ex); } 
     * </pre>
     * @param message
     * @param ioException
     * @return RuntimeIOException
     */
    public static RuntimeIOException exception(String message, IOException ioException) {
        return new RuntimeIOException(message, ioException);
    }
    
    /** Write a file safely - the change happens (the function returns true) or
     * something went wrong (the function throws a runtime exception) and the file is not changed.
     * Note that the tempfile must be in the same direct as the actual file so an OS-atomic rename can be done.  
     */
    public static boolean safeWrite(Path file, IOConsumer<OutputStream> writerAction) {
        Path tmp = createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
        return safeWrite(file, tmp, writerAction);
    }

    /** Write a file safely - the change happens (the function returns true) or
     * something went wrong (the function throws a runtime exception) and the file is not changed.
     * Note that the tempfile must be in the same direct as the actual file so an OS-atomic rename can be done.  
     */
    public static boolean safeWrite(Path file, Path tmpFile, IOConsumer<OutputStream> writerAction) {
        try {
            try(OutputStream out = new BufferedOutputStream(Files.newOutputStream(tmpFile)) ) {
                writerAction.actionEx(out);
            }
            move(tmpFile, file);
            return true;
        } catch(IOException ex) { throw IOX.exception(ex); }
    }

    /** Delete a file. */
    public static void delete(Path path) {
        try { Files.delete(path); }
        catch (IOException ex) {
            FmtLog.error(IOX.class, ex, "IOException deleting %s", path);
            throw IOX.exception(ex);
        }
    }
    
    /** Atomically move a file. */
    public static void move(Path src, Path dst) {
        try { Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE) ; }
        catch (IOException ex) {
            FmtLog.error(IOX.class, ex, "IOException moving %s to %s", src, dst);
            throw IOX.exception(ex);
        }
    }
    
    /** Copy a file, not atomic. *
     * Can copy to a directory or over an existing file.
     * @param srcFilename
     * @param dstFilename
     */
    public static void copy(String srcFilename, String dstFilename) {
        Path src = Paths.get(srcFilename);
        if ( ! Files.exists(src) )
            throw new RuntimeIOException("No such file: "+srcFilename);
        
        Path dst = Paths.get(dstFilename);
        if ( Files.isDirectory(dst) )
            dst = dst.resolve(src.getFileName());
        
        try { Files.copy(src, dst); }
        catch (IOException ex) {
            FmtLog.error(IOX.class, ex, "IOException copying %s to %s", srcFilename, dstFilename);
            throw IOX.exception(ex);
        }
    }
    
    /** Create a directory - thgrow a runtime exception if there are any problems.
     * This function wraps {@code Files.createDirectory}.
     */
    public static void createDirectory(Path dir) {
        try { Files.createDirectory(dir); }
        catch (IOException ex) { throw IOX.exception(ex); }
    }

    /** Convert a {@link Path}  to a {@link Location}. */
    public static Location asLocation(Path path) {
        if ( ! Files.isDirectory(path) )
            throw new RuntimeIOException("Path is not naming a directory: "+path);
        return Location.create(path.toString());
    }
    
//    /** Convert a {@link org.apache.jena.tdb.base.file.Location} to a {@link Path}. */
//    public static Path asPath(org.apache.jena.tdb.base.file.Location location) {
//        if ( location.isMem() )
//            throw new RuntimeIOException("Location is a memory location: "+location);
//        return Paths.get(location.getDirectoryPath());
//    }

    /** Convert a {@link Location} to a {@link Path}. */
    public static Path asPath(org.apache.jena.dboe.base.file.Location location) {
        if ( location.isMem() )
            throw new RuntimeIOException("Location is a memory location: "+location);
        return Paths.get(location.getDirectoryPath());
    }

    /** Convert a {@link Location} to a {@link File}. */
    public static File asFile(Location loc) {
        return new File(loc.getDirectoryPath());
    }

    /** Read the whole of a file */
    public static byte[] readAll(Path pathname) {
        try {
            return Files.readAllBytes(pathname);
        } catch (IOException ex) { throw IOX.exception(ex); }
    }
    
    /** Write the whole of a file */
    public static void writeAll(Path pathname, byte[] value) {
        try {
            Files.write(pathname, value, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException ex) { throw IOX.exception(ex); }
    }

//    /**
//     * Ensure a directory exists, creating the directory and any missing
//     * directories on the path to it.
//     */
//    public static void ensureDirectory(Path dir, FileAttribute<? >... attrs) {
//        if ( Files.exists(dir) ) {
//            if ( ! Files.isDirectory(dir) )
//                throw new DeltaFileException("Exists but not a directory: "+dir);
//        }
//        try { Files.createDirectories(dir, attrs); }
//        catch (IOException ex) { new DeltaFileException("Failed to create directory: "+dir, ex);}
//    }
//    
//    /**
//     * Ensure a file exists - create an empty one if not. This operation does
//     * not create a directory path to the file.
//     */
//    public static void ensureFile(Path filePath, FileAttribute<? >... attrs) {
//        if ( Files.exists(filePath) ) {
//            if ( ! Files.isRegularFile(filePath) )
//                throw new DeltaFileException("Exists but not a regular file: "+filePath);
//        }
//        try { Files.createFile(filePath); }
//        catch (IOException ex) { new DeltaFileException("Failed to create file: "+filePath, ex);}
//    }

    /**
     * Return a temporary filename path.
     * <p>
     * This operation is thread-safe.
     */
    public static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<? >... attrs) {
        try {
            return Files.createTempFile(dir, prefix, suffix, attrs);
        } catch (IOException ex) { throw IOX.exception(ex); }
    }
    
    /** Generate a unique place related to path; 
     * Optionally, provide a mapping of old name to new namebase.
     * This method always adds "-1", "-2" etc. 
     */  
    public static Path uniqueDerivedPath(Path path, Function<String, String> basenameMapping) {
        String base = path.getFileName().toString();
        if ( basenameMapping != null )
            base = basenameMapping.apply(base);
        // Some large limit "just in case"
        for(int x = 0 ; x < 10_000 ; x++ ) {
            String destname = base+"-"+x; 
            Path destpath = path.resolveSibling(destname);
            if ( ! Files.exists(destpath) )
                return destpath;
        }
        return null ;
    }
    
    private static Logger LOG = LoggerFactory.getLogger(Util.class);
    
    /** Find the files in this directory that have namebase as a prefix and 
     *  are then numbered.
     *  <p>
     *  Returns a sorted list from, low to high index.
     */
    public static List<Path> scanForDirByPattern(Path directory, String namebase, String nameSep) {
        Pattern pattern = Pattern.compile(Pattern.quote(namebase)+
                                          Pattern.quote(nameSep)+
                                          "[\\d]+");
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, namebase + "*")) {
            for ( Path entry : stream ) {
                if ( !pattern.matcher(entry.getFileName().toString()).matches() ) {
                    throw new DBOpEnvException("Invalid filename for matching: "+entry.getFileName());
                    // Alternative: Skip bad trailing parts but more likely there is a naming problem.  
                    //   LOG.warn("Invalid filename for matching: {} skipped", entry.getFileName());
                    //   continue;
                }
                // Follows symbolic links.
                if ( !Files.isDirectory(entry) )
                    throw new DBOpEnvException("Not a directory: "+entry);
                paths.add(entry);
            }
        }
        catch (IOException ex) {
            FmtLog.warn(LOG, "Can't inspect directory: (%s, %s)", directory, namebase);
            throw new DBOpEnvException(ex);
        }
        Comparator<Path> comp = (f1, f2) -> {
            int num1 = extractIndex(f1.getFileName().toString(), namebase, nameSep);
            int num2 = extractIndex(f2.getFileName().toString(), namebase, nameSep);
            return Integer.compare(num1, num2);
        };
        paths.sort(comp);
        //indexes.sort(Long::compareTo);
        return paths;
    }

    /** Given a filename in "base-NNNN" format, return the value of NNNN */
    public static int extractIndex(String name, String namebase, String nameSep) {
        int i = namebase.length()+nameSep.length();
        String numStr = name.substring(i);
        int num = Integer.parseInt(numStr);
        return num;
    }

}
