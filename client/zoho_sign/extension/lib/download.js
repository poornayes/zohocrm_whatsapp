var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

function downloadSignedDocument() {
    const urlParams = new URLSearchParams(window.location.search);
    const integId = urlParams.get("integId");
    const osyncId = urlParams.get("osyncId");
    const hash = urlParams.get("hash");
    const requesId=urlParams.get("requestId");
    
    var url = osyncUrl + '/api/v1/zoho-sign/document/download/' + requesId + '?integ_Id=' + integId;
    $.ajax({
        url: url,
        type: "GET",
        crossDomain: true,
        datatype: 'json',
        headers: {
            'Accept': "application/pdf",
            "Content-Type": "application/pdf",
            'Osync-Authorization': hash
        },
        success: function (response, textStatus, jqXHR) {
        },
        error: function (jqXHR, textStatus, errorThrown) {
        }
    });
}
