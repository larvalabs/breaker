let Config = {
  features: {
    suggestedRooms: false
  },
  environment: {
    prod: window.__ENVIRONMENT__ == "production"
  },
  settings: {
    ping_timeout: 20000,
    default_banner: "Message from the moderators to you, the user.",
    dev_tools: window.__USER_ADMIN__ || window.__ENVIRONMENT__ !== "production"
  },
  websocket_url: window.__WEBSOCKET_URL__,
  room_name: window.__ROOM_NAME__
};

export default Config;
