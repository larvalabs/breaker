import ReactDOM from "react-dom";
import React from "react";
import App from "./js/app/App.jsx";
import startSocket from './js/socket.js';

startSocket(WebsocketUrl);

ReactDOM.render(
    <App />,
    document.getElementById('root')
);
