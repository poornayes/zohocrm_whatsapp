var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

function templatelist() {
    var name = $(".tempname");
    var selectedname = "";
    for (i = 0; i < name.length; i++) {
        if (name[i].checked) {
            selectedname += name[i].value;
        }
    }
    $("#templatedetails").href = osyncUrl + "/envelope?Email={{email}}&integid={{integid}}&hash={{hash}}&Firstname={{fname}}&" + selectedname;
}

function search() {
    let input = $('#searchbar').val();
    var searchTerm = input.toLowerCase();

    $("td.templateName").each(function () {
        if ($(this).text().search(new RegExp(searchTerm, "i")) < 0) {
            $(this).parent("tr").hide();
        } else {
            $(this).parent("tr").show();
        }
    });
}

const urlParams = new URLSearchParams(window.location.search);
const integId = urlParams.get("integId");
const osyncId = urlParams.get("osyncId");
const hash = urlParams.get("hash");

function getAllTemplates() {
    var url = osyncUrl + '/api/v1/zoho-sign/templates?integ_Id=' + integId;
    $.ajax({
        url: url,
        type: "GET",
        crossDomain: true,
        datatype: 'json',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Osync-Authorization': hash
        },
        success: function (response, textStatus, jqXHR) {
            var emptyObject = $.isEmptyObject(response);
            var text = "";
            if (emptyObject) {
                //show no templates found
                text = '<tr><td colspan="3" class="text-center pt-5">No templates found</td></tr>';
            } else {
                //show templates
                var allTemplates = response.templates;
                var template = $("#sign_templates").html();
                text = Mustache.render(template, allTemplates);
                $("#allTemplates").html(text).show();
            }
           
            const fname = urlParams.get("firstname");
          const emailId = urlParams.get("email");         
            $('.sendButton').attr("contact_name",fname);
            $('.sendButton').attr("contact_emailId",emailId);
            $("#listPage").show();
            $("#loadingDiv").removeClass('d-flex').hide();
        },
        error: function (jqXHR, textStatus, errorThrown) {
                }
    });
}

function sendTemplate(thisObj) {
    var owner_email = $(thisObj).attr("owner_email");
    var template_id = $(thisObj).attr("template_id");
    var owner_first_name = $(thisObj).attr("owner_first_name");
    var owner_last_name = $(thisObj).attr("owner_last_name");
    var template_name = $(thisObj).attr("template_name");
    var action_id = $(thisObj).attr("action_id");
    var contact_name = $(thisObj).attr("contact_name");
    var contact_emailId = $(thisObj).attr("contact_emailId");

    var templateObj = {
        owner_email: owner_email,
        template_id: template_id,
        owner_first_name: owner_first_name,
        owner_last_name: owner_last_name,
        template_name: template_name,
        action_id: action_id,
        contact_name : contact_name,
        contact_emailId : contact_emailId

    }
    $("#listPage").hide();
    var template = $("#send_template").html();  
    var text = Mustache.render(template, templateObj);
    $("#sendPage").html(text).show();
    
  var contactEmail = $("#contactEmail").val();
  var contactName = $("#contactName").val();

  if(contactName ==="null" && contactEmail ==="null"){

   var contactEmail = $("#contactEmail").val("");
  var contactName = $("#contactName").val("");
  
  }  

}

function closeWindow(){
	$("#sendPage").hide();
	$("#listPage").show();
}

function sendToReceipients() {

    $("#sendButtonLoadingDiv").show();
    $("#mailSentSuccess").hide();
    $("#mailSentFailed").hide();
    $("#emailErrorMessage").hide();
    $("#subjectErrorMessage").hide();

    var template_name=$("template_name").val();
    var templateId = $("#templateNameInSendPage").attr("template_id");
    var contactEmail = $("#contactEmail").val();
   var contactName = $("#contactName").val();
    var actionId = $("#templateNameInSendPage").attr("action_id");

    var subject = $("#subjectText").val();
    var message = $("#messageArea").val();

    contactEmail = $.trim(contactEmail);
    subject = $.trim(subject);

    if(contactEmail.length === 0){
        //no contact email given
        $("#emailErrorMessage").show();
        $("#sendButtonLoadingDiv").hide();
        return;
    }

    if(subject.length === 0){
        //no contact email given
        $("#subjectErrorMessage").show();
        $("#sendButtonLoadingDiv").hide();
        return;
    }

    var url = osyncUrl + '/api/v1/zoho-sign/template/' + templateId + '?integ_Id=' + integId
    var payload = {
        "templates":
        {
            "field_data": {
                "field_text_data": {},
                "field_boolean_data": {},
                "field_date_data": {}
            },
            "actions": [
                {
                    "action_id": actionId,
                    "action_type": "SIGN",
                    "recipient_name": contactName,
                    "role": "ts1",
                    "recipient_email": contactEmail,
                    "recipient_phonenumber": "",
                    "recipient_countrycode": "",
                    "private_notes": subject,
                    "verify_recipient": true,
                    "verification_type": "EMAIL"
                }
            ],
            "notes": message,
            "request_name" : template_name
          
        }
    };

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
            $("#sendButtonLoadingDiv").hide();
            $("#mailSentSuccess").show();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#sendButtonLoadingDiv").hide();
            $("#mailSentFailed").show();
        }
    });
}

