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

package com.hp.hpl.jena.graph.impl;
import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.shared.*;

// Purely syntactic: Uses .equals, not .sameVAlueAs (see the one note at "PURE SYNTAX" below) 

/**
 * An implemantation of graph isomorphism for Graph equality.
 * The underlying algorithm is exponential but will only enter
 * a non-deterministic polynomial part when there are a lot of difficult to
 * distinguish anonymous nodes
 * connected to each other by statements with the same property(s).
 * Non-pathological examples, where most nodes have some properties that
 * help distinguish them from other nodes, will experience nearly linear
 * performance.
 */
public class GraphMatcher extends java.lang.Object {
    static private Random random = new Random(0);
 /**
 * Are the two models isomorphic?
 * The isomorphism is defined as a bijection between the anonymous
 * variables such that the statements are identical. 
 * This is
	 * described in 
	 * <a href="http://www.w3.org/TR/rdf-concepts#section-Graph-syntax">
     * http://www.w3.org/TR/rdf-concepts#section-Graph-syntax
     * </a>
 */
    static public boolean equals(Graph m1,Graph m2)   {
        if ( m1 == m2 )
            return true;
        return match(m1,m2) != null;
    }  
    
    static public int hashCode(Graph g) {
    	ClosableIterator<Triple> ci = GraphUtil.findAll( g );
    	int hash = 0;
    	GraphMatcher gm = new GraphMatcher(g);
    	while ( ci.hasNext() ) {
    		Triple t = ci.next();
    		hash += gm.new AnonStatement(t).myHashCode(null);
    	}
    	return hash;
    }
/**
 * Return an isomorphism between the two models.
 * This function is nondeterministic in that it may return a 
 * different bijection on each call, in cases where there are 
 * multiple isomorphisms between the models.
 * @return <code>null</code> on failure or an array of related pairs 
           (arrays of length 2) of anonymous nodes.
            <code>match(m1,m2)[i][0]</code>  is from <code>m1</code>, 
            and <code>match(m1,m2)[i][1]</code> is the corresponding node in
            <code>m2</code>.
 */
    static public Node[][] match(Graph m1,Graph m2)  {
            return new GraphMatcher(m1).match(new GraphMatcher(m2));
    }
    /* NOTE: inner classes
     *    We use a number of non-static inner classes, these all
     *    refer to the GraphMatcher for context.
     *
     * NOTE: the built-in hashCode() is not modified, so that Set's etc
     * still work.
     * This algorithm depends on a hash function, which I call myHashCode()
     * This has the somewhat perplexing property of changing as we do
     * the binding.
     * obj.myHashCode() depends on:
     *    -  obj and it's non anonymous subcomponents
     *    - ModelMatcher.myHashLevel (in the enclosing ModelMatcher)
     *    - for a bound AnonResource b in obj, it depends on a
     *      random value generated at the time that b got bound
     *    - for an unbound AnonResource, if myHashLevel>0, then
     *      it depends on the value of myHashCode() at myHashLevel-1
     *
     *
     *
     */
    static final private boolean TRACE = false;
    private Graph m;
    private GraphMatcher other;
    private int myHashLevel = 0; // This is usually 0, but can be any value
    // less than MAX_HASH_DEPTH
    
    
    static final private int MAX_HASH_DEPTH = 3;
    // I don't think there's much
    // mileage in a huge number here
    // A large number is likely to be unhelpful in typical
    // cases, but helps in pathological cases.
    // The pathological cases are the slowest, so perhaps it
    // is best to optimise for them.!
    
    // The rehashable - hash table
    // A Map from Integer => Bucket
    // Most of the time the table is a mess,
    // this is reflected in state=BAD
    private Map<Integer, Bucket> table;
    
    // This variable is mainly for sanity checking and
    // documentation. It has one logical impact in
    // AnonResource.myHashCodeFromStatement() and
    // AnonResource.wrapStatements()
    // AnonResource.myHashCodeFromStatement() requires
    // either state != HASH_BAD or myHashLevel = 0,
    // we ensure that one or other is the case in
    // AnonResource.wrapStatements().
    private int state;
    static final private int REHASHING = 1;
    static final private int HASH_OK = 2;
    static final private int HASH_BAD = 4;
    
    // As the algorithm proceeds we move resources
    // from one to the other.
    // At completion unBoundAnonResources is empty.
    private Set<AnonResource> unboundAnonResources = CollectionFactory.createHashedSet();
    private Set<AnonResource> boundAnonResources = CollectionFactory.createHashedSet();
    
    
    
