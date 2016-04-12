
// Create a socket
// var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;

var numNewMessagesTotal = 0;
var preSelectRoomName = '${roomName}';
// Hash room parameter takes precedence over server side param
/*
 if (window.location.hash) {
 preSelectRoomName = window.location.hash.substring(1, window.location.hash.length);
 }
 */
var hasFocus = true;
var rooms = [];
var stayDownObjs = [];
var lastMessageUsernameForRoom = [];
var currentSelectedRoomName;
var socket;
var firstTimeRoomList = true;

var getIndexForRoom = function (roomName) {
  for (var i = 0; i < rooms.length; i++) {
    var roomObj = rooms[i];
    if (roomName.toLowerCase() === roomObj.name.toLowerCase()) {
      return i;
    }
  }
  return -1;
};

var getRoomForName = function (roomName) {
  return rooms[getIndexForRoom(roomName)];
};

var getCurrentSelectedRoomObject = function() {
  return getRoomForName(currentSelectedRoomName);
};

var getStaydownForRoom = function (roomName) {
  return stayDownObjs[getIndexForRoom(roomName)];
};

var getLastMessageUsernameForRoom = function (roomName) {
  return lastMessageUsernameForRoom[getIndexForRoom(roomName)];
};

var setLastMessageUsernameForRoom = function (roomName, username) {
  lastMessageUsernameForRoom[getIndexForRoom(roomName)] = username;
};


var makeMessage = function (roomName, message) {
  var msg = {};
  msg.roomName = roomName;
  msg.message = message;
  return msg;
};


function styleSelectedRoomInSideBar(roomName) {
  $('[id^=roomlist_]').removeClass('active');
  $('[id^=roomlist_]').find('.roomname').removeClass('font-bold');
  $(getRoomlistEntrySelector(roomName)).addClass('active');
  $(getRoomlistEntrySelector(roomName)).find('.roomname').addClass('font-bold');
}

var selectRoom = function (roomName) {
//        roomName = roomName.toLowerCase();
  console.log("Select room: " + roomName);
//        window.location.hash = roomName;
  window.history.replaceState({}, 'Breaker: ', '/c/' + roomName);

  // Hide all thread views, then show selected message thread
  $('[id^=threadparent_]').hide();
  $('#'+getThreadParentId(roomName)).show();

  // Hide all user lists, then show user list for selected room
  $('[id^=userlistparent_]').hide();
  $('#'+getUserListParentId(roomName)).show();

  styleSelectedRoomInSideBar(roomName);

  // Change title
  // $('#room-link').text('#' + roomName);
  // $('#room-link').attr('href', 'https://reddit.com/r/'+roomName);
  $('#room-modmessage').text('[A message from the ' + roomName+ ' mods to you.]');

  var room = getRoomForName(roomName);
  // Do room info stuff
  if (room.banner) {
    $('#room-modmessage').text(room.banner);
  } else {
    $('#room-modmessage').text('');
  }
  // if (room.iconUrl) {
  //   $('#room-icon').attr('src', room.iconUrl);
  //   $('#room-icon').show();
  // } else {
  //   $('#room-icon').hide();
  // }
  
  
  if (room.isUserModerator) {
    $('#room-pref').attr('href', '@{RoomManage.roomPrefs}?roomName=' + room.name);
    $('#room-pref').removeClass('hidden');
  } else {
    $('#room-pref').addClass('hidden');
  }

  resetUnreadCountForRoom(roomName);
  getStaydownForRoom(roomName).checkdown();

  currentSelectedRoomName = roomName;

  markMessagesReadOnServer(roomName);
};

var isMessageNew = function(msgObj) {
  var roomObj = getRoomForName(msgObj.room.name);
  if (msgObj.createDateLongUTC > roomObj.lastSeenMessageTime) {
    return true;
  }
  return false;
};

var markMessagesReadOnServer = function (roomName) {
  $.getJSON('@{Application.markMessagesSeen()}?roomName=' + roomName, function (responseData) {
    console.log("Messages marked read.");
  });
};

function resetUnreadCountForRoom(roomName) {
  var roomObj = getRoomForName(roomName);
  if (roomObj) {
    roomObj.localUnreadCount = 0;
    setUnreadCountForRoom(roomName, roomObj.localUnreadCount);
  }
}

function incrementUnreadCountForRoom(roomName) {
  var roomObj = getRoomForName(roomName);
  if (roomObj) {
    roomObj.localUnreadCount++;
    setUnreadCountForRoom(roomName, roomObj.localUnreadCount);
  }
}

