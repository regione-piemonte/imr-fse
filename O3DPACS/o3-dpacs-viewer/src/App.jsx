import React from "react";
import { Alert, Container, Row } from "react-bootstrap";
import Split from "react-split";
import Loader from "react-spinners/MoonLoader";
import ResizeDetector from "react-resize-detector";
import screenfull from 'screenfull';
import Sidebar from "./components/sidebar/Sidebar";
import Viewport from "./components/viewport/Viewport";
import Header from "./components/header/Header";
import SafetyAlert from "./components/alert/SafetyAlert";
import { TOOLS } from "./utils/tools";
import api from "./api";
import { isArray, isArrayFill, stringToBool } from "./utils";
import {
  filterSeriesByModality,
  getFirstImageSeries,
} from "./utils/seriesHelpers";

import "bootstrap/dist/css/bootstrap.css";
import "./App.css";
import factory from "./types/factory";

/**
 * Generic React props object
 * @typedef {object} Props
 */

/**
 * Generic React event handler
 * @callback  EventHandler
 * @param {object} event event object
 */

/* default state */
const initialState = {
  patient: null,
  activeStudy: null,
  activeSeries: null,
  activeImage: null,
  activeTool: TOOLS.STACK,
  cineMode: false,
  showSidebar: true,
  showInfo: true,
  showSafetyAlert: true,
  splitSizes: [25, 75],
  headerTop: 0,
  headerHeight: 0,
  fullScreen: screenfull.isEnabled && screenfull.isFullscreen,
  fullScreenEnabled: screenfull.isEnabled
};

/**
 * Main application component
 * @param {Props} props
 * @param {string} [props.patientID] patient identifier to serach for
 * @param {string} [props.issuerId] issuer of patientId
 * @param {string} [props.accessionNumber] study identifier to serach for.
 * Shall be filled if patientID nor studyUID is valid.
 * @param {string} [props.studyUID] study identifier to serach for.
 * Shall be filled if patientID nor accessionNumber is valid.
 * @param {string | bool} [props.showOnlyThis=false] if true hide patient history
 * @param {string | bool} [props.showSafetyAlert=true] if true show a modal message with safety use alert.
 * @param {string | bool} [props.keyImagesOnly=false] if true show key objects first.
 * @example <App credentials = {credentials} accessioNumber="1.2.3" showOnlyThis={true}/>
 */
class App extends React.Component {
  static defaultProps = {
    isNotO3DPACS: false,
    showOnlyThis: true,
    showSafetyAlert: true,
    keyImagesOnly: false
  }

  constructor(props) {
    super(props);
    this.containerRef = React.createRef();
    this.viewportRef = React.createRef();
    this.state = initialState;
    this.state.showSafetyAlert = stringToBool(props.showSafetyAlert);
  }

  verifyParameters = (props) => {
    if (props.patientID || props.accessionNumber || props.studyUID) {
      return true;
    }
    return false;
  };

