// Avoid recursive frame insertion...
var extensionOrigin = 'chrome-extension://' + chrome.runtime.id;
var urlPath = window.location.pathname;

if (!location.ancestorOrigins.contains(extensionOrigin) && urlPath.indexOf("/api/v1/") == -1) {

    var width = 400;
    var hidden = false;
    chrome.storage.local.get("hidden", function(result) {
       if (result.hidden) {
           console.log("Hidden is true in local storage.");
           hidden = true;
           redrawSidebar();
       }
    });
    var hideButtonSize = 32;

    var subreddit;
    if (urlPath.indexOf('/r/') > -1) {
        var subredditWithPossibleJunk = urlPath.substring(urlPath.indexOf('/r/')+3, urlPath.length);
        if (subredditWithPossibleJunk.indexOf("/") > -1) {
            subredditWithPossibleJunk = subredditWithPossibleJunk.substring(0, subredditWithPossibleJunk.indexOf('/', 0));
        }
        subreddit = subredditWithPossibleJunk;
        console.log("subreddit: " + subreddit);
    }

    if (!subreddit) {
        subreddit = 'breakerapp';
    }

    // var iframeLocation = 'https://127.0.0.1:9443/c/'+subreddit;
    var iframeLocation = 'https://www.breakerapp.com/c/'+subreddit;

    var iframe = document.createElement('iframe');
    // Must be declared at web_accessible_resources in manifest.json
    iframe.src = chrome.runtime.getURL('frame.html');

    var hideButton = document.createElement('img');
    $(hideButton).css("position", "absolute");
    $(hideButton).css("width", hideButtonSize);
    $(hideButton).css("height", hideButtonSize);
    $(hideButton).css("top", 16);
    $(hideButton).css("z-index", 1001);
    $(hideButton).css("cursor", "pointer");
    $(hideButton).click(function() {
        hidden = !hidden;
        chrome.storage.local.set({"hidden":hidden});
        redrawSidebar();
    });

    console.log("Setting iframe src to " + iframeLocation);
    iframe.src = iframeLocation;

    var originalContentMarginRight = -1;

    var redrawSidebar = function() {
        var left = $(window).width() - width;
        console.log("Window width left: " + $(window).width());
        console.log("Left: " + left);

        // Some styles for a fancy sidebar
        iframe.style.cssText = 'position:fixed;top:0;display:block;' +
          'height:100%;z-index:1000;';

        if (originalContentMarginRight == -1) {
            originalContentMarginRight = $('div.content').css('margin-right');
        }

        if (hidden) {
            console.log("Redrawing sidebar as hidden");
            $(iframe).css('left', $(window).width());
            $(iframe).css('width', width);
            $("body").css("margin-right", 0);

            $('div.side').show();
            $('div.content').css('margin-right', originalContentMarginRight);

            $(hideButton).css("left", $(window).width() - hideButtonSize);
            hideButton.src = chrome.extension.getURL("breaker_icon_dark.png");
            //hideButton.src = chrome.extension.getURL("arrow_left.png");
        } else {
            console.log("Redrawing sidebar as open");
            $(iframe).css('left', left);
            $(iframe).css('width', width);
            $("body").css("margin-right", width);

            $('div.side').hide();
            $('div.content').css('margin-right', $('div.content').css('margin-left'));

            $(hideButton).css("left", left + 10);
            hideButton.src = chrome.extension.getURL("empty.png");  // let the site provided breaker icon be the click target
            //hideButton.src = chrome.extension.getURL("arrow_right.png");
        }
    };

    $(window).resize(redrawSidebar);


/*
    if (subreddit) {
        iframeLocation += '/' + subreddit;
    }
*/


    console.log("Content: " + $("content"));
    //$("body").css("margin-right", width);
    var style = $("<style>body {margin-right: " + width + "px;}</style>");

    var target = document.querySelector('html');
    var observer = new MutationObserver(function(mutations) {

        mutations.forEach(function(mutation) {
            console.log("Mutation: " + mutation.type);
            for (var i = 0; i<mutation.addedNodes.length; i++) {
                var item = mutation.addedNodes.item(i);
                console.log("Added: " + item);
                if (item.tagName.toLowerCase() == 'body') {
                    console.log("Adding css width.");
                    if (!hidden) {
                        $(item).css("margin-right", width);
                    }
                    observer.disconnect();
                }
            }
        });
    });

    var config = {
        attributes: true,
        childList: true,
        characterData: false
    };

    observer.observe(target, config);
    //$(document).append(style);

    //console.log("Starting ready...");

    $(document).ready(function() {
        //console.log("Is ready.");

        document.body.appendChild(iframe);

        document.body.appendChild(hideButton);

        // Find link titles
        //console.log("Titles:");
        var titles = [];
        var links = [];
        $('a.title').each(function(index, el) {
            //console.log("Title: "+$(this).text());
            titles.push($(this).text());
            links.push($(this).attr('href'));
        });

        var pageObj = {
            titles: titles,
            links: links
        };

        window.addEventListener("message", function(event) {
            //console.log("Window msg: " + event);
            if (event.data.titleQuery) {
                console.log("Title query: " + event.data.titleQuery);
                $('a.title').each(function(index, el) {
                    //console.log("Title: "+$(this).text());
                    var titleQuery = event.data.titleQuery.toLowerCase();
                    var title = $(this).text();
                    if (titleQuery.length > 0 && title.toLowerCase().substring(0, titleQuery.length) === titleQuery) {
                        console.log("Title matches: " + title);
                        $(this).css("background-color", "yellow");
                    } else {
                        $(this).css("background-color", "transparent");
                    }
                });
            } else if (event.data.titleQueryCancelled) {
                $('a.title').css("background-color", "transparent");
            }
        }, false);

        $(iframe).ready(function() {
            //window.postMessage('foo','*');
            iframe.contentWindow.postMessage(pageObj, '*');

            redrawSidebar();
        });
        //document.getElementById('breakerframe').contentWindow.articleTitles = titles;
        // todo Figure out an event that means the iframe is loaded
/*
        setTimeout(function() {

        }, 2000);
*/

        redrawSidebar();
    });

}
