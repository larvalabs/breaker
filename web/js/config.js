let Config = {
  features: {
    suggestedRooms: false
  },
  environment: {
    prod: window.__ENVIRONMENT__ == "production"
  }
};

export default Config;