    private GraphMatcher(Graph m1x) {
        m = m1x;
    }
    private Node[][] match(GraphMatcher oth) {
        other = oth;
        oth.other = this;
        in(HASH_BAD);
        
        // check that the size's are the same.
        // If the size is not accurate then it is a lower bound
        
        if (m.getCapabilities().sizeAccurate()
                && m.size() < other.m.size() )
            return null;
        if (other.m.getCapabilities().sizeAccurate()
                && m.size() > other.m.size() )
            return null;
        int myPrep = prepare(other.m);
        if ( myPrep == -1 || myPrep != other.prepare(m) ) {
            return null;
        }
        if ( bind() ) {
            if ( !unboundAnonResources.isEmpty() )
                impossible();
            Node rslt[][] = new Node[boundAnonResources.size()][];
            int ix = 0;
            for ( AnonResource r : boundAnonResources )
            {
                rslt[ix++] = new Node[]{ r.r, r.bound.r };
            }
            return rslt;
        }
        else {
            return null;
        }
    }
    // bind returns true if we have a binding,
    // false if not, in either case table is screwed.
    private boolean bind()   {
        Set<AnonResource> locallyBound = obligBindings();
        if (locallyBound==null)  // Contradiction reached - fail.
            return false;
        check(HASH_OK);
        Bucket bkt = smallestBucket();
        if ( bkt == null )
            return true;  // No smallest bucket - we are finished.
        Bucket otherBkt = other.matchBucket(bkt);
        if ( otherBkt != null ) {
            AnonResource v = bkt.aMember();
            Iterator<AnonResource> candidates = otherBkt.members();
            // System.out.println("Guessing");
            while ( candidates.hasNext() ) {
                check(HASH_OK|HASH_BAD);
                AnonResource otherV = candidates.next();
                trace(true,"Guess: ");
                if (!bkt.bind(v,otherBkt,otherV))
                    continue;
                if (bind())
                    return true;
                v.unbind();
            }
        }
        unbindAll(locallyBound);
        return false;
    }
    /*
     * Called with hashing incorrect.
     * Returns null if situation is unworkable.
     * Returns non-null with no outstanding obvious bindings,
     * and with the hashing correct.
     * The set of obligatorily bound resources is returned.
     *
     */
    private Set<AnonResource> obligBindings() {
        int hashLevel = 0;
        boolean newBinding;
        Set<AnonResource> rslt = CollectionFactory.createHashedSet();
        check(HASH_OK|HASH_BAD);
        do {
            if ( rehash(hashLevel) != other.rehash(hashLevel) ){
                unbindAll(rslt);
                return null;
            }
            refinableHash = false;
            newBinding = false;
            Iterator<Bucket> singles = scanBuckets();
            while ( singles.hasNext() ) {
                newBinding = true;
                Bucket bkt = singles.next();
                Bucket otherBkt = other.matchBucket(bkt);
                if ( otherBkt == null ) {
                    unbindAll(rslt);
                    return null;
                }
                AnonResource bindMe = bkt.aMember();
                if (!bkt.bind(otherBkt)) {
                    unbindAll(rslt);
                    return null;
                }
                rslt.add(bindMe);
            }
            if ( newBinding )
                hashLevel = 0;
            else
                hashLevel++;
        } while (hashLevel<MAX_HASH_DEPTH && (refinableHash||newBinding));
        return rslt;
    }
    // Communication between obligBindings and scanBuckets
    private boolean refinableHash;
    private Iterator<Bucket> scanBuckets() {
        // Looks through buckets,
        // if has single member then return in iterator.
        // Otherwise if some member of the bucket has friends
        // we can refine the hash, and we set refinableHash.
        check(HASH_OK);
        return new FilterIterator<>(
        new Filter<Bucket>() {
            @Override public boolean accept(Bucket o) {
                Bucket b = o;
                if (b.size()==1)
                    return true;
                if (!refinableHash) {
                    Iterator<AnonResource> it = b.members();
                    while ( it.hasNext() )
                        if (!it.next()
                        .friends.isEmpty()) {
                            refinableHash = true;
                            break;
                        }
                }
                return false;
            }
        },table.values().iterator());
        
    }
    private void unbindAll(Set<AnonResource> s)  {
        for ( AnonResource value : s )
        {
            value.unbind();
        }
        in(HASH_BAD);
    }
    private int prepare(Graph otherm)  {
        ClosableIterator<Triple> ss = GraphUtil.findAll( m );
        myHashLevel = 0;
        int hash = 0;
        try {
            while ( ss.hasNext() ) {
                Triple s = ss.next();
                AnonStatement ass = new AnonStatement(s);
                if ( ass.pattern == NOVARS ) {
                    if ( !otherm.contains( s ) ) return -1;
                } else {
                    hash += ass.myHashCode(ass.vars[0]);
                    for (int i=0;i<ass.vars.length;i++) {
                        ass.vars[i].occursIn.add(ass);
                        for (int j=i+1;j<ass.vars.length;j++) {
                            ass.vars[i].friends.add(ass.vars[j]);
                            ass.vars[j].friends.add(ass.vars[i]);
                        }
                    }
                }
            }
            return hash==-1?1:hash;
        }
        finally {
            ss.close();
        }
    }
    private Bucket smallestBucket() {
        check(HASH_OK);
        Iterator<Bucket> bit = table.values().iterator();
        Bucket smallB = null;
        int smallest = Integer.MAX_VALUE;
        while ( bit.hasNext() ) {
            Bucket b = bit.next();
            int sz = b.size();
            if ( sz < smallest ) {
                smallB = b;
                smallest = sz;
            }
        }
        return smallB;
    }
    private Bucket matchBucket(Bucket key) {
        check(HASH_OK);
        Integer hash = new Integer(key.aMember().myHash);
        Bucket rslt = table.get(hash);
        if ( rslt != null ) {
            if ( key.size() != rslt.size() )
                return null;
        }
        return rslt;
    }
    
    
    /* rehash performance notes:
     *Uncommenting below gives an easy way of measuring
     *rehash performance.
     *On a 480ms job the rehash appeared to take over 200ms.
     *(Since with the code below uncommented the same 
     *problem took about 1300ms).
     *
     */
    private int rehash(int lvl) {
        /*
        rehash0(lvl);
        rehash0(lvl);
        rehash0(lvl);
        rehash0(lvl);
         **/
        return rehash0(lvl);
    }
        
