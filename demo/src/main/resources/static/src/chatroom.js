/*
Redirect: window.location.href
*/

function redirectToArticlePostPage() {
    window.location.href = 'article-post.html';
}

function redirectToLoginPage() {
    window.location.href = 'login.html';
}

function redirectToMatchPage() {
    window.location.href = 'match.html';
}

function logoutClearLocalStorage() {
    localStorage.removeItem('accessToken');
    window.alert("You have logged out.");
    setTimeout(function () {
        window.location.href = 'index.html';
    }, 1000); // 在跳轉前等待 1 秒 (1000 毫秒)
}

// Get the navbar h1 element
const navbarTitle = document.querySelector('.navbar h1');

// Add click event listener to the navbar h1 element
navbarTitle.addEventListener('click', function () {
    // Redirect to index.html
    window.location.href = 'index.html';
});


function renderChatrooms(chatrooms) {
    const chatroomList = document.getElementById('chatroom-list');
    const ul = chatroomList.querySelector('ul');

    // 清空原有的列表
    ul.innerHTML = '';

    // 生成新的聊天室連結在側邊欄
    chatrooms.forEach(chatroom => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = `chatroom.html?pairingHistoryId=${chatroom}`;
        a.textContent = `Chatroom ${chatroom}`;
        li.appendChild(a);
        ul.appendChild(li);
    });
}


// 在頁面加載時呼叫API獲取聊天室列表並渲染到頁面上
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('accessToken');
    const headers = {
        'Authorization': 'Bearer ' + token
    }


    // 發送 API 請求，並添加 headers
    fetch('/api/1.0/messages/chatroom', {
        headers: headers
    })
        .then(response => response.json())
        .then(data => {
            renderChatrooms(data);
            console.log(data);
        })
        .catch(error => {
            console.error('Error fetching chatrooms:', error);
        });
});


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

        // 獲取當前時間
        const currentTime = formatDate(new Date());

        // 使用新消息的時間，如果不存在則使用當前時間
        const messageTime = newMessage.formattedSendTime ? newMessage.formattedSendTime : currentTime;

        // 在前端顯示新訊息
        $("#messages").append(`<tr><td style="text-align: left;">${newMessage.content}</td><td style="text-align: right;">${messageTime}</td></tr>`);

    });

    showMessage(pairingHistoryId);
};

// 格式化日期和時間
function formatDate(date) {
    console.log("原date: " + date);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    console.log("後date: " + `${month}/${day} ${hours}:${minutes}`);
    return `${month}/${day} ${hours}:${minutes}`;
}

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
    $("#messages").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

// 清空消息框内容
function clearMessageBox() {
    $("#content").val("");
}


function sendName() {
    const content = $("#content").val(); // 假設你有一個表單元素用於輸入 content
    // var content = tinymce.activeEditor.getContent(); // TinyMCE
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

            clearMessageBox(); // 發送msg後清空訊息

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
                $("#messages").append(`<tr><td style="text-align: left;">${message.content}</td><td style="text-align: right;">${message.formattedSendTime}</td></tr>`);
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


