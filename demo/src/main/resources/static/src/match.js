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


const accessToken = localStorage.getItem('accessToken');

if (accessToken) {
    fetchTodayMatch(accessToken);
} else {
    console.log("No Access Token")
}

let currentIndex = localStorage.getItem('currentIndex') ? parseInt(localStorage.getItem('currentIndex')) : 0; //从 localStorage 中获取 currentIndex，如果不存在则设置为 0

async function fetchTodayMatch(token) {
    try {
        const response = await fetch(`/api/1.0/user/match-today`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Match fetch failed');
        }

        const {data} = await response.json();

        if (currentIndex < data.length) {
            const nickname = data[currentIndex].nickname;
            const image = data[currentIndex].image;
            const pairingHistoryId = data[currentIndex].pairingHistoryId;

            currentIndex++; // 更新 index
            localStorage.setItem('currentIndex', currentIndex); // 把currentIndex存在localstorage中
            // 創建新的 HTML 元素，顯示 nickname 和 image
            const matchDiv = document.getElementById('today-match');
            matchDiv.innerHTML = `
                <div class="match-info">
                    <h1>Today's Perfect Match </h1>
                    <img src="${image}" alt="${nickname}" class="match-avatar">
                    <p class="match-nickname">${nickname}</p>
                 
                    <button class="enter-chat-btn" onclick="redirectToChatRoom('${pairingHistoryId}')">Go to Chatroom</button>

                </div>
            `;
        } else {
            console.log("No more nicknames available");
        }

    } catch (error) {
        console.error('Error:', error);
    }
}


function redirectToChatRoom(pairingHistoryId) {
    window.location.href = `chatroom.html?pairingHistoryId=${pairingHistoryId}`;
}

