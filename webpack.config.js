module.exports = {
  entry: "./web/index.js",
  output: {
    filename: "./public/dist/bundle.js"
  },
  watch:true,
  module: {
    loaders: [
      {
        test: /\.jsx*$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
        query: {
          presets: ['react', 'es2015']
        }
      }
    ]
  },
  resolve: {
    extensions: ['', '.js', '.es6']
  },
};
