/*
 * Copyright (C) 2023 Telicent Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package devtelicent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class TestLibraryVersion {

    @Test
    public void library_version_unknown() {
        String unknownVersion = LibraryVersion.get("some-library");
        Assert.assertEquals(unknownVersion, LibraryVersion.UNKNOWN);
    }

    @Test
    public void library_version_01() {
        String version = LibraryVersion.get("malformed");
        Assert.assertEquals(version, LibraryVersion.UNKNOWN);
    }

    @Test
    public void library_version_list_cached() {
        LibraryVersion.resetCaches();
        LibraryVersion.get("foo");
        LibraryVersion.get("bar");
        LibraryVersion.get("malformed");
        LibraryVersion.get("unknown-library");

        Set<String> libraries = LibraryVersion.cachedLibraries();
        Assert.assertEquals(libraries.size(), 4);
    }
}
