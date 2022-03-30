// var osyncUrl = "https://b92cad9d0d0b.ngrok.io";
var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";
var osyncId = "";
var integId = "";
var hash = "";
function serviceAuth(buttonName) {
  var buttonId = buttonName; //$(thisObj).attr("id");
  var authUrl = $("#" + buttonId).attr("url");
  var authType = $("#" + buttonId).attr("auth_type");
  if (authType === "apikey" || authType === "accsid_apikey" || authType === "baseurl_oauth" || authType === "baseurl_apikey") {
    $("#apiKeyModal").modal('show');
    $("#apiKeyModal").attr("serviceId", $("#" + buttonId).attr("name"));
    $("#apiKeyModal,#apiKeySave").attr("isLeft", $("#" + buttonId).data("isleft"));

    if (authType === "baseurl_oauth") {
      $("#domainUrlInputDiv").show();
      $("#apiKeyInputDiv").hide();
      $("#apiKeySave").text("Save and Authorize");
    } else if (authType === "accsid_apikey") {
      $("#domainUrlInputDiv").show();
      $("#domainUrlInputDiv").show();
      $("#apiKeySave").text("Save");

      $("#domainUrlInput").attr("placeholder", "Enter your twilio Account SID");
      $("#apiKeyInput").attr("placeholder", "Enter your twilio Auth token");

      $("label[for='apiKeyInput']").text("Auth Token");
      $("label[for='domainUrlInput']").text("Account SID");

      $("#apiKeyHelpLabel,#domainUrlHelpLabel").hide();
    } else {
      $("#apiKeyInputDiv").show();
      $("#apiKeySave").text("Save");
    }
  } else {
    var windowName = buttonName;
    var btnText = $("#" + buttonId).text();
    if (btnText == "Revoke") {
      revokeAction(buttonName);
    } else {
      openNewWindow(windowName, $("#" + buttonId).attr("name"), authUrl, buttonId, authType);
    }
  }

}

function showAuthButton(serviceId) {
  var buttonHtmlObj = $("#authorizeTable [name=\'" + serviceId + "\']");
  buttonHtmlObj.removeClass("spinner-grow spinner-grow-sm");
  buttonHtmlObj.removeAttr("onclick");
  buttonHtmlObj.text('Authorized Successfully').removeClass("btn btn-outline-primary").addClass("text-success");
}

function openNewWindow(windowName, serviceName, authURL, buttonName, authType, buttonId) {
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

  
  var userdata = {
    "left_service_id": "357966f7-1202-47d8-a5d2-c039c93a3260",  //HS
    "right_service_id": "c78248af-9f91-47fb-82a1-527df75b45ad", // ZS
    "companyId": getRandomId(),// portalId,
    "name": "aafren",//fName,
    "email": "aafrenaafi@gmail.com",// emailId,
    "planName": "standard"
  }
  var url = osyncUrl + '/api/v1/integrate'
  $.ajax({
    url: url,
    type: "POST",
    crossDomain: true,
    datatype: 'json',
    data: JSON.stringify(userdata),
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    success: function (response, textStatus, jqXHR) {
      osyncId = response.osync_id;
      integId = response.integId;
      hash = response.hash;
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

function completeInstallation(thisObj) {
  $("#installationFailed").hide();
  $("#completeInstallationLoadingDiv").show();
  var url = osyncUrl + '/api/v1/hubspot/sms/portal?integId=' + integId;
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
  var data = evt.data;
  if (data === "") {
    $("#leftButtonLoadingDiv").hide();
    $("#rightButtonLoadingDiv").hide();
  }
  //   if (evt.origin !="https://your-ngrok-url") {
  //     return;
  //   }
  var message;
  var evtData = JSON.parse(evt.data);
  var evtDataValue = JSON.stringify(evtData);
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