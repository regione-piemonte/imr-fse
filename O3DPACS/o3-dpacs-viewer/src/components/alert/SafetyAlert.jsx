import React from "react";
import PropTypes from "prop-types";
import { Button, Modal } from "react-bootstrap";
import "./SafetyAlert.css";

/**
 * User alert used to display product Intended Use
 * @component
 * @param {Props} props
 * @param {boolean} [props.show=false] visibility switch
 * @param {EventHandler} [props.onAcceptClick] accept event callback
 * @param {EventHandler} [props.onDeclineClick] decline event callback
 */
const SafetyAlert = (props) => {
  return (
    <Modal
      backdrop="static"
      backdropClassName="safety-alert-backdrop"
      keyboard={false}
      size="lg"
      centered
      show={props.show}
    >
      <Modal.Header>
        <Modal.Title>Uso Previsto e Informazioni sulla Sicurezza</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="text-uppercase text-center">
          Le immagini DICOM visualizzate <u>non sono destinate ad uso diagnostico</u>. 
          <br />
          Il presente VIEWER puo' essere usato solo come software a scopo di consultazione, 
          ricerca o insegnamento; <u>non puo' essere destinato ad uso diagnostico</u> ed utilizzato a fini clinici e/o 
          per la cura del paziente.
          <br />
          Non e' un dispositivo medico con marchio CE
        </p>
      </Modal.Body>
      <Modal.Footer className="justify-content-center">
        <Button onClick={props.onDeclineClick} variant="danger">
          Rifiuta
        </Button>
        <Button onClick={props.onAcceptClick} variant="success">
          Accetta
        </Button>
      </Modal.Footer>
      {/* <Modal.Header>
        <Modal.Title>Intended Use and Safety Information</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="text-uppercase text-center">
          Any displayed DICOM images are <u>not intended for primary diagnosis</u>.
          <br />
          You can only use this product as a reviewing, research or teaching
          software, <u>not for primary diagnostic</u>, used in clinical workflow and/or
          for patient care.
          <br />
          Not CE Marked.
        </p>
      </Modal.Body>
      <Modal.Footer className="justify-content-center">
        <Button onClick={props.onDeclineClick} variant="danger">
          Decline
        </Button>
        <Button onClick={props.onAcceptClick} variant="success">
          Accept
        </Button>
      </Modal.Footer> */}
    </Modal>
  );
};

SafetyAlert.propTypes = {
  show: PropTypes.bool,
  onClick: PropTypes.func,
};

export default SafetyAlert;
