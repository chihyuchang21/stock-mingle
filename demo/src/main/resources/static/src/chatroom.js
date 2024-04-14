const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/gs-guide-websocket'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    const userPairingHistoryId = $("#userPairingHistoryId").val(); // 先填寫userPairingHistoryId(頻道
    const subscriptionPath = '/topic/chats/' + userPairingHistoryId;
    console.log('Subscribing to path:', subscriptionPath); // 訂閱的路徑
    stompClient.subscribe(subscriptionPath, (chats) => {
        console.log('Received greeting:', chats.body);
        showMessage(JSON.parse(chats.body).content);
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
    }
    else {
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
    const userPairingHistoryId = $("#userPairingHistoryId").val(); // 從界面獲取 userPairingHistoryId
    const destination = "/app/hello/" + userPairingHistoryId; // 要進入的頻道

    console.log('Sending message to:', destination);

    stompClient.publish({
        destination: destination,
        body: JSON.stringify({'userId': userId, 'content': content, 'userPairingHistoryId': userPairingHistoryId})
    });
}

function showMessage(chats) {
    console.log('Showing greeting:', chats); // 添加日志输出
    // $("#greetings").append("<tr><td>" + "ID: " + message.id + ", Name: " + message.userId + ", Content: " + message.content + "</td></tr>");
    $("#greetings").append("<tr><td>" + chats + "</td></tr>");

}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendName());
});
