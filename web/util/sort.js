const sorts = {
  usersAlphabetical(a, b) {
    if (a.get('username').toLowerCase() < b.get('username').toLowerCase()) {
      return -1;
    } else if (b.get('username').toLowerCase() < a.get('username').toLowerCase()) {
      return 1;
    }
    return 0;
  }
};

export default sorts;
