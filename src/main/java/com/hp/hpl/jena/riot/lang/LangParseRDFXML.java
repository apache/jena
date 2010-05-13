/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.PrintStream ;

import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.rdf.arp.ALiteral ;
import com.hp.hpl.jena.rdf.arp.ARP ;
import com.hp.hpl.jena.rdf.arp.AResource ;
import com.hp.hpl.jena.rdf.arp.ParseException ;
import com.hp.hpl.jena.rdf.arp.StatementHandler ;
import com.hp.hpl.jena.riot.ErrorHandler ;

public class LangParseRDFXML //implements LangRIOT
{
    // Need a better swrppaer for RIOT integration.
    // Output to a sink.
    // Integrated error handling.
    
    static ARP arp = new ARP() ;
    static long count ;
    
    // Hacked out of ARP.  Lots of "private" methods
    public static long parseRDFXML(String xmlBase, String filename, ErrorHandler errHandler, InputStream in, boolean outputTriples)
    {
        count = 0 ;
        StatementHandler rslt = outputTriples ? (StatementHandler)new SH(System.out):new NoSH();
        arp.getHandlers().setStatementHandler(rslt);
        try {
            arp.load(in, xmlBase);
        } catch (IOException e) {
            errHandler.error(filename + ": " + ParseException.formatMessage(e), -1 , -1) ;
        } catch (SAXParseException e) {
            // already reported. :-(
        } catch (SAXException sax) {
            errHandler.error(filename + ": " + ParseException.formatMessage(sax), -1 , -1) ;
        }
        return count ;
    }


    static StringBuilder line = new StringBuilder(200) ;
    static private void print(String s) {
        line.append(s);
    }
    
    // Hacked out of ARP.NTriple
        
    private static class NoSH implements StatementHandler {
        public void statement(AResource subj, AResource pred, AResource obj) { count++ ; }
        public void statement(AResource subj, AResource pred, ALiteral lit) { count++ ; }
    }
    private static class SH implements StatementHandler {
        PrintStream out;
        SH(PrintStream out){
            this.out = out;
        }
        public void statement(AResource subj, AResource pred, AResource obj) {
            count++ ;
            line.setLength(0);
            resource(subj);
            resource(pred);
            resource(obj);
            line.append('.');
        }
        public void statement(AResource subj, AResource pred, ALiteral lit) {
            count++ ;
            line.setLength(0);
            resource(subj);
            resource(pred);
            literal(lit);
            line.append('.');
            out.println(line);
        }
    }
    
    static private void resource(AResource r) {
        if (r.isAnonymous()) {
            print("_:j");
            print(r.getAnonymousID());
            print(" ");
        } else {
            print("<");
            escapeURI(r.getURI());
            print("> ");
        }
    }
    static private void escape(String s) {
        int lg = s.length();
        for (int i = 0; i < lg; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\' :
                    print("\\\\");
                    break;
                case '"' :
                    print("\\\"");
                    break;
                case '\n' :
                    print("\\n");
                    break;
                case '\r' :
                    print("\\r");
                    break;
                case '\t' :
                    print("\\t");
                    break;
                default :
                    if (ch >= 32 && ch <= 126)
                        line.append(ch);
                    else {
                        print("\\u");
                        String hexstr = Integer.toHexString(ch).toUpperCase();
                        int pad = 4 - hexstr.length();

                        for (; pad > 0; pad--)
                            print("0");
                        print(hexstr);
                    }
            }
        }
    }
    
    static private boolean okURIChars[] = new boolean[128];
    static {
        for (int i = 32; i < 127; i++)
            okURIChars[i] = true;
        okURIChars['<'] = false;
        okURIChars['>'] = false;
        okURIChars['\\'] = false;

    }
    static private void escapeURI(String s) {
        int lg = s.length();
        for (int i = 0; i < lg; i++) {
            char ch = s.charAt(i);
            if (ch < okURIChars.length && okURIChars[ch]) {
                line.append(ch);
            } else {
                print("\\u");
                String hexstr = Integer.toHexString(ch).toUpperCase();
                int pad = 4 - hexstr.length();

                for (; pad > 0; pad--)
                    print("0");
                print(hexstr);
            }
        }
    }
    static private void literal(ALiteral l) {
        //if (l.isWellFormedXML())
        //  System.out.print("xml");
        line.append('"');
        escape(l.toString());
        line.append('"');
        String lang = l.getLang();
        if (lang != null && !lang.equals("")) {
            line.append('@');
            print(lang);
        }
        String dt = l.getDatatypeURI();
        if (dt != null && !dt.equals("")) {
            print("^^<");
            escapeURI(dt);
            line.append('>');
        }

        line.append(' ');
    }


}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
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