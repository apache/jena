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

package org.apache.jena.iri.impl;
%%


%unicode
%integer
%char
%implements org.apache.jena.iri.ViolationCodes
%implements org.apache.jena.iri.IRIComponents
%implements Lexer
%buffer 2048
%apiprivate


%{
    private Parser parser;
    private int range;
    
    
    public void analyse(Parser p,int r) {
        if (!p.has(r)) 
            return;
        analyse(p,r,p.uri,p.start(r),p.end(r));
    }
    public void analyse(Parser p,int r, String str) {
        analyse(p,r,str,0,str.length());
    }
    synchronized private void analyse(Parser p,int r, String str, int start, int finish) {
        parser = p;
        range = r;
        yyreset(null);
        useXhost = false;
        this.zzAtEOF = true;
        int length = finish - start;
        zzEndRead = length;
        while (length > zzBuffer.length)
            zzBuffer = new char[zzBuffer.length*2];
        str.getChars(
                start,
                finish,
                zzBuffer,
                0);
       try {
            yylex();
       }
       catch (java.io.IOException e) {
       }
       xhost(str,start,finish);
    }
    LexerXHost lexXHost = new LexerXHost((java.io.Reader) null);
    boolean useXhost;
    private void xhost(String str, int start, int finish) {
       if (useXhost) {
           lexXHost.analyse(parser,range,str,start,finish);
       }
    }
    private void error(int e) {
        switch(e) {
          case NOT_DNS_NAME:
          case NON_URI_CHARACTER:
            useXhost = true;
            break;
        }
        parser.recordError(range,e);
    }
    
    private void rule(int rule) {
        parser.matchedRule(range,rule);
    }

%}






