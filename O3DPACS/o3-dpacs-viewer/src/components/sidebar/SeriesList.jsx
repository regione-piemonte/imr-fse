import React from "react";
import PropTypes from "prop-types";
import { ListGroup, ListGroupItem } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faClone } from "@fortawesome/free-solid-svg-icons";
import classNames from "classnames";
import { isFunction } from '../../utils/equals';


/**
 * Render a Series list item (@see SeriesList)
 * @component
 * @param {Props} props input properties
 * @param {bool} [props.active] active state
 * @param {EventHandler} props.onClick mouse click callback event
 * @param {object} props.series series data 
 */
const SeriesListItem = (props) => {
  if (!props.series) {
    return null;
  }
  const onClick = () => {
    if (isFunction(props.onClick)) {
      props.onClick(props.series);
    }
  };
  const label = `${props.series.modality} - ${props.series.description} - ${props.series.numberOfInstances}`;

  return (
    <ListGroupItem
      action
      active={props.active}
      className="app-list-group-item list-group-item py-1 text-left"
      onClick={onClick}
      title={label}
      data-class="SeriesListItem"
      data-description={props.series.description}
    >
      <FontAwesomeIcon
        icon={faClone}
        className={classNames("app-fa-icon", { "app-fa-icon-active": props.active})}
      />
      <span className = "ml-4">
        {label}
      </span>
    </ListGroupItem>
  );
};

SeriesListItem.propTypes = {
  active: PropTypes.bool,
  onClick: PropTypes.func,
  series: PropTypes.object,
};

/**
 * Render a list of Series items
 * @component
 * @param {Props} props
 * @param {object} props.activeSeries active series 
 * @param {object[]} props.series array of study-related series objects
 * @param {EventHandler} props.onClick mouse click callback event
 */
const SeriesList = (props) => {
  const items = props.series?.map((series, index) => {
    const active = series?.uid === props.activeSeries?.uid;
    return (
      <SeriesListItem
        key={index}
        series={series}
        active={active}
        onClick={props.onClick}
      />
    )
  });

  return (
    <ListGroup className="app-list-group">
      {items}
    </ListGroup>
  );
};

SeriesList.propTypes = {
  series: PropTypes.array,
  activeSeries: PropTypes.object,
  onClick: PropTypes.func,
};
export default SeriesList;
