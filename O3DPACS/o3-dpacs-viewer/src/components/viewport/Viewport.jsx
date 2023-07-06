import React from "react";
import ResizeDetector from "react-resize-detector";
import Overlay from "../overlay/Overlay";
import { TOOLS } from "../../utils/tools";
import {ZOOM_MODE, MIN_SCALE, MAX_SCALE} from '../../utils/zoom';
import { MimeType } from "../../types/mime";
import "./Viewport.css"

const defaultState = {
  zoomMode: ZOOM_MODE.SCALE_TO_FIT,
  // zoom scale
  zoomValue: 1.0,
  // pan horizontal offset in pixel
  offsetX: 0,
  // pan vertical offset in pixel
  offsetY: 0,
  // clockwise rotation angle in degrees
  rotation: 0,
  // horizontal flip
  horzFlip: false,
  // linear LUT window with
  windowWidth: 0,
  // linear LUT window center
  windowCenter: 0,
  // frame number
  frameNumber: 1,
  // dragging state
  dragging: false,
};

/**
 * Display image on renderable display area and manage user actions.
 * It uses HTML5 Canvas.
 * @component
 * @param {Props} props
 * @param {object} [props.patient] patient's data
 * @param {object} [props.study] study data
 * @param {object} [props.series] series data 
 * @param {object} [props.image] image data
 * @param {function} [props.scrollFrame] function to scroll froward or backward image or frame
 * @param {Number} props.scrollFrame.delta scroll delta
 * @param {string} [props.activeTool] active tool id
 * @param {boolean} [props.showInfo] show annotation info
 * @param {boolean} [props.cineMode] enable cine mode
 */
class Viewport extends React.Component {
  constructor(props) {
    super(props);
    this.resizeRef = React.createRef();
    this.canvasRef = React.createRef();
    this.state = { ...defaultState };
    // image cache map
    this.cache = {};
    this.cineTimer = null;
    this.cineFps = 30;
  }

  componentDidMount() {
    this.onResize();
    this.updateCine();
  }

  componentWillUnmount() {
    this.clearCache();
  }

  componentDidUpdate(prevProps) {
    if (this.props.image !== prevProps?.image) {
      this.clearCache();
    }
    if (this.props.cineMode !== prevProps.cineMode) {
      this.updateCine();
    }
  }

  clearCache = () => {
    this.cache = {};
  };

  updateCine = () => {
    if (this.props.cineMode) {
      if (this.cineTimer) {
        clearInterval(this.cineTimer);
        this.cineTimer = null;
      }
      const fps = this.cineFps || 30;
      this.cineTimer = setInterval(() => this.scrollFrame(1), 1000 / fps);
    } else if (this.cineTimer) {
      clearInterval(this.cineTimer);
      this.cineTimer = null;
    }
  };

  onResize = (e) => {
    if (this.canvasRef && this.canvasRef.current) {
      const ctx = this.canvasRef.current.getContext("2d");
      if (ctx) {
        const target = this.resizeRef.current;

        // check canvas dim to avoid flickering
        const width = target.clientWidth - 1;
        const height = target.clientHeight - 1;
        if (ctx.canvas.width !== width || ctx.canvas.height !== height) {
          ctx.canvas.width = width;
          ctx.canvas.height = height;
          this.forceUpdate();
        }
      }
    }
  };

  getImageFitScale = (img) => {
    if (img && img.width > 0 && img.height > 0) {
      const ctx = this.canvasRef.current.getContext("2d");
      const canvas = ctx.canvas;

      let ratioX = canvas.width / img.width;
      let ratioY = canvas.height / img.height;
      const scaleToFit = Math.min(ratioX, ratioY);
      return scaleToFit;
    }
    return 1.0;
  };

  drawImage =  (img) => {
    if (img.width > 0 && img.height > 0) {
      const ctx = this.canvasRef.current.getContext("2d");
      const canvas = ctx.canvas;
      const {
        zoomMode,
        zoomValue,
        offsetX,
        offsetY,
        rotation,
        horzFlip,
      } = this.state;

      // decode paint scale
      const scaleToFit = this.getImageFitScale(img);
      let scale = 1.0;
      if (zoomMode === ZOOM_MODE.SCALE_TO_FIT) {
        scale = scaleToFit;
      } else if (zoomMode === ZOOM_MODE.MAGNIFY) {
        scale = zoomValue > 0 ? zoomValue : scaleToFit;
      }

      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // move to canvas center
      ctx.translate(canvas.width / 2, canvas.height / 2);
      // pan
      ctx.translate(offsetX, offsetY);
      // rotate again
      ctx.rotate((rotation * Math.PI) / 180);
      if (horzFlip) {
        ctx.scale(-1, 1);
      }
      ctx.scale(scale, scale);
      // translate to the center of the image
      ctx.translate(-img.width / 2, -img.height / 2);
      // draw image
      ctx.drawImage(img, 0, 0, img.width, img.height);
      //reset trasform
      ctx.setTransform(1, 0, 0, 1, 0, 0);

      //save last drawing scale
      this.drawScale = scale;
    }
  };

