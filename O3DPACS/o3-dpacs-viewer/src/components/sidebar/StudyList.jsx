import React from "react";
import PropTypes from "prop-types";
import { ListGroup, ListGroupItem } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFolder, faFolderOpen } from "@fortawesome/free-solid-svg-icons";
import SeriesList from "./SeriesList";
import { isFunction } from '../../utils/equals';
import {filterSeriesByModality} from '../../utils/seriesHelpers';
import './StudyList.css';


/**
 * Render a study item
 * @component
 * @param {PropTypes} props input props
 * @param {Study} props.study array of studies
 * @param {Boolean} props.active true if study is the active study
 * @param {Series} props.activeSeries active series
 * @param {fuction} props.onSeriesClick series item click callback
 */
const StudyListItem = (props) => {
  const {study, active } = props;
  if (!study) {
    return null;
  }
  const onClick = () => {
    if (isFunction(props.onStudyClick)) {
      props.onStudyClick(study);
    }
  };

  const offline = study.isOffline();
  const series = active ? filterSeriesByModality(study.series) : [];
  const title = study.date + " " + study.time + (offline ? ' (OFFLINE)' : "") + "\n" +
      study.description + "\n" +
      study.accessionNumber;

  return (
    <ListGroupItem 
      key={study.uid} 
      className="list-group-item text-left app-backgroud app-list-group-item-border py-1"
      onClick={onClick}
      title={title}
    >
      <div variant="contained" className={"d-flex align-items-center app-list-group-item py-1"}>
        <FontAwesomeIcon
          inverse
          size="lg"
          icon={active ? faFolderOpen : faFolder}
          className="app-fa-icon mr-2"
        ></FontAwesomeIcon>
        <h5 className="text-left text-truncate m-0">
          {study.date + " " + study.time}
          <span className="text-warning">{offline ? ' (OFFLINE)' : ""}</span>
          <br/>
          {`${study.description} ${study.accessionNumber}`}
        </h5>
      </div>
      <SeriesList
        series={series}
        activeSeries={props.activeSeries}
        onClick={props.onSeriesClick}
      ></SeriesList>
    </ListGroupItem>
  );
}

StudyListItem.propTypes = {
  study: PropTypes.object,
  active: PropTypes.bool,
  activeSeries: PropTypes.object,
  onSeriesClick: PropTypes.func,
};

/**
 * Render a list of study items
 * @component
 * @param {PropTypes} props input props
 * @param {Study[]} props.studies array of studies
 * @param {Study} props.activeStudy active study
 * @param {Series} props.activeSeries active series
 * @param {fuction} props.onStudyClick study item click callback
 */
const StudyList = (props) => {
  const {studies, activeStudy, ...otherProps} = props;
  const items = studies.map((study, index) => {
    const active = study === activeStudy;
    return (
      <StudyListItem
        key={index}
        study= {study} 
        active={active}
        {...otherProps} 
      />
    );
  });

  return (
    <ListGroup className="app-list-group">
      {items}
    </ListGroup>
  );
}

StudyList.propTypes = {
  studies: PropTypes.array,
  activeStudy: PropTypes.object,
  activeSeries: PropTypes.object,
  onStudyClick: PropTypes.func,
  onSeriesClick: PropTypes.func,
  onImageClick: PropTypes.func,
};

export default StudyList;