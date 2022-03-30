var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

const urlParams = new URLSearchParams(window.location.search);

function uploadDocuments() {
  
  const fname = urlParams.get("firstname");
  const emailId = urlParams.get("email");

  var contact_name;
  var contact_emailId;

  var templateObj = {
    contact_name : fname,
    contact_emailId : emailId
}

  var template = $("#upload_document").html();  
  var text = Mustache.render(template, templateObj);
  $("#uploadDocumentDiv").html(text).show();

  var contactEmail = $("#contactEmail").val();
  var contactName = $("#contactName").val();

  if(contactName ==="null" && contactEmail ==="null"){

   var contactEmail = $("#contactEmail").val("");
  var contactName = $("#contactName").val("");
  
  }  

}


function uploadButton()
{

  const integId = urlParams.get("integId");
  const osyncId = urlParams.get("osyncId");
  const hash = urlParams.get("hash");

  $("#uploadFailed").hide();
  $("#uploadSuccessfully").hide();
  $("#emailErrorMessage").hide();
  $("#fnameErrorMessage").hide();
 
  
  var contactEmail = $("#contactEmail").val();
  var contactName = $("#contactName").val();

  var formData = new FormData();
  var files = $("#file")[0].files;
 
  contactName = $.trim(contactName);
  contactEmail = $.trim(contactEmail);
 
  if(contactName.length === 0){
    //no contact email given
    $("#fnameErrorMessage").show();
    $("#uploadButtonLoadingDiv").hide();
    return;
}

  if(contactEmail.length === 0){
    //no contact email given
    $("#emailErrorMessage").show();
    $("#uploadButtonLoadingDiv").hide();
    return;
}

  
  if(files.length > 0 )
  {
    $("#uploadErrorMessage").hide();
    $("#emailErrorMessage").hide();
    var fileName=files[0].name;
  var payload = {
    "requests": {
      "request_name": fileName,
      "actions": [
        {
          "recipient_name": contactName,
          "recipient_email": contactEmail,
          "recipient_phonenumber": "",
          "recipient_countrycode": "",
          "action_type": "SIGN",
          "private_notes": "Please get back to us for further queries",
          "signing_order": 0,
          "verify_recipient": true,
          "verification_type": "EMAIL",
          "verification_code": ""
        }
      ],
      "expiration_days": 1,
      "is_sequential": true,
      "email_reminders": true,
      "reminder_period": 8
    }
  }

    $("#uploadButtonLoadingDiv").show();
    var value=JSON.stringify(payload);
    formData.append("data", value);
    formData.append("file",files[0]);
    
    // var url = osyncUrl + '/api/v1/zoho-sign/documents/upload?integ_Id=' + integId+"&fname="+fname+"&emailId="+emailId;
    var url = osyncUrl + '/api/v1/zoho-sign/documents/upload?integ_Id=' + integId;
    $.ajax({
      url: url ,
      type: "POST",
      crossDomain: true,
      headers: {
        "Osync-Authorization": hash
      },
      processData: false,
      enctype : 'multipart/form-data',
      contentType : false,
      data: formData,
      success: function (response, textStatus, jqXHR) {
        $("#uploadSuccessfully").show();
        $("#uploadButtonLoadingDiv").hide();
      },
      error: function (jqXHR, textStatus, errorThrown) {
          $("#uploadFailed").show();
        $("#uploadButtonLoadingDiv").hide();
      },
      complete:function(){
              }
    });
  }
  else
  {
       $("#uploadErrorMessage").show();
  }
}
