/**
 * @fileoverview main file returned by HOME path
 * @author Allan Della Libera <allan.dellalibea@exprivia.com>
 * @copyright CSI Piemonte 2020
 */

// These must be the first lines in src/index.js
import './polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
// include global css before app specific
import './index.css';
import App from './App';
import api, {apiClient} from './api';
import * as serviceWorker from './serviceWorker';
import {removeTrailingSlash} from './utils/stringHelpers';

// decode input attribute
const props = {};
let credentials;

const el = window.frameElement;
if (el) {
  props.patientID = el.getAttribute("patientID");
  props.idIssuer = el.getAttribute("idIssuer");
  props.accessionNumber = el.getAttribute("accessionNumber");
  props.studyUID = el.getAttribute("studyUID");
  props.showOnlyThis = el.getAttribute("showOnlyThis");
  props.isNotO3DPACS = el.getAttribute("isNotO3DPACS");
  props.retrieveAeTitle = el.getAttribute("retrieveAeTitle");
  props.serviceUrl = removeTrailingSlash(el.getAttribute("serviceURL"));
  props.keyImagesOnly = el.getAttribute("keyImagesOnly");
  credentials = el.getAttribute("credentials");
} else {
  if (process.env.NODE_ENV === 'production') {
    throw new Error('Invalid frameElement');
  }
}

if (process.env.NODE_ENV === 'development') {
  const params = new URLSearchParams(window.location.search);
  params.forEach((value, key) => { 
    if (!props[key]) {
      props[key] = value;
    }
  });
  if (process.env.REACT_APP_CREDENTIALS) {
    credentials = process.env.REACT_APP_CREDENTIALS;
  }
}

// init api
if (credentials) {
  apiClient.authorization = `Basic ${credentials}`;
}
if (props.retrieveAeTitle) {
  api.retrieveAeTitle = props.retrieveAeTitle;
}
if (props.serviceUrl) {
  api.serviceUrl = props.serviceUrl;
}

// render application
ReactDOM.render(
  <React.StrictMode>
    <App {...props} />
  </React.StrictMode>,
  document.getElementById('html-viewer')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
