var osyncId = "";
var integId = "";
var hash = "";
var service = "";
var leftServiceId = "";
var rightServiceId = "";
var code = "";
var dataObj = "";
var auth = 0;
var hashData1="";

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
  auth += 1;

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
  // const portalId = urlParams.get("orgId");
  const fName = urlParams.get("firstName");
  // const emailId = urlParams.get("emailId");

  leftServiceId = "6da72459-d122-4704-95dc-96321824488e";//HS
  rightServiceId = "b283ff18-8107-4a33-b35e-21f2b58bbb75";//Twilio
  var emailId = "help@oapps.xyz";
  var portalId = getRandomId();
  var rightServiceName1 = "Twilio SMS";
  var userdata;
  leftServiceId = urlParams.get("leftServiceId");
  rightServiceId = urlParams.get("rightServiceId");
  rightServiceName1=urlParams.get("rightServiceName");
  if (providerObj.service == "Zoho CRM") {
    new Promise((resolve, reject) => {
      providerObj.getUserData().then(function (zcrmUserData) {
        emailId = zcrmUserData.email;
        portalId = leftServiceId + "_" + rightServiceId + "_" + zcrmUserData.companyId;
        serviceName = providerObj.service;
        userdata = {
          "left_service_id": leftServiceId,  //ZC
          "right_service_id": rightServiceId, // Tw
          "companyId": portalId,// portalId,
          "name": "OAppS",//fName,
          "email": emailId,// emailId,
          "planName": "standard"
        }
        callIntegrate("NA",userdata);
      });
    });
  }
  else if (providerObj.service == "PipeDriveSMS") {
    $("#leftButtonLoadingDiv").show();
    // var pipedriveUserData = providerObj.getUserData();
    code = urlParams.get("code");
    emailId = emailId;
    portalId = getRandomId();
    serviceName = providerObj.service;
    userdata = {
      "left_service_id": leftServiceId,  //PD
      "right_service_id": rightServiceId, // Tw
      "companyId": portalId,// portalId,
      "name": "OAppS",//fName,
      "email": emailId,// emailId,
      "planName": "standard"
    }
    callIntegrate(code,userdata);
  }
  else if (providerObj.service == "Hubspot") {
    $("#leftButtonLoadingDiv").show();
    // var pipedriveUserData = providerObj.getUserData();
    code = urlParams.get("code");
    emailId = emailId;
    portalId = getRandomId();
    serviceName = providerObj.service;
    userdata = {
      "left_service_id": leftServiceId,  //PD
      "right_service_id": rightServiceId, // Tw
      "companyId": portalId,// portalId,
      "name": "OAppS",//fName,
      "email": emailId,// emailId,
      "planName": "standard"
    }
    callIntegrate(code,userdata);
  }
}
function callIntegrate(code,userdata){
  var rightServiceName1 = urlParams.get("rightServiceName");
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
      
      if (providerObj.service == "PipeDriveSMS") {
        dataObj = {
          "osyncId": osyncId,
          "integId": integId,
          "leftServiceId": leftServiceId,
          "rightServiceId": rightServiceId,
          "code": code,
          "osyncUrl": osyncUrl,
        }
        providerObj.doAfterSuccessfulEnable(dataObj);
      }
      if (providerObj.service == "Hubspot") {
        dataObj = {
          "osyncId": osyncId,
          "integId": integId,
          "leftServiceId": leftServiceId,
          "rightServiceId": rightServiceId,
          "code": code,
          "osyncUrl": osyncUrl,
        }
        providerObj.doAfterSuccessfulEnable(dataObj);
      }

      var authPage = $("#list").html();
      var text = Mustache.render(authPage, response);
      $("#list_R").html(text).show();
      $("#enableLoadingDiv").hide();
      $("#enableDiv").hide();
      $("#authorizeDiv").show();

      if (providerObj.service == "PipeDriveSMS"|| providerObj.service == "Shopify") {
        $("#leftButtonTd").hide();
      }

      if (providerObj.service == "Zoho CRM") {
        if (rightServiceName1 == "whatsapp") {
          apiVariableName = "whatsapp_".trim();
        } else {
          apiVariableName = "twiliosmszohocrm__".trim();
        }
        var integIdValueMap = {
          "apiname": apiVariableName + "integId",
          "value": integId
        };
        var hashValueMap = {
          "apiname": apiVariableName + "hash",
          "value": hash
        };

        if (rightServiceName1 === "oapps") {
          $("#completeInstallation").hide();
          integIdValueMap = {
            "apiname": "phoneverifierforzohocrm__integId",
            "value": integId
          };
          hashValueMap = {
            "apiname": "phoneverifierforzohocrm__hash",
            "value": hash
          };
        }

        providerObj.save(integIdValueMap);
        providerObj.save(hashValueMap);
      }
    },
    error: function (jqXHR, textStatus, errorThrown) {

      $("#enableLoadingDiv").hide();
      $("#enableFailed").show();
    },
    complete: function (jqXHR, textStatus, errorThrown) {
    }
  });
}
function completeInstallation(thisObj) {
  var rightService= urlParams.get("rightServiceName");
    if (rightService == "whatsapp") {
    if (auth >= 2){
        providerObj.doCompleteInstallationProcess(integId);
    }
  }else{
      providerObj.doCompleteInstallationProcess(integId);
    
  }

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

function bindClickEvents() {
  
  serviceName = urlParams.get("serviceName");
  rightServiceName1 = urlParams.get("rightServiceName");
  $(document).on('click', '#apiKeySave', function () {
    var domain_url = "";
    if ($("#domainUrlInput").val() != "" || $("#apiKeyInput").val() != "") {
      if ($("#domainUrlInputDiv").is(":visible")) {
        domain_url = $("#domainUrlInput").val();
      }
      if(hash==null || hash=="")
        hash=hashData1;
      apiData = {
        "access_token": $("#apiKeyInput").val(),
        "state": osyncId + "::" + $("#apiKeyModal").attr("serviceId") + "::" + integId + ":: false",
        "refresh_token": "",
        "api_domain": domain_url,
        "twilio_account_sid": domain_url
      }
      var buttonText = $("#apiKeySave").text();
      var isLeft = $("#apiKeySave").attr("isleft");
      var url = osyncUrl + '/api/v1/saveApiKey';
      $.ajax({
        url: url,
        type: "POST",
        crossDomain: true,
        headers: {
          "Osync-Authorization": hash,
          "Content-Type": "application/json"
        },
        data: JSON.stringify(apiData),
        success: function (response, textStatus, jqXHR) {
          var data = response;
          if (typeof data === "string") {
            response = $.parseJSON(data);
          }

          if (buttonText === "Save and Authorize") {
            if (domain_url === "") {
              return false;
            }
            $("#apiKeyModal").modal("hide");
            var authUrl = $("button.btn-outline-primary[data-isleft='" + isLeft + "']").attr("url");
            authUrl = domain_url + authUrl;

            var buttonName = isLeft ? "leftButton" : "rightButton";
            openNewWindow(buttonName, $("button.btn-outline-primary[data-isleft='" + isLeft + "']").attr("name"), authUrl);
          } else {

            $("#apiKeyModal").modal("hide");
            var serviceId = response.data.serviceId;
            var emailId = response.data.userEmail;

            showAuthButton(serviceId);

          }
        },
        error: function (jqXHR, textStatus, errorThrown) {
          $("#uploadFailed").show();
          $("#uploadButtonLoadingDiv").hide();
        },
        complete: function () {
        }
      });
    }
  });
  $('#addPhoneNumberWidget').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget)
    var modalTitle = button.data('title');
    var selectedPhoneNumber = button.data('phone');

    var isAckMessageEnabled = button.data('ackmessage');
    var ackMessage = button.data('ackmessagetext');

    var friendlyName = button.data("friendlyName");

    // var isAutomatedSMSConfigured = button.data("automatesms");

    if (ackMessage != undefined) {
      ackMessage = atob(ackMessage);
    }

    if (isAckMessageEnabled) {
      $("#smsTemplate").show();
    } else {
      $("#smsTemplate").hide();
    }
    // if(isAutomatedSMSConfigured){
    //   $("#automateSMSTemplate").show();   
    //   if(automateMessageTemplates != undefined){
    //     automateMessageTemplates = atob(automateMessageTemplates);
    //   }
    // } else {
    //   $("#automateSMSTemplate").hide();        
    // }
    var btnText = button.data('buttontxt');
    var modal = $(this);
    modal.find('.modal-title').text(modalTitle);

    if (selectedPhoneNumber != undefined) {
      var selectOption = '<option value=\"' + selectedPhoneNumber + '\" >' + selectedPhoneNumber + '</option>';
      modal.find('#twilioPhoneNumbers').append(selectOption).val(selectedPhoneNumber).prop("disabled", true);
      $("#deletePhoneNumber").val(selectedPhoneNumber).show();
    } else {
      modal.find('#twilioPhoneNumbers').val("Select Phone Number").prop("disabled", false);
      $("#deletePhoneNumber").hide();
    }
    modal.find('#acknowledgmentInput').prop("checked", isAckMessageEnabled);
    // modal.find('#automateMessage').prop("checked",isAutomatedSMSConfigured);
    modal.find('#acknowledgementText').val(ackMessage);
    modal.find("#friendlyName").text(friendlyName);
    // if(isAutomatedSMSConfigured){
    //   modal.find("#showAutomateSMSTemplateEditor").show();
    // }

    var buttonTextHtml = '<span class="" role="status" aria-hidden="true"></span>' + btnText;
    modal.find('#addPhoneNumber').html(buttonTextHtml);
  });
  $("#addPhoneNumber").on("click", function () {
    $(this).find("span").addClass("mr-1 spinner-border spinner-border-sm");
    providerObj.savePhone($(this));
  });
  $("#showAccountConfigurationDiv").on("click", function () {
    $("#twilioAuthorizationPage").toggle();
  });
  $("#deletePhoneNumber").on("click", function () {
    $(this).find("span").addClass("mr-1 spinner-border spinner-border-sm");
    save($(this));
  });

}

function showSMSTemplateEditor() {
  $("#smsTemplate,#saveMsgConfiguration").fadeToggle("slow");
}

function showAutomateSMSTemplateEditor() {
  return new Promise((resolve, reject) => {
    $("#automateSMSTemplate,#saveMsgConfiguration").fadeToggle("slow");
    fetchAllTicketStatus().then(function (ticketFieldObj) {
      var ticketStatusArr = [];
      $.each(ticketFieldObj, function (key, data) {
        if (data.name === "status") {
          var allowedValues = data.allowedValues;
          $.each(allowedValues, function (jsonKey, statusData) {
            ticketStatusArr.push(statusData.value);
          });
        }
      });
      var container = '<div class="container"><div class="row bg-secondary p-1 mb-1"><div class="col-3 d-flex justify-content-center text-white">Ticket Status</div><div class="col-7 d-flex justify-content-center text-white">Editor</div><div class="col-2 d-flex justify-content-center text-white">Placeholder</div></div><div class="row"><div class="col-3 p-1"><div class="list-group" id="list-tab" role="tablist">';

      $.each(ticketStatusArr, function (index, status) {
        var id = "list-status-" + index;
        var idATag = "list-status-id-" + index;
        var activeClass = "";
        if (index === 0) {
          activeClass = "active";
        }
        container += '<a style="font-size:0.85em" class="list-group-item list-group-item-action ' + activeClass + '" id="' + idATag + '" data-toggle="list" href="#' + id + '" role="tab" aria-controls="' + id + '" ticket-status="' + status + '">' + status + '</a>';
      });

      container += '</div> </div> <div class="col-9 p-1"><div class="tab-content h-100" id="nav-tabContent">';
      $.each(ticketStatusArr, function (index, status) {
        var labelledby = "list-status-" + index;
        var id = "list-status-id-" + index;
        var activeClass = "";
        if (index === 0) {
          activeClass = "show active";
        }
        container += '<div class="tab-pane h-100 fade ' + activeClass + '" id="' + labelledby + '" role="tabpanel" aria-labelledby="' + labelledby + '">' + constructPlaceHoldersWithTextArea(id, ticketFieldObj, status) + '</div>';
      });
      container += '</div></div></div>';
      $("#automateSMSTemplate").append(container).show();
      resolve();
      $("#automateSMSTemplate").find("textarea:visible").focus();
      $("div#automateSMSTemplate li").off().on("click", function () {
        var textAreaId = $(this).attr("text-area-id");
        $("#" + textAreaId).insertAtCaret(' ' + $(this).attr("placeholder"));
      });
      $.fn.extend({
        insertAtCaret: function (myValue) {
          this.each(function () {
            if (document.selection) {
              this.focus();
              var sel = document.selection.createRange();
              sel.text = myValue;
              this.focus();
            } else if (this.selectionStart || this.selectionStart == '0') {
              var startPos = this.selectionStart;
              var endPos = this.selectionEnd;
              var scrollTop = this.scrollTop;
              this.value = this.value.substring(0, startPos) +
                myValue + this.value.substring(endPos, this.value.length);
              this.focus();
              this.selectionStart = startPos + myValue.length;
              this.selectionEnd = startPos + myValue.length;
              this.scrollTop = scrollTop;
            } else {
              this.value += myValue;
              this.focus();
            }
          });
          return this;
        }
      });
    });
  });
}

function constructPlaceHoldersWithTextArea(id, ticketFieldObj, status) {
  var container = '<div class="row h-100"><div id="textarea-' + id + '" class="col pr-0"><textarea ticket-status="' + status + '" class="h-100 form-control" style="font-size:0.75em" placeholder="Enter automated message for the status ' + status + '" id="' + id + '-textarea" style="height: 100%;width: 100%;"rows="3"></textarea></div>';
  container += '<div id="placeholder-' + id + '" class="h-100 col-4 p-1" style="border: 1px solid rgba(0,0,0,.125);"><ul class="list-group list-group-flush" style="height: 100vh;overflow: auto;">';

  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{ticketNumber}}">Ticket number</li>';
  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{contact_first_name}}">Contact First Name</li>';
  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{contact_last_name}}">Contact Last Name</li>';
  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{agent_first_name}}">Agent First Name</li>';
  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{agent_last_name}}">Agent Last Name</li>';
  container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{agent_email}}">Agent Email</li>';
  container += '<li style="font-size:0.75em"  class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{webUrl}}">Ticket Link</li>';
  $.each(ticketFieldObj, function (index, value) {
    container += '<li style="font-size:0.75em" class="list-group-item cursor" text-area-id="' + id + '-textarea" placeholder="{{' + value.apiName + '}}">' + value.name + '</li>';
  });
  container += '</ul></div></div>';
  return container;
}

