import {isArray} from '../utils/equals';

export function filterSeriesByModality(series) {
  const excludes = ["PR"];
  return series.filter(item => !excludes.includes(item.modality));
}

export function isImageSeries(series) {
  if (series) {
    const instances = series.instances;
    if (isArray(instances)) {
      const image = instances.find((instance) => instance.rows > 0);
      if (image) {
        return true;
      }
    }
  }
  return false;
}

export function getFirstImageSeries(series) {
  if (isArray(series)) {
    for (let i = 0; i < series.length; i++) {
      if (isImageSeries(series[i])) {
        return series[i];
      }
    }
  }
  return null;
}