/**
 * @typedef DateString
 * @type {string}
 * @description String representation of date in the form "YYYY-MM-DD"
 */

/**
 * @typedef TimeString
 * @type {string}
 * @description String representation of time in the form "HH:mm:ss"
 */

/**
 * @typedef DateTimeString
 * @type {string}
 * @description String representation of DateTime in the form "YYYY-MM-DD HH:mm:ss"
 */

/**
 * @typedef Patient
 * @type {object}
 * @description Patient's data
 * @property {string} patientID Primary identifier for the Patient.
 * @property {string} [firstName] Patient's fisrt name
 * @property {string} [middleName] Patient's middle name
 * @property {string} [lastName] Patient's last name
 * @property {DateString} [birthDate] Patient's birth date
 * @property {Number} [numberOfStudies] Number of Patient's related studies
 * @property {Study[]} studies Patient's related studies
 */

/**
 * @typedef Study
 * @type {object}
 * @description Study data
 * @property {string} uid Study Instance UID
 * @property {DateString} [date] Study date
 * @property {TimeString} [time] Study time
 * @property {string} [description] Study description
 * @property {string} [studyStatus] Status of study
 * @property {Number} [numberOfSeries] Number of Study related series
 * @property {Series[]} series Study related series
 */

/**
 * @typedef Series
 * @type {object}
 * @description Series data
 * @property {string} uid Series Instance UID
 * @property {string} modality Series modality
 * @property {string} [description] Series description
 * @property {string} [thumbnail] Url of representative thumbnalil of the series
 * @property {Number} [numberOfInstances] Number of Series related instances
 * @property {Instance[]} instances Series related instances
 */

/**
 * @typedef Instance
 * @type {object}
 * @description Instance data
 * @property {string} uid Instance UID
 * @property {string} [number] A number that identifies this image
 * @property {string} wadourl Http url to retrieve instance with DICOM-URI retrieve.
 * (@See DICOM standard PS-3.18).
 * @property {string} [numberOfFrames] number of image fames.
 * Required if instance is multiframe (numberOfFrames > 1)
 * @property {string} [windowCenter] Window center for display.
 * Required if windowWidth is present.
 * @property {string} [windowWidth] Window width for display.
 * Required if windowCenter is present.
 * @property {string} [rows] Number of image columns.
 * Required if rows is present.
 * @property {string} [rows] Number of image rows.
 * Required if columns is present.
 * @property {string} [imagePositionPatientX] X coordinate of the upper left hand corner 
 * of the image; it is the center of the first voxel transmitted.
 * @See DICOM PS-3 Section C.7.6.2.1.1 Image Position and Image Orientation.
 * @property {string} [imagePositionPatientY] Y coordinate of the upper left hand corner 
 * of the image; it is the center of the first voxel transmitted.
 * @property {string} [imagePositionPatientZ] Z coordinate of the upper left hand corner 
 * of the image; it is the center of the first voxel transmitted.
 * @property {string} [photometricInterpretation] Specifies the intended interpretation 
 * of the pixel data. @See DICOM PS-3 Section C.7.6.3.1.2 for further explanation.
 * @property {string} [bitPerPixel] Number of bits stored for each pixel sample.
 * Required if instance has a renderable pixel data
 * @property {string} [rescaleUntercent] The value b in relationship between stored values 
 * (SV) and the output units. Output units = m*SV+b.
 * @property {string} [rescaleSlope] m in the equation specified in Rescale Intercept.
 * Required if rescaleIntercept is present.
 * @property {string} [transferSyntax] Transfer syntax of stored image data.
 * @property {string} [imageQualty] Lossy factor of redendered image data
 * Trquired if instance is an image
 * @property {string} imageType Mime type of rendered image data. 
 * @property {PixelSpacing} [pixelSpacing] Physical distance between the center of every pixel.
 */

/**
 * @typedef PixelSpacing
 * @type {object}
 * @description Image PixelSpacing data
 * @property {string} columnSpacing horizontal spacing between center of pixels in millimeters
 * @property {string} rowSpacing vertical spacing between center of pixels in millimeters
 */

/**
 * Callback to sidebar study item click event.
 * @callback studyClickEvent
 * @param {Study|Series|Image} data item data object 
 */

export * from './factory';
export * from './mime';