/**
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

package org.apache.jena.riot.adapters;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.stream.*;

class AdapterLib {
    public static org.apache.jena.util.TypedStream convert(TypedInputStream in) {
        return new org.apache.jena.util.TypedStream(in, in.getContentType(), in.getCharset());
    }

    public static LocationMapper copyConvert(org.apache.jena.util.LocationMapper locMap) {
        if ( locMap == null )
            return null;
        LocationMapper lmap2 = new LocationMapper();
        locMap.listAltEntries().forEachRemaining(k->lmap2.addAltEntry(k, locMap.getAltEntry(k)));
        locMap.listAltPrefixes().forEachRemaining(k->lmap2.addAltPrefix(k, locMap.getAltPrefix(k)));
        return lmap2;
    }

    @SuppressWarnings("deprecation")
    public static Locator convert(org.apache.jena.util.Locator oldloc) {
        if ( oldloc instanceof org.apache.jena.util.LocatorFile lFile )
            return new LocatorFile(lFile.getDir());
        if ( oldloc instanceof org.apache.jena.util.LocatorClassLoader classLoc )
            return new LocatorClassLoader(classLoc.getClassLoader());
        if ( oldloc instanceof org.apache.jena.util.LocatorURL )
            return new LocatorHTTP();
        if ( oldloc instanceof org.apache.jena.util.LocatorZip zipLoc )
            return new LocatorZip(zipLoc.getZipFileName());
        throw new RiotException("Unrecognized Locator: " + Lib.className(oldloc));
    }
}
