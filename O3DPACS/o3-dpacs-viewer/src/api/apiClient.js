/**
 * Performs HTTP api requests.
 * @namespace apiClient
 */
const apiClient = {
  /** @private */
  authorization: "",
};

function checkStatus(response) {
  if (!response.ok) {
    throw new Error(`[${response.status}] ${response.statusText}`);
  }
  return response;
}

/**
 * Performs http GET request
 * @apiClient
 * @param {!string} url
 * @param {object} [options] request options 
 */
apiClient.get = function(url, options = {}) {
  return fetch(url, {
    method: "GET",
    headers: {
      Authorization: this.authorization,
    },
    ...options
  }).then(res => checkStatus(res));
}

/**
 * Performs http POST request
 * @param {!string} url url
 * @param {object} data 
 * @param {object} [options] request options
 */
apiClient.post = function(url, data, options = {}) {
  return fetch(url, {
    method: "POST",
    headers: {
      Authorization: this.authorization,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: data,
    ...options
  }).then(res => checkStatus(res));
}

export { apiClient };

