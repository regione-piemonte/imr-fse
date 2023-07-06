describe('Tests toolbar', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.997.0&showSafetyAlert=false');
  });

  it('tests Toolbar buttons', () => {
    cy.get('#TOGGLE_SIDEBAR').should('have.class', 'active');
    cy.get('#STACK').should('have.class', 'active');
    cy.get('#CONTRAST');
    cy.get('#PAN');
    cy.get('#ZOOM');
    cy.get('#SCALE_TO_FIT');
    cy.get('#SCALE_TO_ONE');
    cy.get('#ROTATE');
    cy.get('#HORZ_FLIP');
    cy.get('#TOGGLE_CINE');
    cy.get('#REVERT');
    cy.get('#TOGGLE_INFO').should('have.class', 'active');
    cy.get('#TOGGLE_FULLSCREEN');
  });

  it('tests Sidebar toggler', () => {
    cy.get('#sidebar').should('be.visible');
    cy.get('#TOGGLE_SIDEBAR').click();
    cy.get('#sidebar').should('not.exist');
    cy.get('#TOGGLE_SIDEBAR').click();
    cy.get('#sidebar').should('be.visible');
  });

  it('tests Revert', () => {
    cy.get('#REVERT');
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Flipped');
    cy.get('#REVERT').click();
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
  });

  it('tests Info toggler', () => {
    cy.contains('.LeftUpperList', 'DOE JANE');
    cy.get('#TOGGLE_INFO').click();
    cy.get('.LeftUpperList').should('not.exist');
    cy.get('#TOGGLE_INFO').click();
    cy.contains('.LeftUpperList', 'DOE JANE');
  });
});