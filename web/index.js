import ReactDOM from "react-dom";
import React from "react";
import App from "./app/App";
import Config from './config';
import startSocket from './socket';


startSocket(Config.websocket_url);

ReactDOM.render(
    <App />,
    document.getElementById('root')
);
