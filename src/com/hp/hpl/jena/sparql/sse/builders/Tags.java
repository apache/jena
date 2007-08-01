/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

public class Tags
{
    // -- Common terms
    static public final String tagUndef         = "undef" ;
    static public final String tagNull          = "null" ;
    static public final String tagDefault       = "default" ;
    static public final String tagExec          = "exec" ;
    static public final String tagRow           = "row" ;

    // RDF
    static public final String tagGraph         = "graph" ;
    static public final String tagLoad          = "graph@" ;
    static public final String tagTriple        = "triple" ;
    static public final String tagQuad          = "quad" ;
    static public final String tagPrefixMap     = "prefixmap" ;
    static public final String tagPrefixMapping = "prefixmapping" ;

    // SPARQL 
    static public final String tagDataset       = "dataset" ;
    static public final String tagNamedGraph    = "namedgraph" ;
    static public final String tagBinding       = "binding" ;
    static public final String tagTable         = "table" ;
    static public final String tagResultSet     = "resultset" ;

    // SPARQL algebra
    static public final String tagBGP           = "bgp" ;
    static public final String tagQuadPattern   = "bqp" ;

    static public final String tagFilter        = "filter" ;
    // static public final String tagGraph = "graph" ;
    static public final String tagService       = "service" ;
    static public final String tagJoin          = "join" ;
    static public final String tagLeftJoin      = "leftjoin" ;
    static public final String tagDiff          = "diff" ;
    static public final String tagUnion         = "union" ;

    static public final String tagToList        = "tolist" ;
    static public final String tagOrderBy       = "order" ;
    static public final String tagProject       = "project" ;
    static public final String tagDistinct      = "distinct" ;
    static public final String tagReduced       = "reduced" ;
    static public final String tagSlice         = "slice" ;
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */