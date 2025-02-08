/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { describe, expect, it } from 'vitest'
import { validateGraphName } from '@/utils/validation'

const VALID_GRAPH_NAMES = [
  // From issue GH-2370 discussion
  'urn:x-arq:UnionGraph',
  'urn:x-arq:DefaultGraph',
  'urn:uuid:6e8bc430-9c3a-11d9-9669-0800200c9a66',
  'https://example.org/dataset',
  'https://example.org/dataset#graph',
  'https://example.org/dataset#graph?Aaa',
  // From RFC-3986
  'ftp://ftp.is.co.za/rfc/rfc1808.txt',
  'http://www.ietf.org/rfc/rfc2396.txt',
  'ldap://[2001:db8::7]/c=GB?objectClass?one',
  'mailto:John.Doe@example.com',
  'news:comp.infosystems.www.servers.unix',
  'tel:+1-816-555-1212',
  'telnet://192.0.2.16:80/',
  'urn:oasis:names:specification:docbook:dtd:xml:4.1.2',
  'foo://example.com:8042/over/there?name=ferret#nose',
  'example://a/b/c/%7Bfoo%7D',
  'eXAMPLE://a/./b/../b/%63/%7bfoo%7d',
  'http://example.com:80/',
  'ftp://cnn.example.com&story=breaking_news@10.0.0.1/top_story.htm',
  // From URI.js docs
  'uri://user:pass@example.com:123/one/two.three?q1=a1&q2=a2#body',
  'HTTP://ABC.COM:80',
  'HTTPS://ABC.COM:443/',
  'WS://ABC.COM:80/chat#one',
  'mailto:alpha@example.com,bravo@example.com?subject=SUBSCRIBE&body=Sign%20me%20up!',
  'uri://www.example.org/red%09ros\xE9#red',
  'uri://www.example.org/red%09ros%C3%A9#red',
  'uri://www.example.org/D%C3%BCrst',
  'uri://www.example.org/D\xFCrst',
  'wss://example.com/foo?bar'
]

const INVALID_GRAPH_NAMES = [
  // From issue GH-2370 discussion
  'snoopy',
  'test',
  'default',
  'wss://example.com/ foo?bar',
  'https://this is an invalid URL.com',
  'http%3A//www.example.com/other/graph'
]

describe('validation', () => {
  it('Should reject empty graph names', () => {
    expect(validateGraphName('')).to.equals(false)
    expect(validateGraphName('  ')).to.equals(false)
  })
  it('Should reject graph names that contain spaces', () => {
    expect(validateGraphName('jena graph')).to.equals(false)
    expect(validateGraphName('')).to.equals(false)
  })
  it('Should reject graph names that are not valid URIs', () => {
    for (let graphName of INVALID_GRAPH_NAMES) {
      expect(validateGraphName(graphName)).to.equals(false)
    }
  })
  it('Should accept valid graph names', () => {
    for (let graphName of VALID_GRAPH_NAMES) {
      expect(validateGraphName(graphName), `Rejected valid graph name "${graphName}"`).to.equals(true)
    }
  })
})
