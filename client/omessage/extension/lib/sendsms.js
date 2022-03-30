var urlParams = new URLSearchParams(window.location.search);
var url = "";
var integId = "";
var hash = "";

function getNumbers() {
  providerObj.getAssociatedIntegId().then(function (data) {
    $("#forceAuthAction").hide();

    integId = data;
    //console.log("IntegId", integId);
    providerObj.getAssociatedHash().then(function (data1) {
      hash = data1;
      //console.log("hash", hash);

      providerObj.fetchContact().then(function (contactData) {

        var contactDataVar = contactData;
        //urlParams.get("integId");

        url = osyncUrl + '/api/v1/omessage/' + integId + '/savedNumbers';
        console.log("savedNumbers URL>>>>>>>>>", url);
        $.ajax({
          url: url,
          type: "GET",
          crossDomain: true,
          headers: {
            "Osync-Authorization": hash
          },
          success: function (response, textStatus, jqXHR) {

            var data = response.data;
            if (typeof data === "string") {
              data = JSON.parse(data);
            }
            var phoneList = [];
            $.each(data, function (index, phObj) {
              var phoneNumber = {
                "phone": phObj.phoneNumber
              };
              phoneList.push(phoneNumber);
            });

            var template = $("#phone_template").html();
            var text = Mustache.render(template, phoneList);
            $("#fromPhoneNumber").html(text).show();
            $("#fromPhoneNumber").select2();
            if ($("#fromPhoneNumber option").length == 1) {
              $("#loadingSpinner").hide();
              $("#forceAuthAction").show();
            } else {
              $("#loadingSpinner").hide();
              $("#loadSMSFormPage").show();
              $("#toPhoneNumber").val(contactDataVar);
            }
          },
          error: function (jqXHR, textStatus, errorThrown) {
            $("#loadingSpinner").hide();
            $("#forceAuthAction").show();
            $("#uploadFailed").show();
            $("#uploadButtonLoadingDiv").hide();
          },
          complete: function () {
            if (providerObj.service == "PipeDriveSMS") {
              $("#addPhone").show();
            }
          }
        });
      }).catch(function (err) {
        $("#loadingSpinner").hide();
        $("#forceAuthAction").show();
        console.log("in sendSMShtml Catch", err);
      });
    });
  }).catch(function (err) {
    $("#loadingSpinner").hide();
    $("#forceAuthAction").show();
    console.log("in sendSMShtml Catch", err);
  });

}

function sendSMS(thisObj) {
  let st = Date.now();
  console.log("send start time in millis", st);
  $("#toPhoneErrorDiv,#fromPhoneErrorDiv,#msgToSendErrorDiv").hide();
  $(thisObj).find("span").addClass("spinner-border spinner-border-sm");
  $("#butSettings").addClass("w40per");
  $("#btnTxtSpan").addClass("flright");
  $("#btnTxtImg").removeClass("hide");

  var fromNumber = $("#fromPhoneNumber").val();
  if (!validatePhoneNumber(fromNumber)) {
    $("#fromPhoneErrorDiv").show();
    $(thisObj).find("i").hide();
    $(thisObj).css({
      "background-color": "#fff",
      "color": "#28a745",
    });
    $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
    return false;
  }
  var toNumber = $("#toPhoneNumber").val();
  if (!validatePhoneNumber(toNumber)) {
    $("#toPhoneErrorDiv").show();
    $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
    return false;
  }
  var msgbody = $.trim($("#msgTosend").val());
  if (msgbody === "") {
    $("#msgToSendErrorDiv").show();
    $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
    return false;
  }

  if (toNumber === "") {
    $("#toPhoneErrorDiv").show();
    $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
    return false;
  }
  providerObj.getAssociatedObjectId().then(function (data) {
    associatedObjectId = data;
    providerObj.getAssociatedObjectType().then(function (typeData) {
      associatedObjectType = typeData;
      if (providerObj.service != "PipeDriveSMS") {
        associatedObjectId = associatedObjectId[0]
      }

      var payload = {
        "from": fromNumber,
        "to": toNumber,
        "messageBody": msgbody,
        "associatedObjectId": associatedObjectId,
        "associatedObjectType": associatedObjectType
      }
      url = osyncUrl + '/api/v1/omessage/' + integId + '/send'
      $.ajax({
        url: url,
        type: "POST",
        crossDomain: true,
        data: JSON.stringify(payload),
        headers: {
          "Osync-Authorization": hash,
          "Content-Type": "application/json"
        },
        success: function (response, textStatus, jqXHR) {
          if (response.status) {
            $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
            $("#msgTosend").val("");
            updateTextCount();
            $("#showSuccessMessage").show();
            $("#showSuccessMessage").addClass("d-flex");
          }
        },
        error: function (jqXHR, textStatus, errorThrown) {
          $("#showErrorMessage").show();
          $("#showErrorMessage").addClass("d-flex");
          $(thisObj).find("span").removeClass("spinner-border spinner-border-sm");
        },
        complete: function () {
          let et = Date.now();
          console.log("send end time in millis", et);
          let tt = et - st;
          console.log("send time taken in millis", tt);
        }
      });
    })
  })
};

