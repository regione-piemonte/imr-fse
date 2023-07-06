describe('Tests sidebar', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.153.0&showSafetyAlert=false');
  })
  it('tests series', () => {
    // iterate series items and checks series.description
    cy.get('button[data-class="SeriesListItem"').each(($el) => {
      cy.get($el).click();
      cy.contains('.RightUpperList', `${$el.attr("data-description")}`);
    });
  });
  it('tests tiles', () => {
    // iterate tiles of fisrt series
    cy.get('button[data-class="SeriesListItem"').first().click();
    // first tile should be activated
    cy.get('.tile-img').first().should('have.class', 'tile-img-active');
    // first tile should have id="scroll-tile"
    cy.get('.tile-img').first().should('have.id', 'scroll-tile');
    // checks if image numbers is equal to overlay line into viewport
    cy.get('.tile-img').each(($el) => {
      cy.get($el).click();
      cy.contains('.LeftBottomList', `Number: ${$el.attr("alt")}`);
    });
  });
});