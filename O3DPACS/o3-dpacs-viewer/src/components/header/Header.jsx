import React from "react";
import PropTypes from "prop-types";
import { Nav, Navbar, Col } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import * as fa from "@fortawesome/free-solid-svg-icons";
import { isFunction } from "../../utils";
import { TOOLS } from "../../utils/tools";

const NavTool = (props) => {
  const { children, className, icon, id, onClick, ...otherProps } = props;
  const onClickLink = (e) => {
    if (isFunction(onClick)) {
      onClick(e, id);
    }
  };
  return (
    <Nav.Link
      as="button"
      className="nav-tool mx-2"
      id={id}
      onClick={onClickLink}
      {...otherProps}
      style={{ fontWeight: props.active ? "bold" : "inherit" }}
    >
      {icon ? <FontAwesomeIcon icon={icon} size="lg" /> : null}
      <span className="d-inline d-lg-none ml-2">{children}</span>
    </Nav.Link>
  );
};

/**
 * Header component
 * @component
 * @param {Props} props 
 * @param {string} [props.activeTool] active tool id
 * @param {EventHandler?} [props.onClickTool] callback in the form (id) => {}
 * @param {object?} [props.patient] patient data
 * @param {boolean} [props.showInfo] annotation visibility
 * @param {boolean} [props.showSidebar] sideBar visibility 
 * @example Render Header with stack tool active, Info and Sidebar visible
 * <Header activeTool="TOOLS.STACK" patient={patient} showInfo={true} showSidebar={true} />
 */
const Header = (props) => {
  const onClick = (e, id) => {
    if (isFunction(props.onClickTool)) {
      props.onClickTool(id);
    }
  };

  let patientInfo = "";
  if (props.patient) {
    patientInfo = (props.patient.lastName || "") + " " + (props.patient.firstName || "");
    if (props.patient.sex) patientInfo += " (" + props.patient.sex + ")";
    if (props.patient.birthDate) patientInfo += " " + props.patient.birthDate;
  }

  return (
    <Navbar
      id={props.id}
      style={{ top: "inherit" }} // allow fixed top to move down 
      fixed="top"
      bg="dark"
      variant="dark"
      expand="lg"
    >
      <Col xs="10" lg="3" className="text-left text-ellipsis">
        <Navbar.Brand>
          <FontAwesomeIcon className="ml-0 mr-1" icon={fa.faUser} /> {patientInfo}
        </Navbar.Brand>
      </Col>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="mr-auto justify-content-center fit-width">
          <NavTool
            id={TOOLS.TOGGLE_SIDEBAR}
            onClick={onClick}
            active={props.showSidebar}
            icon={fa.faList}
            title="Toggle Sidebar"
          >
            Sidebar
          </NavTool>
          <NavTool
            id={TOOLS.STACK}
            onClick={onClick}
            active={props.activeTool === TOOLS.STACK}
            icon={fa.faClone}
            title={"Stack scroll (S)"}
          >
            Stack
          </NavTool>
          <NavTool
            id={TOOLS.CONTRAST}
            onClick={onClick}
            active={props.activeTool === TOOLS.CONTRAST}
            icon={fa.faAdjust}
            title={"Change contrast (W)"}
          >
            Contrast
          </NavTool>
          <NavTool
            id={TOOLS.PAN}
            active={props.activeTool === TOOLS.PAN}
            onClick={onClick}
            icon={fa.faHandPaper}
            title={"Translate Image (T)"}
          >
            Pan
          </NavTool>
          <NavTool
            id={TOOLS.ZOOM}
            onClick={onClick}
            active={props.activeTool === TOOLS.ZOOM}
            icon={fa.faSearch}
            title={"Change zoom (Z)"}
          >
            Zoom
          </NavTool>
          <NavTool
            id={TOOLS.SCALE_TO_FIT}
            onClick={onClick}
            icon={fa.faExpand}
            title={"Display fitting to window (F)"}
          >
            Fit
          </NavTool>
          <NavTool
            id={TOOLS.SCALE_TO_ONE}
            onClick={onClick}
            icon={fa.faCompress}
            title={"Display without zoom"}
          >
            1:1
          </NavTool>
          <NavTool
            id={TOOLS.ROTATE}
            onClick={onClick}
            icon={fa.faRedo}
            title="Rotate 90 degrees clockwise"
          >
            Rotate
          </NavTool>
          <NavTool
            id={TOOLS.HORZ_FLIP}
            onClick={onClick}
            icon={fa.faArrowsAltH}
            title="Flip Horizontally"
          >
            Flip
          </NavTool>
          <NavTool
            id={TOOLS.TOGGLE_CINE}
            onClick={onClick}
            active={props.cineMode}
            icon={props.cineMode ? fa.faStop : fa.faPlay}
            title={props.cineMode ? "Stop (C)" : "Play (C)"}
          >
            {props.cineMode ? "Stop" : "Play"}
          </NavTool>
          <NavTool
            id={TOOLS.REVERT}
            onClick={onClick}
            icon={fa.faHistory}
            title={"Display Reset (Esc)"}
          >
            Revert
          </NavTool>
          <NavTool
            id={TOOLS.TOGGLE_INFO}
            onClick={onClick}
            active={props.showInfo}
            icon={fa.faInfo}
            title="Toggle Overlay (I)"
          >
            Info
          </NavTool>
          {props.fullScreenEnabled && <NavTool
            id={TOOLS.TOGGLE_FULLSCREEN}
            onClick={onClick}
            active={props.fullScreen}
            icon={props.fullScreen ? fa.faCompressAlt : fa.faExpandAlt}
            title="Toggle Full Screen (F11)"
          >
            Full Screen
          </NavTool>}
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
};

Header.propTypes = {
  activeTool: PropTypes.string,
  cineMode: PropTypes.bool,
  onClickTool: PropTypes.func,
  patient: PropTypes.object,
  showInfo: PropTypes.bool,
  showSidebar: PropTypes.bool,
};

export default Header;




















