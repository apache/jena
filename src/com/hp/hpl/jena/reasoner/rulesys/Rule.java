/******************************************************************
 * File:        Rule.java
 * Created by:  Dave Reynolds
 * Created on:  29-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Rule.java,v 1.1 2003-04-17 15:24:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;

import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.datatypes.xsd.*;

/**
 * Representation of a generic inference rule. 
 * <p>
 * This represents the rule specification but most engines will 
 * compile this specification into an abstract machine or processing
 * graph. </p>
 * <p>
 * The rule specification comprises a list of antecendents (body) and a list
 * of consequents (head). If there is more than one consequent then a backchainer
 * should regard this as a shorthand for several rules, all with the
 * same body but with a singleton head. </p>
 * <p>
 * Each element in the head or body can be a TriplePattern, a Functor or a Rule.
 * A TriplePattern is just a triple of Nodes but the Nodes can represent
 * variables, wildcards and embedded functors - as well as constant uri or
 * literal graph nodes. A functor comprises a functor name and a list of 
 * arguments. The arguments are Nodes of any type except functor nodes
 * (there is no functor nesting). The functor name can be mapped into a registered
 * java class that implements its semantics. Functors play three roles -
 * in heads they represent actions (procedural attachement); in bodies they
 * represent builtin predicates; in TriplePatterns they represent embedded
 * structured literals that are used to cache matched subgraphs such as
 * restriction specifications. </p>
 *<p>
 * We include a trivial, recursive descent parser but this is just there
 * to allow rules to be embedded in code. External rule syntax based on N3
 * and RDF could be developed. The embedded syntax supports rules such as:
 * <blockindent>    
 * <code>[ (?C rdf:type *), guard(?C, ?P)  -> (?c rb:restriction some(?P, ?D)) ].</code><br />
 * <code>[ (?s owl:foo ?p) -> [ (?s owl:bar ?a) -> (?s ?p ?a) ] ].</code><br />
 * <code>[name: (?s owl:foo ?p) -> (?s ?p ?a)].</code><br />
 * </blockindent>
 * only built in namespaces are recognized as such, * is a wildcard node, ?c is a variable, 
 * name(node ... node) is a functor, (node node node) is a triple pattern, [..] is an
 * embedded rule, commas are ignore and can be freely used as separators. Functor names
 * may not end in ':'.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-04-17 15:24:24 $
 */
public class Rule {
    
//=======================================================================
// variables

    /** Rule body */
    protected Object[] body;
    
    /** Rule head or set of heads */
    protected Object[] head;
    
    /** Optional name for the rule */
    protected String name;
    
    /** A namespace used for Rubic specific properties */
    public static final String RBNamespace = "urn:x-hp-jena:rubik/";
    
    /**
     * Constructor
     * @param body a list of TriplePatterns or Functors.
     * @param head a list of TriplePatterns, Functors or rules
     */
    public Rule(List head, List body) {
        this(null, head, body);
    }
    
    /**
     * Constructor
     * @param name a label for rule
     * @param body a list of TriplePatterns or Functors.
     * @param head a list of TriplePatterns, Functors or rules
     */
    public Rule(String name, List head, List body) {
        this.name = name;
        this.head = head.toArray();
        this.body = body.toArray();
    }
    
    /**
     * Constructor
     * @param name a label for rule
     * @param body an array of TriplePatterns or Functors.
     * @param head an array of TriplePatterns, Functors or rules
     */
    public Rule(String name, Object[] head, Object[] body) {
        this.name = name;
        this.head = head;
        this.body = body;
    }
    
//=======================================================================
// accessors

    /**
     * Return the number of body elements
     */
    public int bodyLength() {
        return body.length;
    }
    
    /**
     * Return the n'th body element
     */
    public Object getBodyElement(int n) {
        return body[n];
    }
    
    /**
     * return the entire rule body as an array of objects
     */
    public Object[] getBody() {
        return body;
    }
        
    
    /**
     * Return the number of head elements
     */
    public int headLength() {
        return head.length;
    }
    
    /**
     * Return the n'th head element
     */
    public Object getHeadElement(int n) {
        return head[n];
    }
    
    /**
     * return the entire rule head as an array of objects
     */
    public Object[] getHead() {
        return head;
    }
    
