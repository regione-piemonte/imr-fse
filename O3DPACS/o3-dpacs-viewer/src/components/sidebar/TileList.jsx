import React from "react";
import PropTypes from "prop-types";
import classNames from 'classnames';
import Tile from './Tile';
import './TileList.css';
import isElementVisible from '../../utils/isElementVisible';

/**
 * TileList component
 * @component
 * @description Render a list of image thumbnails
 * @param {Props} props
 * @param {object} props.activeImage active image object
 * @param {object[]} props.images array of images data
 * @param {EventHandler} props.onClick mouse click callback event
 * @example <TileList activeImage={this.activeImage} images={this.images} onClick={this.onClick} />
 */
class TileList extends React.Component {
  componentDidUpdate(prevProps) {
    const nextImage = this.props.activeImage;
    const prevImage = prevProps?.activeImage;
    if (nextImage && nextImage !== prevImage) {
      this.scrollToSelected();
    } 
  }

  scrollToSelected = () => {
    if (document) {
      const element = document.querySelector("#scroll-tile");
      const container = document.querySelector("#scroll-tile-container");
      if (element && container && !isElementVisible(element, container)) {
        element.scrollIntoView(false, {block: "nearest"});
      }
    }
  }

  onTileClick = (e, image) => {
    if (typeof this.props.onTileClick === 'function') {
      this.props.onTileClick(image);
    }
  }

  render() {
    const tiles = this.props.images.map((image, index) => {
      const active = image === this.props.activeImage;
      return image && (
        <Tile
          key={index}
          className={classNames("tile-img", {"tile-img-active": active})}
          alt={image.number}
          src={image.iconUrl}
          contentType={image.contentType}
          onClick={(e) => this.onTileClick(e, image)}
          id={active ? "scroll-tile" : null}
          brText={`${image.numberOfFrames || ""}`}
        ></Tile>
      );
    });

    return (
      <div className="tile-list">
        {tiles}
      </div>
    );
  }
}

TileList.propTypes = {
  activeImage: PropTypes.object,
  images: PropTypes.array.isRequired,
  onClick: PropTypes.func,
};

export default TileList;
