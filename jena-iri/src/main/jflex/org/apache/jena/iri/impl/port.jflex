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
%extends AbsLexer
%buffer 2048


%{
    
    
    
    char[] zzBuffer() {
     yyreset(null);
    this.zzAtEOF = true;
    int length = parser.end(range)-parser.start(range);
    zzEndRead = length;
    while (length > zzBuffer.length)
        zzBuffer = new char[zzBuffer.length*2];
    if (length==0)
           error(PORT_SHOULD_NOT_BE_EMPTY);
      return zzBuffer;
    }
    
%}
%class LexerPort
%%
[1-9] {
rule(1); 
}

^0 {
rule(2); 
if (yychar==0) error(PORT_SHOULD_NOT_START_IN_ZERO);
}

0 {
rule(3); 
}
[^1] {
rule(4); error(ILLEGAL_CHARACTER);}

