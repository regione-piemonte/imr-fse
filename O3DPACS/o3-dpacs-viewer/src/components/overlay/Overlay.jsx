import React from "react";
import PropTypes from "prop-types";
import classNames from "classnames";
import {stringCamelize} from '../../utils/stringHelpers';
import "./Overlay.css";

const OverlayLines = (props) => {
  const formatLines = (data) => {
    if (data) {
      const items = Object.keys(data).map((item) => {
        let caption = "";
        if (item) {
          let name = item.trim();
          if (name.substring(0, 1) === "-") {
            name = "";
          } else if (name.length > 0) {
            name = `${stringCamelize(item)}:`;
          }
          const value = data[item] !== undefined ? data[item] : "";
          caption = `${name} ${value}`.trim();
        }
        return caption;
      });
      return items;
    }
    return [];
  };

  const divClassNames = classNames(props.spanClasses, "Text");
  const listClassNames = classNames(props.listClasses);
  const iconClassNames = classNames("bi", "bi-star");
  const lines = formatLines(props.data);
  const fontSize = props.fontSize || "1em";
  const lineHeight = fontSize;
  const itemStyle = {
    lineHeight: `${lineHeight}`,
    fontSize: `${fontSize}`,
    listStyleType: "none",
    marginTop: "0px",
    marginBottom: "0px",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
  };
  const koItemStyle = {
    lineHeight: `${lineHeight}`,
    fontSize: `${fontSize}`,
    listStyleType: "none",
    marginTop: "0px",
    marginBottom: "0px",
    whiteSpace: "nowrap",
    overflow: "hidden",
    textOverflow: "ellipsis",
    color: "#a5a768"
  }

  const starIconStyle = {
  verticalAlign: 'unset'
  }

  return (
    <div className={props.className} style={props.style}>
      <div className={divClassNames}>
        <ul className={listClassNames}>
          {lines.map((item, key) => (
            item.startsWith('KEY:') ? 
            <li key={key} style={koItemStyle}>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className={iconClassNames} viewBox="0 0 16 16" style={starIconStyle}>
                <path d="M2.866 14.85c-.078.444.36.791.746.593l4.39-2.256 4.389 2.256c.386.198.824-.149.746-.592l-.83-4.73 3.522-3.356c.33-.314.16-.888-.282-.95l-4.898-.696L8.465.792a.513.513 0 0 0-.927 0L5.354 5.12l-4.898.696c-.441.062-.612.636-.283.95l3.523 3.356-.83 4.73zm4.905-2.767-3.686 1.894.694-3.957a.565.565 0 0 0-.163-.505L1.71 6.745l4.052-.576a.525.525 0 0 0 .393-.288L8 2.223l1.847 3.658a.525.525 0 0 0 .393.288l4.052.575-2.906 2.77a.565.565 0 0 0-.163.506l.694 3.957-3.686-1.894a.503.503 0 0 0-.461 0z"/>
              </svg>
              {" " + item || <br/>}
            </li>
            :
            <li key={key} style={itemStyle}>
              {item || <br/>}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

/**
 * @component Overlay annotation component
 * @description Render annotation info on image corners as lines of text
 */
class Overlay extends React.Component {
  /**
   * constructor
   * @param {Props} props input properties
   * @param {object?} [props.patient] patient's data
   * @param {object?} [props.study] study data
   * @param {object?} [props.series] series data 
   * @param {object?} [props.image] image data
   * @param {bool} [props.frameNumber] frame index for multiframe images with base 1
   * @param {Number} [props.windowCenter] window-center
   * @param {Number} [props.windowWidth] window_width
   * @param {Number} [props.scale] scale value
   * @param {bool} [props.rotation] rotation angle in degrees
   * @param {bool} [props.horzFlip] horizontal flip
   * @param {Number} [props.cineFps] cine frame-rate in frame-per-second
   */
  constructor(props) {
    super(props);
    this.state = {
      fontSize: "1.2em",
    };
  }

  getOverlayData = () => {
    const { patient, study, series, image, windowWidth, windowCenter, 
      scale, rotation, horzFlip, frameNumber, cineFps } = this.props;

    const data = {
      tl: {},
      tr: {},
      br: {},
      bl: {},
    }
    if(patient && study) {
      data.tl.patientName = patient.lastName + " " + patient.firstName;
      data.tl.patientId = patient.patientID;
      data.tl.birthdate = patient.birthDate;
    }
    if (study) {
      data.tr['-tDescription'] = study.description || "";
      data.tr.date = (study.date || "") + " " + (study.time || "");
      data.tr.accession = (study.accessionNumber || "")
    }
    if (series) {
      if(series.modality === 'KO' && image && image.referencedSeries) {
        data.tr.series = `${image.referencedSeries.modality || ""} - ${image.referencedSeries.description || ""}`;
      } else {
        data.tr.series = `${series.modality || ""} - ${series.description || ""}`;
      }
    }
    if (image) {
      if (cineFps) {
        data.br['FPS'] = cineFps;
      }
      if(rotation || horzFlip) {
        let text = '';
        if (rotation) {
          text = `Rotated ${rotation}°`;
        }
        if(horzFlip) {
          text += (text.length > 0) ? ' + Flipped' : 'Flipped'; 
        }
        data.br['-trasformation'] = text;
      }
      const numberOfFrames = image.numberOfFrames;
      if (numberOfFrames > 1 && frameNumber > 0) {
        data.br.frame = `${frameNumber}/${image.numberOfFrames}`;
      }
      data.br.window = image.windowWidth;
      data.br.level = image.windowCenter;
      if (windowWidth > 0) {
        data.br.window = windowWidth;
        data.br.level = windowCenter;
      }
      data.br.scale = Number.isFinite(scale) ? Number.parseFloat(scale).toFixed(2) : '';
      data.br.quality = image.imageQuality + '%';

      // imageData
      if(series.modality === 'KO') {
        data.bl.KEY = series.codeMeaning;
      }
      data.bl.number = image.number || "";
      data.bl.size = `${image.rows}x${image.columns} pixel`;
      data.bl.photometric = image.photometricInterpretation;
      if (image.pixelSpacing) {
        data.bl.spacing = `${image.pixelSpacing.columnSpacing || ""}, ${image.pixelSpacing.rowSpacing || ""}`;
      }
      if (image.imagePositionPatientX) {
        data.bl.position = `${image.imagePositionPatientX}, ${image.imagePositionPatientY}, ${image.imagePositionPatientZ}`;
      }
    }
    return data;
  }

  render() {
    const data = this.getOverlayData();
    const { fontSize } = this.state;
    return (
      <div className="OverlayGroup disable-select">
        <OverlayLines
          className="OverlayGroupItem TopLeft"
          fontSize={fontSize}
          align="left"
          top
          data={data.tl}
          spanClasses="UpperTextDiv"
          listClasses="LeftUpperList"
        />
        <OverlayLines
          className="OverlayGroupItem TopRight" 
          fontSize={fontSize}
          align="right"
          top
          data={data.tr}
          spanClasses="UpperTextDiv"
          listClasses="RightUpperList"
        />
        <OverlayLines
          className="OverlayGroupItem BottomRight"
          fontSize={fontSize}
          align="right"
          bottom
          data={data.br}
          spanClasses="BottomTextDiv"
          listClasses="RightBottomList"
        />
        <OverlayLines
          className="OverlayGroupItem BottomLeft"
          fontSize={fontSize}
          align="left"
          bottom
          data={data.bl}
          spanClasses="BottomTextDiv"
          listClasses="LeftBottomList"
        />
      </div>
    );
  }
}

Overlay.propTypes = {
  patient: PropTypes.object,
  study: PropTypes.object,
  series: PropTypes.object,
  image: PropTypes.object,
  frameNumber: PropTypes.number,
  windowCenter: PropTypes.number,
  windowWidth: PropTypes.number,
  scale: PropTypes.number,
  rotation: PropTypes.number,
  horzFlip: PropTypes.bool,
  cineFps: PropTypes.number
}

export default Overlay;










