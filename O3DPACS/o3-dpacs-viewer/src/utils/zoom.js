/**
 * Zoom modes as specified by DICOM Consistent Presentation of Images (CPI)
 * @enum  {string}
 * @memberof utils
 * @constant
 * @property {string} SCALE_TO_FIT Scale to fit inner viewport area
 * @property {string} MAGNIFY Free zoom value
 * @property {string} TRUE_SIZE Not used
 */
export const ZOOM_MODE = {
  SCALE_TO_FIT: "SCALE TO FIT",
  MAGNIFY: "MAGNIFY",
  TRUE_ISZE: "TRUE SIZE",
};

/**
 * Minimun allowed scale value
 * @memberof utils
 * @constant
 * @default
 */
export const MIN_SCALE = 0.125;
/**
 * Maximun allowed scale value
 * @memberof utils
 * @constant
 * @default
 */
export const MAX_SCALE = 8.0;