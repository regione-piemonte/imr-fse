describe('Tests zoom', {
  viewportHeight: 800,
  viewportWidth: 1024
}, () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.1007.0&showSafetyAlert=false');
    cy.get('#viewport').should('not.to.be.undefined');
    cy.get('#canvas').should('not.to.be.undefined');
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(2000);
  });
  
  it('select Zoom tool', () => {
    cy.get('#ZOOM').click();
    cy.get('#ZOOM').should('have.class', 'active');
  });

  it('tests 1:1', () => {
    cy.contains('.RightBottomList', 'Scale:');
    // scroll forward
    cy.get('#SCALE_TO_ONE').click();
    cy.get('#SCALE_TO_ONE').should('not.have.class', 'active');
    cy.contains('.RightBottomList', 'Scale: 1.00');
  });

  it('tests scale-to-fit', () => {
    cy.contains('.RightBottomList', 'Scale:');
    // scroll forward
    cy.get('#SCALE_TO_FIT').click();
    cy.get('#SCALE_TO_FIT').should('not.have.class', 'active');
    cy.contains('.RightBottomList', 'Scale: 0.37');
  });
  
  it('tests zoom plus', () => {
    // scroll forward
    cy.get('#viewport')
      .trigger('mousedown', 'center', { buttons: 1 })
      .trigger('mousemove', 'bottom', { buttons: 1, movementY: -100 })
      .trigger('mouseup', 'bottom');
    cy.contains('.RightBottomList', 'Scale: 1.35');
  });

  it('tests zoom minus', () => {
    cy.contains('.RightBottomList', 'Scale:');
    // scroll forward
    cy.get('#viewport')
      .trigger('mousedown', 'bottom', { buttons: 1 })
      .trigger('mousemove', 'center', { buttons: 1, movementY: +100 })
      .trigger('mouseup', 'center');
    cy.contains('.RightBottomList', 'Scale: 0.37');
  });
});