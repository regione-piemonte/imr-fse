export function isFunction(value) {
  return (typeof(value) === "function") || (value instanceof Function);
}

export function isString(value) {
  return (typeof(value) === "string") || (value instanceof String); 
}

export function isArray(value) {
  return Array.isArray(value);
}

export function isArrayEmpty(value) {
  return Array.isArray(value) && value.length <= 0;
}

export function isArrayFill(value) {
  return Array.isArray(value) && value.length > 0;
}