var setUnreadCountForRoom = function (roomName, count) {
  var countStr = count;
  if (count == 0) {
    countStr = "";
  }
  $(getRoomlistEntrySelector(roomName)).find('.unreadcount').text(countStr);
};

var setupRoomClicks = function () {
  $('.roomselect').click(function() {
    var clickRoomName = $(this).attr('data-roomname');
    console.log("Room click: " + clickRoomName);
    selectRoom(clickRoomName);
  });
};

var setupMessageInputHandlers = function() {
  $('.input-message').keypress(function (e) {
    if (e.charCode == 13 || e.keyCode == 13) {
//            $('#send').click()
      var messageElement = $(this);
      var message = messageElement.val();
      messageElement.val('');
      var messageObj = makeMessage(messageElement.attr('data-roomname'), message);
      socket.send(JSON.stringify(messageObj))
      getStaydownForRoom(currentSelectedRoomName).intend_down = true;

      e.preventDefault()
    }
  });
};

var requestMemberlist = function (roomName) {
  var messageObj = makeMessage(roomName, '##memberlist##');
  socket.send(JSON.stringify(messageObj));
};

// Ping socket
var startPing = function(roomName) {
  setInterval(function () {
    if (socket.readyState == 1) {
      // console.log("Sending ping.");
      var messageObj = makeMessage(roomName, "##ping##");
      socket.send(JSON.stringify(messageObj));
    } else {
      console.log("Can't ping, connection not open.");
    }
  }, 20000);
};

// Uncomment this block to test network disconnect/reconnect
/*
 setInterval(function () {
 console.log("Cycling connection...");
 socket.refresh();
 }, 5000);
 */

Messenger.options = {
  extraClasses: 'messenger-fixed messenger-on-bottom messenger-on-right',
  theme: 'flat'
};

function getUserEntrySelector(roomName, username) {
  roomName = roomName.toLowerCase();
  return '#userlist_' + roomName + '_' + username;
}

// Display a message
function getUserListSelector(roomName) {
  roomName = roomName.toLowerCase();
  return '#userlistparent_' + roomName + ' #userlist';
}

function getModListSelector(roomName) {
  roomName = roomName.toLowerCase();
  return '#userlistparent_' + roomName + ' #modlist';
}

function getOnlineListSelector(roomName) {
  roomName = roomName.toLowerCase();
  return '#userlistparent_' + roomName + ' #onlinelist';
}

function getThreadSelector(roomName) {
  roomName = roomName.toLowerCase();
  return '#threadparent_' + roomName + ' #thread';
}

function getUserListParentId(roomName) {
  roomName = roomName.toLowerCase();
  return 'userlistparent_' + roomName;
}

function getThreadParentId(roomName) {
  roomName = roomName.toLowerCase();
  return 'threadparent_' + roomName;
}

function getRoomlistEntrySelector(roomName) {
  roomName = roomName.toLowerCase();
  return '#roomlist_' + roomName;
}

function getMessageSelector(msgUuid) {
  return '#message_' + msgUuid;
}

function doesMessageElementExist(uuid) {
  return $(getMessageSelector(uuid)).length > 0;
}