    private int rehash0( int level ) {
        in(REHASHING);
        this.table = CollectionFactory.createHashedMap();
        // Set a global to define the hash of an AnonResource
        // level = 0 ==> AnonResource.myHashCode() = 0
        // level = n+1 ==> AnonResource.myHashCode() = hash[n]
        myHashLevel = level;
        
        // Now compute all hashes and stick things in the
        // right buckets.
        for ( AnonResource a : unboundAnonResources )
        {
            Integer hash = new Integer( a.myHashCode() );
            Bucket bkt = table.get( hash );
            if ( bkt == null )
            {
                bkt = new Bucket();
                table.put( hash, bkt );
            }
            bkt.add( a );
        }
        
        // Produce a checksum for the table.
        int rslt = 0;

        for ( Map.Entry<Integer, Bucket> pair : table.entrySet() )
        {
            int hash = pair.getKey().intValue();
            Bucket bkt = pair.getValue();
            int sz = bkt.size();
            rslt += sz * 0x10001 ^ hash;
        }
        
        in(HASH_OK);
        return rslt;
        
    }
    
    /* subjects identified by bits 0 and 1,
     * predicate           by bits 2 and 3,
     * object              by      4 and 5
     * If neither bit set then role is bound.
     * If lower bit is set then role is unbound to
     * singleton variable in triple.
     * If higher bit is set then role is unbound
     * with anonymous variable that is also
     * unbound to a different role.
     * It is an error if both bits are set.
     *
     *
     * These funny things are read like this: e.g.
     *
     * SXPYOX - the subject is a variable X,
     *          the predicate is another var Y
     *          the object is the same var X
     *
     */
    static final private int NOVARS = 0;
    static final private int SX = 1;
    static final private int PX = 4;
    static final private int OX = 16;
    // SD, PD and OD are illegal values
    // by themselves, should only
    // be found in combination with
    // each other.
    // D for duplicate.
    static final private int SD = 2;
    static final private int PD = 8;
    static final private int OD = 32;
    static final private int SXPY = SX|PX;
    static final private int SXOY = SX|OX;
    static final private int PXOY = PX|OX;
    static final private int SXPYOZ = SX|PX|OX;
    static final private int SXPX = SD|PD;
    static final private int SXOX = SD|OD;
    static final private int PXOX = PD|OD;
    static final private int SXPXOY = SD|PD|OX;
    static final private int SXPYOX = SD|OD|PX;
    static final private int SXPYOY = SX|PD|OD;
    static final private int SXPXOX = SD|PD|OD;
    static final private int S = SX|SD;
    static final private int P = PX|PD;
    static final private int O = OX|OD;
    
