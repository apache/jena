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
import { createShareableLink } from '@/utils/query'

describe('query', () => {
  it('creates valid shareable links', () => {
    // The expected prefix.
    const prefix = 'https://host:1234/#/query/?query='
    // The list of tests (parametrized tests). The expected value is the URL-encoded query value.
    const tests = [
      {
        value: '',
        expected: `${prefix}`
      },
      {
        value: `PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT * WHERE {
  ?sub ?pred ?obj .
} LIMIT 10`,
        expected: `${prefix}PREFIX%20rdf%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0APREFIX%20rdfs%3A%20%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0ASELECT%20%2A%20WHERE%20%7B%0A%20%20%3Fsub%20%3Fpred%20%3Fobj%20.%0A%7D%20LIMIT%2010`
      },
      // See: https://github.com/apache/jena/issues/1745
      {
        value: '+++++',
        expected: `${prefix}%2B%2B%2B%2B%2B`
      }
    ]
    const originalDocument = global.document
    global.document = {
      location: {
        protocol: 'https:',
        host: 'host:1234',
        pathname: '/',
        search: ''
      }
    }
    tests.forEach(test => {
      expect(createShareableLink(test.value, '/query/')).to.equal(test.expected)
    })
    global.document = originalDocument
  })
})
