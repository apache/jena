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

package org.apache.jena.irix;

import java.util.Objects;
import java.util.Optional;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;

/**
 * A resolver is a base IRI and a policy for resolution. The policy choices are
 * <ul>
 * <li>whether to resolve against the base, or only consider the IRI being processed</li>
 * <li>whether to allow relative IRIs after resolving</li>
 * </ul>
 * <p>
 * The normal behaviour is to resolve URIs and not allow relative IRIs.
 * </p><p>
 * A setup with "don't resolve" and "allow relative URIs" passes URIs through untouched,
 * although a parser may issue a warning.
 * </p><p>
 * The base may be null to support passing around a resolver that accepts/reject
 * IRIs. For application to check IRIs, use {@link IRIs#check}/{@link IRIs#checkEx}
 * directly.
 * </p>
 */
public class IRIxResolver {

    private final IRIx base;
    private final boolean resolve;
    private final boolean allowRelative;

    private IRIxResolver(IRIx base, boolean resolve, boolean allowRelative) {
        this.base = base;
        this.resolve = resolve;
        this.allowRelative = allowRelative;
    }

    /** Return the base of this resolver */
    public IRIx getBase() { return base; }

    /** Return the base of this resolver as a string */
    public String getBaseURI() { return base == null ? null : base.str(); }

    /*
     * Some providers are expensive compared to the cost of parsing.
     * During parsing a lot of IRIs are being created, many the same. For example,
     * e.g. properties or blocks of triples with the same subject.
     */
    private static int DftCacheSize = 500;
    private Cache<String, IRIx> cache = CacheFactory.createCache(DftCacheSize);

    /** Resolve the argument URI string according to resolver policy */
    public IRIx resolve(String other) {
        Objects.requireNonNull(other);
        if ( cache == null )
            return resolve0(other);

        // To allow for exceptions, we try the cache, and if no hit,
        // make the result then try again to fill the cache.
        // Because for a given key there is one right answer, it does not matter
        // if any thread gets in and changes the cache (cache operations are
        // thread safe).

        IRIx iri = cache.getIfPresent(other);
        if ( iri != null )
            return iri;
        // May throw an exception
        IRIx iriValue = resolve0(other);
        return cache.get(other, k->iriValue);
    }

    private IRIx resolve0(String str) {
        IRIx x = (base != null && resolve)
                ? base.resolve(str)
                : IRIx.create(str);
        if ( ! allowRelative && x.isRelative() )
            throw new RelativeIRIException("Relative IRI: <"+str+">");
        return x;
    }

    /** Create a new resolver with the same policies as the old one but with a different base URI. */
    public IRIxResolver resetBase(IRIx newBase) {
        return new IRIxResolver(newBase, resolve, allowRelative);
    }

    @Override
    public String toString() {
        return "IRIxResolver[base=" + base + ", resolve=" + resolve + ", relative=" + allowRelative+"]";
    }

   public static Builder create() { return new Builder(); }

   /**
    * Create a {@link IRIxResolver} with the base URI which is resolved against the
    * current system default base.
    */
   public static Builder create(IRIxResolver original) {
       Builder builder = new Builder();
       builder.base = Optional.ofNullable(original.base);
       builder.resolve = original.resolve;
       builder.allowRelative = original.allowRelative;
       return builder;
   }

   /**
    * Create a builder for a {@link IRIxResolver} with the base URI which is resolved against the
    * current system default base.
    */
   public static Builder create(IRIx baseIRI) {
       return new Builder().base(baseIRI);
   }

   /**
    * Create a builder for a {@link IRIxResolver} with the base URI
    * which is resolved against the current system default base.
    */
   public static Builder create(String baseStr) {
       IRIx base = (baseStr == null) ? null : IRIs.resolveIRI(baseStr);
       return new Builder().base(base);
   }

   public static class Builder {
       // null is "unset".
       private Optional<IRIx> base   = null;
       private boolean resolve       = true;
       private boolean allowRelative = true;

       private Builder() {}

       public Builder base(IRIx baseURI) {
           this.base = Optional.ofNullable(baseURI);
           return this;
       }

       public Builder base(String baseStr) {
           IRIx baseIRI = (baseStr == null) ? null : IRIs.resolveIRI(baseStr);
           this.base = Optional.ofNullable(baseIRI);
           return this;
       }

       public Builder noBase() {
           this.base = Optional.empty();
           return this;
       }

       public Builder resolve(boolean resolveURIs) {
           this.resolve = resolveURIs;
           return this;
       }

       public Builder allowRelative(boolean allowRelative) {
           this.allowRelative = allowRelative;
           return this;
       }

       public IRIxResolver build() {
           if ( base == null )
               throw new IRIException("Base has not been set");
           IRIx baseIRI = base.orElse(null);
           return new IRIxResolver(baseIRI, resolve, allowRelative);
       }
   }
}

