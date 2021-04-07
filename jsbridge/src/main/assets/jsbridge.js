//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if(window.jlpay){
       console.log('jlpay: exsited');
     return;
    }
    //回调的列表，存储回调列表信息
    var responseCallbacks = {};
    //每次收到自动加1的callbackId
    var callbackUnionId = 1;
     //用于转发消息到客户端
    //获取授权信息
    function getAuthInfo(data){
         send('getAuthInfo',data);
    }
    //定位
    function getLocation(data){
        send('getLocation',data);
    }
    //扫码
    function scanQRCode(data){
         send('scanQRCode',data);
    }
    //扫码
    function checkPermisssion(data){
         send('checkPermisssion',data);
    }

    //发送消息的通道, method(方法名）,message(消息体)
    function send(method,data){
        var success = data.success;
        var callbackId ='';
        //是否有回调对象
        if(success){
          //为当前callback生成一个callbackId
          callbackId = 'cb_id_' + (callbackUnionId++);
          //保存当前方法的callback对象
          responseCallbacks[callbackId]=success;
        }
        try{
            jsbridge.send(method,JSON.stringify(data),callbackId);
        }catch(e){
            console.log('js method error'+e);
        }
    }

    //用于接收来自客户端的信息
    function receive(messageJson){
        try{
            var message =  JSON.parse(messageJson);
            var callbackId = message.callbackId;
            if(callbackId){
               var jsonObject = JSON.parse(message.data);
               responseCallbacks[callbackId](jsonObject);
            }
        }catch(e){
            console.log('js parse error：'+e);
        }


    }


    function log(msg){
        console.log(msg);
    }

    window.jlpay = {
        getAuthInfo: getAuthInfo,
        getLocation: getLocation,
        scanQRCode: scanQRCode,
        checkPermisssion: checkPermisssion,
        receive: receive

    };


})();
