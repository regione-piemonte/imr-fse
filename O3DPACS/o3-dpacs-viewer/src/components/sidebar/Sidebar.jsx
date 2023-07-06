import React from "react";
import PropTypes from "prop-types";
import TileList from "./TileList";
import StudyList from "./StudyList";
import "./Sidebar.css";

/**
 * Sidebar component
 * @component
 * @param {Props} props
 * @param {Object[]} props.studies array of studies
 * @param {Object} [props.activeStudy] active study
 * @param {Object} [props.activeSeries] active series
 * @param {Object} [props.activeImage] active image instance
 * @param {studyClickEvent} [props.onStudyClick] callback fired when user click on study item
 * @param {studyClickEvent} [props.onSeriesClick] callback fired when user click on series item
 * @param {studyClickEvent} [props.onImageClick] callback fired when user click on image item
 */
const Sidebar = (props) => {
  const images = props.activeSeries?.instances || [];

  return (
    <div id={props.id} className="d-flex flex-column fit-height mr-1">
      <div className="sidebar-row study-list mb-2">
        <StudyList
          studies={props.studies}
          activeStudy={props.activeStudy}
          activeSeries={props.activeSeries}
          onStudyClick={props.onStudyClick}
          onSeriesClick={props.onSeriesClick}
        ></StudyList>
      </div>
      <div id="scroll-tile-container" className="sidebar-row">
        <TileList
          images={images}
          activeStudy={props.activeStudy}
          activeSeries={props.activeSeries}
          activeImage={props.activeImage}
          onTileClick={props.onImageClick}
        ></TileList>
      </div>
    </div>
  );
};

Sidebar.propTypes = {
  studies: PropTypes.array,
  activeStudy: PropTypes.object,
  activeSeries: PropTypes.object,
  activeImage: PropTypes.object,
  onStudyClick: PropTypes.func,
  onSeriesClick: PropTypes.func,
  onImageClick: PropTypes.func,
};

export default Sidebar;