  updateCanvas(image) {
    if (!this.canvasRef?.current) {
      return;
    }
    const ctx = this.canvasRef.current.getContext("2d");
    if (!image || !image.wadourl) {
      ctx.fillRect(0, 0, ctx.canvas.innerWidth, ctx.canvas.innerHeight);
      return;
    }

    const { windowCenter, windowWidth, dragging, frameNumber } = this.state;
    const numberOfFrames = image.numberOfFrames;

    let url = image.wadourl;
    if (frameNumber >= 1) {
      url += "&frameNumber=" + frameNumber;
    }
    const cacheId = url;
    // do not use window settings for cacheId to limit cache usage
    if (windowWidth > 0) {
      url +=
        "&windowCenter=" +
        windowCenter.toFixed(0) +
        "&windowWidth=" +
        windowWidth.toFixed(0);
      if (dragging) {
        url += "&imageQuality=25";
      }
    }

    let bitmap = image.bitmap;
    if (numberOfFrames > 1) {
      bitmap = this.cache[cacheId];
    }
    if (!bitmap) {
      bitmap = new Image();
    }
    if (bitmap.src !== url) {
      // cancel load, see https://html.spec.whatwg.org/multipage/embedded-content.html#attr-img-src
      bitmap.alt = "";
      bitmap.src = "";
      bitmap.onload = () => {
        image.bitmap = bitmap;
        this.cache[cacheId] = bitmap;
        this.forceUpdate();
      };
      bitmap.src = url;
    }
    if (bitmap && bitmap.complete) {
      this.drawImage(bitmap);
    }
  }

  scrollFrame = (delta) => {
    if (typeof this.props.scrollFrame === "function") {
      let { image, cineMode } = this.props;
      if (image) {
        // scroll frames
        const numberOfFrames = image.numberOfFrames;
        let frameNumber = this.state.frameNumber || 1;
        if (numberOfFrames > 1) {
          frameNumber += delta;
          if (cineMode && frameNumber > numberOfFrames) {
            frameNumber = 1;
          }
          frameNumber = Math.max(1, Math.min(frameNumber, numberOfFrames));
          this.setState({ frameNumber });
          this.updateCanvas(image);
          return;
        }
      }
      // scroll images
      image = this.props.scrollFrame(delta);
      if (image) {
        this.updateCanvas(image);
      }
    }
  };

  onMouseDown = (e) => {
  };

  onMouseUp = (e) => {
    this.setState({ dragging: false });
    // do not show contextal menu
    if (e.buttons === 2) {
      e.preventDefault();
    }
  };

  onStackChange = (e) => {
    // IHE BIR requirements:
    // Vertical movement of the mouse upwards for other images shall:
    // |- scroll towards earlier frames of a multi-frame images
    // |- scroll towards lower Instance Numbers in a series of single frame images.
    const delta = Math.round(e.movementY / 3);
    if (delta) {
      this.scrollFrame(delta);
    }
  };

  onZoomChange = (e) => {
    const dy = e.movementY;
    const k = (MAX_SCALE - MIN_SCALE) / window.innerHeight;
    const scale = this.drawScale - k * dy;
    if (scale >= MIN_SCALE && scale <= MAX_SCALE) {
      this.setState({
        zoomMode: ZOOM_MODE.MAGNIFY,
        zoomValue: scale,
      });
    }
  };

  onPanChange = (e) => {
    if (e.movementX || e.movementY) {
      let { offsetX, offsetY } = this.state;
      if (e.movementX) {
        offsetX += e.movementX;
      }
      if (e.movementY) {
        offsetY += e.movementY;
      }
      this.setState({ offsetX, offsetY });
    }
  };

  onContrastChange = (e) => {
    if (e.movementX || e.movementY) {
      let { windowWidth, windowCenter } = this.state;
      if (!windowWidth) {
        windowWidth = Number.parseFloat(this.props.image.windowWidth);
        windowCenter = Number.parseFloat(this.props.image.windowCenter);
      }
      if (Number.isFinite(windowWidth) > 0) {
        // IHE BIR requirements
        // Horizontal movement of the mouse to the right will widen the window width (flatten the perceived contrast).
        // Vertical movement of the mouse upwards will lower the window center (increase the 825 perceived brightness)
        if (Math.abs(e.movementX) >= Math.abs(e.movementY)) {
          windowWidth += e.movementX;
        } else {
          windowCenter += e.movementY;
        }
        this.setState({ windowCenter, windowWidth, dragging: true });
      }
    }
  };

  onMouseMove = (e) => {
    const { activeTool } = this.props;
    // 1 Primary button (usually the left button)
    if (e.buttons === 1) {
      switch (activeTool) {
        case TOOLS.STACK:
          return this.onStackChange(e);
        case TOOLS.ZOOM:
          return this.onZoomChange(e);
        case TOOLS.PAN:
          return this.onPanChange(e);
        case TOOLS.CONTRAST:
          return this.onContrastChange(e);
        default:
          break;
      }
    }
  };

