/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.atlas.net;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.Lib;

public class Host {
    /**
     * Returns a host address in a form suitable for an IRI authority host.
     * This is not a loopback address, 127.0.0.x or ::1.
     */
    public static String getHostAddressForIRI() {
        try {
            InetAddress addr = getLocalHostLANAddress$();
            if ( addr == null )
                return null;
            String hostAddress = addr.getHostAddress();
            if ( addr instanceof Inet6Address ) {
                // Remove zoneId
                hostAddress = hostAddress.replaceFirst("%.*","");
                // If you do want the zone id, the % needs encoding.
                // hostAddress = hostAddress.replaceFirst("%","%25");
                // Wrap in "[ ... ]"
                hostAddress = "["+hostAddress+"]";
            }
            return hostAddress;
        } catch ( UnknownHostException ex) {
            return null;
        }
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address
     * (not a loopback address, 127.0.0.x or ::1). If there is no other choice, it returns
     * <code>InetAddress.getLocalHost</code>.
     * Throw a {@link RuntimeIOException runtime IO exception} if no address choice found.
     */
    public static InetAddress getLocalHostLANAddress() {
        try {
            return getLocalHostLANAddress$();
        } catch (IOException ioException) {
            throw IOX.exception(ioException);
        }
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    private static InetAddress getLocalHostLANAddress$() throws UnknownHostException {
        // This code comes from:
        // https://issues.apache.org/jira/browse/JCS-40
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (UnknownHostException e) { throw e; }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * Test whether a hostname from a URL refers to the localhost.
     * "hostname" is the format in URIs; IPv6 addresses have "[...]".
     * This function does not return 'true' for the host name of the DNS
     */
    public static boolean isLocalhostLoopback(String urlHostName) {
        if (urlHostName == null || urlHostName.isEmpty())
            return false;
        String host = Lib.lowercase(urlHostName);

        // Do some checking for common ways to write "localhost"

        // String-based checks for standard loopback DNS names / IP addresses.
        if (host.equalsIgnoreCase("localhost") ||
                host.endsWith(".localhost") ||
                host.equals("127.0.0.1") ||
                host.equals("0.0.0.0") ||
                // There are lot of ways to write ::1 in IPv6. This is only some of them
                host.equals("[::1]") ||
                // IPv6 with IPv4 address
                host.equals("[::ffff:127.0.0.1]") ||
                host.equals("[0:0:0:0:0:0:0:1]") ||
                host.endsWith(".localhost") ||
                // De facto aliases (Debian, Ubuntu)
                host.equalsIgnoreCase("ip6-localhost") ||
                host.equalsIgnoreCase("ip6-loopback")) {
            return true;
        }

        // If it isn't one of the fastpath forms, lookup the name.
        try {
            // Check ALL addresses: a name may resolve to several InetAddr
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (isLocal(addr)) {
                    return true;
                }
            }
        } catch (UnknownHostException ex) {
            return false;
        } catch (SocketException ioException) {
            // Something broken.
            throw IOX.exception(ioException);
        }
        return false;

    }

    /**
     * Test whether an {@code InetAddress} is going to use the loopback interface.
     */
    private static boolean isLocal(InetAddress addr) throws SocketException {
        return
                // 127.0.0.0/8, ::1
                addr.isLoopbackAddress()
                // Connects to localhost on most OSes
                || addr.isAnyLocalAddress()
                // DNS name of the machine.
                || NetworkInterface.getByInetAddress(addr) != null;
    }

}
