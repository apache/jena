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
 * Tests the upload view.
 *
 * Instead of injecting test data, we use the Manage New view to
 * add new datasets. So we also cover parts of the Manage view.
 */

describe('upload', function () {
  beforeEach(function () {
    // Intercept new dataset request.
    cy.intercept('POST', '/$/datasets').as('createDataset')
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
    // Wait for the create dataset request to be processed and have a return status, then continue.
    cy.wait('@createDataset')

    cy.intercept('/$/server').as('server')
    // We wait until the route navigation guard is called, so that the
    // view has the dataset information loaded.
    cy.visit('/#/dataset/skosmos/upload')
    cy.wait('@server')
    cy.intercept('/$/server').as('server')
  })
  afterEach(function () {
    // Special endpoint that clears the datasets data.
    cy.request({
      url: '/tests/reset',
      retryOnStatusCodeFailure: true
    })
  })
  it('displays an empty progress bar by default', function () {
    // The progress is present.
    cy
      .get('.progress')
      .should('be.visible')
    // We have two inner progress bars.
    cy
      .get('.progress-bar')
      .should('have.length', 2)
    // And each progress bar has the current value set to zero.
    cy
      .get('.progress-bar')
      .each(($el) => {
        cy
          .wrap($el)
          .should('have.attr', 'aria-valuenow', 0)
      })
  })
  it('displays the progress for success and failure', function () {
    // Intercept upload calls.
    // Fails every other upload.
    let fail = false
    cy.intercept('POST', '/skosmos/data', (req) => {
      console.log('in upload request')
      console.log(req)
      const statusCode = fail ? 500 : 200
      fail = !fail
      req.reply({
        statusCode
      })
    }).as('upload')
    cy
      .get('input[type=file]')
      .should('exist')
    // Prepare three files to be uploaded (Second will fail! See intercept above!).
    const NUMBER_OF_FILES = 3
    for (let idx = 0; idx < NUMBER_OF_FILES; idx++) {
      cy
        .get('input[type=file]')
        .selectFile({
            contents: Cypress.Buffer.from(`@prefix ex:   <http://test.com'onclick=alert(123);'> .
  ex:ABC a ex:DEF .`),
            fileName: `file${idx}.ttl`,
            lastModified: Date.now(),
          },
          {
            force: true
          })
    }
    // We have three files, the json-server handler is programmed to
    // fail every other time, so we will have 2 successes and one failure.
    cy
      .get('button.upload-file')
      .each(($el) => {
        cy
          .wrap($el)
          .click({force: true})
        cy.wait('@upload')
      })
    // Overall progress now shows 2/3 success, 1/3 failure.
    cy
      .get('.progress')
      .eq(0)
      .find('.progress-bar')
      .eq(0)
      .should('have.text', '2/3')
    cy
      .get('.progress')
      .eq(0)
      .find('.progress-bar')
      .eq(1)
      .should('have.text', '1/3')
    // First and third files are shown as success, second is failure.
    cy
      .get('.progress')
      .eq(1)
      .find('.progress-bar')
      .eq(0)
      .should('have.class', 'bg-success')
    cy
      .get('.progress')
      .eq(2)
      .find('.progress-bar')
      .eq(0)
      .should('have.class', 'bg-danger')
    cy
      .get('.progress')
      .eq(3)
      .find('.progress-bar')
      .eq(0)
      .should('have.class', 'bg-success')
  })
})
