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

package org.apache.jena.dboe.sys;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.dboe.base.file.Location;

public class IO_DB {
    // Location == org.apache.jena.dboe.base.file.Location

    /** Convert a {@link Path}  to a {@link Location}. */
    public static Location asLocation(Path path) {
        Objects.requireNonNull(path, "IOX.asLocation(null)");
        if ( ! Files.isDirectory(path) )
            throw new RuntimeIOException("Path is not naming a directory: "+path);
        return Location.create(path.toString());
    }

    /** Convert a {@link Location} to a {@link Path}. */
    public static Path asPath(Location location) {
        if ( location.isMem() )
            throw new RuntimeIOException("Location is a memory location: "+location);
        return Paths.get(location.getDirectoryPath());
    }

    /** Convert a {@link Location} to a {@link File}. */
    public static File asFile(Location loc) {
        return new File(loc.getDirectoryPath());
    }
}
