/**
 * Checks when an element is visible intoa scrollable area of a container
 * @param {HTMLElement} element search element.
 * @param {HTMLElement} container parent container element.
 * @returns {Boolean} true if visible else false. 
 */
export default function isElementVisible(element, container) {
  const elementRect = element.getBoundingClientRect();
  const containerRect = container.getBoundingClientRect();
  if (elementRect.top <= containerRect.top) {
    return ((containerRect.top - elementRect.top) <= elementRect.height/2);
  }
  return ((elementRect.bottom - containerRect.bottom) <= elementRect.height/2);
}