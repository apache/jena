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

package com.hp.hpl.jena.sparql.sse ;

public class Tags
{
    public static final String LPAREN             = "(" ;
    public static final String RPAREN             = ")" ;

    public static final String LBRACKET           = "[" ;
    public static final String RBRACKET           = "]" ;

    public static final String LBRACE             = "{" ;
    public static final String RBRACE             = "}" ;

    // -- Common terms
    public static final String tagUndef           = "undef" ;
    public static final String tagNull            = "null" ;
    public static final String tagDefault         = "default" ;
    public static final String tagExec            = "exec" ;
    public static final String tagRow             = "row" ;
    public static final String tagVars            = "vars" ;

    // RDF
    public static final String tagGraph           = "graph" ;
    public static final String tagLoad            = "graph@" ;
    public static final String tagTriple          = "triple" ;
    public static final String tagQuad            = "quad" ;
    public static final String tagTriplePath      = "path" ;

    public static final String tagBase            = "base" ;
    public static final String tagPrefix          = "prefix" ;
    public static final String tagPrefixMap       = "prefixmap" ;
    public static final String tagPrefixMapping   = "prefixmapping" ;

    // SPARQL
    public static final String tagDataset         = "dataset" ;
    public static final String tagBinding         = "binding" ;
    public static final String tagTable           = "table" ;
    public static final String tagResultSet       = "resultset" ;

    // SPARQL algebra
    public static final String tagBGP             = "bgp" ;
    public static final String tagQuadPattern     = "quadpattern" ;
    public static final String tagQuadBlock       = "quadblock" ;
    public static final String tagFilter          = "filter" ;
    // public static final String tagGraph = "graph" ;
    public static final String tagLabel           = "label" ;
    public static final String tagService         = "service" ;
    public static final String tagProc            = "proc" ;
    public static final String tagPropFunc        = "propfunc" ;
    public static final String tagJoin            = "join" ;
    public static final String tagSequence        = "sequence" ;
    public static final String tagDisjunction     = "disjunction" ;
    public static final String tagLeftJoin        = "leftjoin" ;
    public static final String tagConditional     = "conditional" ;
    public static final String tagDiff            = "diff" ;
    public static final String tagMinus           = "minus" ;
    public static final String tagUnion           = "union" ;
    public static final String tagDatasetNames    = "datasetnames" ;

    public static final String tagToList          = "tolist" ;
    public static final String tagOrderBy         = "order" ;
    public static final String tagTopN            = "top" ;
    public static final String tagGroupBy         = "group" ;
    public static final String tagProject         = "project" ;
    public static final String tagDistinct        = "distinct" ;
    public static final String tagReduced         = "reduced" ;
    public static final String tagAssign          = "assign" ;
    public static final String tagExtend          = "extend" ;
    public static final String symAssign          = ":=" ;
    public static final String tagSlice           = "slice" ;
    public static final String tagRename          = "rename" ;

    // Paths
    public static final String tagPath            = "path" ;
    public static final String tagPathSeq         = "seq" ;
    public static final String tagPathAlt         = "alt" ;
    public static final String tagPathMod         = "mod" ;

    public static final String tagPathZeroOrMore1 = "path*" ;
    public static final String tagPathZeroOrMoreN = "pathN*" ;
    public static final String tagPathOneOrMore1  = "path+" ;
    public static final String tagPathOneOrMoreN  = "pathN+" ;
    public static final String tagPathZeroOrOne   = "path?" ;
    public static final String tagPathFixedLength = "pathN" ;
    public static final String tagPathDistinct    = "distinct" ;
    public static final String tagPathMulti       = "multi" ;
    public static final String tagPathShortest    = "shortest" ;

    public static final String tagPathReverse     = "reverse" ;
    public static final String tagPathRev         = "rev" ;
    public static final String tagPathLink        = "link" ;
    public static final String tagPathNotOneOf    = "notoneof" ;

    // Not used - nowadays extensions are not explicitly flagged in the algebra.
    // But needed to override existing operations.
    public static final String tagExt             = "ext" ;

    // Expressions
    // sym => swiggly thing, tag => word-ish thing

    public static final String tagExpr            = "expr" ;
    public static final String tagExprList        = "exprlist" ;

