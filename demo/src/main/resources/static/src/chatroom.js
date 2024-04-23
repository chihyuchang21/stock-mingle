const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/gs-guide-websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    // const userPairingHistoryId = $("#userPairingHistoryId").val(); // 先填寫userPairingHistoryId(頻道

    const params = new URLSearchParams(window.location.search);

    const pairingHistoryId = params.get('pairingHistoryId');
    console.log(pairingHistoryId);

    const subscriptionPath = '/topic/chats/' + pairingHistoryId;
    console.log('Subscribing to path:', subscriptionPath); // 訂閱的路徑

    stompClient.subscribe(subscriptionPath, (chats) => {
        console.log('Received Message:', chats.body);
        console.log('Parsed content:', JSON.parse(chats.body).content); // JSON Parse的結果

        // 解析新訊息
        const newMessage = JSON.parse(chats.body);

        // 在前端顯示新訊息
        $("#greetings").append(`<tr><td style="text-align: left;">${newMessage.content}</td><td style="text-align: right;">${newMessage.formattedSendTime}</td></tr>`);

    });

    showMessage(pairingHistoryId);
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    const content = $("#content").val(); // 假設你有一個表單元素用於輸入 content
    const params = new URLSearchParams(window.location.search);
    const pairingHistoryId = params.get('pairingHistoryId');
    const token = localStorage.getItem('accessToken');

    // 使用 fetch 函數獲取使用者(sender)的資訊
    fetch('/api/1.0/user/profile', {
        headers: {
            'Authorization': 'Bearer ' + token // 將 access token 放在 Authorization header 中
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user profile');
            }
            return response.json();
        })
        .then(data => {
            console.log('Fetched user profile:', data); // 輸出 fetch 到的資料

            const senderUserId = data.data.id; // 從回傳的資料中獲取 senderId，記得要用data.data.id
            const senderNickname = data.data.nickname; // 獲取用戶的暱稱


            console.log("senderId:" + senderUserId);
            console.log("data.id:" + data.data.id);

            const timestamp = new Date().getTime(); //現在時間
            const destination = "/app/hello/" + pairingHistoryId; // 要進入的頻道

            console.log('Sending message to:', destination);

            // 發送訊息
            stompClient.publish({
                destination: destination,
                body: JSON.stringify({
                    'senderUserId': senderUserId,
                    'content': senderNickname + ': ' + content, // 包含暱稱的訊息內容
                    'userPairingHistoryId': pairingHistoryId,
                    'sendTime': timestamp
                })
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}


function showMessage(pairingHistoryId) {
    fetch(`/api/1.0/messages?userPairingHistoryId=${pairingHistoryId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch messages');
            }
            return response.json();
        })
        .then(data => {
            console.log('Messages:', data);
            // 在這裡處理從後端返回的訊息，將其顯示在前端頁面上
            data.forEach(message => {
                $("#greetings").append(`<tr><td style="text-align: left;">${message.content}</td><td style="text-align: right;">${message.formattedSendTime}</td></tr>`);
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });


}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    // $("#connect").click(() => connect());
    connect();
    // $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendName());
});