    static private boolean legalPattern(int mask) {
        switch (mask) {
            case NOVARS:
            case SX:
            case OX:
            case PX:
            case SXPY:
            case SXOY:
            case PXOY:
            case SXPYOZ:
            case SXPX:
            case SXOX:
            case PXOX:
            case SXPXOY:
            case SXPYOX:
            case SXPYOY:
            case SXPXOX:
                return true;
                default:
                    return false;
        }
    }
    // if i = 0 return the X component of pattern
    // if i = 1 return the Y component of pattern
    // if i = 2 return the Z component of pattern
    static private int varPosInPattern(int i,int pattern) {
        switch (pattern) {
            case NOVARS:
                break;
            case SX:
                if (i==0) return SX;
                break;
            case OX:
                if (i==0) return OX;
                break;
            case PX:
                if (i==0) return PX;
                break;
            case SXPY:
                switch (i) {
                    case 0:
                        return SX;
                    case 1:
                        return PX;
                }
                break;
            case SXOY:
                switch (i) {
                    case 0:
                        return SX;
                    case 1:
                        return OX;
                }
                break;
            case PXOY:
                switch (i) {
                    case 0:
                        return PX;
                    case 1:
                        return OX;
                }
                break;
            case SXPYOZ:
                switch (i) {
                    case 0:
                        return SX;
                    case 1:
                        return PX;
                    case 2:
                        return OX;
                }
                break;
            case SXPX:
                if (i==0) return SXPX;
                break;
            case SXOX:
                if (i==0) return SXOX;
                break;
            case PXOX:
                if (i==0) return PXOX;
                break;
            case SXPXOY:
                switch (i) {
                    case 0:
                        return SXPX;
                    case 1:
                        return OX;
                }
                break;
            case SXPYOX:
                switch (i) {
                    case 0:
                        return SXOX;
                    case 1:
                        return PX;
                }
                break;
            case SXPYOY:
                switch (i) {
                    case 0:
                        return SX;
                    case 1:
                        return PXOX;
                }
                break;
            case SXPXOX:
                if (i==0) return SXPXOX;
                break;
        }
        System.out.println("Bad: " + i + " " + pattern);
        impossible();
        return 0;
    }
    static private interface SomeResource {
        int myHashCodeFromStatement();
        boolean mightBeEqual(SomeResource r);
    }
    static private class FixedResource implements SomeResource {
        int hash;
        Node node;
        @Override
        public String toString() {
            return "f" + hash;
        }
        @Override
        public int myHashCodeFromStatement() {
            return hash;
        }
        FixedResource(Node n) {
            hash = n.hashCode();
            node = n;
        }
        @Override
        public boolean mightBeEqual(SomeResource r) {
            if (r!=null && (r instanceof FixedResource)) {
                FixedResource f = (FixedResource)r;
                return hash == f.hash && node.equals(f.node); // PURE SYNTAX
            } else {
                return false;
            }
        }
    }
    
    // Record the occurence of variable r in bag.
    static void count(Map<SomeResource, int[]> bag, SomeResource r,int pos) {
        if ( r instanceof AnonResource ) {
            int v[] = bag.get(r);
            if (v==null) {
                v=new int[]{-1,-1,-1};
                bag.put(r,v);
            }
            for (int i=0;i<3;i++)
                if ( v[i] == -1 ) {
                    v[i] = pos;
                    return;
                }
        }
    }
    private class AnonStatement {
        int varCount;
        AnonResource vars[];
        SomeResource subj;
        SomeResource pred;
        SomeResource obj;
        int pattern;
        AnonStatement(Triple s) {
            Map<SomeResource, int[]> bag = CollectionFactory.createHashedMap();
            pattern = NOVARS;
            subj = convert(s.getSubject());
            pred = convert(s.getPredicate());
            obj = convert(s.getObject());
            count(bag,subj,0);
            count(bag,pred,2);
            count(bag,obj,4);
            varCount = bag.size();
            vars = new AnonResource[varCount];
            add(subj);
            add(pred);
            add(obj);
            for ( int[] v : bag.values() )
            {
                int last = 2;
                int p;
                while ( v[last] == -1 )
                {
                    last--;
                }
                if ( last == 0 )
                {
                    p = SX;
                }
                else
                {
                    p = SD;
                }
                for ( int i = 0; i <= last; i++ )
                {
                    pattern |= p << v[i];
                }
            }
            if (!legalPattern(pattern)) {
                System.out.println("s: " + subj + " p: " + pred + " o: " + obj + " pattern: " + pattern);
                impossible();
            }
        }
        private void add(SomeResource r) {
            if ( r instanceof AnonResource ) {
                for (int i=0;i<vars.length; i++)
                    if (vars[i]==null || vars[i]==r ) {
                        vars[i] = (AnonResource)r;
                        return;
                    }
                impossible();
            }
        }
        
