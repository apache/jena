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

package org.apache.jena.iri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.jena.iri.impl.AbsIRIFactoryImpl ;
import org.apache.jena.iri.impl.Main ;

/**
 * An IRI.
 * The IRI may or may not conform to a variety of standards,
 * {@link IRIFactory}. Methods allow:
 * <ul>
 * <li>
 *  accessing the
 * components of the IRI, (both in raw and decoded form).
 * <li> converting an IRI to a URI.
 * <li>
 * accessing {@link Violation}s of the 
 standards being enforced.
 <li>creating new IRI objects 
 * by resolving against this IRI as a base.
 * <li>
 * creating relative IRIs that when resolved against this IRI,
 * would give a given absolute IRI.
 * </ul>
 * 
 * <p>
 * The standards, and other setttings, 
 * which are used in the methods are
 * determined by the configuration of the {@link IRIFactory}
 * used to create this IRI.
 * To check for conformance with a different standard,
 * the IRI must be passed to {@link IRIFactoryI#create(IRI)}
 * of a different appropriately configured {@link IRIFactory}.
 * </p>
 */
abstract public class IRI  extends AbsIRIFactoryImpl implements IRIFactoryI, IRIRelativize {

    /**
     * Advanced usage only:
     * all the violations found in analyzing
     * this IRI. Some will be errors; others
     * will be warnings;
     * some will be violations of standards that are
     * not being enforced, or are irrelevant (e.g. the http
     * specification for an ftp URL); 
     * some will be simply internal
     * workings of the IRI code.
     * @return All the {@link Violation}s found.
     */
//    Iterator allViolations();
    /**
     * The error and warning conditions
     * associated with this IRI violating the
     * standards associated with its factory.
      @param includeWarnings If true then warnings are returned as well as errors.
    
     * @return The {@link Violation}s found which violate the
     *         factory's standards.
     */
    abstract public Iterator<Violation> violations(boolean includeWarnings);
    
    
    /**
     * The authority component, found between
     * the first "//" and the next "/".
     * No decoding is done; this method is cheap.
     * @return The authority component, or null if none.
     */
    abstract public String getRawAuthority();
    
    /**
     * The authority component, found between
     * the first "//" and the next "/".
     * Any  legal  percent escape sequences are decoded.
     * If the host name is an Internationalized Domain Name,
     * then that is decoded too.
     * This method may be more expensive than {@link #getRawAuthority()}.
     * @return The authority component, or null if none.
     */
    abstract public String getAuthority();
    /**
     * The fragment, found after a "#" at the end
     * of the main URI (note a fragment may itself
     * contain a "#").
     * No decoding is done; this method is cheap.
     * @return The fragment, or null if none,
     */
    abstract public String getRawFragment();
    /**
     * The fragment, found after a "#" at the end
     * of the main URI (note a fragment may itself
     * contain a "#").
     * Any  legal  percent escape sequences are decoded.
     * This method may be more expensive than {@link #getRawFragment()}.
     * @return The fragment, or null if none,
     */
    abstract public String getFragment();
    /**
     * The host part of the authority.
     * Found between an optional "@" and an
     * optional ":" in the authority.
     * 
     * No decoding is done; this method is cheap.
     * @return The host, or null if none.
     */
    abstract public String getRawHost();
    /**
     * The host part of the authority.
     * Found between an optional "@" and an
     * optional ":" in the authority.
     * 
     * Any  legal  percent escape sequences are decoded.
     * Any legal punycode is decoded.
     * This method may be more expensive than {@link #getRawHost()}.
     
     * @return The host, or null if none.
     */
    abstract public String getHost();
    
    /**
     * The host part of the authority, encoded
     * as an International Domain Name.
     * 
     * Any  legal  percent escape sequences are decoded.
     * 
     * Any non-ASCII characters are encoded as punycode, if possible.
     * 
     * This may be impossible (even in the ASCII case),
     * in which case an exception is thrown.
     * 
     * This method may be more expensive than {@link #getRawHost()}.     
     * @return The host as an IDN, or null if none.
     * @throws MalformedURLException An Internationalized Domain Name algorithm failed, there is no equivalent ascii string.
    
     */
    abstract public String getASCIIHost() throws MalformedURLException ;
    
