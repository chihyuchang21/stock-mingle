// Check for access token
const accessToken = localStorage.getItem('accessToken');

function redirectToArticlePostPage() {
    window.location.href = 'article-post.html';
}

function redirectToLoginPage() {
    window.location.href = 'login.html';
}

function redirectToMatchPage() {
    window.location.href = 'match.html';
}

function publishClickEvent(categoryId) {
    var timestamp = new Date().getTime();

    // 從local storage中獲取Bearer Token
    var token = localStorage.getItem('accessToken');

    // send categoryId
    var userClickEvent = {
        categoryId: categoryId,
        timestamp: timestamp
    };

    fetch('/api/1.0/articles/click-events', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token // 在標頭中包含Bearer Token
        },
        body: JSON.stringify(userClickEvent)
    })
        .then(response => {
            if (response.ok) {
                console.log(userClickEvent);
                console.log("Click Event sent successfully!");
                // 跳轉到文章詳細內容
                window.location.href = "article-detail.html";
            } else {
                console.error("Click Event failed to send!");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function fetchArticles(pageNumber = 0) {
    // 清空文章列表和分頁按鈕
    var articleList = document.getElementById('articleList');
    articleList.innerHTML = '';

    var pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    // 發送分頁請求
    fetch(`/api/1.0/articles?paging=${pageNumber}`)
        .then(response => response.json())
        .then(data => {
            // 渲染文章列表
            data.forEach(article => {
                var articleDiv = document.createElement('div');

                // Listen for click event
                articleDiv.addEventListener('click', function () {
                    publishClickEvent(article.categoryId, article.timestamp);
                });

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <p>${article.content}</p>
                <p>Author: ${article.userId}</p>
                <p>Likes: ${article.likeCount}</p>
                <p>Comments: ${article.commentCount}</p>
                <hr>
            `;
                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 0; i <= 20; i++) {
                var pageButton = document.createElement('button');
                pageButton.textContent = i;
                pageButton.addEventListener('click', function () {
                    fetchArticles(i);
                });
                pagination.appendChild(pageButton);
            }
        })

        .catch(error => {
            console.error('Error:', error);
        });
}

function fetchArticlesByAlgo(pageNumber = 0) {
    // 清空文章列表和分頁按鈕
    var articleList = document.getElementById('articleList');
    articleList.innerHTML = '';

    var pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    const token = localStorage.getItem('accessToken');

    // 發送分頁請求
    fetch(`/api/1.0/articles/algo?paging=${pageNumber}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(data => {
            // 渲染文章列表
            data.forEach(article => {
                var articleDiv = document.createElement('div');

                // Listen for click event
                articleDiv.addEventListener('click', function () {
                    publishClickEvent(article.categoryId, article.timestamp);
                });

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <p>${article.content}</p>
                <p>Author: ${article.userId}</p>
                <p>Likes: ${article.likeCount}</p>
                <p>Comments: ${article.commentCount}</p>
                <hr>
            `;
                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 0; i <= 20; i++) {
                var pageButton = document.createElement('button');
                pageButton.textContent = i;
                pageButton.addEventListener('click', function () {
                    fetchArticlesByAlgo(i);
                });
                pagination.appendChild(pageButton);
            }
        })

        .catch(error => {
            console.error('Error:', error);
        });
}

// Get article list when the page loads
window.onload = function () {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        fetchArticles();
    } else {
        fetchArticlesByAlgo();
    }
};