const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    "/api",
    createProxyMiddleware({
      target: "http://192.168.24.65:7080",
      logLevel: 'debug',
      pathRewrite: {
        "^/api": "/"
      },
    })
  );
  app.use(
    "/o3-dpacs-wado",
    createProxyMiddleware({
      target: "http://192.168.24.65:7080",
      logLevel: 'debug',
      pathRewrite: {
        "^/o3-dpacs-wado": "/o3-dpacs-wado"
      },
    })
  );
};