  componentDidMount() {
    if (window && window.addEventListener) {
      window.addEventListener("resize", this.onResize);
    }
    if (screenfull.isEnabled) {
      screenfull.onChange = this.onFullScreenChange;
    }

    if (!this.verifyParameters(this.props)) {
      this.setState({ error: "No filter found, please check configuration" });
      return;
    }

    const keyImagesOnly = (this.props.keyImagesOnly && this.props.keyImagesOnly.length > 0) ?
                          stringToBool(this.props.keyImagesOnly) : false;

    let activeImage = null;
    this.setState({ loading: true });
    api
      .getPatientInfo({ ...this.props })
      .then((data) => {
        if (data.patients.length === 0) {
          return this.setState({ error: "No study found" });
        }
        if (data.patients.length > 1) {
          return this.setState({ error: "More the one patient found" });
        }
        const patient = data.patients[0];
        this.setState({ ...this.state, patient });
        const studies = patient.studies;
        if (isArrayFill(studies)) {
          this.onStudyClick(studies[0]);
          // retrieve instances
          studies.forEach((study) => {
            const studyUID = study.uid;
            if (!study.isOffline() && isArrayFill(study.series)) {
              // check KO series presence
              const koPresent = study.series.some(s => s.modality === 'KO');
              
              study.series.forEach((series) => {
                const seriesUID = series.uid;
                api.getInstances({ studyUID, seriesUID }).then((instanceData) => {
                  const { patients } = instanceData;
                  if (
                    patients &&
                    patients.length === 1 &&
                    patients[0].patientID === patient.patientID &&
                    patients[0].studies &&
                    patients[0].studies.length === 1
                  ) {
                    const series2 = patients[0].studies[0].series[0];
                    if (series2 && series2.instances) {
                      // preparing KO instances
                      if (series2.modality === 'KO') {
                        // removing KO series
                        this.removeSeries(study, series2);
                        
                        if (isArrayFill(series2.instances)) {
                          series2.instances.forEach(inst => {
                            // converting KO instance to series
                            series = this.convertKOInstanceToSeries(inst, patients, study, series);
                            // adding converted KO instance to series
                            study.series.push(series);

                            if (!activeImage)
                             activeImage = this.checkActiveImage(studyUID, series, koPresent, keyImagesOnly, activeImage, study, true);
                          });
                        }
                      } else { // Images
                        series.instances = series2.instances;
                        if (isArrayFill(series.instances) && !activeImage) {
                          activeImage = this.checkActiveImage(studyUID, series, koPresent, keyImagesOnly, activeImage, study, false);
                        }
                      }
                    }
                    // Sorting series by Key Objects
                    if(koPresent) this.sortKOSeries(study, keyImagesOnly);
                  }
                });
              });
            }
          });
        }
      })
      .then((res) => this.setState({ loading: false }))
      .catch((error) => {
        this.setState({ error: error.message, errorInfo: error.stack, loading: false });
        console.log("error laoding patient info, cause: ", error);
      });
  }
  
  removeSeries(study, series2) {
    study.series = study.series.filter(function (s) {
      return s.uid !== series2.uid;
    });
  }

  convertKOInstanceToSeries(inst, patients, study, series) {
    const series3 = factory.KoSeries(inst);
    // check if series date and time are the same as the study
    this.checkKODateTime(series3, patients[0].studies);
    // retrieving referenced series
    this.getReferencedSeries(inst, study);

    series3.instances = inst.referencedInstances;
    return series3;
  }

  checkKODateTime(series, study) {
    if (series.contentDate && series.contentTime && study.date && study.time &&
      series.contentDate === study.date && series.contentTime === study.time) {
      // same date and time of study. Removing them from the series description
      series.description = series.codeMeaning;
    }
  }

  getReferencedSeries(inst, study) {
    inst.referencedInstances.forEach((refIns) => {
      const refSeries = study.series.find((ser) => ser.uid === refIns.referencedSeriesUID);
      if (refSeries)
        refIns.referencedSeries = {
          modality: refSeries.modality,
          description: refSeries.description
        };
    });
  }

  sortKOSeries(study, keyImagesOnly) {
    const kos = study.series.filter(s => s.modality === 'KO');
    const imgs = study.series.filter(s => s.modality !== 'KO');
    if (keyImagesOnly)
      study.series = kos.concat(imgs);

    else
      study.series = imgs.concat(kos);
  }

  checkActiveImage(studyUID, series, koPresent, keyImagesOnly, activeImage, study, ko) {
    if (!this.props.studyUID ||
      this.props.studyUID === studyUID) {
      const image = series.instances.find(
        (instance) => instance.rows > 0
      );
      if (image && ((ko && keyImagesOnly) || (!ko && (!koPresent || !keyImagesOnly)) ) ) {
        activeImage = image;
        this.setState({
          loading: false,
          activeStudy: study,
          activeSeries: series,
          activeImage,
        });
      }
    }
    return activeImage;
  }