    /**
     * The path component of the IRI; always
     * present, possibly the empty string.
     * This includes any leading "/".
     * Found after the authority, and before any
     * "?" or "#".
     * No decoding is done; this method is cheap.
     * @return The path.
     */
    abstract public String getRawPath();
    /**
     * The path component of the IRI; always
     * present, possibly the empty string.
     * This includes any leading "/".
     * Found after the authority, and before any
     * "?" or "#".
     * Any  legal  percent escape sequences are decoded.
     * This method may be more expensive than {@link #getRawPath()}.

     * @return The path.
     */
    abstract public String getPath();
    /**
     * Return code from {@link #getPort()},
     * indicating no port component found.
     */
    final public static int NO_PORT = -1;
    /**
     * Return code from {@link #getPort()},
     * indicating that a port component was found,
     * but it is not a non-negative integer.
     */
    final public static int ILLFORMED_PORT = -2;
    /**
     * The port number from the authority component.
     * Found after the last ":" in the authority.
     * 
     * @return The port number, or {@link #NO_PORT} or {@link #ILLFORMED_PORT}.
     */
    abstract public int getPort();
    /**
     * The query component of the IRI.
     * Found after the "?" and before an optional "#".
     * No decoding is done; this method is cheap.
     * @return The query component, or null if none.
     */
    abstract public String getRawQuery();
    /**
     * The query component of the IRI.
     * Found after the "?" and before an optional "#".
     * Any  legal  percent escape sequences are decoded.
     * This method may be more expensive than {@link #getRawQuery()}.
     * @return The query component, or null if none.
     */
    abstract public String getQuery();
    /**
     * The scheme component of the IRI.
     * Found before the first ":".
     * @return The scheme component, or null if none.
     */
    abstract public String getScheme();
    /**
     * The user information part of the authority
     * component of the IRI.
     * Found before the first "@" in the authority.
     * May include a ":" separating the user name
     * from a password, {@link ViolationCodes#HAS_PASSWORD}.
     * 
     * No decoding is done; this method is cheap.
     * 
     * @return The user information, or null if none.
     */
    abstract public String getRawUserinfo();
    /**
     * The user information part of the authority
     * component of the IRI.
     * Found before the first "@" in the authority.
     * May include a ":" separating the user name
     * from a password, {@link ViolationCodes#HAS_PASSWORD}.
     * Any  legal  percent escape sequences are decoded.
     * This method may be more expensive than {@link #getRawUserinfo()}.
     * @return The user information, or null if none.
     */
    abstract public String getUserinfo();
    /**
     * Are there any violations of the factory's
     * specification settings.
     * Quick check, equivalent to 
     * <code>violations(includeWarnings).hasNext()</code>,
     * but faster.
     * @param includeWarnings If true then warnings are reported as well as errors.
     * @return true if the factory's specifications have been violated.
     */
    abstract public boolean hasViolation(boolean includeWarnings);
    

    /**
     * Are there any violations of the given
     * specification.
     * Quick check, equivalent to 
     * <code>exceptions(conformance).hasNext()</code>,
     * but faster.
     * @param conformance the specifications to check against.
     * @return true if the given specification(s) have been violated.
     */
//    boolean hasException(int conformance);
   
    /**
     * Does this IRI specify a scheme.
     * @return True if this IRI has a scheme specified.
     */
    abstract public boolean isAbsolute();

//    public boolean isIRI();
    
  /**
   * Is this an 'opaque' IRI.
   * Is this IRI an absolute IRI with a
   *  path matching the path-rootless
   * production:
   * i.e. it has a scheme but no authority, and a path not starting in "/".
   * Note: in the earlier RFC 2396 this concept was called being opaque.
   * @return True if the non-empty path of this non-relative IRI is rootless
   */
    abstract public boolean isRootless();
//    public boolean isRDFURIReference();
    /**
     * Is this IRI a relative reference without
     * a scheme specified.
     * @return True if the IRI is a relative reference
     */
    abstract public boolean isRelative();
//    public boolean isURIinASCII();
    
//    boolean isXSanyURI();
    /**
     * Returns an IRI that when resolved against
     * this IRI would return <code>abs</code>.
     * If possible, a relative IRI is formed,
     * using any of the methods specified in flags,
     * which is a bitwise or of values from
     * {@link IRIRelativize}.
     * <p>
     * If <code>abs</code> contains a dot
     * segment (either "/./" or "/../") then
     * the contract cannot be satisfied and an incorrect
     * answer is returned. This incorrect return value has an
     * {@link ViolationCodes#NON_INITIAL_DOT_SEGMENT}
     * violation associated with it. 
     * </p>
     * @param abs An absolute IRI to make relative.
     * @param flags Which type of relative IRIs to permit.
     * @return A relative or absolute IRI equivalent to abs.
     */
    abstract public IRI relativize(IRI abs, int flags);

