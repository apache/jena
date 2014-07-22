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

package org.apache.jena.riot.adapters ;

import java.util.Iterator ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.stream.* ;

import com.hp.hpl.jena.sparql.util.Utils ;

class AdapterLib {
    public static com.hp.hpl.jena.util.TypedStream convert(TypedInputStream in) {
        return new com.hp.hpl.jena.util.TypedStream(in, in.getContentType(), in.getCharset()) ;
    }

    public static LocationMapper copyConvert(com.hp.hpl.jena.util.LocationMapper locMap) {
        LocationMapper lmap2 = new LocationMapper() ;
        if ( locMap == null )
            return null ;

        Iterator<String> sIter1 = locMap.listAltEntries() ;
        for (; sIter1.hasNext();) {
            String k = sIter1.next() ;
            lmap2.addAltEntry(k, locMap.getAltEntry(k)) ;
        }

        Iterator<String> sIter2 = locMap.listAltPrefixes() ;

        for (; sIter2.hasNext();) {
            String k = sIter2.next() ;
            lmap2.addAltEntry(k, locMap.getAltPrefix(k)) ;
        }
        return lmap2 ;
    }

    public static Locator convert(com.hp.hpl.jena.util.Locator oldloc) {
        if ( oldloc instanceof com.hp.hpl.jena.util.LocatorFile ) {
            com.hp.hpl.jena.util.LocatorFile lFile = (com.hp.hpl.jena.util.LocatorFile)oldloc ;
            return new LocatorFile(lFile.getDir()) ;
        }
        if ( oldloc instanceof com.hp.hpl.jena.util.LocatorClassLoader ) {
            com.hp.hpl.jena.util.LocatorClassLoader classLoc = (com.hp.hpl.jena.util.LocatorClassLoader)oldloc ;
            return new LocatorClassLoader(classLoc.getClassLoader()) ;
        }
        if ( oldloc instanceof com.hp.hpl.jena.util.LocatorURL )
            return new LocatorHTTP() ;
        if ( oldloc instanceof com.hp.hpl.jena.util.LocatorZip ) {
            com.hp.hpl.jena.util.LocatorZip zipLoc = (com.hp.hpl.jena.util.LocatorZip)oldloc ;
            return new LocatorZip(zipLoc.getZipFileName()) ;
        }

        throw new RiotException("Unrecognized Locator: " + Utils.className(oldloc)) ;
    }
}
