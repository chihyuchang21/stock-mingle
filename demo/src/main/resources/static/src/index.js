/*
Redirect: window.location.href
*/
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

// Get the navbar h1 element
const navbarTitle = document.querySelector('.navbar h1');

// Add click event listener to the navbar h1 element
navbarTitle.addEventListener('click', function () {
    // Redirect to index.html
    window.location.href = 'index.html';
});


/*
ClickEvent
*/
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
            } else {
                console.error("Click Event failed to send!");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

/*
ArticleDetails
*/
function fetchArticleDetails(articleId) {
    // Check for access token
    const token = localStorage.getItem('accessToken');

    fetch(`/api/1.0/articles/details?id=${articleId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => response.json())
        .then(article => {
            // 跳轉到文章詳細頁面並帶著文章ID
            window.location.href = `article-detail.html?id=${article.id}`;

            // Debugging: log the fetched article details
            console.log(article);
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

/*
fetchArticle (for guest)
*/
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

                // Listen for click event --> 傳送article ID查詢文章詳細內容
                articleDiv.addEventListener('click', function () {
                    fetchArticleDetails(article.id); // 發到後端
                });
                console.log("article.id: " + article.id);

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <p>${article.content}</p>
<!--                暫時把image寫死-->
                    <div class="image-wrapper">
                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="400" height="250">
                    </div>               
                     <div class="article-details">
                    <p>Author: ${article.userId}</p>
                    <p>Likes: ${article.likeCount}</p>
                    <p>Comments: ${article.commentCount}</p>
                    <p>Category: ${article.categoryId.category}</p>
                </div>
                <hr>
            `;
                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 0; i <= 50; i++) {
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

/*
fetchArticle (for member, with Algo)
*/
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

                // Listen for click event --> 統計點擊次數
                articleDiv.addEventListener('click', function () {
                    publishClickEvent(article.categoryId, article.timestamp);
                });

                // Listen for click event --> 傳送article ID查詢文章詳細內容
                articleDiv.addEventListener('click', function () {
                    fetchArticleDetails(article.id); // 發到後端
                });
                console.log("article.id: " + article.id);

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <p>${article.content}</p>
<!--                暫時把image寫死-->
                    <div class="image-wrapper">
                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="400" height="250">
                    </div>
                <div class="article-details">
                    <p>Author: ${article.userId}</p>
                    <p>Likes: ${article.likeCount}</p>
                    <p>Comments: ${article.commentCount}</p>
                    <p>Category: ${article.categoryId.category}</p>
                </div>
                <hr>
            `;
                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 0; i <= 5; i++) {
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