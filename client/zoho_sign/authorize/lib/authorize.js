var osyncUrl="https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";
function serviceAuths(buttonName) {
  
  var buttonId = buttonName; 
  var authUrl = $("#" + buttonId).attr("url");
  var authType = $("#" + buttonId).attr("auth_type");
  if (authType === "apikey") {
    $("#" + buttonId + "LoadingDiv").hide();
    $("#apiKeyModal").modal('show');
    $("#apiKeyModal").attr("serviceId", $("#" + buttonId).attr("name"));
    $("#apiKeyModal").attr("isLeft", $("#" + buttonId).data("isleft"));
  } else {
    var windowName = buttonName;
    var btnText = $("#" + buttonId).text();
    $("#" + buttonId + "LoadingDiv").show();
    var openwindow= openNewWindow(windowName, $("#" + buttonId).attr("name"), authUrl, buttonId, authType); 
  }
}

function showAuthButton(serviceId) {
  var buttonHtmlObj = $("#authorizeTable [name=\'" + serviceId + "\']");
  buttonHtmlObj.removeClass("spinner-grow spinner-grow-sm");
  buttonHtmlObj.removeAttr("onclick");
  buttonHtmlObj.text('Authorized Successfully').removeClass("btn btn-outline-primary").addClass("text-success");
}

function openNewWindow(windowName, serviceName, authURL, buttonName, authType,buttonId) {
  var winSize = 'height=620,width=600,top=200,left=300,resizable';
  windowName = window.open(authURL, serviceName, winSize);
  
  $("#buttonName")
  if (window.focus) { windowName.focus(); }  
}

function showAuthPageHS() {
  $("#enableFailed").hide();
  $("#enableLoadingDiv").show();
  const urlParams = new URLSearchParams(window.location.search);
  const portalId = urlParams.get("portalId");
  const fName = urlParams.get("firstName");
  const emailId = urlParams.get("email");
  var userdata={
    "left_service_id": "764ead9d-ef56-4840-8ea2-ab1059afdd53",  //HS
    "right_service_id": "a96b29fb-3da1-43d6-8f18-7984f3cfd6dd", // ZS
    "companyId": getRandomId(),// portalId,
    "name": "OAppS",//fName,
    "email": "help@oapps.com",// emailId,
    "planName": "standard"
  }  
  var url = osyncUrl + '/api/v1/integrate'
  $.ajax({
    url: url,
    type: "POST",
    crossDomain: true,
    datatype: 'json',
    data:JSON.stringify(userdata),
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    success: function (response, textStatus, jqXHR) {
      var osyncId=response.osync_id;
      var integId=response.integId;
      var hash=response.hash;
      var authPage = $("#list").html();
      var text = Mustache.render(authPage, response);
      $("#list_R").html(text).show();
      $("#enableLoadingDiv").hide();;;
      $("#enableDiv").hide();
      $("#authorizeDiv").show();
    },
    error: function (jqXHR, textStatus, errorThrown) {
      $("#enableLoadingDiv").hide();
      $("#enableFailed").show();
    }
  });
}

function completeInstallation(thisObj){
  $("#installationFailed").hide();
  $("#completeInstallationLoadingDiv").show();
  var osyncId= $(thisObj).attr("osyncId");
  var integId= $(thisObj).attr("integId");
  var hash= $(thisObj).attr("hash");
  var url = osyncUrl + '/api/v1/hubspot/portalid/update?integId=' + integId;
  $.ajax({
    url: url,
    type: "POST",
    crossDomain: true,
    datatype: 'json',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Osync-Authorization': hash
    },
    success: function (response, textStatus, jqXHR) {
      $("#completeInstallationLoadingDiv").hide();
      $("#completeInstallation").hide();
      $("#installationCompletedSuccessfully").show();
    },
    error: function (jqXHR, textStatus, errorThrown) {
      $("#completeInstallationLoadingDiv").hide();
      $("#installationFailed").show(); 
    }
  });
}

function displayMessage(evt) {
  var data=evt.data;
  if(data==="")
  {
    $("#leftButtonLoadingDiv").hide();
    $("#rightButtonLoadingDiv").hide();
  }
  //   if (evt.origin !="https://your-ngrok-url") {
  //     return;
  //   }
  var message;
  var evtData = JSON.parse(evt.data);
  var evtDataValue=JSON.stringify(evtData);
  var serviceId = evtData.data.serviceId;
  var emailId = evtData.data.userEmail;
  showAuthButton(serviceId);
}

if (window.addEventListener) {
  window.addEventListener("message", displayMessage, false);
} else {
  window.attachEvent("onmessage", displayMessage);
}


function getRandomId() {
  var result = '';
  var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for (var i = 0; i < 8; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}