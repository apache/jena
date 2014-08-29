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

class FullEventRecord extends EventRecord {
    String state;
    EventRecord moreCharacter[];
    FullEventRecord(String fields[]) {
        this.fields = fields;
        state = fields[0];
        ix = 1;
        startEvents = upto("$");
        rsltState = fields[ix++];
        rsltCharacter = upto("{");
        moreCharacter = new EventRecord[count(";")];
        for (int i=0;i<moreCharacter.length;i++){
            moreCharacter[i] = new EventRecord();
            moreCharacter[i].startEvents = upto("$");
            moreCharacter[i].rsltState = fields[ix++];
            moreCharacter[i].rsltCharacter = upto(";");
            moreCharacter[i].checkStar(state);
        }
        checkStar(state);
        
    }
    @Override
    public String toString() {
        StringBuilder rslt = new StringBuilder( "|"+state+"| " +  super.toString());
        rslt.append("{ ");
        for ( EventRecord aMoreCharacter : moreCharacter )
        {
            rslt.append( aMoreCharacter.toString() + " ; " );
        }
        rslt.append(" }");
        return rslt.toString();
          
    }
    private int count(String sep) {
        int rslt = 0;
        for (int i = 0; i+ix <fields.length; i++)
            if (sep.equals(fields[i+ix]))
                rslt++;
        return rslt;
    }
    private String[] upto(String sep) {
        String rslt[] = new String[dist(sep)];
        System.arraycopy(fields,ix,rslt,0,rslt.length);
        ix += rslt.length + 1;
        return rslt;
    }
    private int dist(String sep) {
        int rslt;
        if (ix >= fields.length)
            return 0;
        for (rslt = 0; !sep.equals(fields[ix+rslt]); rslt++)
            if (rslt+ix==fields.length-1)
                return 0;
        return rslt;
    }
    String fields[];
    int ix;
}
