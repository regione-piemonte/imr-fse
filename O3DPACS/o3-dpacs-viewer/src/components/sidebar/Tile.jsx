import React from "react";
import PropTypes from "prop-types";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import * as fa from "@fortawesome/free-solid-svg-icons";
import { MimeType } from '../../types/mime';
import './Tile.css';

/**
 * Render an image thumbnail
 * @component
 * @param {Props} props
 * @param {!string} props.src image url
 * @param {bool} [props.active] active status
 * @param {string} [props.alt] HTML img alt
 * @param {string} [props.brText] bottom-right short annotation text on image.
 * Used to depict image contents, for example number of frames or type.
 * @param {string} [props.className] CSS style class
 * @param {string} [props.id] element identifier
 * @param {EventHandler} [props.onClick] mouse click handler
 */
const Tile = (props) => {
  if ([MimeType.PDF, MimeType.VIDEO_MPEG].includes(props.contentType)) {
    const icon = props.contentType === MimeType.PDF ? fa.faFilePdf : fa.faFileVideo;
    return (
      <div className="tile-container">
        <FontAwesomeIcon icon={icon} size="6x" onClick={props.onClick} />
      </div>
    )
  }
  return (
    <div className="tile-container">
      <img
        className={props.className}
        alt={props.alt}
        src={props.src}
        onClick={props.onClick}
        id={props.id}
        active={props.active}
      >
      </img>
      <span className="text-bottom-right">
        {props.brText}
      </span>
    </div>
  );
}

Tile.propTypes = {
  src: PropTypes.string.isRequired,
  active: PropTypes.bool,
  alt: PropTypes.string,
  brText: PropTypes.string,
  className: PropTypes.string,
  id: PropTypes.string,
};


export default Tile;