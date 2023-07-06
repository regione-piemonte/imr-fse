describe('Tests contrast', {
  viewportHeight: 800,
  viewportWidth: 1024
}, () => {
  it('load a monochrome study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.997.0&showSafetyAlert=false');
    cy.get('#viewport').should('not.to.be.undefined');
    cy.get('#canvas').should('not.to.be.undefined');
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(2000);
  });
  
  it('select Constrast tool', () => {
    cy.get('#CONTRAST').click();
    cy.get('#CONTRAST').should('have.class', 'active');
  });

  it('tests drag down (increase Level)', () => {
    // scroll forward
    cy.contains('.RightBottomList', 'Level: 4785');
    cy.get('#viewport')
      .trigger('mousedown', 'center', { buttons: 1 })
      .trigger('mousemove', 'bottom', { buttons: 1, movementX: 0, movementY: +300 })
      .trigger('mouseup', 'bottom');
    cy.contains('.RightBottomList', 'Level: 5085');
  });
  it('tests drag up (decrease Level)', () => {
    cy.get('#viewport')
    .trigger('mousedown', 'bottom', { buttons: 1 })
    .trigger('mousemove', 'center', { buttons: 1, movementX: 0, movementY: -300 })
    .trigger('mouseup', 'center');
    cy.contains('.RightBottomList', 'Level: 4785');
  });

  it('tests drag right (increase window)', () => {
    // scroll forward
    cy.contains('.RightBottomList', 'Window: 2860');
    cy.get('#viewport')
      .trigger('mousedown', 'center', { buttons: 1 })
      .trigger('mousemove', 'right', { buttons: 1, movementX: 300, movementY: 0 })
      .trigger('mouseup', 'right');
    cy.contains('.RightBottomList', 'Window: 3160');
  });
  it('tests drag left (decrease window)', () => {
    cy.get('#viewport')
    .trigger('mousedown', 'right', { buttons: 1 })
    .trigger('mousemove', 'center', { buttons: 1, movementX: -300, movementY: 0 })
    .trigger('mouseup', 'center');
    cy.contains('.RightBottomList', 'Window: 2860');
  });
});