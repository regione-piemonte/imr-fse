import xml2js from 'xml2js';
import { isArray, isArrayFill } from '../utils';
import { apiClient } from './apiClient';
import factory from '../types/factory';

/**
 * @namespace api
 */
const api = {
  serviceUrl: '/o3-dpacs-wado',
  retrieveAeTitle: '',
};

/**
 * Decode xml api response data to json format
 * @private
 * @param {string} xml
 * @retrun {json} json decoded object
 */
function xmlToJSon(xml) {
  let res = { patients: [] };
  xml2js.parseString(xml, (err, data) => {
    if (err) {
      return res;
    }
    const pats = data?.results?.patient;
    if (isArray(pats)) {
      // read patients
      pats.forEach(pat => {
        const patient = factory.Patient(pat.$);
        // read studies
        if (isArray(pat.study)) {
          pat.study.forEach(sty => {
            const study = factory.Study(sty.$);
            // read series
            if (isArray(sty.series)) {
              sty.series.forEach(ser => {
                const series = factory.Series(ser.$);
                // read instances
                if (isArray(ser.instance)) {
                  ser.instance.forEach(inst => {
                    const instance = factory.Instance(inst.$);
                    if (isArrayFill(inst.pixelSpacing)) {
                      instance.pixelSpacing = factory.PixelSpacing(inst.pixelSpacing[0].$);
                    }
                    if (isArrayFill(inst.imagerPixelSpacing)) {
                      instance.imagerPixelSpacing = factory.PixelSpacing(inst.imagerPixelSpacing[0].$);
                    }
                    // read referenced instances
                    if(isArrayFill(inst.referencedInstance)) {
                      inst.referencedInstance.forEach(ref => {
                        const referencedInstance = factory.Instance(ref.$);
                        if (isArrayFill(ref.pixelSpacing)) {
                          referencedInstance.pixelSpacing = factory.PixelSpacing(ref.pixelSpacing[0].$);
                        }
                        if (isArrayFill(ref.imagerPixelSpacing)) {
                          referencedInstance.imagerPixelSpacing = factory.PixelSpacing(ref.imagerPixelSpacing[0].$);
                        }
                        instance.referencedInstances.push(referencedInstance);
                      });
                    }
                    series.instances.push(instance);
                  });
                }
                study.series.push(series);
              });
            }
            patient.studies.push(study);
          })
        }
        res.patients.push(patient);
      });
    }
  });
  return res;
}

/**
 * get patient data
 * @param {obect} props 
 * @param {string} [props.patientID] patient identifier
 * @param {string} [props.idIssuer] isssuer of patientID
 * @param {string} [props.accessionNumber] accession number. Required if studyUID is not specified.
 * @param {string} [props.studyUID] study identifier. Required if accessionNumber is not specified.
 * @param {Boolean} [props.showOnlyThis=false] returns only values of this patient.
 * @return {Promise} Promise object represents the array of patients found
 * 
 * @example const patients = await getPatientInfo({patientID="123"});
 */
api.getPatientInfo = function(props) {
  const showOnlyThis = props.showOnlyThis || false;

  const url = `${this.serviceUrl}/getPatientInfo`;
  const params = new URLSearchParams();
  if (props.patientID) {
    params.append("patientID", props.patientID);
    if (props.idIssuer) {
      params.append("idIssuer", props.idIssuer);
    }
    params.append("showOnlyThis", false);
  }
  else if (props.accessionNumber) {
    params.append("accessionNumber", props.accessionNumber);
    if (props.idIssuer) {
      params.append("idIssuer", props.idIssuer);
    }
    params.append("showOnlyThis", showOnlyThis);
  }
  else if (props.studyUID) {
    params.append("studyUID", props.studyUID);
    params.append("showOnlyThis", showOnlyThis);
  }

  return new Promise((resolve, reject) => {
    return apiClient.post(url, params.toString())
      .then(res => res.text())
      .then(res => xmlToJSon(res))
      .then(res => resolve(res))
      .catch((error) => {
        console.warn('getPatintInfo error', error);
        return reject(error);
      });
    });
}

/**
 * get series instance data
 * @param {object} props
 * @param {!string} props.studyUID study identifier
 * @param {!string} props.seriesUID series identifier
 * @return {Promise} Promise object represents the array of patients/studies/series/instances found
 */
api.getInstances = function(props) {
  const url = `${this.serviceUrl}/getInstances`;
  const params = new URLSearchParams();
  params.append("studyUID", props.studyUID);
  params.append("seriesUID", props.seriesUID);
  const retrieveAeTitle = props.retrieveAeTitle || this.retrieveAeTitle;
  if (retrieveAeTitle) {
    params.append("retrieveAeTitle", retrieveAeTitle);
  }

  return new Promise((resolve, reject) => {
    return apiClient.post(url, params.toString())
      .then(res => res.text())
      .then(res => xmlToJSon(res))
      .then(res => resolve(res))
      .catch((error) => {
        console.warn('getInstances error', error);
        return reject(error);
      });
  });
}

export default api;