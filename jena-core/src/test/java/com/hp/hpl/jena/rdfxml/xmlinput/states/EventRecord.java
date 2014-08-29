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

package com.hp.hpl.jena.rdfxml.xmlinput.states;

class EventRecord {
    String startEvents[];
    String rsltState;
    String rsltCharacter[];  
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(toString(startEvents));
        buf.append(" = "+ rsltState +" " );
        buf.append(toString(rsltCharacter));
        return buf.toString();
    }
    static private String toString(String[] s) {
        StringBuilder buf = new StringBuilder();
        for ( String value : s )
        {
            buf.append( value + " " );
        }
        return buf.toString();
    }
    void checkStar(String st) {
        if (rsltState.equals("*"))
            rsltState = st;
    }
    int triples = 0;
    int objects = 0;
    int preds = 0;
    int scope = 0;
    int reify = 0;
    boolean inited = false;
    public void initCounts() {
        if (!inited) {
            inited = true;
            for ( String aRsltCharacter : rsltCharacter )
            {
                int n = 0;
                try
                {
                    n = Integer.parseInt( aRsltCharacter.substring( 1 ) );
                }
                catch ( RuntimeException e )
                {
                    System.err.println( toString() );
                    throw e;
                }
                switch ( aRsltCharacter.charAt( 0 ) )
                {
                    case 'T':
                        triples = n;
                        break;
                    case 'O':
                        objects = n;
                        break;
                    case 'P':
                        preds = n;
                        break;
                    case 'E':
                        scope = n;
                        break;
                    case 'R':
                        reify = n;
                        break;
                }
            }
        }
        
    }
    public String toEventString() {
        
        return toString(startEvents);
    }
    public String toResultString() {
        return rsltState + " " +toString(rsltCharacter);
    }
}
