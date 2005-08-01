/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states.test;

class EventRecord {
    String startEvents[];
    String rsltState;
    String rsltCharacter[];  
    
    public String toString() {
        StringBuffer buf = new StringBuffer(toString(startEvents));
        buf.append(" = "+ rsltState +" " );
        buf.append(toString(rsltCharacter));
        return buf.toString();
    }
    static private String toString(String[] s) {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<s.length;i++)
            buf.append(s[i]+" ");
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
            for (int i=0;i<rsltCharacter.length;i++) {
                int n = 0;
                try {
                 n = Integer.parseInt(rsltCharacter[i].substring(1));
                }
                catch (RuntimeException e) {
                    System.err.println(toString());
                    throw e;
                }
                switch (rsltCharacter[i].charAt(0)) {
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

/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