  componentWillUnmount() {
    if (window && window.removeEventListener) {
      window.removeEventListener("resize", this.onResize);
    }
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ error, errorInfo });
  }

  onResize = (width, height) => {
    const state = { fullScreen: screenfull.isFullscreen };
    const el = document.getElementById("header");
    if (el) {
      state.headerTop = el.offsetTop;
      state.headerHeight = el.offsetHeight;
    }
    this.setState(state);
  }

  setActivateItem = (image, series, study) => {
    const state = {};
    if (image) {
      state.activeImage = image;
    }
    if (series) {
      state.activeSeries = series;
    }
    if (study) {
      state.activeStudy = study;
    }
    this.setState(state);
  }

  onStudyClick = (study) => {
    if (study && study !== this.state.activeStudy) {
      const series = getFirstImageSeries(study.series);
      if (isArrayFill(series)) {
        const image = series.instances[0];
        this.setActivateItem(image, series, study);
      }
    }
  }

  onSeriesClick = (series) => {
    if (series !== this.state.activeSeries) {
      const image = isArrayFill(series.instances) ? series.instances[0] : null;
      this.setActivateItem(image, series);
    }
  }

  onImageClick = (image) => {
    this.setActivateItem(image);
  }

  scrollFrame = (delta) => {
    const { activeSeries, activeImage } = this.state;
    if (activeSeries && activeImage) {
      const instances = activeSeries.instances;
      let index = instances.findIndex((item) => item.uid === activeImage.uid);
      if (index >= 0) {
        index += delta;
        if (this.state.cineMode && index >= instances.length) {
          index = 0;
        }
        if (index >= 0 && index < instances.length) {
          const image = instances[index];
          this.setActivateItem(image);
          return image;
        }
      }
    }
    return null;
  }

  scrollSeries = (delta) => {
    const series = this.state.activeStudy?.series;
    if (!series) {
      return;
    }
    const displaySeries = filterSeriesByModality(series);
    if (!displaySeries) {
      return;
    }
    const { activeSeries } = this.state;
    let index = displaySeries.findIndex(
      (item) => item.uid === activeSeries?.uid
    );
    if (index >= 0) {
      index += delta;
      if (index >= 0 && index < displaySeries.length) {
        this.onSeriesClick(displaySeries[index]);
      }
    }
  }

  onClickTool = (id) => {
    const viewport = this.viewportRef.current;
    switch (id) {
      case TOOLS.STACK:
      case TOOLS.CONTRAST:
      case TOOLS.ZOOM:
      case TOOLS.PAN:
        this.setState({ activeTool: id });
        break;
      case TOOLS.SCALE_TO_FIT:
        viewport.scaleToFit();
        break;
      case TOOLS.SCALE_TO_ONE:
        viewport.scaleTo(1);
        break;
      case TOOLS.ROTATE:
        viewport.rotateRight();
        break;
      case TOOLS.HORZ_FLIP:
        viewport.flipHorizontal();
        break;
      case TOOLS.TOGGLE_CINE:
        const cineMode = !!this.state.cineMode;
        this.setState({ cineMode: !cineMode });
        break;
      case TOOLS.REVERT:
        viewport.revert();
        break;
      case TOOLS.TOGGLE_SIDEBAR:
        const showSidebar = !!this.state.showSidebar;
        this.setState({ showSidebar: !showSidebar });
        break;
      case TOOLS.TOGGLE_INFO:
        const showInfo = !!this.state.showInfo;
        this.setState({ showInfo: !showInfo });
        break;
      case TOOLS.TOGGLE_FULLSCREEN:
        this.toggleFullScreen();
        break;
      default:
        break;
    }
  }

  onKeyDown = (e) => {
    if (e.key === "c") {
      this.onClickTool(TOOLS.TOGGLE_CINE);
    } else if (e.key === "i") {
      this.onClickTool(TOOLS.TOGGLE_INFO);
    } else if (e.key === "s") {
      this.onClickTool(TOOLS.STACK);
    } else if (e.key === "w") {
      this.onClickTool(TOOLS.CONTRAST);
    } else if (e.key === "z" && !e.ctrlKey) {
      this.onClickTool(TOOLS.ZOOM);
    } else if (e.key === "f") {
      this.onClickTool(TOOLS.SCALE_TO_FIT);
    } else if (e.key === "Escape") {
      this.onClickTool(TOOLS.REVERT);
    } else if (e.key === "ArrowLeft" || e.key === "ArrowUp") {
      this.scrollFrame(-1);
    } else if (e.key === "ArrowRight" || e.key === "ArrowDown") {
      this.scrollFrame(+1);
    } else if (e.key === "PageUp") {
      this.scrollSeries(-1);
    } else if (e.key === "PageDown") {
      this.scrollSeries(+1);
    } else if (e.key === "F11") {
      e.preventDefault(); // prevent default browser shortcut
      this.toggleFullScreen();
    }
  }

  onSplitDragEnd = (splitSizes) => {
    if (isArray(splitSizes)) {
      this.setState({ splitSizes });
    }
  }

  onAcceptClick = (e) => {
    this.setState({ showSafetyAlert: false });
  }

  onDeclineClick = (e) => {
    if (window && window.history) {
      window.history.back();
    }
  }

  onFullScreenChange = (e) => {
    const fullScreen = screenfull.isFullscreen;
    this.setState({ fullScreen });
  }

  toggleFullScreen = () => {
    if (screenfull.isEnabled) {
      screenfull.toggle();
    }
  }

  renderLoader = () => {
    return (
      <Container className="app app-container">
        <div className="app-loader fit-width fot-height">
          <Loader color="#627FC0" />
        </div>
      </Container>
    );
  }

  render() {
    const { state } = this;
    if (state.error) {
      return (
        <Alert variant="danger">
          <Alert.Heading>{state.error}</Alert.Heading>
          <p>{state.errorInfo || ''}</p>
        </Alert>
      );
    }
    if (state.loading) {
      return this.renderLoader();
    }
    if (!state.patient?.studies) {
      return <Container className="app app-container" />;
    }

    const headerHeight = state.headerHeight || 0;
    const headerBottom = state.headerTop + state.headerHeight || 0;
    return (
      <ResizeDetector
        handleHeight
        onResize={this.onResize}
        targetRef={this.containerRef}
      >
        <SafetyAlert
          key="safetyAlert"
          onAcceptClick={this.onAcceptClick}
          onDeclineClick={this.onDeclineClick}
          show={state.showSafetyAlert}
        />
        <Container
          key="appContainer"
          ref={this.containerRef}
          className="app app-container d-flex flex-column"
          tabIndex={0}
          onKeyDown={this.onKeyDown}
        >
          <Header
            key="appHeader"
            id="header"
            patient={state.patient}
            activeTool={state.activeTool}
            onClickTool={this.onClickTool}
            cineMode={state.cineMode}
            showSidebar={state.showSidebar}
            showInfo={state.showInfo}
            fullScreen={state.fullScreen}
            fullScreenEnabled={state.fullScreenEnabled}
          />
          <span style={{ height: `${headerHeight}px`, width: "100%" }} />
          <Row
            className="app-main m-0 p-0 flex-fill"
            style={{ maxHeight: `calc(100vh - ${headerBottom}px` }}
          >
            <Split
              className="app-gutter d-flex fit-width fit-height"
              minSize={0}
              sizes={state.showSidebar ? state.splitSizes : [0, 100]}
              gutterSize={state.showSidebar ? 8 : 0}
              onDragEnd={this.onSplitDragEnd}
            >
              <div className="fit-height">
                {state.showSidebar && (
                  <Sidebar
                    id="sidebar"
                    studies={state.patient.studies}
                    activeStudy={state.activeStudy}
                    activeSeries={state.activeSeries}
                    activeImage={state.activeImage}
                    onStudyClick={this.onStudyClick}
                    onSeriesClick={this.onSeriesClick}
                    onImageClick={this.onImageClick}
                  ></Sidebar>
                )}
              </div>
              <div className="app-viewer fit-height">
                <Viewport
                  id="viewport"
                  ref={this.viewportRef}
                  patient={state.patient}
                  study={state.activeStudy}
                  series={state.activeSeries}
                  image={state.activeImage}
                  scrollFrame={this.scrollFrame}
                  activeTool={state.activeTool}
                  showInfo={state.showInfo}
                  cineMode={state.cineMode}
                ></Viewport>
              </div>
            </Split>
          </Row>
        </Container>
      </ResizeDetector>
    );
  }
}

export default App;




























































