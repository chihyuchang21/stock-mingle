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
        showMessage(pairingHistoryId);
        console.log('Parsed content:', JSON.parse(chats.body).content); // JSON Parse的結果
    });
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
    const userId = $("#name").val();
    const content = $("#content").val(); // 假設你有一個表單元素用於輸入 content
    const params = new URLSearchParams(window.location.search);
    const pairingHistoryId = params.get('pairingHistoryId');
    // const userPairingHistoryId = $("#userPairingHistoryId").val(); // 從界面獲取 userPairingHistoryId
    const destination = "/app/hello/" + pairingHistoryId; // 要進入的頻道

    const timestamp = new Date().getTime(); //現在時間
    console.log('Sending message to:', destination);

    stompClient.publish({
        destination: destination,
        body: JSON.stringify({
            'userId': userId,
            'content': content,
            'userPairingHistoryId': pairingHistoryId,
            'sendTime': timestamp
        })
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
                $("#greetings").append(`<tr><td style="text-align: left;">${message.content}</td><td style="text-align: right;">${message.sendTime}</td></tr>`);
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}


$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendName());
});
