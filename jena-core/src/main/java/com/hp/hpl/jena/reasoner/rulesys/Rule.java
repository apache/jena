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

package com.hp.hpl.jena.reasoner.rulesys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.RulesetNotFoundException;
import com.hp.hpl.jena.shared.WrappedIOException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.Tokenizer;

/**Representation of a generic inference rule. 
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
 * <p>
 * The equality contract for rules is that two rules are equal if each of terms
 * (ClauseEntry objects) are equals and they have the same name, if any.
 * </p>
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
 */
public class Rule implements ClauseEntry {
    
//=======================================================================
// variables

    /** Rule body */
    protected ClauseEntry[] body;
    
    /** Rule head or set of heads */
    protected ClauseEntry[] head;
    
    /** Optional name for the rule */
    protected String name;
    
    /** The number of distinct variables used in the rule */
    protected int numVars = -1;
    
    /** Flags whether the rule was written as a forward or backward rule */
    protected boolean isBackward = false;
    
    /** Flags whether the rule is monotonic */
    protected boolean isMonotonic = true;
    
    static Logger logger = LoggerFactory.getLogger(Rule.class);
    
    /**
     * Constructor
     * @param body a list of TriplePatterns or Functors.
     * @param head a list of TriplePatterns, Functors or rules
     */
    public Rule(List<ClauseEntry> head, List<ClauseEntry> body) {
        this(null, head, body);
    }
    
    /**
     * Constructor
     * @param name a label for rule
     * @param body a list of TriplePatterns or Functors.
     * @param head a list of TriplePatterns, Functors or rules
     */
    public Rule(String name, List<ClauseEntry> head, List<ClauseEntry> body) {
        this(name, 
                head.toArray(new ClauseEntry[head.size()]),
                body.toArray(new ClauseEntry[body.size()]) );
    }
    
    /**
     * Constructor
     * @param name a label for rule
     * @param body an array of TriplePatterns or Functors.
     * @param head an array of TriplePatterns, Functors or rules
     */
    public Rule(String name, ClauseEntry[] head, ClauseEntry[] body) {
        this.name = name;
        this.head = head;
        this.body = body;
        this.isMonotonic = allMonotonic(head);
    }
    
