describe('Tests overlays', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.3.0&showSafetyAlert=false');
    // select fisrt series
    cy.get('button[data-class="SeriesListItem"').first().click();
  });

  it('tests TopLeft info', () => {
    cy.contains('.LeftUpperList', 'PatientName: DOE JANE');
    cy.contains('.LeftUpperList', 'PatientId: 1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.3.0');
    cy.contains('.LeftUpperList', 'Birthdate:');
  });

  it('tests TopRight info', () => {
    cy.contains('.RightUpperList', 'CT CHEST W/O CONTRAST');
    cy.contains('.RightUpperList', 'Date: 2007-11-30 14:29:23');
    cy.contains('.RightUpperList', 'Accession:');
    cy.contains('.RightUpperList', 'Series: CT - AP/LAT');
  });

  it('tests BottomLeft info', () => {
    cy.contains('.LeftBottomList', 'Number: 1');
    cy.contains('.LeftBottomList', 'Size: 642x888 pixel');
    cy.contains('.LeftBottomList', 'Photometric: MONOCHROME2');
    cy.contains('.LeftBottomList', 'Spacing: 0.596847, 0.545455');
    cy.contains('.LeftBottomList', 'Position: -0.000, 265.000, 0.000');
  });

  it('tests BottomRight info', () => {
    cy.contains('.RightBottomList', 'Window: 500');
    cy.contains('.RightBottomList', 'Level: 50');
    cy.contains('.RightBottomList', 'Scale: 0.83');
    cy.contains('.RightBottomList', 'Quality: 75%');
  });
});