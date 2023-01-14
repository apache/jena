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
 * Tests the dataset view.
 *
 * Instead of injecting test data, we use the Manage New view to
 * add new datasets. So we also cover parts of the Manage view.
 */
describe('datasets', () => {
  it('Visits datasets page, also the application landing-page', () => {
    cy.visit('/')
    cy
      .contains('Loading')
      .should('not.exist')
    cy
      .get('h2.text-center')
      .contains('Apache Jena Fuseki')
    cy
      .get('tr.jena-table-empty')
      .contains('No datasets created')
  })
  it('Filters without any data', () => {
    cy.visit('/')
    cy
      .contains('Loading')
      .should('not.exist')
    cy
      .get('#filterInput')
      .type('pumpkin')
    cy
      .get('tr.jena-table-empty-filtered')
      .contains('No datasets found')
  })
  describe('After creating new datasets', () => {
    before(() => {
      // Special endpoint that clears the datasets data.
      cy.request('/tests/reset')
      for (const datasetName of ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k']) {
        // NOTE: Cypress appears to get confused when re-using the same URL,
        //       so we use a ?temp=... in the URL to force distinct requests.
        cy
          .visit(`/#/manage/new?temp=${datasetName}`)
          .then(() => {
            cy
              .get('#dataset-name')
              .type(datasetName)
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
      }
    })
    after(() => {
      // Special endpoint that clears the datasets data.
      cy.request('/tests/reset')
    })
    it('Edits the graph', () => {
      cy.visit('/#/dataset/a/edit')
      cy.intercept('/a*').as('getGraph')
      cy
        .contains('Loading')
        .should('not.exist')
      cy
        .get('h3')
        .contains('Available Graphs')
        .should('be.visible')
      // List the current graphs.
      cy
        .get('button')
        .contains('list current graphs')
        .click()
      cy
        .contains('Loading')
        .should('not.exist')
      // Now the table must have the new columns with the graph name and count.
      cy
        .get('table.jena-table > thead > tr > th')
        .eq(0)
        .should('contain', 'name')
      cy
        .get('table.jena-table > thead > tr > th')
        .eq(1)
        .should('contain', 'count')
      cy
        .get('table.jena-table > tbody > tr > td')
        .eq(0)
        .should('contain', 'default')
      cy
        .get('table.jena-table > tbody > tr > td')
        .eq(1)
        .should('contain', '42')
      // Clicking on the graph name must now load the contents of the graph into the editor.
      cy
        .get('table.jena-table > tbody > tr > td')
        .eq(0)
        .find('a')
        .first()
        .click()
      cy.wait('@getGraph')
      cy
        .get('.CodeMirror-code')
        .should('contain.text', 'Harry Potter and the Goblet of Fire')
    })
    it('Visits datasets page', () => {
      cy.visit('/')
      cy
        .contains('Loading')
        .should('not.exist')
      cy
        .get('h2.text-center')
        .contains('Apache Jena Fuseki')
      // We display 5 item per page, in the tables.
      cy
        .get('tr.jena-table-cell')
        .should('have.length', 5)
      // We must have three pages, in the table pagination.
      cy
        .get('span[role="menuitemradio"]')
        .should('have.length', 3)
    })
    it('Uses the table pagination', () => {
      cy.visit('/')
      cy
        .contains('Loading')
        .should('not.exist')
      // First we must confirm we are looking at the first page of the
      // navigation by default. Sounds obvious, but testing includes
      // testing for the obvious as well.
      cy
        .get('span[role="menuitemradio"]')
        .contains('1')
        .should('have.class', 'active')
      // The first cell, of the first row, must contain the first dataset,
      // /a.
      cy
        .get('tbody > tr')
        .first()
        .get('td')
        .first()
        .should('contain', '/a')
      // Go to page 2.
      cy
        .get('span[role="menuitemradio"]')
        .contains('2')
        .click({ force: true })
      // The first cell, of the first row, must contain the sixth dataset,
      // /f.
      cy
        .get('tbody > tr')
        .first()
        .get('td')
        .first()
        .should('contain', '/f')
    })
    it('Sorts the table', () => {
      cy.visit('/')
      cy
        .contains('Loading')
        .should('not.exist')
      // The first cell, of the first row, must contain the first dataset,
      // /a.
      cy
        .get('tbody > tr')
        .first()
        .get('td')
        .first()
        .should('contain', '/a')
      // Click on the name column to sort by name.
      cy
        .get('thead > tr')
        .first()
        .get('th')
        .first()
        .contains('name')
        .click()
      // Still /a as it is now ascending.
      cy
        .get('tbody > tr')
        .first()
        .get('td')
        .first()
        .should('contain', '/a')
      // Click on the name column to sort by name (again).
      cy
        .get('thead > tr')
        .first()
        .get('th')
        .first()
        .contains('name')
        .click()
      // Now the first row is showing the dataset /k, the last.
      cy
        .get('tbody > tr')
        .first()
        .get('td')
        .first()
        .should('contain', '/k')
    })
    it('Filters the table', () => {
      cy.visit('/')
      cy
        .contains('Loading')
        .should('not.exist')
      // The first cell, of the first row, must contain the first dataset,
      // /a.
      cy
        .get('#filterInput')
        .type('J')
      // Now it contains only one row, with the /j dataset (filter is
      // case-insensitive).
      cy
        .get('tbody > tr')
        .should('have.length', 1)
      cy
        .get('tbody > tr > td')
        .first()
        .should('contain', '/j')
      // Clear the text now.
      cy
        .get('button.btn')
        .contains('Clear')
        .click()
      // Now the table is showing 5 (paginated) datasets.
      cy
        .get('tbody > tr')
        .should('have.length', 5)
    })
    it('Cannot add two datasets with the same name', () => {
      cy.visit('/')
      cy
        .contains('Loading')
        .should('not.exist')
      cy.intercept({
        method: 'POST',
        url: '/$/datasets'
      }).as('post')
      cy
        .visit('/#/manage/new')
        .then(() => {
          cy
            .get('#dataset-name')
            .type('a')
          cy
            .get('#data-set-type-mem')
            .click()
          cy
            .get('button[type="submit"]')
            .click()
          // Now the UI must have received an HTTP response with status code 409
          // from Jena, due to the duplicate dataset name.
          cy
            .get('@post')
            .its('response')
            .should('have.property', 'statusCode', 409)
        })
    })
    it('Visualizes the dataset information (Info View, tab)', () => {
      cy.visit('/#/dataset/a/info')
      cy
        .contains('Loading')
        .should('not.exist')
      cy
        .get('h3')
        .contains('Available Services')
        .should('be.visible')
      // Dataset Size displays no data by default.
      cy
        .get('table#dataset-size-table > tbody > tr > td')
        .should('contain', 'No data')
      // Count the triples.
      cy
        .get('#count-triples-button')
        .click()
      cy
        .get('#count-triples-submit-button')
        .click()
      cy
        .contains('Loading')
        .should('not.exist')
      // Now the table must have the new column header and body.
      cy
        .get('table#dataset-size-table > thead > tr > th')
        .eq(0)
        .should('contain', 'graph name')
      cy
        .get('table#dataset-size-table > thead > tr > th')
        .eq(1)
        .should('contain', 'triples')
      cy
        .get('table#dataset-size-table > tbody > tr > td')
        .eq(0)
        .should('contain', 'default graph')
      cy
        .get('table#dataset-size-table > tbody > tr > td')
        .eq(1)
        .should('contain', '42')
    })
  })
})
