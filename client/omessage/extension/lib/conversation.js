var integId = "";
var hash = "";
var contactDataVar = "";
var fromNumber = "";

function getNumbers(rightService) {
  $("#loadingSpinner").show();
  $("#tooltip").show();
  providerObj.getAssociatedIntegId(this, rightService).then(function (data) {
    $("#forceAuthAction").hide();
    integId = data;
    providerObj.getAssociatedHash(this, rightService).then(function (data1) {
      hash = data1;
      providerObj.fetchContact().then(function (contactData) {
        var contactDataVar = contactData;
        if(contactDataVar==null){
          contactDataVar="";
          console.log("No con");
          $("#fromPhoneNumber").prop('disabled', true);
          $("#refreshbtn").prop('disabled', true);
          $("#msgTosend").prop('disabled',true);
          $("#sendButton1").prop('disabled',true);
          $("#nophone").show();
          $("#phonenumred").hide();
          $("#sendDiv").addClass('disable');
          
        }
        if(!contactDataVar.startsWith("+")){
          var msgs = $("#NoCountryCode").html();
          var countryCode = Mustache.render(msgs, {});
          $("#conversationScrollDiv").html(countryCode).show();
          $("#fromPhoneNumber").prop('disabled', true);
          $("#msgTosend").prop('disabled',true);
          $("#sendButton").prop('disabled',true);
        }
        url = osyncUrl + '/api/v1/omessage/' + integId + '/savedNumbers';
        $("#loadingSpinner").hide();
        $.ajax({
          url: url,
          type: "GET",
          crossDomain: true,
          headers: {
            "Osync-Authorization": hash
          },
          success: function (response, textStatus, jqXHR) {
            $("#loadingSpinner").hide();
            var data = response.data;
            if (data != null) {
              $("#showChats").show();

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
              $("#loadingSpinner").hide();
              recipient_phonenumber = contactDataVar;
            } else {
              $("#showChat").hide();
              $("#loadingSpinner").hide();
              $("#loadingDiv").hide();
              $("#forceAuthAction").show();
            }
          },
          error: function (jqXHR, textStatus, errorThrown) {
            $("#loadingSpinner").hide();
            $("#forceAuthAction").show();
            $("#uploadButtonLoadingDiv").hide();
          },
          complete: function () {
          }
        });
      
      });
    });
  });

}

function callShowConversation() {
  var fromNumber = $("#fromPhoneNumber").val();
  showConversation(fromNumber, recipient_phonenumber)
}
function showConversation(fromNumber, recipient_phonenumber) {
  if(recipient_phonenumber!=null && recipient_phonenumber!=""){
  $("#showChats").show();
  $("#tooltip").show();
  $("#loadingSpinner").show();
  var url = osyncUrl + '/api/v1/omessage/whatsapp/coversations/view?phoneNumber=' + recipient_phonenumber + '&fromNumber=' + fromNumber + '&integId=' + integId;
  $.ajax({
    url: url,
    type: "GET",
    crossDomain: true,
    contentType: "text/html; charset=UTF-8",
    headers: {
      "Osync-Authorization": hash
    },
    success: function (response, textStatus, jqXHR) {
      $("#loadingSpinner").hide();
      if (response === undefined) {
        var msgs = $("#no_conversations").html();
        var noConversation = Mustache.render(msgs, {});
        $("#conversationScrollDiv").html(noConversation).show();
        $("#loadingSpinner").hide();
      } else {
        var conver = JSON.stringify(response.messages);
        var allMsgs = $.parseJSON(conver);
        var msgs = $("#show_conversations").html();
        text = Mustache.render(msgs, allMsgs);
        $("#conversationScrollDiv").html(text).show();
        $("#loadingDiv").removeClass("d-flex").hide();
        var conversationHeight = $("#conversations").height();
        $("#conversationScrollDiv").scrollTop(conversationHeight);
      }
    },
    error: function (jqXHR, textStatus, errorThrown) {
      $("#loadingSpinner").hide();
      console.log('no conversation');
      var view = {
        msg: 'No conversation msgs.. Start sending msgs'
      };
      var msgs = $("#no_conversations").html();
      text = Mustache.render(msgs, view);
      $("#conversationScrollDiv").html(text).show();
      $("#loadingDiv").removeClass("d-flex").hide();
    }
  });
}
}
function hideErr() {
  $("#msgToSendErrorDiv").hide();
  $("#emptytext").hide();
  $('#msgTosend').keydown(function (e) {
    if (e.keyCode == 13) {
      sendWhatsApp(this, true);

    }
  })
}
function sendWhatsApp(thisObj) {
  if(recipient_phonenumber!=null && recipient_phonenumber!=""){
  $("#showChats").show();
  $("#tooltip").show();
  
  var fromNumber = $("#fromPhoneNumber").val();
  $("#msgToSendErrorDiv").hide();
  var msgbody = $.trim($("#msgTosend").val());
  if (msgbody != "") {
    $("#loadingSpinner").show();  
    $("#msgTosend").val('');
    var payload = {
      "from": fromNumber,
      "to": recipient_phonenumber,
      "messageBody": msgbody,

    }
    var url = osyncUrl + '/api/v1/omessage/whatsapp/send?integId=' + integId;
    $.ajax({
      url: url,
      type: "POST",
      crossDomain: true,
      datatype: 'json',
      data: JSON.stringify(payload),
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'Osync-Authorization': hash
      },
      success: function (response, textStatus, jqXHR) {
        if (response.status) {
          $("#loadingSpinner").hide();
          showConversation(fromNumber, recipient_phonenumber);
        }
      },
      error: function (jqXHR, textStatus, errorThrown) {
        console.log('not sent');
        $("#loadingSpinner").hide();
      },
      complete: function () {
      }

    });


  }else{
    $("#emptytext").show();
    
  }
}
}