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






%class LexerHost
%%
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipVFuture}\\])
ipVFuture => (v@{lowerHexDig}+\\.[-a-zA-Z0-9._~!\$&'()*+,;=:]*)
lowerHexDig => ([0-9a-f])
*/
((\[(v([0-9a-f])+\.[-a-zA-Z0-9._~!$&'()*+,;=:]*)\])) {
rule(1); }
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipVFuture}\\])
ipVFuture => (v@{lowerHexDig}+\\.[-a-zA-Z0-9._~!\$&'()*+,;=:]*)
lowerHexDig => ([0-9A-Fa-f])
*/
((\[(v([0-9A-Fa-f])+\.[-a-zA-Z0-9._~!$&'()*+,;=:]*)\])) {
rule(2); error(IPv6ADDRESS_SHOULD_BE_LOWERCASE);}
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipV6Address}\\])
ipV6Address => (((@{h16}:){6}@{ls32}|::(@{h16}:){5}@{ls32}|@{h16}?::(@{h16}:){4}@{ls32}|((@{h16}:){0,1}@{h16})?::(@{h16}:){3}@{ls32}|((@{h16}:){0,2}@{h16})?::(@{h16}:){2}@{ls32}|((@{h16}:){0,3}@{h16})?::(@{h16}:){1}@{ls32}|((@{h16}:){0,4}@{h16})?::@{ls32}|((@{h16}:){0,5}@{h16})?::@{h16}|((@{h16}:){0,6}@{h16})?::))
h16 => (@{lowerHexDig}{1,4})
lowerHexDig => ([0-9a-f])
ls32 => ((@{h16}:@{h16}|@{ipV4Address}))
ipV4Address => ((@{decOctet}\\.){3}@{decOctet})
decOctet => (([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))
*/
((\[((((([0-9a-f]){1,4}):){6}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|::((([0-9a-f]){1,4}):){5}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(([0-9a-f]){1,4})?::((([0-9a-f]){1,4}):){4}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9a-f]){1,4}):){0,1}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){3}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9a-f]){1,4}):){0,2}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){2}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9a-f]){1,4}):){0,3}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){1}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9a-f]){1,4}):){0,4}(([0-9a-f]){1,4}))?::(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9a-f]){1,4}):){0,5}(([0-9a-f]){1,4}))?::(([0-9a-f]){1,4})|(((([0-9a-f]){1,4}):){0,6}(([0-9a-f]){1,4}))?::))\])) {
rule(3); }
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipV6Address}\\])
ipV6Address => (((@{h16}:){6}@{ls32}|::(@{h16}:){5}@{ls32}|@{h16}?::(@{h16}:){4}@{ls32}|((@{h16}:){0,1}@{h16})?::(@{h16}:){3}@{ls32}|((@{h16}:){0,2}@{h16})?::(@{h16}:){2}@{ls32}|((@{h16}:){0,3}@{h16})?::(@{h16}:){1}@{ls32}|((@{h16}:){0,4}@{h16})?::@{ls32}|((@{h16}:){0,5}@{h16})?::@{h16}|((@{h16}:){0,6}@{h16})?::))
h16 => (@{lowerHexDig}{1,4})
lowerHexDig => ([0-9a-f])
ls32 => ((@{h16}:@{h16}|@{ipV4Address}))
ipV4Address => (([0-9]+\\.){3}[0-9]+)
*/
((\[((((([0-9a-f]){1,4}):){6}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|::((([0-9a-f]){1,4}):){5}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(([0-9a-f]){1,4})?::((([0-9a-f]){1,4}):){4}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9a-f]){1,4}):){0,1}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){3}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9a-f]){1,4}):){0,2}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){2}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9a-f]){1,4}):){0,3}(([0-9a-f]){1,4}))?::((([0-9a-f]){1,4}):){1}(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9a-f]){1,4}):){0,4}(([0-9a-f]){1,4}))?::(((([0-9a-f]){1,4}):(([0-9a-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9a-f]){1,4}):){0,5}(([0-9a-f]){1,4}))?::(([0-9a-f]){1,4})|(((([0-9a-f]){1,4}):){0,6}(([0-9a-f]){1,4}))?::))\])) {
rule(4); error(IP_V4_OCTET_RANGE);}
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipV6Address}\\])
ipV6Address => (((@{h16}:){6}@{ls32}|::(@{h16}:){5}@{ls32}|@{h16}?::(@{h16}:){4}@{ls32}|((@{h16}:){0,1}@{h16})?::(@{h16}:){3}@{ls32}|((@{h16}:){0,2}@{h16})?::(@{h16}:){2}@{ls32}|((@{h16}:){0,3}@{h16})?::(@{h16}:){1}@{ls32}|((@{h16}:){0,4}@{h16})?::@{ls32}|((@{h16}:){0,5}@{h16})?::@{h16}|((@{h16}:){0,6}@{h16})?::))
h16 => (@{lowerHexDig}{1,4})
lowerHexDig => ([0-9A-Fa-f])
ls32 => ((@{h16}:@{h16}|@{ipV4Address}))
ipV4Address => ((@{decOctet}\\.){3}@{decOctet})
decOctet => (([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))
*/
((\[((((([0-9A-Fa-f]){1,4}):){6}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|::((([0-9A-Fa-f]){1,4}):){5}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(([0-9A-Fa-f]){1,4})?::((([0-9A-Fa-f]){1,4}):){4}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9A-Fa-f]){1,4}):){0,1}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){3}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9A-Fa-f]){1,4}):){0,2}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){2}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9A-Fa-f]){1,4}):){0,3}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){1}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9A-Fa-f]){1,4}):){0,4}(([0-9A-Fa-f]){1,4}))?::(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))))|(((([0-9A-Fa-f]){1,4}):){0,5}(([0-9A-Fa-f]){1,4}))?::(([0-9A-Fa-f]){1,4})|(((([0-9A-Fa-f]){1,4}):){0,6}(([0-9A-Fa-f]){1,4}))?::))\])) {
rule(5); error(IPv6ADDRESS_SHOULD_BE_LOWERCASE);}
/*
host => (@{ipLiteral})
ipLiteral => (\\[@{ipV6Address}\\])
ipV6Address => (((@{h16}:){6}@{ls32}|::(@{h16}:){5}@{ls32}|@{h16}?::(@{h16}:){4}@{ls32}|((@{h16}:){0,1}@{h16})?::(@{h16}:){3}@{ls32}|((@{h16}:){0,2}@{h16})?::(@{h16}:){2}@{ls32}|((@{h16}:){0,3}@{h16})?::(@{h16}:){1}@{ls32}|((@{h16}:){0,4}@{h16})?::@{ls32}|((@{h16}:){0,5}@{h16})?::@{h16}|((@{h16}:){0,6}@{h16})?::))
h16 => (@{lowerHexDig}{1,4})
lowerHexDig => ([0-9A-Fa-f])
ls32 => ((@{h16}:@{h16}|@{ipV4Address}))
ipV4Address => (([0-9]+\\.){3}[0-9]+)
*/
((\[((((([0-9A-Fa-f]){1,4}):){6}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|::((([0-9A-Fa-f]){1,4}):){5}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(([0-9A-Fa-f]){1,4})?::((([0-9A-Fa-f]){1,4}):){4}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9A-Fa-f]){1,4}):){0,1}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){3}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9A-Fa-f]){1,4}):){0,2}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){2}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9A-Fa-f]){1,4}):){0,3}(([0-9A-Fa-f]){1,4}))?::((([0-9A-Fa-f]){1,4}):){1}(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9A-Fa-f]){1,4}):){0,4}(([0-9A-Fa-f]){1,4}))?::(((([0-9A-Fa-f]){1,4}):(([0-9A-Fa-f]){1,4})|(([0-9]+\.){3}[0-9]+)))|(((([0-9A-Fa-f]){1,4}):){0,5}(([0-9A-Fa-f]){1,4}))?::(([0-9A-Fa-f]){1,4})|(((([0-9A-Fa-f]){1,4}):){0,6}(([0-9A-Fa-f]){1,4}))?::))\])) {
rule(6); error(IPv6ADDRESS_SHOULD_BE_LOWERCASE);error(IP_V4_OCTET_RANGE);}
/*
host => (@{ipLiteral})
ipLiteral => (\\[[^]*)
*/
((\[[^]*)) {
rule(7); error(IP_V6_OR_FUTURE_ADDRESS_SYNTAX);}
/*
host => (@{ipV4Address})
ipV4Address => ((@{decOctet}\\.){3}@{decOctet})
decOctet => (([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))
*/
((((([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]))\.){3}(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])))) {
rule(8); }
/*
host => (@{ipV4Address})
ipV4Address => (([0-9]+\\.){3}[0-9]+)
*/
((([0-9]+\.){3}[0-9]+)) {
rule(9); error(IP_V4_OCTET_RANGE);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.?)) {
rule(10); }
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.?)) {
rule(11); error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(12); error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(13); error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(14); error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(15); error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(16); error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(17); error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((-|((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.)*((-|((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.?)) {
rule(18); error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.)*((-|((([a-z0-9])|_)))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.?)) {
rule(19); error(DNS_LABEL_DASH_START_OR_END);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.)*((-|((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(20); error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.)*((-|((([a-zA-Z0-9])|_)))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(21); error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((-|((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(22); error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-z0-9])|[_\x80-\uFFFF])))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(23); error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(24); error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(25); error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|_))|(([a-z0-9]){2}--))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_))|(([a-z0-9]){2}--))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.?)) {
rule(26); error(ACE_PREFIX);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|_))|(([a-z0-9]){2}--))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_))|(([a-z0-9]){2}--))((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.?)) {
rule(27); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(28); error(ACE_PREFIX);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(29); error(ACE_PREFIX);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(30); error(ACE_PREFIX);error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--))((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(31); error(ACE_PREFIX);error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(32); error(ACE_PREFIX);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--))((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(33); error(ACE_PREFIX);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|_))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(((([a-z0-9])|_))))?)\.?)) {
rule(34); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|_))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|_))+-)*((([a-z0-9])|_))+)?)(-|((([a-z0-9])|_))))?)\.?)) {
rule(35); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(36); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|_))+-)*((([a-zA-Z0-9])|_))+)?)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(37); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(38); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF]))|(([a-z0-9]){2}--)|-)((((((([a-z0-9])|[_\x80-\uFFFF]))+-)*((([a-z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(39); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(40); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => (@{labelSingleDashInside}?)
labelPrefix => (@{labelChar}|@{acePrefix}|-)
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
acePrefix => (@{letterDigit}{2}--)
labelSingleDashInside => ((@{labelChar}+-)*@{labelChar}+)
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|(([a-zA-Z0-9]){2}--)|-)((((((([a-zA-Z0-9])|[_\x80-\uFFFF]))+-)*((([a-zA-Z0-9])|[_\x80-\uFFFF]))+)?)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(41); error(ACE_PREFIX);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(((([a-z0-9])|_))))?)\.?)) {
rule(42); error(DOUBLE_DASH_IN_REG_NAME);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(-|((([a-z0-9])|_))))?)\.)*((((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(-|((([a-z0-9])|_))))?)\.?)) {
rule(43); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(44); error(DOUBLE_DASH_IN_REG_NAME);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(-|((([a-zA-Z0-9])|_))))?)\.)*((((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(45); error(DOUBLE_DASH_IN_REG_NAME);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelPostfix => (@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(46); error(DOUBLE_DASH_IN_REG_NAME);error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(47); error(DOUBLE_DASH_IN_REG_NAME);error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelPostfix => (@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(48); error(DOUBLE_DASH_IN_REG_NAME);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(49); error(DOUBLE_DASH_IN_REG_NAME);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelPostfix => (@{labelChar})
*/
(((((-|((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(((([a-z0-9])|_))))?)\.)*((-|((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(((([a-z0-9])|_))))?)\.?)) {
rule(50); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(-|((([a-z0-9])|_))))?)\.)*((-|((([a-z0-9])|_)))(((((([a-z0-9])|_))|-)*)(-|((([a-z0-9])|_))))?)\.?)) {
rule(51); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelPostfix => (@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(((([a-zA-Z0-9])|_))))?)\.)*((-|((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(((([a-zA-Z0-9])|_))))?)\.?)) {
rule(52); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|_)
letterDigit => ([a-zA-Z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(-|((([a-zA-Z0-9])|_))))?)\.)*((-|((([a-zA-Z0-9])|_)))(((((([a-zA-Z0-9])|_))|-)*)(-|((([a-zA-Z0-9])|_))))?)\.?)) {
rule(53); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelPostfix => (@{labelChar})
*/
(((((-|((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(54); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-z0-9])|[_\x80-\uFFFF])))(((((([a-z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(55); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelPostfix => (@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(56); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);}
/*
host => (@{regname})
regname => ((@{label}\\.)*@{label}\\.?)
label => (@{labelPrefix}(@{labelInside}@{labelPostfix})?)
labelInside => ((@{labelChar}|-)*)
labelPrefix => (-|@{labelChar})
labelChar => (@{unreservedDNSLabel})
unreservedDNSLabel => (@{letterDigit}|[_\\x80-\\uFFFF])
letterDigit => ([a-zA-Z0-9])
labelPostfix => (-|@{labelChar})
*/
(((((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.)*((-|((([a-zA-Z0-9])|[_\x80-\uFFFF])))(((((([a-zA-Z0-9])|[_\x80-\uFFFF]))|-)*)(-|((([a-zA-Z0-9])|[_\x80-\uFFFF]))))?)\.?)) {
rule(57); error(DOUBLE_DASH_IN_REG_NAME);error(DNS_LABEL_DASH_START_OR_END);error(NON_URI_CHARACTER);error(LOWERCASE_PREFERRED);error(DNS_LABEL_DASH_START_OR_END);}
/*
host => (@{regname})
regname => ([^]*)
*/
(([^]*)) {
rule(58); error(NOT_DNS_NAME);}