  /**
   * Increase/descrease frome rate
   * @param {Number} delta number of frame-per-seconds to add.
   * Positive values increase frame-rate, negative values descrease it.
   * @retuns {void}
   */
  scrollFps(delta) {
    let fps = this.cineFps + (delta > 0 ? 1 : -1);
    fps = Math.max(1, Math.min(60, fps));
    if (this.cineTimer) {
      this.cineFps = fps;
      this.updateCine();
    }
  }

  onWheel = (e) => {
    if (this.props.cineMode) {
      this.scrollFps(e.deltaY);
    } else {
      this.scrollFrame(e.deltaY > 0 ? 1 : -1);
    }
  }

  onContextMenu = (e) => {
    e.preventDefault();
  }

  /**
   * Change default viewport scale
   * @param {Number} value scale value
   */
  scaleTo(value) {
    if (value >= MIN_SCALE && value <= MAX_SCALE) {
      this.setState({
        zoomMode: ZOOM_MODE.MAGNIFY,
        zoomValue: value,
      });
    }
  }

  /**
   * Set scale to fit image into viewport
   */
  scaleToFit() {
    this.setState({
      zoomMode: ZOOM_MODE.SCALE_TO_FIT,
    });
  }

  /**
   * Rotate and flip horizontal image.
   * Internal Viewport canvas transformation manage always rotation before flip
   * so this function compute rotation and flip factors to manage thei in this order.
   * @private
   * @param {number} angle clock-wise rotation factor. Shall be a multiple of 90°.
   * @param {boolean} horzFlip horizonal flip flag
   * @retruns {void}
   */
  rotateAndFlip(angle, horzFlip) {
    if (angle || horzFlip) {
      const flipFrom = this.state.horzFlip || false;
      const angleFrom = this.state.rotation || 0;
      let flipTo = flipFrom;
      let angleTo = (angleFrom + (angle || 0)) % 360;
      if (horzFlip) {
        flipTo = !flipFrom;
        angleTo = (360 - angleTo) % 360;
      }
      // make angle positive
      if (angleTo < 0) {
        angleTo = (angleTo + 360) % 360;
      }
      this.setState({
        rotation: angleTo,
        horzFlip: flipTo,
      });
    }
  }

  /**
   * Rotate image by 90 degrees clockwise.
   */
  rotateRight() {
    this.rotateAndFlip(90, false);
  }

  /**
   * Flip image along Y axis.
   */
  flipHorizontal() {
    this.rotateAndFlip(0, true);
  }

  /**
   * Flip iage along X axiz.
   * Vertical flip correpond to rotation of 180° plus a horizontal flip
   */
  flipVertical() {
    this.rotateAndFlip(180, true);
  }

  /**
   * Revert all trasformations and restore default display.
   */
  revert() {
    this.setState({ ...defaultState });
  }

  render() {
    const {props, state} = this;
    let contentType;
    if (props.image) {
      contentType = props.image.contentType;
      const url = props.image.wadourl + '&contentType=' + contentType;
      if (contentType === MimeType.PDF) {
        return (
          <iframe className="no-space fit-parent" src={url} title="document" />
        );
      } else if (contentType === MimeType.VIDEO_MPEG) {
        return (
          <video className="no-space fit-parent" src={url} controls title="video"/>
        );
      }
    }

    this.updateCanvas(props.image);
    const cineFps = props.cineMode ? this.cineFps : null;
    return (
      <div
        id={props.id}
        className="no-space fit-parent"
        style={{ backgroundColor: "black", position: "relative" }}
        onContextMenu={this.onContextMenu}
        onMouseDown={this.onMouseDown}
        onMouseMove={this.onMouseMove}
        onMouseUp={this.onMouseUp}
        onWheel={this.onWheel}
      >
        <ResizeDetector
          className="no-space fit-parent"
          style={{ backgroundColor: "inherit" }}
          targetRef={this.resizeRef}
          handleWidth
          handleHeight
          onResize={this.onResize}
        >
          <div
            className="no-space fit-parent"
            ref={this.resizeRef}
            style={{ backgroundColor: "inherit" }}
          >
            <canvas
              id="canvas"
              className="no-space"
              ref={this.canvasRef}
              style={{ position: "absolute", top: 0, left: 0 }}
              onKeyDown={props.onKeyDown}
            ></canvas>
          </div>
        </ResizeDetector>
        {props.showInfo && (
          <Overlay
            patient={props.patient}
            study={props.study}
            series={props.series}
            image={props.image}
            frameNumber={state.frameNumber}
            windowCenter={state.windowCenter}
            windowWidth={state.windowWidth}
            rotation={state.rotation}
            horzFlip={state.horzFlip}
            scale={this.drawScale}
            cineFps={cineFps}
          ></Overlay>
        )}
      </div>
    );
  }
}

export default Viewport;










































































































