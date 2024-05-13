/*
Redirect: window.location.href
*/

function redirectToArticlePostPage() {
    // Check for access token
    const accessToken = localStorage.getItem('accessToken');
    // If access token is not found
    if (!accessToken) {
        // Display alert message
        alert("Please log in or sign up first.");
        // Redirect to login page
        window.location.href = 'login.html';
    } else {
        // Redirect to match page
        window.location.href = 'article-post.html';
    }
}

function redirectToLoginPage() {
    window.location.href = 'login.html';
}

function redirectToChatroomPage() {
    // Check for access token
    const accessToken = localStorage.getItem('accessToken');
    // If access token is not found
    if (!accessToken) {
        // Display alert message
        alert("Please log in or sign up first.");
        // Redirect to login page
        window.location.href = 'login.html';
    } else {
        // Redirect to match page
        window.location.href = 'chatroom-sockjs.html';
    }
}

// 要先登入
function redirectToMatchPage() {
    // Check for access token
    const accessToken = localStorage.getItem('accessToken');

    // If access token is not found
    if (!accessToken) {
        // Display alert message
        alert("Please log in or sign up first.");
        // Redirect to login page
        window.location.href = 'login.html';
    } else {
        // Redirect to match page
        window.location.href = 'match.html';
    }
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
    fetchStockIndex();
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

        // DB中還沒有產生配對，印出配對說明
        if (Array.isArray(data) && data.length === 0) {
            const matchDiv = document.getElementById('today-match');
            matchDiv.innerHTML = `
        <div class="match-info">
            <h1>How Stock Mingle Match ?</h1>
            <img src="image/question-mark.png" alt="default" class="match-avatar">
            <p class="match-nickname">A 24-hour fate awaits here.<br>At midnight, this page will automatically pair you with a friend.<br>If either of you fails to join the chat, the fate will vanish forever, never to appear again...</p>
            <div id="countdown"></div>
        </div>
    `;

            // 設定目標時間為今天午夜
            const targetTime = new Date();
            targetTime.setHours(24, 0, 0, 0); // 設定為午夜
            const countdownElement = document.getElementById('countdown');

            function updateCountdown() {
                const now = new Date();
                const difference = targetTime.getTime() - now.getTime();

                if (difference <= 0) {
                    countdownElement.textContent = "倒數結束";
                    return;
                }

                const hours = Math.floor(difference / (1000 * 60 * 60));
                const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
                const seconds = Math.floor((difference % (1000 * 60)) / 1000);

                countdownElement.textContent = `${hours} hours ${minutes} minutes ${seconds} seconds until midnight`;
            }

            // 每秒更新一次倒數計時器
            setInterval(updateCountdown, 1000);
        }

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
                 
                    <button class="enter-chat-btn" onclick="redirectToChatRoom('${pairingHistoryId}')">Go to Chatroom</button><br>
            <div id="countdown"></div>
        </div>
    `;

            // 設定目標時間為今天午夜
            const targetTime = new Date();
            targetTime.setHours(24, 0, 0, 0); // 設定為午夜
            const countdownElement = document.getElementById('countdown');

            function updateCountdown() {
                const now = new Date();
                const difference = targetTime.getTime() - now.getTime();

                if (difference <= 0) {
                    countdownElement.textContent = "倒數結束";
                    return;
                }

                const hours = Math.floor(difference / (1000 * 60 * 60));
                const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
                const seconds = Math.floor((difference % (1000 * 60)) / 1000);

                countdownElement.textContent = `${hours} hours ${minutes} minutes ${seconds} seconds until midnight`;
            }

            // 每秒更新一次倒數計時器
            setInterval(updateCountdown, 1000);
        } else {
            console.log("No more nicknames available");
        }

    } catch (error) {
        console.error('Error:', error);
    }
}

function fetchStockIndex() {
    fetch('/api/1.0/stock-info-to-front-end')
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to fetch stock information.');
            }
        })
        .then(data => {
            // parse stock info
            data.forEach(stock => {
                const name = stock.name;
                const value = stock.value;

                // put info in elements
                switch (name) {
                    case 'Dow Jones Industrial Average':
                        document.querySelector('.dow-jones').textContent = `Dow Jones: ${value}`;
                        break;
                    case 'S&P 500':
                        document.querySelector('.sp-500').textContent = `S&P 500: ${value}`;
                        break;
                    case 'NASDAQ Composite':
                        document.querySelector('.nasdaq').textContent = `NASDAQ: ${value}`;
                        break;
                    case 'Philadelphia Semiconductor Index':
                        document.querySelector('.philadelphia').textContent = `Philadelphia: ${value}`;
                        break;
                    default:
                        break;
                }
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}


function redirectToChatRoom(pairingHistoryId, nickname) {
    console.log(nickname);
    window.location.href = `chatroom-sockjs.html?pairingHistoryId=${pairingHistoryId}`;
}

function fetchUserProfile(token) {
    fetch('/api/1.0/user/profile', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Network response was not ok.');
        })
        .then(data => {
            const accountBtn = document.querySelector('.account-btn');
            // 若 user 圖片存在，則把 account-btn 的 src 換成 image;
            // console.log(data);
            console.log("data.data.nickname: " + data["data"]["nickname"]);
            console.log("data.data.image: " + data["data"]["image"]);
            if (data) {
                accountBtn.src = data["data"]["image"];
                // 改圓角
                accountBtn.style.borderRadius = "50%";
                accountBtn.style.width = "32px"; /* 設置按鈕寬度 */
                accountBtn.style.height = "32px"; /* 設置按鈕高度 */

                console.log("accountBtn.src: " + accountBtn.src);
            }
        })
        .catch(error => {
            console.error('There has been a problem with your fetch operation:', error);
        });
}


// Get article list when the page loads
window.onload = function () {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
    } else {
        fetchUserProfile(accessToken);
    }
};
