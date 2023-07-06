describe('Tests stack', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.997.0&showSafetyAlert=false');
  });
  it('select Stack tool', () => {
    cy.get('#STACK').click();
    cy.get('#STACK').should('have.class', 'active');
    cy.get('#viewport');
    cy.get('#canvas');
  });
  it('scroll instance', () => {
    cy.contains('.LeftBottomList', 'Number: 2');
    // scroll forward
    cy.get('#viewport')
      .trigger('mousemove')
      .trigger('wheel', { deltaY: 1 });
    cy.contains('.LeftBottomList', 'Number: 4');
    // scroll backward
    cy.get('#viewport')
      .trigger('mousemove')
      .trigger('wheel', { deltaY: -1 });
    cy.contains('.LeftBottomList', 'Number: 2');
  });
});