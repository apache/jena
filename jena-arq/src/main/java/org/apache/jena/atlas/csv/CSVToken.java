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

package org.apache.jena.atlas.csv;

import static org.apache.jena.atlas.csv.CSVTokenType.COMMA ;
import static org.apache.jena.atlas.csv.CSVTokenType.EOF ;
import static org.apache.jena.atlas.csv.CSVTokenType.NL ;
import org.apache.jena.atlas.lib.Lib ;

class CSVToken
{
    final CSVTokenType type ;
    final String    image ;
    final long      line ;
    final long      col ;

    public boolean same(CSVToken obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( type != obj.type )
            return false ;
        if ( type == COMMA || type == NL || type == EOF )
            return true ;

        if ( image == null && obj.image != null )
            return false ;
        return Lib.equal(this.image, obj.image) ;
    }

    public CSVToken(long line, long col, CSVTokenType type, String image) {
        super() ;
        this.type = type ;
        this.image = image ;
        this.line = line ;
        this.col = col ;
    }

    @Override
    public String toString() {
        switch (type) {
            case STRING :
            case QSTRING :
                return "Token [" + line + ", " + col + "] " + type + " |" + image + "|" ;
            default :
                return "Token [" + line + ", " + col + "] " + type ;
        }
    }
}