function addPhone(thisObj) {

  portalId = urlParams.get("companyId");
  var ids = portalId.split("_");
    leftServiceId = ids[0];
    rightServiceId = ids[1];
    companyId = ids[2];
  selectedIds = urlParams.get("selectedIds");
  resource = urlParams.get("resource");
var href ='https://api-osync.oapps.xyz/app/omessage/authorize/index.html?leftServiceId='+leftServiceId+'&rightServiceId='+rightServiceId+'&serviceName=pd&selectedIds='+selectedIds+'&companyId='+companyId+'&resource='+resource;
window.open(href);
}

function updateTextCount() {
  var exisitingMsg = $("#msgTosend").val();
  var exisitingMsgLen = exisitingMsg.length;
  var maxCharactersAllowed = 160;
  if (providerObj.service == "PipeDriveSMS") {
    var maxCharactersAllowed = 1000;
  }
  var remainingAllowedText = maxCharactersAllowed - exisitingMsgLen;

  $("#textCount").text(remainingAllowedText);
  if (remainingAllowedText <= 20) {
    $("#textCount").addClass("redtxt");
  } else {
    $("#textCount").removeClass("redtxt");
  }
  if (remainingAllowedText <= 0) {
    $("#textCount").text("exceeded the allowed txt");
  }
};

function validatePhoneNumber(phoneNumber) {
  var filter = /^[0-9-+]+$/;
  if (!filter.test(phoneNumber)) {
    return false;
  }
  if (phoneNumber.length < 10) {
    return false;
  }
  return true;
}

function addActivity(fromNumber, smsText) {
  var name = urlParams.get("uName");
  var entityId = urlParams.get("uId");
  var module = urlParams.get("module");
  var recordData = {
    "$se_module": module,
    "Subject": "SMS Sent",
    "Description": "From Number :" + fromNumber + "\nSMS Text :" + smsText,
    "Call_Type": "Outbound",
    "Call_Start_Time": getISO8601Format(),
    Who_Id: {
      "name": name,
      "id": entityId
    }
  };
  if (module == "Leads" || module == "Accounts") {
    recordData = {
      "$se_module": module,
      "Subject": "SMS Sent",
      "Description": "From Number :" + fromNumber + "\nSMS Text :" + smsText,
      "Call_Type": "Outbound",
      "Call_Start_Time": getISO8601Format(),
      What_Id: {
        "name": name,
        "id": entityId
      }
    }
  }
  var res = ZOHO.CRM.API.insertRecord({ Entity: "calls", APIData: recordData, Trigger: ["workflow"] });
  // logObj = {
  //      "SendSMS_JS_Activity_Adding for" : recordData, 
  //      "SendSMS_JS_Activity_Adding_Response" : res
  //     };
  // logList.push(JSON.stringify(logObj));
}

function getISO8601Format() {

  var dt = new Date(),
    current_date = dt.getDate(),
    current_month = dt.getMonth() + 1,
    current_year = dt.getFullYear(),
    current_hrs = dt.getHours(),
    current_mins = dt.getMinutes(),
    current_secs = dt.getSeconds(),
    current_datetime;

  // Add 0 before date, month, hrs, mins or secs if they are less than 0
  current_date = current_date < 10 ? '0' + current_date : current_date;
  current_month = current_month < 10 ? '0' + current_month : current_month;
  current_hrs = current_hrs < 10 ? '0' + current_hrs : current_hrs;
  current_mins = current_mins < 10 ? '0' + current_mins : current_mins;
  current_secs = current_secs < 10 ? '0' + current_secs : current_secs;

  // Current datetime
  // String such as 2016-07-16T19:20:30
  current_datetime = current_year + '-' + current_month + '-' + current_date + 'T' + current_hrs + ':' + current_mins + ':' + current_secs;

  var timezone_offset_min = new Date().getTimezoneOffset(),
    offset_hrs = parseInt(Math.abs(timezone_offset_min / 60)),
    offset_min = Math.abs(timezone_offset_min % 60),
    timezone_standard;

  if (offset_hrs < 10)
    offset_hrs = '0' + offset_hrs;

  if (offset_min < 10)
    offset_min = '0' + offset_min;

  // Add an opposite sign to the offset
  // If offset is 0, it means timezone is UTC
  if (timezone_offset_min < 0)
    timezone_standard = '+' + offset_hrs + ':' + offset_min;
  else if (timezone_offset_min > 0)
    timezone_standard = '-' + offset_hrs + ':' + offset_min;
  else if (timezone_offset_min == 0)
    timezone_standard = 'Z';

  //return timezone_standard;
  // Timezone difference in hours and minutes
  // String such as +5:30 or -6:00 or Z
  var datetimeVal = current_datetime + timezone_standard;
  return datetimeVal;
}