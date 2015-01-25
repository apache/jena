/*
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

package quack;

import java.io.File ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.sys.Names ;

/** Index name, as location and index name within location */ 
public class IndexRef {
    private final Location location ;
    private final String indexName ;
    private final String filename ;

    private static final String patternStr = "^([A-Z]+)(?:-(\\d*))?$" ;
    private static final Pattern pattern = Pattern.compile(patternStr) ;
    
    /** Create from a string of the form: location/name */
    public static IndexRef parse(String string) {
        int i = string.lastIndexOf(File.separatorChar) ;
        if ( i < 0 )
            return IndexRef.parse(Location.create("."), string) ;
        String x = string.substring(0,i+1) ;
        String y = string.substring(i+1) ;
        return IndexRef.parse(Location.create(x), y) ; 
    }
    
    public static IndexRef parse(Location location, String string) {
        Matcher m = pattern.matcher(string);
        if ( ! m.matches() )
            throw new IllegalArgumentException("Can't grok index name string. '"+string+"'") ;
        String filename = m.group(0) ;
        String idxName = m.group(1) ;
        String shard =  m.group(2) ;
        return new IndexRef(location, idxName, filename) ; 
    }

    private IndexRef(String location, String name, String filename) {
        this(Location.create(location), name, filename) ;
    }

    public IndexRef(Location location, String indexName) {
        this(location, indexName, indexName) ;
    }
    
    public IndexRef(Location location, String indexName, String filename) {
        super() ;
        this.location = location ;
        this.indexName = indexName ;
        this.filename = filename ;
        if ( indexName.length() != 3 && indexName.length() != 4 )
            throw new IllegalArgumentException("Index name is not 3 or 4. '"+indexName+"'") ; 
        
    }
    
    public Location getLocation() {
        return location ;
    }

    public String getIndexName() {
        return indexName ;
    }

    public String getBaseFileName() {
        return filename ;
    }

    public String getFileName() {
        return location.getPath(filename) ;
    }

    @Override
    public String toString() {
        return String.format("%s/%s[%s]", location.getDirectoryPath(), filename, indexName) ;
    }

    public boolean exists() {
        return location.exists(filename, Names.bptExtTree) &&
               location.exists(filename, Names.bptExtRecords) ;
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode()) ;
        result = prime * result + ((indexName == null) ? 0 : indexName.hashCode()) ;
        result = prime * result + ((location == null) ? 0 : location.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        IndexRef other = (IndexRef)obj ;
        if ( filename == null ) {
            if ( other.filename != null )
                return false ;
        } else if ( !filename.equals(other.filename) )
            return false ;
        if ( indexName == null ) {
            if ( other.indexName != null )
                return false ;
        } else if ( !indexName.equals(other.indexName) )
            return false ;
        if ( location == null ) {
            if ( other.location != null )
                return false ;
        } else if ( !location.equals(other.location) )
            return false ;
        return true ;
    }
    
}

