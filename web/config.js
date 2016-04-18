let Config = {
  features: {
    suggestedRooms: false
  },
  environment: {
    prod: window.__ENVIRONMENT__ == "production"
  },
  websocket_url: window.__WEBSOCKET_URL__,
  room_name: window.__ROOM_NAME__
};

export default Config;