        // returns the location of v in this statement.
        // e.g. PXOX to say that v is both the predicate and object.
        int varPos(AnonResource v) {
        	if ( v == null)
        	  return 0;
            for (int i=0;i<vars.length;i++)
                if ( vars[i] == v )
                    return varPosInPattern(i,pattern);
            impossible();
            return 0;
        }
        int myHashCode(AnonResource v) {
            int vX = varPos(v);
            int hash = vX;
            // The multipliers are chosen to be 2 bit numbers.
            // These muddle up the bits; should be quick in an optimised
            // compilation or JIT (a shift and an add); and ensure
            // that positional information (SPO) is encoded in the hash.
            if ( (vX & S) == 0) {
                hash ^= subj.myHashCodeFromStatement() * 0x101;
            }
            if ( (vX & P )== 0 ) {
                hash ^= pred.myHashCodeFromStatement() * 0x3f;
            }
            if ( (vX & O )== 0 ) {
                hash ^= obj.myHashCodeFromStatement() * 0x41;
            }
            return hash;
        }
        boolean contextualEquals(AnonResource v,AnonStatement sB,AnonResource vB) {
            int vX = varPos(v);
            if ( vX != sB.varPos(vB) )
                return false;
            return
            ((vX & S) != 0 || subj.mightBeEqual(sB.subj))
            && ((vX & P) != 0 || pred.mightBeEqual(sB.pred))
            && ((vX & O) != 0 || obj.mightBeEqual(sB.obj));
            
        }
    }
    
    // Bucket's live longer than the table that they sit in.
    // If a bucket is matched before the main bind() loop then
    // we are iterating over it's members while the rest of the
    // algorithm is proceeding.
    private class Bucket {
        Set<AnonResource> anonRes = CollectionFactory.createHashedSet();
        int hash[] = new int[MAX_HASH_DEPTH];
        boolean bind(Bucket singleton) {
            return bind(aMember(),singleton,singleton.aMember());
        }
        boolean bind(AnonResource mine,Bucket other,AnonResource binding) {
            if ( mine.checkBinding(binding) ) {
                 mine.bind(binding);
                return true;
            } else {
                return false;
            }
        }
        
        void add(AnonResource r) {
            anonRes.add(r);
        }
        AnonResource aMember() {
            return anonRes.iterator().next();
        }
        Iterator<AnonResource> members() {
            return anonRes.iterator();
        }
        int size() {
            return anonRes.size();
        }
    }
    private class AnonResource  implements SomeResource {
        AnonResource bound;
        Node r;
        Set<AnonStatement> occursIn = CollectionFactory.createHashedSet(); // The AnonStatements containing me.
        int hash[] = new int[MAX_HASH_DEPTH];
        int boundHash;
        Set<AnonResource> friends = CollectionFactory.createHashedSet(); // Other vars in AnonStatements containing me.
        int myHash;
        
        @Override
        public String toString() {
            String rslt = r.toString();
            if ( bound!=null )
                rslt += "[" + bound.r.toString() + "]";
            return rslt;
        }
        
