import {isString} from '../utils/equals';

/**
 * @namespace factory
 */
const factory = {};

/**
 * Create a Patient
 * @memberof factory
 * @param {object} props input properties
 * @returns {Patient} patient object 
 */
factory.Patient = function (props) {
  const patient = {
    patientID: props.patientID,
    firstName: props.firstName,
    middleName: props.middleName,
    lastName: props.lastName,
    birthDate: props.birthDate,
    numberOfStudies: props.numberOfStudies,
    studies: [],
  }
  return patient;
}

 /**
 * @typedef StudyStatus
 * @type {object}
 * @enum  {string}
 * @memberof utils
 * @constant
 * @property {string} ARCHIVED archived
 * @property {string} NEARLINE nearline
 * @property {string} OFFLINE offline
 * @property {string} ONLINE online
 */
export const StudyStatus = {
  ARCHIVED: 'a',
  NEARLINE: "n",
  OFFLINE: "p",
  ONLINE: "o",
};

/**
 * Create a Study
 * @memberof factory
 * @param {object} props input properties
 * @returns {Study} study object 
 */
factory.Study = function (props) {
  const study = {
    accessionNumber: props.accessionNumber,
    date: props.date,
    description: props.description,
    numberOfSeries: props.numberOfSeries,
    series: [],
    studyStatus: props.studyStatus,
    time: props.time,
    uid: props.uid,
    isOffline() {
      return this.studyStatus === StudyStatus.OFFLINE;
    }
  }
  return study;
}

/**
 * Create a Series
 * @memberof factory
 * @param {object} props input properties
 * @returns {Series} series object 
 */
factory.Series = function (props) {
  const series = {
    description: props.description,
    instances: [],
    modality: props.modality,
    numberOfInstances: props.numberOfInstances,
    thumbnail: props.thumbnail,
    uid: props.uid,
  }
  return series;
}

/**
 * Create a Key Object Series
 * @memberof factory
 * @param {object} props input properties
 * @returns {Series} KO series object 
 */
 factory.KoSeries = function (props) {
  const codeMeaning = (props.codeMeaning && props.codeMeaning.trim().length > 0 ? props.codeMeaning : "Key Note");
  const koSeries = {
    codeMeaning: codeMeaning,
    contentDate: props.contentDate,
    contentTime: props.contentTime,
    description: (props.contentDate + " " + props.contentTime).trim() + " " + codeMeaning,
    instances: [],
    modality: 'KO',
    numberOfInstances: props.numberOfReferencedInstances,
    thumbnail: props.wadourl,
    uid: props.uid,
  }
  return koSeries;
}

/**
 * Init instance with default values 
 * @private
 * @param {object} instance 
 */
function initInstance(instance) {
  // check if there is a valid windowWidth and windowCenter 
  if (isString(instance.rows)) {
    instance.rows = Number.parseInt(instance.rows);
    instance.columns = Number.parseInt(instance.columns);
  }
  if (isString(instance.windowWidth)) {
    instance.windowWidth = Number.parseFloat(instance.windowWidth);
    instance.windowCenter = Number.parseFloat(instance.windowCenter);
  }
  if (isString(instance.bitPerPixel)) {
    instance.bitPerPixel = Number.parseInt(instance.bitPerPixel);
  }
  if (isString(instance.numberOfFrames)) {
    instance.numberOfFrames = Number.parseInt(instance.numberOfFrames);
  }
  if (instance && instance.rows > 0 && instance.columns > 0 && instance.bitPerPixel) {
    if ((instance.windowWidth || 0) < 1) {
      instance.windowWidth = Math.round(1 << instance.bitPerPixel);
      instance.windowCenter = Math.round(instance.windowWidth / 2);
    }
    instance.iconUrl = instance.wadourl + `&rows=128&columns=128&contentType=image/jpeg`;
    if (instance.imageQuality) {
      instance.iconUrl += `&imageQuality=${instance.imageQuality}`
    }
  }
}

/**
 * Create an Instance
 * @memberof factory
 * @param {object} props input properties
 * @returns {Instance} instance object 
 */
factory.Instance = function (props) {
  const instance = {
    bitPerPixel: props.bitPerPixel,
    columns: props.columns,
    contentType: props.contentType,
    imagePositionPatientX: props.imagePositionPatientX,
    imagePositionPatientY: props.imagePositionPatientY,
    imagePositionPatientZ: props.imagePositionPatientZ,
    imageQuality: props.imageQuality,
    instanceDateTime: props.instanceDateTime,
    echoTime: props.echoTime,
    number: props.number,
    numberOfFrames: props.numberOfFrames,
    photometricInterpretation: props.photometricInterpretation,
    pixelSpacing: props.pixelSpacing,
    rescaleIntercept: props.rescaleIntercept,
    rescaleSlope: props.rescaleSlope,
    rows: props.rows,
    transferSyntax: props.transferSyntax,
    wadourl: props.wadourl,
    windowCenter: props.windowCenter,
    windowWidth: props.windowWidth,
    uid: props.uid,
    //Key Object instance attributes
    contentDate: props.contentDate,
    contentTime: props.contentTime,
    codeMeaning: props.codeMeaning,
    numberOfReferencedInstances: props.numberOfReferencedInstances,
    referencedSeriesUID: props.referencedSeries,
    referencedSeries: null,
    referencedInstances: []
    ////
  }
  initInstance(instance);
  return instance;
}


/**
 * Create an Instance
 * @memberof factory
 * @param {object} props input properties
 * @returns {PixelSpacing} PixelSpacing object
 */
factory.PixelSpacing = function (props) {
  const pixelSpacing = {
    columnSpacing: props.columnSpacing,
    rowSpacing: props.rowSpacing
  }
  return pixelSpacing;
}

export default factory;