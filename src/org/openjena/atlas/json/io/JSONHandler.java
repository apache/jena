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

package org.openjena.atlas.json.io;

public interface JSONHandler
{
    void startParse() ;
    void finishParse() ; 

    
    void startObject() ;
    void finishObject() ; 

    // "Pair" is the name used in the JSON spec. 
    void startPair() ;
    void keyPair() ;
    void finishPair() ; 

    void startArray() ;
    void element() ;
    void finishArray() ;
    
    void valueString(String image) ;
    void valueInteger(String image) ;
    void valueDouble(String image) ;
    void valueBoolean(boolean b) ;
    void valueNull() ;
    void valueDecimal(String image) ;
}
