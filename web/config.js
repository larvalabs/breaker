let Config = {
  guest: window.__USER_GUEST__ === "true",
  admin: window.__USER_ADMIN__ === "true",
  features: {
    suggestedRooms: false,
    useFlairStyle: (roomName) => {
      // We can switch here based on subreddit
      return false;
    }
  },
  environment: {
    prod: window.__ENVIRONMENT__ == "production"
  },
  settings: {
    ping_timeout: 20000,
    default_banner: "Message from the moderators to you, the user.",
    dev_tools: window.__USER_ADMIN__ || window.__ENVIRONMENT__ !== "production",
    flairScaleForRoom: (roomName) => {
      if(["newyorkislanders"].includes(roomName)){
        return "flair-scale-half"
      }
      
      return null
    }
  },
  websocket_url: window.__WEBSOCKET_URL__,
  room_name: window.__ROOM_NAME__
};

export default Config;
