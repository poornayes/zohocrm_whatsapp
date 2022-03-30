var SMSServiceController = {
    getAssigner: function (providerName,rightServiceName) {
        var assigner;
        if (providerName === "zc"){  
            var urlParamsObj = new URLSearchParams(window.location.search);
            rightServiceName = urlParamsObj.get("rightServiceName");
            if(rightServiceName==null)
            rightServiceName=urlParamsObj.get("rightService");
            if (rightServiceName === "whatsapp"){
                assigner = ZohoWhatsappCRM;
            } else {
                assigner = ZohoCRM;
            }
            
        }else if(providerName === "pd"){
            assigner = PipeDriveSMS;
        }else if(providerName === "hubspot"){
            console.log("proName"+providerName);
            assigner = Hubspot;
        } else if( providerName == "ShopRC") {
            console.log("proName >>>>> "+Shopify);
            assigner = Shopify;
        }

        return assigner;
    }
}
