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
 * Tests the Query view and YASGUI & family components.
 */
describe('Query', () => {
  before(() => {
    // Special endpoint that clears the datasets data.
    cy.request('/tests/reset')
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
  after(() => {
    // Special endpoint that clears the datasets data.
    cy.request('/tests/reset')
  })
  /**
   * Bug: https://github.com/apache/jena/issues/1443
   */
  it('Uses the correct SPARQL Endpoint', () => {
    const SPARQL_ENDPOINT = '/skosmos/update'
    cy.visit('/#/dataset/skosmos/query')
    cy.server()
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
})