var display = function (event) {
//        console.log("Received event: " + event.type);
  if (event.type === 'roomlist') {
    $('#threadparent').hide();
    $('#userlistparent').hide();

    var arrayLength = event.rooms.length;
    for (var i = 0; i < arrayLength; i++) {
      var roomObj = event.rooms[i];

      var roomElement = tmpl('roomentry_tmpl', {roomObj: roomObj});
      var existingElements = $(getRoomlistEntrySelector(roomObj.name));
      if (existingElements.length > 0) {
//                console.log("Existing message, replacing with new render.");
        existingElements.first().replaceWith(roomElement);
      } else {
//                console.log("New message, appending.");
        $('#roomlist').append(roomElement);

        // Clone and configure thread for this room
        var parentClone = $('#threadparent').first().clone().appendTo('#centercol');
        $(parentClone).find('.input-message')
            .attr('placeholder', 'Type a message to ' + roomObj.name + '...')
            .attr('data-roomname', roomObj.name);
        parentClone.attr('id', getThreadParentId(roomObj.name));

        // Make staydown scroll handler for room
        var staydown = new StayDown({
          target: $('#'+getThreadParentId(roomObj.name)+ ' #thread_scrollparent')[0],
          interval: 500
        });
        stayDownObjs.push(staydown);

        lastMessageUsernameForRoom.push('');

        var linkTitles = [];
        var linkHrefs = [];

        function receiveMessage(event) {
          // do something with event.data;
          console.log("Received window event: " + event+" from origin "+event.origin);
          var pageData = event.data;
          if (pageData && pageData.titles) {
            console.log("Has titles: " + pageData.titles.length);
            console.log("Has links: " + pageData.links.length);
            linkTitles = pageData.titles;
            linkHrefs = pageData.links;
          }
        }

        window.addEventListener("message", receiveMessage, false);

        // Clone and configure user list for this room
        var userlistParentClone = $('#userlistparent').first().clone().appendTo('#rightcol');
        userlistParentClone.attr('id', getUserListParentId(roomObj.name));

        var setupMentions = function() {
          var thisRoomObj = roomObj;

          // Link mentions
          $(parentClone).find('textarea.mention').mentionsInput({
            triggerChar: ['@', '#'],
            onDataRequest:function (mode, query, callback, triggerChar) {

              console.log("Mention for trigger " + triggerChar+": "+query);

              if (triggerChar == '#') {
                var respObjs = [];

                for (var i = 0; i < linkTitles.length; i++) {
                  var title = linkTitles[i];
                  if (title.toLowerCase().indexOf(query.toLowerCase()) == 0) {
                    var respUserObj = {
                      id: linkHrefs[i],
                      name: title+' | '+linkHrefs[i].trim(),
                      avatar: '',
                      type: 'link'
                    };
                    respObjs.push(respUserObj);
                  }
                }

                window.parent.postMessage({titleQuery: query}, '*');

                callback.call(this, respObjs);

              } else if (triggerChar == '@') {
                $.getJSON('@{Application.userSearch()}?roomName='+thisRoomObj.name+'&query='+query, function(responseData) {
                  var responseObj = responseData;
                  var responseObjs = [];

                  function addUsersFromResponse(userArray) {
                    for (var i = 0; i < userArray.length; i++) {
                      var userObj = userArray[i];
                      var respUserObj = {
                        id: userObj.id,
                        name: '@' + userObj.username,
                        avatar: userObj.profileImageUrl,
                        type: 'contact'
                      };
                      responseObjs.push(respUserObj);
                    }
                  }

                  addUsersFromResponse(responseObj.onlineUsers);
                  addUsersFromResponse(responseObj.offlineUsers);
//                                responseData = _.filter(responseData, function(item) { return item.name.toLowerCase().indexOf(query.toLowerCase()) > -1 });
                  callback.call(this, responseObjs);
                });

              }

            },
            cancelled: function() {
              console.log("Autocomplete cancelled.");
              window.parent.postMessage({titleQueryCancelled: true}, '*');
            },
            itemSelected: function(item) {
              console.log("Item selected :"+item);
              window.parent.postMessage({titleQueryCancelled: true}, '*');
            }
          });

          // User mentions
          /*
           $(parentClone).find('textarea.mention').mentionsInput({
           onDataRequest:function (mode, query, callback) {

           $.getJSON('@{Application.userSearch()}?roomName='+thisRoomObj.name+'&query='+query, function(responseData) {
           var responseObj = responseData;
           var responseObjs = [];

           function addUsersFromResponse(userArray) {
           for (var i = 0; i < userArray.length; i++) {
           var userObj = userArray[i];
           var respUserObj = {
           id: userObj.id,
           name: '@' + userObj.username,
           avatar: userObj.profileImageUrl,
           type: 'contact'
           };
           responseObjs.push(respUserObj);
           }
           }

           addUsersFromResponse(responseObj.onlineUsers);
           addUsersFromResponse(responseObj.offlineUsers);
           //                                responseData = _.filter(responseData, function(item) { return item.name.toLowerCase().indexOf(query.toLowerCase()) > -1 });
           callback.call(this, responseObjs);
           });

           }
           });
           */

        };
        setupMentions();

        startPing(roomObj.name);
      }

      requestMemberlist(roomObj.name);
    }

    if (firstTimeRoomList) {
      rooms = event.rooms;
      for (var i = 0; i < rooms.length; i++) {
        rooms[i].localUnreadCount = 0;
      }
      if (preSelectRoomName) {
        selectRoom(preSelectRoomName);
      } else {
        selectRoom(rooms[0].name);
      }

      firstTimeRoomList = false;
    }

    styleSelectedRoomInSideBar(currentSelectedRoomName);
    setupRoomClicks();
    setupMessageInputHandlers();
  } else if (event.type === 'message') {
    console.log("Message uuid : " + event.message.uuid);
    var msgTemplateId = 'message_tmpl';
    if (getLastMessageUsernameForRoom(event.room.name) === event.user.username) {
      msgTemplateId = 'messageshort_tmpl';
    }
    var msg = tmpl(msgTemplateId, {event: event});

    var messageIsNew = isMessageNew(event.message);

    if (doesMessageElementExist(event.message.uuid)) {
//                console.log("Existing message, replacing with new render.");
      var msgObjects = $(getMessageSelector(event.message.uuid));
      msgObjects.first().replaceWith(msg);
    } else {
      console.log("New message, appending.");

      var roomName = event.room.name;

      // If not a bot message, update msg counts
      if (!event.user.bot) {
        if (!hasFocus && messageIsNew) {
          numNewMessagesTotal++;
          updateTitle();

          incrementUnreadCountForRoom(roomName);
        } else if (roomName != currentSelectedRoomName && messageIsNew) {
          incrementUnreadCountForRoom(roomName);
        }
      }

      $(getThreadSelector(event.room.name)).append(msg);
    }

    setLastMessageUsernameForRoom(event.room.name, event.user.username);

    jQuery(getMessageSelector(event.message.uuid)+' .timeago').timeago();

    if (messageIsNew) {
      markMessagesReadOnServer(event.room.name);
    }

  } else if (event.type === 'servermessage') {
    var msgTemplateId = 'servermessage_tmpl';
    var msg = tmpl(msgTemplateId, {event: event});
    $(getThreadSelector(event.room.name)).append(msg);
  } else if (event.type === 'join') {
    updateUserInUserList(event.user, event.room);
  } else if (event.type === 'leave') {
    updateUserInUserList(event.user, event.room);
//            $(getUserEntrySelector(event.room.name, event.user.username)).remove();
  } else if (event.type === 'memberlist') {
    console.log("Member list for " + event.room.name);
    var arrayLength = event.users.length;
    for (var i = 0; i < arrayLength; i++) {
      var userObj = event.users[i];
      var username = userObj.username;

      updateUserInUserList(userObj, event.room);
    }
  }

//        $('#thread').scrollTo('max')
};

