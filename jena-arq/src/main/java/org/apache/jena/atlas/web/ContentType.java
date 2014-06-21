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

package org.apache.jena.atlas.web ;

/** A restricted view of MediaType */
public class ContentType
{
    private MediaType           mediaType ;
    private static final String charsetParamName = "charset" ;

    public static ContentType create(String string) {
        if ( string == null )
            return null ;
        ContentType ct = new ContentType(MediaType.create(string)) ;
        return ct ;
    }

    public static ContentType create(String ctString, String charset) {
        MediaType.ParsedMediaType x = MediaType.parse(ctString) ;
        x.params.put(charsetParamName, charset) ;
        return new ContentType(new MediaType(x)) ;
    }

    private ContentType(MediaType m) {
        mediaType = m ;
    }

    public String getContentType() {
        return mediaType.getContentType() ;
    }

    public String getCharset() {
        return mediaType.getCharset() ;
    }

    public String getType() {
        return mediaType.getType() ;
    }

    public String getSubType() {
        return mediaType.getSubType() ;
    }

    public String toHeaderString() {
        return mediaType.toHeaderString() ;
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode()) ;
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
        ContentType other = (ContentType)obj ;
        if ( mediaType == null ) {
            if ( other.mediaType != null )
                return false ;
        } else if ( !mediaType.equals(other.mediaType) )
            return false ;
        return true ;
    }

    @Override
    public String toString() {
        return mediaType.toString() ;
    }
}
