describe('Tests App', () => {
  it('visits the api', () => {
    cy.visit('/api');
  })
  it('visits the app', () => {
    cy.visit('/');
  })
});

describe('Tests parameters', () => {
  it('visits the app without parameters', () => {
    cy.visit('/');
    cy.get('.alert-heading');
    cy.contains('No filter found, please check configuration');
  })
  it('visits the app withn invalid study', () => {
    cy.visit('/?studyUID=0');
    cy.get('.alert-danger');
    cy.contains('Not Found');
  })
});

describe('Tests main components', () => {
  it('load a valid study', () => {
    cy.visit('/?studyUID=1.3.6.1.4.1.5962.99.1.2237260787.1662717184.1234892907507.997.0');
    cy.get('.modal-header');
  })
  it('accept the agreement', () => {
    cy.contains('Accept').click();
  })
  it('checks app Navbar', () => {
    cy.get('#header');
  })
  it("checks Patient's Name into Navbar", () => {
    cy.contains('.navbar-brand', 'DOE JANE');
  })
  it('checks app Sidebar', () => {
    cy.get('#sidebar');
  })
  it('checks app Viewport', () => {
    cy.get('#viewport');
  })
  it("checks Patient's Name overlay into Viewport", () => {
    cy.contains('.LeftUpperList', 'DOE JANE');
  })
  it('checks Study Date overlay into Viewport', () => {
    cy.contains('.RightUpperList', '2006-10-03 11:20:29');
  })
  it('checks the image canvas', () => {
    cy.get('#canvas');
  })
});