var updateUserInUserList = function (userObj, roomObj) {
  var userHtml = tmpl('userentry_tmpl', {roomName: roomObj.name, user: userObj});
  $(getUserEntrySelector(roomObj.name, userObj.username)).remove();

  if (userObj.modForRoom) {
    insertUserInCorrectPlace(getModListSelector(roomObj.name), userHtml);
  } else if (userObj.online) {
    insertUserInCorrectPlace(getOnlineListSelector(roomObj.name), userHtml);
  } else {
    insertUserInCorrectPlace(getUserListSelector(roomObj.name), userHtml);
  }

};

var insertUserInCorrectPlace = function (userListSelector, userToInsertHtml) {
  var userEntryId = $(userToInsertHtml).attr('id');
  var children = $(userListSelector).children();
  var userToInsertBefore;
  for (var i=0; i<children.length; i++) {
    var child = $(children.get(i));
    if (userEntryId.toLowerCase() < child.attr('id').toLowerCase()) {
      userToInsertBefore = child;
      break;
    }

  }
  if (userToInsertBefore) {
    console.log("sort; Inserting " + userEntryId + " before " + userToInsertBefore.attr('id')+" in list "+userListSelector);
    userToInsertBefore.before(userToInsertHtml);
  } else {
    console.log("sort; Appending " + userEntryId + " in list "+userListSelector);
    $(userListSelector).append(userToInsertHtml);
  }

};

var previewLoaded = function (e) {
  // Makes the UI feel a little more responsive to trigger a check right when preview loads
  getStaydownForRoom(currentSelectedRoomName).checkdown();
};

var updateTitle = function () {
  var title = 'breaker';
  if (numNewMessagesTotal > 0) {
    title = '(' + numNewMessagesTotal + ') ' + title;
  }
  document.title = title;
};
updateTitle();

var firstConnect = true;

// Stuff to do with window focus and unread messages

var onBlur = function onBlur() {
  hasFocus = false;
};
var onFocus = function onFocus() {
  hasFocus = true;
  numNewMessagesTotal = 0;
  updateTitle();
  resetUnreadCountForRoom(currentSelectedRoomName);
};

if (/*@cc_on!@*/false) { // check for Internet Explorer
  document.onfocusin = onFocus;
  document.onfocusout = onBlur;
} else {
  window.onfocus = onFocus;
  window.onblur = onBlur;
}