    // Compute the monotonicity flag
    // Future support for negation would affect this
    private boolean allMonotonic(ClauseEntry[] elts) {
        for ( ClauseEntry elt : elts )
        {
            if ( elt instanceof Functor )
            {
                Builtin b = ( (Functor) elt ).getImplementor();
                if ( b != null )
                {
                    if ( !b.isMonotonic() )
                    {
                        return false;
                    }
                }
                else
                {
                    throw new ReasonerException(
                        "Undefined Functor " + ( (Functor) elt ).getName() + " in " + toShortString() );
                }
            }
        }
        return true;
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
    public ClauseEntry getBodyElement(int n) {
        return body[n];
    }
    
    /**
     * return the entire rule body as an array of objects
     */
    public ClauseEntry[] getBody() {
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
    public ClauseEntry getHeadElement(int n) {
        return head[n];
    }
    
    /**
     * return the entire rule head as an array of objects
     */
    public ClauseEntry[] getHead() {
        return head;
    }
    
    /**
     * Return true if the rule was written as a backward (as opposed to forward) rule.
     */
    public boolean isBackward() {
        return isBackward;
    }
    
    /**
     * Set the rule to be run backwards.
     * @param flag if true the rule should run backwards.
     */
    public void setBackward(boolean flag) {
        isBackward = flag;
    }
    
    /**
     * Get the name for the rule - can be null.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the number of distinct variables for this rule.
     * Used internally when cloing rules, not normally required.
     */
    public void setNumVars(int n) {
        numVars = n;
    }
    
    /**
     * Return the number of distinct variables in the rule. Or more precisely, the
     * size of a binding environment needed to represent the rule.
     */
    public int getNumVars() {
        if (numVars == -1) {
            // only have to do this if the rule was generated programatically
            // the parser will have prefilled this in for normal rules
            int max = findVars(body, -1);
            max = findVars(head, max);
            numVars = max + 1;
        }
        return numVars;
    }
    
    /**
     * Find all the variables in a clause array.
     */
    private int findVars(Object[] nodes, int maxIn) {
        int max = maxIn;
        for ( Object node : nodes )
        {
            if ( node instanceof TriplePattern )
            {
                max = findVars( (TriplePattern) node, max );
            }
            else
            {
                max = findVars( (Functor) node, max );
            }
        }
        return max;
    }
    
    /**
     * Find all the variables in a TriplePattern.
     */
    private int findVars(TriplePattern t, int maxIn) {
        int max = maxIn;
        max = maxVarIndex(t.getSubject(), max);
        max = maxVarIndex(t.getPredicate(), max);
        Node obj = t.getObject();
        if (obj instanceof Node_RuleVariable) {
            max = maxVarIndex(obj, max);
        } else if (Functor.isFunctor(obj)) {
            max = findVars((Functor)obj.getLiteralValue(), max);
        }
        return max;
    }
        
    /**
     * Find all the variables in a Functor.
     */
    private int findVars(Functor f, int maxIn) {
        int max = maxIn;
        Node[] args = f.getArgs();
        for ( Node arg : args )
        {
            if ( arg.isVariable() )
            {
                max = maxVarIndex( arg, max );
            }
        }
        return max;
    }
    
    /**
     * Return the maximum node index of the variable and the max so far. 
     */
    private int maxVarIndex(Node var, int max) {
        if (var instanceof Node_RuleVariable) {
            int index = ((Node_RuleVariable)var).index;
            if (index > max) return index;            
        }
        return max;
    }
    
    /**
     * Instantiate a rule given a variable binding environment.
     * This will clone any non-bound variables though that is only needed
     * for trail implementations.
     */
    public Rule instantiate(BindingEnvironment env) {
        HashMap<Node_RuleVariable, Node> vmap = new HashMap<>();
        return new Rule(name, cloneClauseArray(head, vmap, env), cloneClauseArray(body, vmap, env));
    }
    
    /**
     * Clone a rule, cloning any embedded variables.
     */
    public Rule cloneRule() {
        if (getNumVars() > 0) {
            HashMap<Node_RuleVariable, Node> vmap = new HashMap<>();
            return new Rule(name, cloneClauseArray(head, vmap, null), cloneClauseArray(body, vmap, null));
        } else {
            return this;
        }
    }
    
    /**
     * Clone a clause array.
     */
    private ClauseEntry[] cloneClauseArray(ClauseEntry[] clauses, Map<Node_RuleVariable, Node> vmap, BindingEnvironment env) {
        ClauseEntry[] cClauses = new ClauseEntry[clauses.length];
        for (int i = 0; i < clauses.length; i++ ) {
            cClauses[i] = cloneClause(clauses[i], vmap, env);
        }
        return cClauses;
    }
    
    /**
     * Clone a clause, cloning any embedded variables.
     */
    private ClauseEntry cloneClause(ClauseEntry clause, Map<Node_RuleVariable, Node> vmap, BindingEnvironment env) {
        if (clause instanceof TriplePattern) {
            TriplePattern tp = (TriplePattern)clause;
            return new TriplePattern (
                            cloneNode(tp.getSubject(), vmap, env),
                            cloneNode(tp.getPredicate(), vmap, env),
                            cloneNode(tp.getObject(), vmap, env)
                        );
        } else {
            return cloneFunctor((Functor)clause, vmap, env);
        }
    }
    
    /**
     * Clone a functor, cloning any embedded variables.
     */
    private Functor cloneFunctor(Functor f, Map<Node_RuleVariable, Node> vmap, BindingEnvironment env) {
        Node[] args = f.getArgs();
        Node[] cargs = new Node[args.length];
        for (int i = 0; i < args.length; i++) {
            cargs[i] = cloneNode(args[i], vmap, env);
        }
        Functor fn = new Functor(f.getName(), cargs);
        fn.setImplementor(f.getImplementor());
        return fn;
    }
    
    /**
     * Close a single node.
     */
    private Node cloneNode(Node nIn, Map<Node_RuleVariable, Node> vmap, BindingEnvironment env) {
        Node n = (env == null) ? nIn : env.getGroundVersion(nIn);
        if (n instanceof Node_RuleVariable) {
            Node_RuleVariable nv = (Node_RuleVariable)n;
            Node c = vmap.get(nv);
            if (c == null) {
                c = ((Node_RuleVariable)n).cloneNode();
                vmap.put(nv, c);
            }
            return c;
        } else if (Functor.isFunctor(n)) {
            Functor f = (Functor)n.getLiteralValue();
            return Functor.makeFunctorNode(cloneFunctor(f, vmap, env));
        } else {
            return n;
        }
    }
    
    /**
     * Returns false for rules which can affect other rules non-monotonically (remove builtin
     * or similar) or are affected non-monotonically (involve negation-as-failure).
     */
    public boolean isMonotonic() {
        return isMonotonic;
    }
    
    /**
     * Returns true if the rule does not depend on any data, and so should 
     * be treated as an axiom.
     */
    public boolean isAxiom() {
        if (isBackward() && body.length > 0) return false;
        for ( ClauseEntry aBody : body )
        {
            if ( aBody instanceof TriplePattern )
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Printable string describing the rule
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("[ ");
        if (name != null) {
            buff.append(name);
            buff.append(": ");
        }
        if (isBackward) {
            for ( ClauseEntry aHead : head )
            {
                buff.append( PrintUtil.print( aHead ) );
                buff.append( " " );
            }
            buff.append("<- ");
            for ( ClauseEntry aBody : body )
            {
                buff.append( PrintUtil.print( aBody ) );
                buff.append( " " );
            }
        } else {
            for ( ClauseEntry aBody : body )
            {
                buff.append( PrintUtil.print( aBody ) );
                buff.append( " " );
            }
            buff.append("-> ");
            for ( ClauseEntry aHead : head )
            {
                buff.append( PrintUtil.print( aHead ) );
                buff.append( " " );
            }
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
     * Answer the list of rules parsed from the given URL.
     * @throws RulesetNotFoundException
     */
    public static List<Rule> rulesFromURL( String uri ) {
        BufferedReader br = null;
        try {
            InputStream in = FileManager.get().open(uri);
            if (in == null) throw new RulesetNotFoundException( uri );
            br = FileUtils.asBufferedUTF8( in );
            return parseRules( Rule.rulesParserFromReader( br ) );
        } finally {
            if (br != null) try { br.close(); } catch (IOException e2) {}
        }
    }
        
    /**
     * Processes the source reader stripping off comment lines and noting prefix
     * definitions (@prefix) and rule inclusion commands (@include).
     * Returns a parser which is bound to the stripped source text with 
     * associated prefix and rule inclusion definitions.
    */
    public static Parser rulesParserFromReader( BufferedReader src ) {
       try {
           StringBuilder result = new StringBuilder();
           String line;
           Map<String, String> prefixes = new HashMap<>();
           List<Rule> preloadedRules = new ArrayList<>();
           while ((line = src.readLine()) != null) {
               if (line.startsWith("#")) continue;     // Skip comment lines
               line = line.trim();
               if (line.startsWith("//")) continue;    // Skip comment lines
               if (line.startsWith("@prefix")) {
                   line = line.substring("@prefix".length());
                   String prefix = nextArg(line);
                   String rest = nextAfterArg(line);
                   if (prefix.endsWith(":")) prefix = prefix.substring(0, prefix.length() - 1);
                   String url = extractURI(rest);
                   prefixes.put(prefix, url);

               } else if (line.startsWith("@include")) {
                   // Include referenced rule file, either URL or local special case
                   line = line.substring("@include".length());
                   String url = extractURI(line);
                   // Check for predefined cases
                   if (url.equalsIgnoreCase("rdfs")) {
                       preloadedRules.addAll( RDFSFBRuleReasoner.loadRules() );
                       
                   } else if (url.equalsIgnoreCase("owl")) {
                       preloadedRules.addAll( OWLFBRuleReasoner.loadRules() ) ;
                       
                   } else if (url.equalsIgnoreCase("owlmicro")) {
                       preloadedRules.addAll( OWLMicroReasoner.loadRules() ) ;
                       
                   } else if (url.equalsIgnoreCase("owlmini")) {
                       preloadedRules.addAll( OWLMiniReasoner.loadRules() ) ;
                       
                   } else {
                       // Just try loading as a URL
                       preloadedRules.addAll( rulesFromURL(url) );
                   }

               } else {
                   result.append(line);
                   result.append("\n");
               }
           }
           Parser parser = new Parser(result.toString());
           parser.registerPrefixMap(prefixes);
           parser.addRulesPreload(preloadedRules);
           return parser;
       }
       catch (IOException e) 
           { throw new WrappedIOException( e ); }
   }

    /** 
     * Helper function find a URI argument in the current string,
     * optionally surrounded by matching <>.
     */
    private static String extractURI(String lineSoFar) {
        String token = lineSoFar.trim();
        if (token.startsWith("<")) {
            int split = token.indexOf('>');
            token = token.substring(1, split);
        }
        return token;
    }

    /** 
     * Helper function to return the next whitespace delimited argument
     * from the string
     */
    private static String nextArg(String token) {
        int start = nextSplit(0, false, token);
        int stop = nextSplit(start, true, token);
        return token.substring(start, stop);
    }
    
    /** 
     * Helper function to return the remainder of the line after
     * stripping off the next whitespace delimited argument
     * from the string
     */
    private static String nextAfterArg(String token) {
        int start = nextSplit(0, false, token);
        int stop = nextSplit(start, true, token);
        int rest = nextSplit(stop, false, token);
        return token.substring(rest);
    }
    
    /**
     * Helper function - find index of next whitespace or non white
     * after the start index. 
     */
    private static int nextSplit(int start, boolean white, String line) {
        int i = start;
        while (i < line.length()) {
            boolean isWhite = Character.isWhitespace(line.charAt(i));
            if ((white & isWhite) || (!white & !isWhite)) {
                return i;
            }
            i++;
        }
        return i;
    }

    /**
     * Run a pre-bound rule parser to extract it's rules
     * @return a list of rules
     * @throws ParserException if there is a problem
     */
    public static List<Rule> parseRules(Parser parser) throws ParserException {
        boolean finished = false;
        List<Rule> ruleset = new ArrayList<>();
        ruleset.addAll(parser.getRulesPreload());
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

    /**
     * Parse a string as a list a rules.
     * @return a list of rules
     * @throws ParserException if there is a problem
     */
    public static List<Rule> parseRules(String source) throws ParserException {
        return parseRules(new Parser(source));
    }
    

//=======================================================================
// parser support

    /**
     * Inner class which provides minimalist parsing support based on
     * tokenisation with depth 1 lookahead. No sensible error reporting on offer.
     * No embedded spaces supported.
     */
    public static class Parser {
        
        /** Tokenizer */
        private Tokenizer stream;
        
        /** Look ahead, null if none */
        private String lookahead;
        
        // Literal parse state flags
        private static final int NORMAL = 0;
        private static final int STARTED_LITERAL = 1;
        
        /** Literal parse state */
        private int literalState = NORMAL;
        
        /** Trace back of recent tokens for error reporting */
        protected List<String> priorTokens = new ArrayList<>();
        
        /** Maximum number of recent tokens to remember */
        private static final int maxPriors = 20;
        
        /** Variable table */
        private Map<String, Node_RuleVariable> varMap;
        
        /** Local prefix map */
        private PrefixMapping prefixMapping = PrefixMapping.Factory.create();
        
        /** Pre-included rules */
        private List<Rule> preloadedRules = new ArrayList<>();
        
        /**
         * Constructor
         * @param source the string to be parsed
         */
        Parser(String source) {
            stream = new Tokenizer(source, "()[], \t\n\r", "'\"", true);
            lookahead = null;
        }
        
        /**
         * Register a new namespace prefix with the parser
         */
        public void registerPrefix(String prefix, String namespace ) {
            prefixMapping.setNsPrefix(prefix, namespace);
        }
        
        /**
         * Register a set of prefix to namespace mappings with the parser
         */
        public void registerPrefixMap(Map<String, String> map) {
            prefixMapping.setNsPrefixes(map);
        }
        
        /**
         * Return a map of all the discovered prefixes
         */
        public Map<String, String> getPrefixMap() {
            return prefixMapping.getNsPrefixMap();
        }
        
        /**
         * Add a new set of preloaded rules.
         */
        void addRulesPreload(List<Rule> rules) {
            preloadedRules.addAll(rules);
        }
        
        /**
         * Return the complete set of preloaded rules;
         */
        public List<Rule> getRulesPreload() {
            return preloadedRules;
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
                if (literalState == NORMAL) {
                    // Skip separators unless within a literal
                    while (isSeparator(token)) {
                        token = stream.nextToken();
                    }
                }
                if (token.equals("'")) {
                    if (literalState == NORMAL) {
                        literalState = STARTED_LITERAL;
                    } else {
                        literalState = NORMAL;
                    }
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
            StringBuilder trace = new StringBuilder();
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
            Node_RuleVariable node = varMap.get(name);
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
                // Dropped support for anon wildcards until the implementation is better resolved
            } else if (token.equals("*") || token.equals("_")) {
                throw new ParserException("Wildcard variables no longer supported", this);
////                return Node_RuleVariable.ANY;
//                return Node_RuleVariable.WILD;
            } else if (token.startsWith("<") && token.endsWith(">")) {
                String uri = token.substring(1, token.length()-1);
                return NodeFactory.createURI(uri);
            } else if (token.startsWith( "_" )) { // TODO rationalise [this is for the RIF code]
                return NodeFactory.createAnon( new AnonId( token.substring( 1 ) ) );
            } else if (token.indexOf(':') != -1) {
                String exp = prefixMapping.expandPrefix(token); // Local map first
                exp = PrintUtil.expandQname(exp);  // Retain global map for backward compatibility
                if (exp == token) {
                    // No expansion was possible
                    String prefix = token.substring(0, token.indexOf(':'));
                    if (prefix.equals("http") || prefix.equals("urn") || prefix.equals("file")
                     || prefix.equals("ftp") || prefix.equals("mailto")) {
                        // assume it is all OK and fall through
                    } else {
                        // Likely to be a typo in a qname or failure to register
                        throw new ParserException("Unrecognized qname prefix (" + prefix + ") in rule", this);
                    }
                }
                return NodeFactory.createURI(exp);
            } else if (peekToken().equals("(")) {
                Functor f = new Functor(token, parseNodeList(), BuiltinRegistry.theRegistry);
                return Functor.makeFunctorNode( f );
            } else if (token.equals("'") || token.equals("\"")) {
                // A plain literal
                String lit = nextToken();
                // Skip the trailing quote
                nextToken();
                // Check for an explicit datatype
                if (peekToken().startsWith("^^")) {
                    String dtURI = nextToken().substring(2);
                    if (dtURI.indexOf(':') != -1) {
                        // Thanks to Steve Cranefield for pointing out the need for prefix expansion here
                        String exp = prefixMapping.expandPrefix(dtURI); // Local map first
                        exp = PrintUtil.expandQname(exp);  // Retain global map for backward compatibility
                        if (exp == dtURI) {
                            // No expansion was possible
                            String prefix = dtURI.substring(0, dtURI.indexOf(':'));
                            if (prefix.equals("http") || prefix.equals("urn") 
                             || prefix.equals("ftp") || prefix.equals("mailto")) {
                                // assume it is all OK and fall through
                            } else {
                                // Likely to be a typo in a qname or failure to register
                                throw new ParserException("Unrecognized qname prefix (" + prefix + ") in rule", this);
                            }
                        } else {
                            dtURI = exp;
                        }
                    } 
                    RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtURI);
                    return NodeFactory.createLiteral(lit, "", dt);
                } else {
                    return NodeFactory.createLiteral(lit, "", false);
                }                
            } else  if ( Character.isDigit(token.charAt(0)) || 
                         (token.charAt(0) == '-' && token.length() > 1 && Character.isDigit(token.charAt(1))) ) {
                // A number literal
               return parseNumber(token);
            } else {
                // A  uri
                return NodeFactory.createURI(token);
            }
        }
        
        /**
         * Turn a possible numeric token into typed literal else a plain literal
         * @return the constructed literal node
         */
        Node parseNumber(String lit) {
            if ( Character.isDigit(lit.charAt(0)) || 
                (lit.charAt(0) == '-' && lit.length() > 1 && Character.isDigit(lit.charAt(1))) ) {
                if ( lit.contains( "." ) ) {
                    // Float?
                    if (XSDDatatype.XSDfloat.isValid(lit)) {
                        return NodeFactory.createLiteral(lit, "", XSDDatatype.XSDfloat);
                    }
                } else {
                    // Int?
                    if (XSDDatatype.XSDint.isValid(lit)) {
                        return NodeFactory.createLiteral(lit, "", XSDDatatype.XSDint);
                    }
                }
            }
            // Default is a plain literal
            return NodeFactory.createLiteral(lit, "", false);
        }
        
        /**
         * Parse a list of nodes delimited by parentheses
         */
        List<Node> parseNodeList() {
            String token = nextToken();
            if (!token.equals("(")) {
                throw new ParserException("Expected '(' at start of clause, found " + token, this);
            }
            token = nextToken();
            List<Node> nodeList = new ArrayList<>();
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
        ClauseEntry parseClause() {
            String token = peekToken();
            if (token.equals("(")) {
                List<Node> nodes = parseNodeList();
                if (nodes.size() != 3) {
                    throw new ParserException("Triple with " + nodes.size() + " nodes!", this);
                }
                if (Functor.isFunctor(nodes.get(0))) {
                    throw new ParserException("Functors not allowed in subject position of pattern", this);
                }
                if (Functor.isFunctor(nodes.get(1))) {
                    throw new ParserException("Functors not allowed in predicate position of pattern", this);
                }
                return new TriplePattern(nodes.get(0), nodes.get(1), nodes.get(2));
            } else if (token.equals("[")) {
                nextToken();
                return doParseRule(true);
            } else {
                String name = nextToken();
                List<Node> args = parseNodeList();
                Functor clause = new Functor(name, args, BuiltinRegistry.theRegistry);
                if (clause.getImplementor() == null) {
                    // Not a.error error becase later processing can add this
                    // implementation to the registry
                    logger.warn("Rule references unimplemented functor: " + name);
                }
                return clause;
            }
        }
        
        
        /**
         * Parse a rule, terminated by a "]" or "." character.
         */
        public Rule parseRule() {
            return doParseRule(false);
        }
        
        /**
         * Parse a rule, terminated by a "]" or "." character.
         * @param retainVarMap set to true to ccause the existing varMap to be left in place, which
         * is required for nested rules.
         */
        private Rule doParseRule(boolean retainVarMap) {
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
                if (!retainVarMap) varMap = new HashMap<>();
                // Body
                List<ClauseEntry> body = new ArrayList<>();
                token = peekToken();
                while ( !(token.equals("->") || token.equals("<-")) ) {
                    body.add(parseClause());
                    token = peekToken();
                }
                boolean backwardRule = token.equals("<-");
                List<ClauseEntry> head = new ArrayList<>();
                token = nextToken();   // skip -> token
                token = peekToken();
                while ( !(token.equals(".") || token.equals("]")) ) {
                    head.add(parseClause());
                    token = peekToken();
                } 
                nextToken();        // consume the terminating token
                Rule r = null;
                if (backwardRule) {
                    r =  new Rule(name, body, head);
                } else {
                    r = new Rule(name, head, body);
                }
                r.numVars = varMap.keySet().size();
                r.isBackward = backwardRule;
                return r;
            } catch (NoSuchElementException e) {
                throw new ParserException("Malformed rule", this);
            }
        }

    }
   
    /** Equality override */
    @Override
    public boolean equals(Object o) {
        // Pass 1 - just check basic shape
        if (! (o instanceof Rule) ) return false;
        Rule other = (Rule) o;
        if (other.head.length != head.length) return false;
        if (other.body.length != body.length) return false;
        // Pass 2 - check clause by clause matching
        for (int i = 0; i < body.length; i++) {
            if (! (body[i]).sameAs(other.body[i]) ) return false;
        }
        for (int i = 0; i < head.length; i++) {
            if (! (head[i]).sameAs(other.head[i]) ) return false;
        }
        // Also include the rule name in the equality contract
        if (name != null) {
            if ( !name.equals(other.name) ) return false;
        } else {
            if (other.name != null) return false;
        }
        return true;
    }
        
    /** hash function override */
    @Override
    public int hashCode() {
        int hash = 0;
        for ( ClauseEntry aBody : body )
        {
            hash = ( hash << 1 ) ^ aBody.hashCode();
        }
        for ( ClauseEntry aHead : head )
        {
            hash = ( hash << 1 ) ^ aHead.hashCode();
        }
        return hash;
    }
    
    /**
     * Compare clause entries, taking into account variable indices.
     * The equality function ignores differences between variables.
     */
    @Override
    public boolean sameAs(Object o) {
        return equals(o);
    }
    
//=======================================================================
// Other supporting inner classes

    /**
     * Inner class. Exception raised if there is a problem
     * during rule parsing.
     */
    public static class ParserException extends JenaException {
        
        /** constructor */
        public ParserException(String message, Parser parser) {
            super(constructMessage(message, parser));
        }
        
        /**
         * Extract context trace from prior tokens stack
         */
        private static String constructMessage(String baseMessage, Parser parser) {
            StringBuilder message = new StringBuilder();
            message.append(baseMessage);
            message.append("\nAt '");
            message.append(parser.recentTokens());
            message.append("'");
            return message.toString();
        }
        
    }
    
}
