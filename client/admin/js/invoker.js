function invokeGetRequest(requestUrl) {
    return new Promise((resolve, reject) => {
        var url = requestUrl;
        jQuery.ajax({
            "url": url,
            "type": "GET",
            "contentType": "application/json",
            "dataType": "json",
            crossDomain: true,
            "success": function (response) {
               resolve(response);
            },
            "error": function (error) {
                reject(error);
            }
        });
    });
}

function invokePostRequest(requestUrl,postBody) {
    return new Promise((resolve, reject) => {
        var url = requestUrl;
        jQuery.ajax({
            "url": url,
            "type": "POST",
            "data": JSON.stringify(postBody),
            "contentType": "application/json",
            "dataType": "json",
            crossDomain: true,
            "success": function (response) {
                console.log(response);
                resolve(response);
            },
            "error": function (error) {
                console.log(error);
                reject(error);
            }
        });
    });
}

function invokePostRequestToSetCookie(requestUrl, data) {
    return new Promise((resolve, reject) => {
        var url = requestUrl;
        $.ajax({
            url: url,
            type: "POST",
            data: JSON.stringify(data),
            crossDomain: true,
            context: data,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            xhrFields: {
                withCredentials: true
            },
            success: function (data, textStatus, jqXHR) {
                args = data;
                resolve(data);
            },
            complete : function(data,response){
                console.log("complete called bayya");
            },
            done : function(data,response){
                console.log("done called bayya");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
            }
        });
    });
}

function invokePostRequestToSetCookie(requestUrl,postBody) {
    return new Promise((resolve, reject) => {
        var url = requestUrl;
        jQuery.ajax({
            "url": url,
            "type": "POST",
            "data": JSON.stringify(postBody),
            "contentType": "application/json",
            "dataType": "jsonp",
            crossDomain: true,
            "xhrFields": {
                "withCredentials": true
            },
            "success": function (response) {
                console.log(response);
                resolve(response);
            },
            "error": function (error) {
                console.log(error);
                reject(error);
            }
        });
    });
}
