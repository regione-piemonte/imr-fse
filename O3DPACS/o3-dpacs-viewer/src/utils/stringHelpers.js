
/**
 * Camelize a string, cutting the string by multiple separators like
 * hyphens, underscores and spaces.
 * @memberof utils
 * @param {string} text Text to camelize
 * @returns {string} Camelized text
 */
export function stringCamelize(text) {
  return text
    .match(/[a-z]+/gi)
    .map((word) => word.charAt(0).toUpperCase() + word.substr(1))
    .join("");
}

/**
 * Converts a string to a boolean value.
 * hyphens, underscores and spaces.
 * @memberof utils
 * @param {string} strinh input value
 * @returns {Boolean} Boolean value if successful else undefined.
 */
export function stringToBool(string) {
  try {
    return JSON.parse(`${string}`);
  } catch {
    return undefined;
  }
}

/**
 * Remove all trailing path components from a url string
 * @param {string} url input url
 * @returns {string}
 */
export function removeTrailingSlash(url) {
  if (typeof url === 'string') {
    const ret = url;
    return ret.replace(/\/+$/, '');
  }
  return undefined;
}