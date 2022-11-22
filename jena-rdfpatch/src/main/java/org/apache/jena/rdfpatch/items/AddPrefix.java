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

package org.apache.jena.rdfpatch.items;

import org.apache.jena.graph.Node;

public class AddPrefix extends ChangeItem {
    public final Node gn;
    public final String prefix;
    public final String uriStr;

    public AddPrefix(Node gn, String prefix, String uriStr) {
        this.gn = gn;
        this.prefix = prefix;
        this.uriStr = uriStr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gn == null) ? 0 : gn.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + ((uriStr == null) ? 0 : uriStr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AddPrefix other = (AddPrefix)obj;
        if ( gn == null ) {
            if ( other.gn != null )
                return false;
        } else if ( !gn.equals(other.gn) )
            return false;
        if ( prefix == null ) {
            if ( other.prefix != null )
                return false;
        } else if ( !prefix.equals(other.prefix) )
            return false;
        if ( uriStr == null ) {
            if ( other.uriStr != null )
                return false;
        } else if ( !uriStr.equals(other.uriStr) )
            return false;
        return true;
    }
}