// var osyncUrl = "https://b92cad9d0d0b.ngrok.io";
var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

const urlParams = new URLSearchParams(window.location.search);

function getNumbers() {

  const integId = urlParams.get("integId");
  const hash = urlParams.get("hash");
  var mobile = $.trim(urlParams.get("mobile"));
  var phone = $.trim(urlParams.get("phone"));

  var url = osyncUrl + '/api/v1/hubspot/sms/' + integId + '/numbers';
  $.ajax({
    url: url,
    type: "GET",
    crossDomain: true,
    headers: {
      "Osync-Authorization": hash
    },
    success: function (response, textStatus, jqXHR) {
      console.log(response);
      var data = response.data;
      if (typeof data === "string") {
        data = JSON.parse(data);
      }
      console.log(data.phone);
      var phoneList = [];
      $.each(data.phone, function (index, value) {
        var phoneNumber = {
          "phone": value
        };
        console.log(value);
        phoneList.push(phoneNumber);
      });

      var template = $("#phone_template").html();
      var text = Mustache.render(template, phoneList);
      $("#fromPhoneNumber").html(text).show();
      $("#fromPhoneNumber").select2();
      $("#loadingSpinner").hide();
      $("#loadSMSFormPage").show();

      if(phone != null && phone != ""){
        $("#toPhoneNumber").val(phone);
      } else if(mobile != null && mobile != ""){
        $("#toPhoneNumber").val(mobile);
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

function sendSMS(thisObj) {
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

  var payload = {
    "from": fromNumber,
    "to": toNumber,
    "messageBody": msgbody,
  }

  const integId = urlParams.get("integId");
  const hash = urlParams.get("hash");

  var url = osyncUrl + '/api/v1/hubspot/sms/' + integId + '/send';
  $.ajax({
    url: url,
    type: "POST",
    crossDomain: true,
    data: JSON.stringify(payload),
    headers: {
      "Osync-Authorization": hash,
      "Content-Type" : "application/json"
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
    }
  });
};

function updateTextCount() {
  var exisitingMsg = $("#msgTosend").val();
  var exisitingMsgLen = exisitingMsg.length;
  var maxCharactersAllowed = 160;
  var remainingAllowedText = maxCharactersAllowed - exisitingMsgLen;
  $("#textCount").text(remainingAllowedText);
  if (remainingAllowedText <= 20) {
    $("#textCount").addClass("redtxt");
  } else {
    $("#textCount").removeClass("redtxt");
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