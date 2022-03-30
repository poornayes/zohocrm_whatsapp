var SMSHandler = {
    service : "Zoho CRM",
    getNumbers : function(integId,hash){
        var url = osyncUrl + '/api/v1/omessage/' + integId + '/numbers';
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
                $.each(data.phone, function (index, value) {
                    var phoneNumber = {
                        "phone": value
                    };
                    phoneList.push(phoneNumber);
                });
                
                var template = $("#phone_template").html();
                var text = Mustache.render(template, phoneList);
                $("#twilioPhoneNumbers").html(text).show();
                $("#twilioPhoneNumbers").select2();
                $("#loadingSpinner").hide();
                $("#loadSMSFormPage").show();
                
                if (phone != null && phone != "") {
                    $("#toPhoneNumber").val(phone);
                } else if (mobile != null && mobile != "") {
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
    },
    getAllSavedNumbers : function(integId,hash){
        var url = osyncUrl + '/api/v1/omessage/' + integId + '/numbers';
        $.ajax({
            url: url,
            type: "GET",
            crossDomain: true,
            headers: {
                "Osync-Authorization": hash
            },
            success: function (response, textStatus, jqXHR) {
                resolve(response);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#uploadFailed").show();
                $("#uploadButtonLoadingDiv").hide();
            },
            complete: function () {
            }
        });
    },
    deleteNumber : function(){
        
    },
    saveNumber : function(){
        
    },
    send : function(){
        
    }
};

function getRandomId() {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < 8; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}