    /**
     * Get the name for the rule - can be null.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Printable string describing the rule
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("[ ");
        if (name != null) {
            buff.append(name);
            buff.append(": ");
        }
        for (int i = 0; i < body.length; i++) {
            buff.append(PrintUtil.print(body[i]));
            buff.append(" ");
        }
        buff.append("-> ");
        for (int i = 0; i < head.length; i++) {
            buff.append(PrintUtil.print(head[i]));
            buff.append(" ");
        }
        buff.append("]");
        return buff.toString();
    }
    
    /**
     * Print a short description of the rule, just its name if it
     * has one, otherwise the whole rule description.
     */
    public String toShortString() {
        if (name != null) {
            return name;
        } else {
            return toString();
        }
    }
    
//=======================================================================
// parser access

    /**
     * Parse a string as a rule.
     * @throws ParserException if there is a problem
     */
    public static Rule parseRule(String source) throws ParserException {
        Parser parser = new Parser(source);
        return parser.parseRule();
    }
    
    /**
     * Parse a string as a list a rules.
     * @return a list of rules
     * @throws ParserException if there is a problem
     */
    public static List parseRules(String source) throws ParserException {
        Parser parser = new Parser(source);
        boolean finished = false;
        List ruleset = new ArrayList();
        while (!finished) {
            try {
                parser.peekToken();
            } catch (NoSuchElementException e) {
                finished = true;
                break;
            }
            Rule rule = parser.parseRule();
            ruleset.add(rule);
        }
        return ruleset;
    }
    

//=======================================================================
// parser support

    /**
     * Inner class which provides minimalist parsing support based on
     * tokenisation with depth 1 lookahead. No sensible error reporting on offer.
     * No embedded spaces supported.
     * TODO: Add propert literal support
     */
    static class Parser {
        
        /** Tokenizer */
        private StringTokenizer stream;
        
        /** Look ahead, null if none */
        private String lookahead;
        
        /** Trace back of recent tokens for error reporting */
        protected List priorTokens = new ArrayList();
        
        /** Maximum number of recent tokens to remember */
        private static final int maxPriors = 20;
        
        /** Variable table */
        private Map varMap;
        
        /**
         * Constructor
         * @param source the string to be parsed
         */
        Parser(String source) {
            stream = new StringTokenizer(source, "()[], \t\n\r", true);
            lookahead = null;
        }
        
        /**
         * Return the next token
         */
        String nextToken() {
            if (lookahead != null) {
                String temp = lookahead;
                lookahead = null;
                return temp;
            } else {
                String token = stream.nextToken();
                while (isSeparator(token)) {
                    token = stream.nextToken();
                }
                priorTokens.add(0, token);
                if (priorTokens.size() > maxPriors) {
                    priorTokens.remove(priorTokens.size()-1);
                }
                return token;
            }
        }
        
        /**
         * Return a trace of the recently seen tokens, for use
         * in error reporting
         */
        public String recentTokens() {
            StringBuffer trace = new StringBuffer();
            for (int i = priorTokens.size()-1; i >= 0; i--) {
                trace.append(priorTokens.get(i));
                trace.append(" ");
            }
            return trace.toString();
        }
        
        /**
         * Peek ahead one token.
         */
        String peekToken() {
            if (lookahead == null) {
                lookahead = nextToken();
            }
            return lookahead;
        }
        
        /**
         * Push back a previously fetched token. Only depth 1 supported.
         */
        void pushback(String token) {
            lookahead = token;
        }
        
        /**
         * Returns true if token is an skippable separator
         */
        boolean isSeparator(String token) {
            if (token.length() == 1) {
                char c = token.charAt(0);
                return (c == ',' || Character.isWhitespace(c));
            }
            return false;
        }
        
        /**
         * Returns true if token is a syntax element ()[]
         */
        boolean isSyntax(String token) {
            if (token.length() == 1) {
                char c = token.charAt(0);
                return (c == '(' || c == ')' || c == '[' || c == ']');
            }
            return false;
        }
        
