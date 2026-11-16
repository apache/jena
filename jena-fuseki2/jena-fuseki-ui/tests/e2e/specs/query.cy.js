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

/**
 * Tests the Query view and embedded LDForge editor.
 */
describe('Query', function() {
  beforeEach(function() {
    // Special endpoint that clears the datasets data.
    cy.request({
      url: '/tests/reset',
      retryOnStatusCodeFailure: true
    })
    // Create a sample dataset.
    cy
      .visit('/#/manage/new')
      .then(() => {
        cy
          .get('#dataset-name')
          .type('skosmos')
        cy
          .get('#data-set-type-mem')
          .click()
        cy
          .get('button[type="submit"]')
          .click()
        // We are redirected to the Manage datasets view.
        cy
          .get('table.jena-table')
          .should('be.visible')
      })
  })

  afterEach(function() {
    // Special endpoint that clears the datasets data.
    cy.request({
      url: '/tests/reset',
      retryOnStatusCodeFailure: true
    })
  })
  /**
   * Bug: https://github.com/apache/jena/issues/1443
   */
  it('Uses the correct SPARQL Endpoint', function() {
    const SPARQL_ENDPOINT = '/skosmos/update'
    cy.visit('/#/dataset/skosmos/query')
    cy
      .intercept('POST', SPARQL_ENDPOINT, {
        statusCode: 203,
        body: {}
      })
      .as('sparql')
    // Set a different endpoint.
    cy
      .get('#sparql-endpoint')
      .clear()
      .type(SPARQL_ENDPOINT)
    // Now run the query.
    cy
      .get('button[aria-label="Run query"]')
      .click()
    // It must have called the URL using the endpoint we defined earlier on.
    // Note that besides the custom URL, this test also expects the HTTP
    // Status Code 203, which is not really used in Jena, but it is used
    // here to verify we are indeed calling the intercepted endpoint above.
    cy
      .wait('@sparql')
      .its('response')
      .should('have.property', 'statusCode', 203)
  })

  it('Can resize the query editor', function() {
    cy.visit('/#/dataset/skosmos/query')
    cy.get('.cm-content').invoke('text').as('queryBeforeResize', { type: 'static' })
    cy.get('.cm-editor').should('be.visible').invoke('height').as('beforeHeight', { type: 'static' })
    cy.get('[role="separator"][aria-label="Resize query editor"]')
      .scrollIntoView()
      .should('be.visible')
      .then($handle => {
        // Synthetic Cypress pointer events cannot acquire native pointer capture.
        // Dispatch the drag directly to the handle while exercising its real resize logic.
        cy.stub($handle[0], 'setPointerCapture')
        cy.wrap($handle)
          .trigger('pointerdown', { eventConstructor: 'PointerEvent', pointerId: 1, button: 0, clientY: 100 })
          .trigger('pointermove', { eventConstructor: 'PointerEvent', pointerId: 1, clientY: 220, force: true })
          .trigger('pointerup', { eventConstructor: 'PointerEvent', pointerId: 1, clientY: 220, force: true })
      })
    cy.get('@beforeHeight').then(beforeHeight => {
      cy.get('.cm-editor').invoke('height').should('be.greaterThan', beforeHeight + 80)
    })
    cy.get('@queryBeforeResize').then(query => {
      cy.get('.cm-content').invoke('text').should('equal', query)
    })
  })

  it('Toggles prefix badges without accumulating blank lines', function() {
    cy.visit('/#/dataset/skosmos/query')
    cy.contains('button', 'Selection of triples').click()
    cy.get('.cm-content').invoke('text').then(original => {
      for (let i = 0; i < 3; i++) {
        cy.contains('button.fuseki-prefix', /^rdf$/).click().should('have.attr', 'aria-pressed', 'true')
        cy.get('.cm-content').should('contain.text', 'PREFIX rdf:')
        cy.contains('button.fuseki-prefix', /^rdf$/).click().should('have.attr', 'aria-pressed', 'false')
        cy.get('.cm-content').invoke('text').should('equal', original)
      }
    })
    cy.contains('button', 'Selection of classes').click()
    cy.contains('button.fuseki-prefix', /^owl$/).should('have.attr', 'aria-pressed', 'true')
    cy.contains('button.fuseki-prefix', /^rdfs$/).should('have.attr', 'aria-pressed', 'true')
  })

  it('Renders query results with LDForge', function() {
    cy.visit('/#/dataset/skosmos/query')
    cy.get('#sparql-endpoint').should('have.value', '/skosmos/sparql')
    cy.get('button[aria-label="Run query"]').click()
    cy.get('.ldforge-results').should('contain.text', 'Semantic Web')
    cy.get('button[aria-label="Download CSV"]').should('be.visible')
  })
})
