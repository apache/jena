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

package org.apache.jena.rdfpatch.filelog.rotate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.rdfpatch.PatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMgr {
    static Logger LOG = LoggerFactory.getLogger(FileMgr.class);

    private static final int IDX_TRIES = 1000;

    public static Path freshFilename(Path directory, String filename) {
        return freshFilename(directory, filename, 0, INC_SEP, "%d");
    }

    /** Find a unique file name, assumes that it will not take too many probes.
     * Conceptually, ".0" is the base filename.
     * This function does not create the file.
     * Returns the full file name, directory included.
     *
     * @param directory Directory to look in
     * @param filename  Base file name, without index modifer
     * @param startingFrom  Begin probing at index
     * @param format String format of the number as modifier e.g. "%03d"
     */

    public static Path freshFilename(Path directory, String filename, int startingFrom, String sep, String format) {
        for ( int idx = startingFrom; idx < IDX_TRIES+startingFrom; idx++ ) {
            String fn = ( idx == 0 ) ? filename : basename(filename, idx, sep, format);
            Path p = directory.resolve(fn);
            if ( ! Files.exists(p) )
                return p;
        }
        FmtLog.warn(LOG, "Failed to find a unique file extension name for "+filename);
        return null;
    }

    /** Find files matching a pattern.
     * The pattern has groups:
     * <ul>
     * <li>1 : the base filename.
     * <li>2 : the separator
     * <li>3 : the policy modifier.
     * </ul>
     *
     * @param directory Path
     * @param namebase base name of interest
     * @param pattern Regex to extract the part of the filename for a {@link Filename}
     * @return Unsorted List&lt;Filename&gt; of matches.
     */
    public static <X> List<Filename> scan(Path directory, String namebase, Pattern pattern) {
        // pattern must have 3 groups.
        if ( ! Files.isDirectory(directory) ) {
            FmtLog.error(LOG, "Not a directory: %s", directory);
            throw new FileRotateException("Not a directory: "+directory.toString());
        }
        List<Filename> indexes = new ArrayList<>();
        // Crude filtering by Files.newDirectoryStream because we will process again to extract the parts.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, namebase+"*")) {
            for ( Path f : stream ) {
                Filename filename = fromPath(directory, f, pattern);
                if ( filename != null )
                    indexes.add(filename);
            }
        } catch (IOException ex) {
            FmtLog.warn(LOG, "Can't inspect directory: (%s, %s)", directory, namebase);
            throw new PatchException(ex);
        }
        return indexes;
    }

    /**
     * Find files matching a pattern and also include the base filename.
     */
    public static List<Filename> scanIncludeBase(Path directory, String filename, Pattern pattern) {
        List<Filename> filenames = scan(directory, filename, pattern);
        if ( Files.exists(directory.resolve(filename)) ) {
            Filename fn = new Filename(directory, filename, null, null, null);
            filenames.add(fn);
        }
        //[gz]
        return filenames;
    }

    /** Create a {@link Filename} */
    private static Filename fromPath(Path directory, Path filepath, Pattern pattern) {
        filepath = directory.resolve(filepath).getFileName();
        directory = directory.resolve(filepath).getParent();
        return fromPath(directory, filepath.getFileName().toString(), pattern);
    }

    /** Create a {@link Filename} */
    /*package*/ public static Filename fromPath(Path directory, String fn, Pattern pattern) {
        Matcher matcher = pattern.matcher(fn);
        if ( ! matcher.matches() )
            return null;
        if ( matcher.groupCount() != 3 ) {
            FmtLog.info(LOG, "Match but wrong groups: "+fn);
            return null;
        }

        String basename = matcher.group(1);
        String separator = matcher.group(2);
        String modifier = matcher.group(3);
        String compression = null;
        if ( matcher.groupCount() >= 4 )
            compression = matcher.group(4);

        return new Filename(directory, basename, separator, modifier, compression);
    }

    /** Shift files with a ".NNN" up by one, and move the base file to "filename.1".
     *
     * @param directory
     * @param filename
     */
    public static void shiftFiles(Path directory, String filename) {
        shiftFiles(directory, filename, 1, "%d");
    }

    /** Match an incremental file (does not match the base file name). **/
    /*package*/ static Pattern patternIncremental = Pattern.compile("(.*)(\\.)(\\d+)");

    /*package*/ static final String INC_SEP = ".";
    /** Compare, and it there is no modifier, put in 0 */
    /*package*/ static Comparator<Filename> cmpNumericModifier = (x,y)->{
        long vx = indexFromFilename(x);
        long vy = indexFromFilename(y);
        return Long.compare(vx, vy);
    };

    /*package*/ static long indexFromFilename(Filename filename) {
        if ( filename.isBasename() )
            return 0;
        return Long.parseLong(filename.modifier);
    }

    /**
     * Look for matching files of the "incremental" pattern: "filename.nnn". This
     * includes the filename itself, if it exists.
     */
    public static List<Filename> scanForIncrement(Path directory, String filename) {
        return scanIncludeBase(directory, filename, patternIncremental);
    }

    /**
     * @param directory
     * @param filename
     * @param increment
     * @param format
     */
    public static void shiftFiles(Path directory, String filename, int increment, String format) {
        if ( increment <= 0 )
            throw new IllegalArgumentException("Increment must be positive: got "+increment);

        // Move files of the form "NAME" and "NAME.num" up
        List<Filename> files = scanForIncrement(directory, filename);
        Collections.sort(files, cmpNumericModifier.reversed());
        // Guava: Lists.reverse(List) -- is a view.

        // Pass 1 : check the list of files (checks rebuilding file names)
        for ( Filename fn : files ) {
            Path src = directory.resolve(fn.asFilenameString());
            if ( ! Files.exists(src) )
                throw new FileRotateException("Does not exist: "+src);
            if ( ! fn.isBasename() )
                // Check parsing.
                Integer.parseInt(fn.modifier);
        }

        // Pass 2 : do it.

        for ( Filename fn : files ) {
            Path src1 = fn.absolute();
            Path src = directory.resolve(fn.asFilenameString());
            if ( ! Files.exists(src) )
                throw new FileRotateException("Does not exist: "+src);
            long idx = fn.isBasename() ? 0L : Integer.parseInt(fn.modifier);
            long idx2 = idx+increment;
            String target = String.format("%s%s"+format, fn.basename, INC_SEP, idx2);
            Path dst = directory.resolve(target);
            try {
                Files.move(src, dst);
            }
            catch (IOException e) { IO.exception(e); }
        }
    }



    /** Create a file name using the base and the numeric modifier, converted to a number using the format */
    /*package*/ static String basename(String base, long idx, String sep, String modFormat) {
        return String.format("%s%s"+modFormat, base, sep, idx);
     }

}