        /**
         * Find the variable index for the given variable name
         * and return a Node_RuleVariable with that index.
         */
        Node_RuleVariable getNodeVar(String name) {
            Node_RuleVariable node = (Node_RuleVariable)varMap.get(name);
            if (node == null) {
                node = new Node_RuleVariable(name, varMap.size());
                varMap.put(name, node);
            }
            return node;
        }
        
        /**
         * Translate a token to a node.
         */
        Node parseNode(String token) {
            if (token.startsWith("?")) {
                return getNodeVar(token);
            } else if (token.equals("*") || token.equals("_")) {
                return Node.ANY;
            } else if (token.indexOf(':') != -1) {
                return Node.createURI(PrintUtil.expandQname(token));
            } else if (peekToken().equals("(")) {
                Functor f = new Functor(token, parseNodeList(), BuiltinRegistry.theRegistry);
                LiteralLabel ll = new LiteralLabel(f, null, Functor.FunctorDatatype.theFunctorDatatype);
                return new Node_Literal(ll);
            } else if (token.startsWith("'")) {
                // A literal - either plain or integer
                token = token.substring(1);
                token = token.substring(0, token.length()-1); // string ', no checking yet
                try {
                    Integer.parseInt(token);
                    return Node.createLiteral(token, "", XSDDatatype.XSDint);
                } catch (NumberFormatException e) {
                    return Node.createLiteral(token, "", false);
                }
            } else {
                // A uri
                return Node.createURI(token);
            }
        }
        
        /**
         * Parse a list of nodes delimited by parentheses
         */
        List parseNodeList() {
            String token = nextToken();
            if (!token.equals("(")) {
                throw new ParserException("Expected '(' at start of clause, found " + token, this);
            }
            token = nextToken();
            List nodeList = new ArrayList();
            while (!isSyntax(token)) {
                nodeList.add(parseNode(token));
                token = nextToken();
            }
            if (!token.equals(")")) {
                throw new ParserException("Expected ')' at end of clause, found " + token, this);
            }
            return nodeList;
        }
        
        /**
         * Parse a clause, could be a triple pattern, a rule or a functor
         */
        Object parseClause() {
            String token = peekToken();
            if (token.equals("(")) {
                List nodes = parseNodeList();
                if (nodes.size() != 3) {
                    throw new ParserException("Triple with " + nodes.size() + " nodes!", this);
                }
                return new TriplePattern((Node)nodes.get(0), (Node)nodes.get(1), (Node)nodes.get(2));
            } else if (token.equals("[")) {
                nextToken();
                return parseRule();
            } else {
                String name = nextToken();
                List args = parseNodeList();
                return new Functor(name, args, BuiltinRegistry.theRegistry);
            }
        }
        
        /**
         * Parse a rule, terminated by a "]" or "." character.
         */
        Rule parseRule() {
            try {
                // Skip initial '[' if present
                if (peekToken().equals("[")) {
                    nextToken();
                }
                // Check for optional name
                String name = null;
                String token = peekToken();
                if (token.endsWith(":")) {
                    name = token.substring(0, token.length()-1);
                    nextToken();
                }
                // Start rule parsing with empty variable table
                varMap = new HashMap();
                // Body
                List body = new ArrayList();
                while (!peekToken().equals("->")) {
                    body.add(parseClause());
                }
                List head = new ArrayList();
                token = nextToken();   // skip -> token
                do {
                    head.add(parseClause());
                    token = peekToken();
                } while ( !(token.equals(".") || token.equals("]")) );
                nextToken();        // consume the terminating token
                return new Rule(name, head, body);
            } catch (NoSuchElementException e) {
                throw new ParserException("Malformed rule", this);
            }
        }

    }
    
//=======================================================================
// Other supporting inner classes

    /**
     * Inner class. Exception raised if there is a problem
     * during rule parsing.
     */
    public static class ParserException extends RuntimeException {
        
        /** constructor */
        public ParserException(String message, Parser parser) {
            super(constructMessage(message, parser));
        }
        
        /**
         * Extract context trace from prior tokens stack
         */
        private static String constructMessage(String baseMessage, Parser parser) {
            StringBuffer message = new StringBuffer();
            message.append(baseMessage);
            message.append("\nAt '");
            message.append(parser.recentTokens());
            message.append("'");
            return message.toString();
        }
        
    }
    
}

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
