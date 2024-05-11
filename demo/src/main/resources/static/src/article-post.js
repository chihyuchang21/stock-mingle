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


function logoutClearLocalStorage() {
    localStorage.removeItem('accessToken');
    window.alert("You have logged out.");
    setTimeout(function () {
        window.location.href = 'index.html';
    }, 1000); // 在跳轉前等待 1 秒 (1000 毫秒)
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

// Get the navbar h1 element
const navbarTitle = document.querySelector('.navbar h1');

// Add click event listener to the navbar h1 element
navbarTitle.addEventListener('click', function () {
    // Redirect to index.html
    window.location.href = 'index.html';
});


document.addEventListener('DOMContentLoaded', function () {
    var articleForm = document.getElementById('articleForm');
    articleForm.addEventListener('submit', function (event) {
        event.preventDefault();

        var title = document.getElementById('title').value;
        // var content = document.getElementById('content').value;
        var content = tinymce.activeEditor.getContent(); // 使用TinyMCE提供的方法获取内容
        var categoryId = {
            id: document.getElementById('category').value,
            category: document.getElementById('category').options[document.getElementById('category').selectedIndex].text
        };

        // 從local storage中獲取Bearer Token
        var token = localStorage.getItem('accessToken');

        // 構建article對象
        var article = {
            title: title,
            categoryId: categoryId,
            content: content,
            likeCount: 0,
            commentCount: 0,
        };

        // 送到後端的data
        console.log('Sending data to backend:', article);

        // Send message data to back end
        fetch('/api/1.0/articles', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token // 在標頭中包含Bearer Token
            },
            body: JSON.stringify(article)
        })
            .then(response => {
                if (response.ok) {
                    alert("Article published successfully!");
                    // Clear input fields
                    document.getElementById('title').value = '';
                    tinymce.activeEditor.setContent(''); // Clear content in TinyMCE editor
                    document.getElementById('category').selectedIndex = 0; // Reset category selection
                    // TODO: fetchArticles();
                } else {
                    alert("Failed to publish article!");
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("Failed to publish article!");
            });
    });
});

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