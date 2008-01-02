/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states.test;

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
    public String toString() {
        StringBuffer rslt = new StringBuffer( "|"+state+"| " +  super.toString());
        rslt.append("{ ");
        for (int i=0;i<moreCharacter.length;i++)
            rslt.append(moreCharacter[i].toString() + " ; ");
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

/*
 *  (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
