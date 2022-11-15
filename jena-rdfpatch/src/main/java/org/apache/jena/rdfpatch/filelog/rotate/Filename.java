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

import java.nio.file.Path;
import java.util.Objects;

import org.apache.jena.atlas.logging.FmtLog;

/**
 * A Structured filename with components: (directory, basename, separator, modifier, compression)
 * some of which may be null.
 * {@code separator} is between basename and modifier.
 * compression is "gz", bz2" etc.
 */
public class Filename {
    public final Path directory;
    public final String basename;
    public final String separator;
    public final String modifier;
    public final String compression;
    private Path absolute;

    private static String SEP = ".";

    public Filename(Path directory, String basename, String separator, String modifier, String compression) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(basename, "basename");
        this.directory = directory;
        this.basename = basename;

        if ( 1 == countNonNulls(separator, modifier) ) {
            FmtLog.warn(FileMgr.LOG, "Both separator and modifier must be set, or both be null: (s=%s, m=%s)", separator, modifier);
            separator = null;
            modifier = null;
        }

        this.separator = separator;
        this.modifier = modifier;
        if ( compression != null && compression.startsWith(SEP) )
            compression.substring(SEP.length());
        this.compression = compression;
        this.absolute = null;
    }

    private Path toAbsolutePath() {
        String fn = asFilenameString();
        return directory.resolve(fn).toAbsolutePath();
    }

    public boolean isBasename() {
        return modifier==null || separator==null;
    }

    public boolean isCompressed() {
        return compression != null;
    }

    /** As a filename, without directory. */
    public String asFilenameString() {
        String fn = basename;
        if ( ! isBasename() )
            fn = fn+separator+modifier;
        if ( isCompressed() )
            fn = fn+SEP+compression;
        return fn;
    }

    /** As a absolute file system filename. */
    public Path absolute() {
        if ( absolute == null )
            absolute = toAbsolutePath();
        return absolute;
    }

    // display version.
    @Override
    public String toString() {
        //String fn = directory+"  "+basename;
        String fn = basename;
        //String MARK = "|";
        String MARK = "";
        if ( ! isBasename() )
            fn = fn + MARK + separator + MARK + modifier;
        if ( isCompressed() )
            fn = fn + MARK + compression;
        return fn;
    }

    private static int countNonNulls(Object ... objects) {
        int x = 0;
        for ( Object obj : objects ) {
            if ( obj != null )
                x++;
        }
        return x;
    }
}