    /**
     * Returns an IRI that when resolved against
     * this IRI would return <code>abs</code>.
     * If possible, a relative IRI is formed,
     * using default methods.
     * <p>
     * If <code>abs</code> contains a dot
     * segment (either "/./" or "/../") then
     * the contract cannot be satisfied and an incorrect
     * answer is returned. This incorrect return value has an
     * {@link ViolationCodes#NON_INITIAL_DOT_SEGMENT}
     * violation associated with it. 
     * </p>
     * @param abs An absolute IRI to make relative.
     * @return A relative or absolute IRI equivalent to abs.
     */
    abstract public IRI relativize(IRI abs);
    /**
     * Returns an IRI that when resolved against
     * this IRI would return <code>abs</code>.
     * If possible, a relative IRI is formed,
     * using default methods.
     * <p>
     * If <code>abs</code> contains a dot
     * segment (either "/./" or "/../") then
     * the contract cannot be satisfied and an incorrect
     * answer is returned. This incorrect return value has an
     * {@link ViolationCodes#NON_INITIAL_DOT_SEGMENT}
     * violation associated with it. 
     * </p>
     * 
     * @param abs An absolute IRI to make relative.
     * @return A relative or absolute IRI equivalent to abs.
     */
    abstract public IRI relativize(String abs);
    /**
     * Returns an IRI that when resolved against
     * this IRI would return <code>abs</code>.
     * If possible, a relative IRI is formed,
     * using any of the methods specified in flags,
     * which is a bitwise or of values from
     * {@link IRIRelativize}.
     * <p>
     * If <code>abs</code> contains a dot
     * segment (either "/./" or "/../") then
     * the contract cannot be satisfied and an incorrect
     * answer is returned. This incorrect return value has an
     * {@link ViolationCodes#NON_INITIAL_DOT_SEGMENT}
     * violation associated with it. 
     * </p>
     * @param abs An absolute IRI to make relative.
     * @param flags Which type of relative IRIs to permit.
     * @return A relative or absolute IRI equivalent to abs.
     */
    abstract public IRI relativize(String abs, int flags);
    // TODO check percent encoding to punycode.
    /**
     * Converts the IRI into ASCII.
     * The hostname is converted into punycode;
     * other components are converted using percent encoding.
     * Even if the IRI is already ASCII, the hostname
     * may be modified, if, for example, it (inappropriately)
     * uses percent encoding.
     * This may be impossible (even in the ASCII case),
     * in which case an exception is thrown.
     * 
     * 
     * @return An ASCII string corresponding to this IRI.
     * @throws MalformedURLException An Internationalized Domain Name algorithm failed, there is no equivalent ascii string.
     */
    abstract public String toASCIIString() throws MalformedURLException;
    /**
     * The logical IRI string as originally specified, 
     * use {@link #toDisplayString()} for display purposes
     * such as error messages.
     * @return The IRI string
     */
    @Override
    abstract public String toString();
    /**
     * The IRI string with any recommended bi-directional control
     * characters (if necessary) to ensure correct display.
     * @return The IRI string formatted with unicode bidi control characters
     */
    abstract public String toDisplayString();
    /**
     * Converts the IRI to an ASCII string, and then to a URL.
     * 
     * @return a URL corresponding to this IRI.
     * @throws MalformedURLException If IDNA conversion failed, or from java.net.URL 
     */
    abstract public URL toURL() throws MalformedURLException;
    
    /**
     * Resolves an IRI against this one.
     * This method is an alias for
     * {@link IRIFactory#create(IRI)}.
     * @see IRIFactory#construct(IRI)
     * @param relative IRI to resolve
     * @return The resolution of relative against this.
     */
    final public IRI resolve(IRI relative) {
    	return create(relative);
    }

    /**
     * Resolves an IRI against this one.
     * This method is an alias for
     * {@link IRIFactory#create(String)}.
     * @see IRIFactory#construct(String)
     * @param relative IRI to resolve
     * @return The resolution of relative against this.
     */
    final public IRI resolve(String relative) {
    	return create(relative);
    }
    /**
     * To be defined - 
     * return result does not violate any minting conditions.
     * @param useDns  If true, do DNS look ups to normalize hostname.
     * @return An equivalent, normalized IRI
     */
    abstract public IRI normalize(boolean useDns);
    
    /**
     * To be defined: use the comparison ladder.
     * @param iri IRI
     * @param other  Specifies where on the ladder to make the comparison.
     * @return True if this IRI is equal to the given one, when normalized with rules specified by other.
     */
    abstract public boolean ladderEquals(IRI iri,int other);
    
    /**
     * To be defined: use the comparison ladder.
     * @param iri IRI
     * @return A value for other to make {@link #ladderEquals(IRI, int)} true, or -1 if none. 
     */
    abstract  public int ladderEquals(IRI iri);

    /**
     * Check one or more IRIs against a specification.
     * Usage:
     * <pre>
     * java <em>&lt;classpath&gt;</em> [ -help ] [ -iri | -uri | -xml | -schema | -xlink ] [ -f <em>file</em> ] [ <em>iri</em> ... ]
     * </pre>
     * If no file or iris are specified on the command line, then standard input is used.
     * In fact more than one spec can be used, in which case violations
     * of any of them are reported.
     * @param args See above.
     */
    static public void main(String args[]){
            new Main().main(args);
        }
    
}