        AnonResource(Node r) {
            unboundAnonResources.add(this);
            this.r = r;
        }
        @Override
        public int myHashCodeFromStatement() {
            if ( bound != null )
                return boundHash;
            if (myHashLevel==0) {
                return 0xcafebabe;
            }
            check(REHASHING|HASH_OK);
            return hash[myHashLevel-1];
        }
        // MUST NOT BE CALLED FROM WITHIN THE LOOP
        // OF OBLIG BINDINGS, use myHash
        // ONLY INTENDED TO BE CALLED FROM WITHIN rehash
        int myHashCode() {
            check(REHASHING);
            if ( bound!=null )
                impossible();
            myHash = 0;
            for ( AnonStatement ass : occursIn )
            {
                myHash += ass.myHashCode( this );
            }
            hash[myHashLevel] = myHash;
            return myHash;
        }
        void bind(AnonResource pair)  {
            bound = pair;
            
            if (!unboundAnonResources.remove(this))
                impossible();
            boundAnonResources.add(this);
            
            if ( pair.bound == null ) {
                trace( true, r.getBlankNodeId()+ "=" + pair.r.getBlankNodeId() + ", " );
                pair.bind(this);
                // choice any arbitary number here
                // helps spread the bits.
                bound.boundHash= boundHash =random.nextInt();
                //  if ( myHash != bound.myHash )
                //      impossible();
                // Sometimes they are different, after we have
                // guessed badly, changed bound.myHash and then
                // backtracked.
            }
            
            if ( bound.bound != this )
                impossible();
        }
        void unbind()  {
            AnonResource pair = bound;
            bound = null;
            
            if (!boundAnonResources.remove(this))
                impossible();
            
            unboundAnonResources.add(this);
            
            if ( pair.bound != null ) {
                trace( false, r.getBlankNodeId() + "!=" + pair.r.getBlankNodeId() + ", " );
                if ( pair.bound != this )
                    impossible();
                
                pair.unbind();
            }
            
            in(HASH_BAD);
        }
        boolean checkBinding( AnonResource pair ) {
            
            if ( occursIn.size() != pair.occursIn.size() )
                return false;
            
            Set<StatementWrapper> ourStatements = wrapStatements();
            Set<StatementWrapper> otherStatements = pair.wrapStatements();
            
            return ourStatements.removeAll(otherStatements)
            && ourStatements.isEmpty();
            
        }
        private Set<StatementWrapper> wrapStatements() {
            if ( state == HASH_BAD ) {
                // We are already in(HASH_BAD).
                // We need to use AnonResource.myHashCodeFromStatement().
                // That is OK as long as myHashLevel is 0
                myHashLevel = 0;
            }
            Set<StatementWrapper> statements = CollectionFactory.createHashedSet();
            // Add all our statements to the set.
            for ( AnonStatement anOccursIn : occursIn )
            {
                statements.add( wrapStatement( anOccursIn ) );
            }
            return statements;
        }
        @Override
        public boolean mightBeEqual(SomeResource r) {
            if (r!=null && (r instanceof AnonResource)) {
                AnonResource a = (AnonResource)r;
                return a==this
                || bound == a
                || (bound == null && a.bound == null);
            } else {
                return false;
            }
        }
        StatementWrapper wrapStatement(AnonStatement s) {
            return new StatementWrapper(s);
        }
        // inner inner class -- ouch!
        private class StatementWrapper {
            int wrapHash;
            AnonStatement statement;
            @Override public boolean equals(Object o) {
                if (o == null || (!(o instanceof StatementWrapper)))
                    return false;
                StatementWrapper w = (StatementWrapper)o;
                return wrapHash == w.wrapHash &&
                statement.contextualEquals(AnonResource.this,w.statement,w.asAnonR());
            }
            @Override public int hashCode() {
                return wrapHash;
            }
            
            StatementWrapper( AnonStatement s ) {
                wrapHash = s.myHashCode(AnonResource.this);
                statement = s;
            }
            
            AnonResource asAnonR() {
                return AnonResource.this;
            }
        }
    }
    
    private Map<Node, SomeResource> anonLookup = CollectionFactory.createHashedMap();
    
    private SomeResource convert(Node n) {
        if ( n.isBlank() ) {
            SomeResource anon = anonLookup.get(n);
            if ( anon == null ) {
                anon = new AnonResource( n );
                anonLookup.put(n,anon);
            }
            return anon;
        } else {
            return new FixedResource(n);
        }
    }
    
    private void check(int s) {
        if (( state & s) == 0 ) 
            impossible();
    }
    
    private void in(int s) {
        state = s;
        other.state = s;
    }
    
    static private void impossible() {
        throw new JenaException( "Cannot happen!" );
    }
    
    static private int col = 0;
    static private boolean lastDir = false;
    
    static private void trace(boolean dir, String s) {
        if (TRACE) {
            if ( dir != lastDir ) {
                traceNL();
                lastDir = dir;
            }
            int nCol = col + s.length();
            if ( col != 0 && nCol > 70 ) {
                traceNL();
                nCol = s.length();
            }
            System.out.print(s);
            System.out.flush();
            col = nCol;
        }
    }
    static private void traceNL() {
        if ( TRACE ) {
            System.out.println();
            col = 0;
        }
    }
    
}
