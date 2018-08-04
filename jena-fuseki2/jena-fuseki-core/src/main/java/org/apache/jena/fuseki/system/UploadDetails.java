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

package org.apache.jena.fuseki.system;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;

/** Record of an upload */
public class UploadDetails {
    public enum PreState { EXISTED, ABSENT, UNKNOWN } 
    
    private final long count ;
    private final long tripleCount ;
    private final long quadCount ;
    private PreState state = PreState.UNKNOWN ;
    
    /*package*/ UploadDetails(long parserCount, long parserTripleCount, long parserQuadCount) {
        this.count = parserCount ;
        this.tripleCount = parserTripleCount ;
        this.quadCount = parserQuadCount ;
    }
    
    public static String detailsStr(long count, long tripleCount, long quadCount) {
        return String.format("Count=%d Triples=%d Quads=%d", count, tripleCount, quadCount) ;
    }
    
    public String detailsStr() {
        return detailsStr(count, tripleCount, quadCount) ;
    }
    
    public static String jCount = "count" ; 
    public static String jTriplesCount = "tripleCount" ; 
    public static String jQuadsCount = "quadCount" ; 
    
    public static JsonValue detailsJson(long count, long tripleCount, long quadCount) {
        JsonBuilder b = new JsonBuilder() ;
        b.startObject("details") ;
        b.key(jCount).value(count) ;
        b.key(jTriplesCount).value(tripleCount) ;
        b.key(jQuadsCount).value(quadCount) ;
        b.finishObject("details") ;
        return b.build() ;
    }

    public JsonValue detailsJson() {
        return detailsJson(count, tripleCount, quadCount) ;
    }

    public long getCount() {
        return count ;
    }

    public long getTripleCount() {
        return tripleCount ;
    }

    public long getQuadCount() {
        return quadCount ;
    }

    public void setExistedBefore(boolean existedBefore) {
        if ( existedBefore )
            setExistedBefore(PreState.EXISTED) ;
        else
            setExistedBefore(PreState.ABSENT) ;
    }
    public void setExistedBefore(PreState state) { this.state = state ; }
    
    public PreState getExistedBefore() { return state ; }
}
