import ReactDOM from "react-dom";
import React from "react";
import App from "./app/App";
import startSocket from './socket';

startSocket(WebsocketUrl);

ReactDOM.render(
    <App />,
    document.getElementById('root')
);