    public static final String symEQ              = "=" ;
    public static final String tagEQ              = "eq" ;
    public static final String symNE              = "!=" ;
    public static final String tagNE              = "ne" ;
    public static final String symGT              = ">" ;
    public static final String tagGT              = "gt" ;
    public static final String symLT              = "<" ;
    public static final String tagLT              = "lt" ;
    public static final String symLE              = "<=" ;
    public static final String tagLE              = "le" ;
    public static final String symGE              = ">=" ;
    public static final String tagGE              = "ge" ;
    public static final String symOr              = "||" ;
    public static final String tagOr              = "or" ;
    public static final String symAnd             = "&&" ;
    public static final String tagAnd             = "and" ;

    public static final String symPlus            = "+" ;
    public static final String tagAdd             = "add" ;
    public static final String tagUnaryPlus       = "unaryplus" ;
    public static final String symMinus           = "-" ;
    public static final String tagSubtract        = "subtract" ;
    public static final String tagUnaryMinus      = "unaryminus" ;
    public static final String symMult            = "*" ;
    public static final String tagMultiply        = "multiply" ;
    public static final String symDiv             = "/" ;
    public static final String tagDivide          = "divide" ;

    public static final String symNot             = "!" ;
    public static final String tagNot             = "not" ;
    public static final String tagStr             = "str" ;
    public static final String tagStrLang         = "strlang" ;
    public static final String tagStrDatatype     = "strdt" ;
    public static final String tagRand            = "rand" ;

    public static final String tagLang            = "lang" ;
    public static final String tagLangMatches     = "langMatches" ;
    public static final String tagSameTerm        = "sameTerm" ;
    public static final String tagDatatype        = "datatype" ;
    public static final String tagBound           = "bound" ;
    public static final String tagCoalesce        = "coalesce" ;
    public static final String tagIf              = "if" ;
    public static final String tagIsIRI           = "isIRI" ;
    public static final String tagIsURI           = "isURI" ;
    public static final String tagIsBlank         = "isBlank" ;
    public static final String tagIsLiteral       = "isLiteral" ;
    public static final String tagRegex           = "regex" ;
    public static final String tagExists          = "exists" ;
    public static final String tagNotExists       = "notexists" ;

    public static final String tagYear            = "year" ;
    public static final String tagMonth           = "month" ;
    public static final String tagDay             = "day" ;
    public static final String tagHours           = "hours" ;
    public static final String tagMinutes         = "minutes" ;
    public static final String tagSeconds         = "seconds" ;
    public static final String tagTimezone        = "timezone" ;
    public static final String tagTZ              = "tz" ;

    public static final String tagNow             = "now" ;
    public static final String tagUUID            = "uuid" ;
    public static final String tagStrUUID         = "struuid" ;
    public static final String tagVersion         = "version" ;

    public static final String tagMD5             = "md5" ;
    public static final String tagSHA1            = "sha1" ;
    public static final String tagSHA224          = "sha224" ;
    public static final String tagSHA256          = "sha256" ;
    public static final String tagSHA384          = "sha384" ;
    public static final String tagSHA512          = "sha512" ;

    public static final String tagStrlen          = "strlen" ;
    public static final String tagSubstr          = "substr" ;
    public static final String tagReplace         = "replace" ;
    public static final String tagStrUppercase    = "ucase" ;
    public static final String tagStrLowercase    = "lcase" ;
    public static final String tagStrEnds         = "strends" ;
    public static final String tagStrStarts       = "strstarts" ;
    public static final String tagStrBefore       = "strbefore" ;
    public static final String tagStrAfter        = "strafter" ;
    public static final String tagStrContains     = "contains" ;
    public static final String tagStrEncodeForURI = "encode_for_uri" ;
    public static final String tagConcat          = "concat" ;

    public static final String tagNumAbs          = "abs" ;
    public static final String tagNumRound        = "round" ;
    public static final String tagNumCeiling      = "ceil" ;
    public static final String tagNumFloor        = "floor" ;
    public static final String tagIsNumeric       = "isNumeric" ;

    public static final String tagBNode           = "bnode" ;
    public static final String tagIri             = "iri" ;
    public static final String tagUri             = "uri" ;

    public static final String tagIn              = "in" ;
    public static final String tagNotIn           = "notin" ;
    public static final String tagCall            = "call" ;

    public static final String tagTrue            = "true" ;
    public static final String tagFalse           = "false" ;

    public static final String tagAsc             = "asc" ;
    public static final String tagDesc            = "desc" ;

    public static final String tagCount           = "count" ;
    public static final String tagSum             = "sum" ;
    public static final String tagMin             = "min" ;
    public static final String tagMax             = "max" ;
    public static final String tagAvg             = "avg" ;
    public static final String tagSample          = "sample" ;
    public static final String tagGroupConcat     = "group_concat" ;
    public static final String tagSeparator       = "separator" ;
}
