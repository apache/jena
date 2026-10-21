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
 * Tests the navigation menu.
 *
 * The menu uses Bootstrap's collapse plugin, which is the only Bootstrap
 * plugin loaded in main.js. Other specs run at 1440x1024, where the menu
 * is expanded and the toggler is hidden, so they never exercise it.
 */
describe('menu', () => {
  describe('on a small screen', () => {
    beforeEach(() => {
      // Below Bootstrap's md breakpoint (768px), so navbar-expand-md collapses the menu.
      cy.viewport(375, 667)
    })
    it('Opens and closes the collapsed menu with the toggler', () => {
      cy.visit('/')
      cy
        .get('#navbarNav')
        .should('not.be.visible')
      cy
        .get('.navbar-toggler')
        .should('be.visible')
        .and('have.attr', 'aria-expanded', 'false')
        .click()
      cy
        .get('#navbarNav')
        .should('have.class', 'show')
        .contains('manage')
        .should('be.visible')
      cy
        .get('.navbar-toggler')
        .should('have.attr', 'aria-expanded', 'true')
        .click()
      cy
        .get('#navbarNav')
        .should('not.be.visible')
      cy
        .get('.navbar-toggler')
        .should('have.attr', 'aria-expanded', 'false')
    })
  })
})