function showAutomateSMSTemplateEditorOnEditPage(thisObj) {
  $(thisObj).text("Loading..");

  var phoneConfCallBack = function (phoneConfObj) {
    var automateMessageTemplates = phoneConfObj.phone[0].automateMessageTemplates;
    console.log(automateMessageTemplates);
    $("#automateSMSTemplate").empty();
    showAutomateSMSTemplateEditor().then(function () {
      $(thisObj).hide();
      $("#automateSMSTemplate").show();
      $.each(automateMessageTemplates, function (index, value) {
        var savedTicketStatus = value.status;
        var savedMessage = atob(value.message);
        $("#automateSMSTemplate").find("textarea[ticket-status=\'" + savedTicketStatus + "\']").val(savedMessage);
      });
    });
  }
  getPhoneDetailsFromDB(phoneConfCallBack);

}
function fetchAllSavedPhoneNumbers(integId, hash) {
  SMSHandler.getAllSavedNumbers(integId, hash).then(function (response) {
    if (response === "") {
      $("#newMessageConfigDiv").addClass("d-flex justify-content-center");
      $("#addPhoneNumberButton").addClass("btn-primary btn-lg");
      $("#emptyPhoneMessage").show();
    } else {
      $("#emptyPhoneMessage").hide();
      $("#newMessageConfigDiv").removeClass("d-flex justify-content-center");
      $("#addPhoneNumberButton").removeClass("btn-primary btn-lg").addClass("btn-link btn-sm");
      var phoneHtml = getPhoneNumberHtml(phoneNumber, isAcknowledgementConfigured, acknowledgementTemplate, isAutomatedMessageConfigured, automateMessageTemplates);
      $("#newMessageConfigDiv").prepend(phoneHtml);
    }
  });
}
function getPhoneNumberHtml(phone, isAckMessageEnabled, acknowledgementText, isAutomatedMessageConfigured, automateMessageTemplates) {
  var phoneHtml = '<button type="button" data-phone="' + phone + '" data-ackmessage="' + isAckMessageEnabled + '" data-ackmessagetext="' + acknowledgementText + '" data-automatesms="' + isAutomatedMessageConfigured + '" data-automateMessageTemplates="' + btoa(JSON.stringify(automateMessageTemplates)) + '" class="btn btn-outline-primary ml-3 mt-3 btn-lg"  data-toggle="modal" data-target="#addPhoneNumberWidget" data-title="Details" data-buttontxt="Update">' + phone + '</button>';
  return phoneHtml;
}
function callAuthorize(extensionData){
  integId=extensionData.integId;
  hashData1=extensionData.hash;
  
  showAuthPageHS();
};


