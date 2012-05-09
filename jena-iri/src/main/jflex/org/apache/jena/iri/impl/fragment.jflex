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
%implements org.apache.jena.iri.IRIExceptionCodes
%implements org.apache.jena.iri.IRIComponents
%implements Lexer
%buffer 2048
%apiprivate


%{
    private Parser parser;
    private int range;
    
    synchronized public void analyse(Parser p,int r) {
        parser = p;
        range = r;
        if (!parser.has(range)) 
            return;
        yyreset(null);
        this.zzAtEOF = true;
        int length = parser.end(range)-parser.start(range);
        zzEndRead = length;
        while (length > zzBuffer.length)
            zzBuffer = new char[zzBuffer.length*2];
        parser.uri.getChars(
                parser.start(range),
                parser.end(range),
                zzBuffer,
                0);
       try {
            yylex();
       }
       catch (java.io.IOException e) {
       }
    }
    
    
    private void error(int e) {
        parser.recordError(range,e);
    }
    
    private void rule(int rule) {
        parser.matchedRule(range,rule);
    }

%}






%class LexerFragment
%%
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~])|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(1); }
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~])|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(2); error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~])|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(3); error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(4); error(IRI_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(5); error(IRI_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(6); error(IRI_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(7); error(IRI_CHAR);error(LTR_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(8); error(IRI_CHAR);error(LTR_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(9); error(IRI_CHAR);error(LTR_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(10); error(UNWISE_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(11); error(UNWISE_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(12); error(UNWISE_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(13); error(WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(14); error(WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(15); error(WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(16); error(NOT_XML_SCHEMA_WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(17); error(NOT_XML_SCHEMA_WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(18); error(NOT_XML_SCHEMA_WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(19); error(IRI_CHAR);error(UNWISE_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(20); error(IRI_CHAR);error(UNWISE_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(21); error(IRI_CHAR);error(UNWISE_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(22); error(IRI_CHAR);error(WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(23); error(IRI_CHAR);error(WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(24); error(IRI_CHAR);error(WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(25); error(IRI_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(26); error(IRI_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\u200D\\u2010-\\u2029\\u202F-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\u200D\u2010-\u2029\u202F-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(27); error(IRI_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(28); error(IRI_CHAR);error(LTR_CHAR);error(UNWISE_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(29); error(IRI_CHAR);error(LTR_CHAR);error(UNWISE_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(30); error(IRI_CHAR);error(LTR_CHAR);error(UNWISE_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(31); error(IRI_CHAR);error(LTR_CHAR);error(WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(32); error(IRI_CHAR);error(LTR_CHAR);error(WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(33); error(IRI_CHAR);error(LTR_CHAR);error(WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(34); error(IRI_CHAR);error(LTR_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(35); error(IRI_CHAR);error(LTR_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\-a-zA-Z0-9_\\~\\xA0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]|@{unwise})
unwise => ([\\>\\<\\"{}\\|\\^`\\x20\\t\\n\\r])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\-a-zA-Z0-9_\~\xA0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|([\>\<\"{}\|\^`\x20\t\n\r]))|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(36); error(IRI_CHAR);error(LTR_CHAR);error(NOT_XML_SCHEMA_WHITESPACE);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\x00-\\x20\\-a-zA-Z0-9_\\~\\x80-\\uFFFF\\>\\<\\"{}\\|\\^`\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-F])
subDelims => ([!\$&'()*+,;=])
*/
((((([\x00-\x20\-a-zA-Z0-9_\~\x80-\uFFFF\>\<\"{}\|\^`\t\n\r])|\.)|(%([0-9A-F]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(37); error(ARBITRARY_CHAR);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\x00-\\x20\\-a-zA-Z0-9_\\~\\x80-\\uFFFF\\>\\<\\"{}\\|\\^`\\t\\n\\r])
pctEncoded => (%@{upperHexDig}{2})
upperHexDig => ([0-9A-Fa-f])
subDelims => ([!\$&'()*+,;=])
*/
((((([\x00-\x20\-a-zA-Z0-9_\~\x80-\uFFFF\>\<\"{}\|\^`\t\n\r])|\.)|(%([0-9A-Fa-f]){2})|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(38); error(ARBITRARY_CHAR);error(PERCENT_ENCODING_SHOULD_BE_UPPERCASE);}
/*
fragment => ((@{pchar}|[/\\?])*)
pchar => (@{unreserved}|@{pctEncoded}|@{subDelims}|[:@])
unreserved => (@{unreservedNotDot}|\\.)
unreservedNotDot => ([\\x00-\\x20\\-a-zA-Z0-9_\\~\\x80-\\uFFFF\\>\\<\\"{}\\|\\^`\\t\\n\\r])
pctEncoded => (%)
subDelims => ([!\$&'()*+,;=])
*/
((((([\x00-\x20\-a-zA-Z0-9_\~\x80-\uFFFF\>\<\"{}\|\^`\t\n\r])|\.)|(%)|([!$&'()*+,;=])|[:@])|[/\?])*) {
rule(39); error(ARBITRARY_CHAR);error(ILLEGAL_PERCENT_ENCODING);}
/*
fragment => ([^]*)
*/
([^]*) {
rule(40); error(ILLEGAL_CHAR);}

