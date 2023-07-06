describe('Tests toolbar', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.153.0&showSafetyAlert=false');
  });

  it('tests key "C" for Cine', () => {
    cy.get('#TOGGLE_CINE').should('not.have.class', 'active');
    cy.get('#viewport').type('c');
    cy.get('#TOGGLE_CINE').should('have.class', 'active');
    cy.get('#viewport').type('c');
    cy.get('#TOGGLE_CINE').should('not.have.class', 'active');
  });

  it('tests key "I" for Info', () => {
    cy.get('#TOGGLE_INFO').should('have.class', 'active');
    cy.get('#viewport').type('i');
    cy.get('#TOGGLE_INFO').should('not.have.class', 'active');
    cy.get('#viewport').type('i');
    cy.get('#TOGGLE_INFO').should('have.class', 'active');
  });

  it('tests key "S" for Stack', () => {
    cy.get('#STACK').should('have.class', 'active');
    cy.get('#viewport').type('w');
    cy.get('#STACK').should('not.have.class', 'active');
    cy.get('#viewport').type('s');
    cy.get('#STACK').should('have.class', 'active');
  });

  it('tests key "W" for Window', () => {
    cy.get('#CONTRAST').should('not.have.class', 'active');
    cy.get('#viewport').type('w');
    cy.get('#CONTRAST').should('have.class', 'active');
    cy.get('#viewport').type('s');
    cy.get('#CONTRAST').should('not.have.class', 'active');
  });

  it('tests key "Z" for Zoom', () => {
    cy.get('#ZOOM').should('not.have.class', 'active');
    cy.get('#viewport').type('z');
    cy.get('#ZOOM').should('have.class', 'active');
    cy.get('#viewport').type('s');
    cy.get('#ZOOM').should('not.have.class', 'active');
  });

  it('tests key "F" for Fit-to-Scale', () => {
    cy.get('#SCALE_TO_FIT').should('not.have.class', 'active');
    cy.get('.tile-img').first().click();
    cy.get('#viewport').type('f');
    cy.contains('.RightBottomList', 'Scale: 2.36');
  });

  it('tests key "ESC" for Revert', () => {
    cy.get('#REVERT').should('not.have.class', 'active');
    cy.get('#ROTATE').click();
    cy.contains('.RightBottomList', 'Rotated');
    cy.get('#viewport').type('{esc}');
    cy.contains('.RightBottomList', 'Rotated').should('not.exist');
  });

  it('tests key "RIGHT" for Next Image', () => {
    cy.get('.tile-img').first().click();
    cy.contains('.LeftBottomList', 'Number: 8');
    cy.get('#viewport').type('{rightarrow}');
    cy.contains('.LeftBottomList', 'Number: 9');
  });

  it('tests key "LEFT" for Prev Image', () => {
    cy.get('#viewport').type('{downarrow}');
    cy.contains('.LeftBottomList', 'Number: 10');
  });

  it('tests key "DOWN" for Next Image', () => {
    cy.get('#viewport').type('{uparrow}');
    cy.contains('.LeftBottomList', 'Number: 9');
  });

  it('tests key "UP" for Prev Image', () => {
    cy.get('#viewport').type('{leftarrow}');
    cy.contains('.LeftBottomList', 'Number: 8');
  });

  it('tests key "PAGE-DOWN" for Next Series', () => {
    const selector = 'button[data-class="SeriesListItem"';
    cy.get(selector).eq(0).click();
    cy.contains('.RightUpperList', 'Series: MR - Localizer');

    cy.get('#viewport').click();
    cy.get('#viewport').type('{pagedown}');
    cy.get(selector).eq(0).should('not.have.class', 'active');
    cy.get(selector).eq(1).should('have.class', 'active');
    cy.contains('.RightUpperList', 'Series: MR - Localizer');

    cy.get('#viewport').type('{pagedown}');
    cy.get(selector).eq(1).should('not.have.class', 'active');
    cy.get(selector).eq(2).should('have.class', 'active');
    cy.contains('.RightUpperList', 'Series: MR - Sagittal SSFSE upper');
  });

  it('tests key "PAGE-UP" for Prev Series', () => {
    const selector = 'button[data-class="SeriesListItem"';
    cy.get(selector).eq(2).click();
    cy.contains('.RightUpperList', 'Series: MR - Sagittal SSFSE upper');

    cy.get('#viewport').click();
    cy.get('#viewport').type('{pageup}');
    cy.get(selector).eq(2).should('not.have.class', 'active');
    cy.get(selector).eq(1).should('have.class', 'active');
    cy.contains('.RightUpperList', 'Series: MR - Localizer');

    cy.get('#viewport').type('{pageup}');
    cy.get(selector).eq(1).should('not.have.class', 'active');
    cy.get(selector).eq(0).should('have.class', 'active');
    cy.contains('.RightUpperList', 'Series: MR - Localizer');
  });
});