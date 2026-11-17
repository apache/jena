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

/** Tests against an isolated real Fuseki server; see tests/integration/README.md. */
describe('Query against real Fuseki', () => {
  function openQuery (query) {
    cy.visit(`/#/dataset/validation/query?query=${encodeURIComponent(query)}`)
    cy.get('#sparql-endpoint').should('have.value', '/validation/sparql')
    cy.intercept('POST', '/validation/sparql').as('query')
    cy.get('button[aria-label="Run query"]').click()
    return cy.wait('@query')
  }

  it('renders SELECT bindings returned as SPARQL Results JSON', () => {
    openQuery('SELECT ?name ?age WHERE { <https://example.org/alice> <https://example.org/name> ?name; <https://example.org/age> ?age }')
      .then(({ response }) => {
        expect(response.statusCode).to.equal(200)
        expect(response.headers['content-type']).to.contain('application/sparql-results+json')
        expect(response.body.results.bindings[0].name['xml:lang']).to.equal('en')
        expect(response.body.results.bindings[0].age.datatype).to.equal('http://www.w3.org/2001/XMLSchema#integer')
      })
    cy.get('.ldforge-results table').should('contain.text', 'Alice').and('contain.text', '42')
  })

  it('renders a true ASK result', () => {
    openQuery('ASK { <https://example.org/alice> ?p ?o }')
      .its('response.body.boolean').should('equal', true)
    cy.get('.ldforge-boolean').should('have.text', 'True')
  })

  it('renders a false ASK result', () => {
    openQuery('ASK { <https://example.org/missing> ?p ?o }')
      .its('response.body.boolean').should('equal', false)
    cy.get('.ldforge-boolean').should('have.text', 'False')
  })

  it('parses and renders a CONSTRUCT graph', () => {
    openQuery('CONSTRUCT { ?s <https://example.org/name> ?name } WHERE { ?s <https://example.org/name> ?name }')
      .then(({ response }) => {
        expect(response.statusCode).to.equal(200)
        expect(response.headers['content-type']).to.match(/turtle|n-triples/)
      })
    cy.get('.ldforge-results table').should('contain.text', 'Alice').and('contain.text', 'Bób')
  })

  it('parses and renders a DESCRIBE graph', () => {
    openQuery('DESCRIBE <https://example.org/alice>')
      .its('response.statusCode').should('equal', 200)
    cy.get('.ldforge-results table').should('contain.text', 'Alice').and('contain.text', 'POINT(4.9 52.37)')
  })

  it('handles an empty SELECT result', () => {
    openQuery('SELECT ?name WHERE { <https://example.org/missing> <https://example.org/name> ?name }')
      .its('response.body.results.bindings').should('have.length', 0)
    cy.get('.ldforge-status').should('contain.text', '0 results')
  })

  it('displays a Fuseki syntax error', () => {
    openQuery('SELECT WHERE {')
      .its('response.statusCode').should('equal', 400)
    cy.get('.ldforge-status').should('contain.text', '400')
  })
})
