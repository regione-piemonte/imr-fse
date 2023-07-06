describe('Tests geometry trasformations', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.997.0&showSafetyAlert=false');
  });

  it('tests Rotate', () => {
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 90°');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 180°');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 270°');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
  });

  it('tests Horizontal Flip', () => {
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Flipped');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
  });

  it('tests Rotate and Horizontal Flip', () => {
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    
    // rotate 90 + flip
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 90°');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Rotated 270°');
    cy.contains('.RightBottomList', 'Flipped');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Rotated 90°');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    
    // rotate 180 + flip
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 180°');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Rotated 180°');
    cy.contains('.RightBottomList', 'Flipped');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    
    // rotate 270 + flip
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated 270°');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Rotated 90°');
    cy.contains('.RightBottomList', 'Flipped');
    cy.get('#HORZ_FLIP').click();
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
    
    // revert transformations
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
    cy.contains('.RightBottomList', 'Flipped').should('not.exist');
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
});