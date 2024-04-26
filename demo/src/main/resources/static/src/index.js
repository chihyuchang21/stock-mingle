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
                articleDiv.classList.add('article-div'); // 添加 class 為 article-div


                // Listen for click event
                articleDiv.addEventListener('click', function () {
                    publishClickEvent(article.categoryId, article.timestamp);
                });

                // Listen for click event --> 傳送article ID查詢文章詳細內容
                articleDiv.addEventListener('click', function () {
                    fetchArticleDetails(article.id); // 發到後端
                });
                console.log("article.id: " + article.id);

                var content = article.content;
                var maxContentLength = 300; // 文章最長內容

                // 若大於最長內容則只顯示 300 字
                var truncatedContent = content.length > maxContentLength ? content.substring(0, maxContentLength) + "..." : content;

                // 根據文章類別設定class
                var categoryClass = getCategoryClass(article.categoryId.category);

                // Convert Markdown to HTML
                // var markdownContent = article.content;
                // var htmlContent = marked(markdownContent);

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <div class="content">${truncatedContent}</div>
                
<!--                暫時把image寫死-->
                    <div class="image-wrapper">
                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="400" height="250">
                    </div>               
                     <div class="article-details">
                    <p>Author: ${article.userId.nickname}</p>
                    <p>Likes: ${article.likeCount}</p>
                    <p>Comments: ${article.commentCount}</p>
                    <p class="${categoryClass}"> # ${article.categoryId.category}</p>
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
                articleDiv.classList.add('article-div'); // 添加 class 為 article-div


                // Listen for click event --> 統計點擊次數
                articleDiv.addEventListener('click', function () {
                    publishClickEvent(article.categoryId, article.timestamp);
                });

                // Listen for click event --> 傳送article ID查詢文章詳細內容
                articleDiv.addEventListener('click', function () {
                    fetchArticleDetails(article.id); // 發到後端
                });
                console.log("article.id: " + article.id);

                var content = article.content;
                var maxContentLength = 300; // 文章最長內容

                // 若大於最長內容則只顯示 300 字
                var truncatedContent = content.length > maxContentLength ? content.substring(0, maxContentLength) + "..." : content;

                // 根據文章類別設定class
                var categoryClass = getCategoryClass(article.categoryId.category);

                articleDiv.innerHTML = `
                <h3>${article.title}</h3>
                <div class="content">${truncatedContent}</div>
<!--                暫時把image寫死-->
                    <div class="image-wrapper">
                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="400" height="250">
                    </div>
                <div class="article-details">
                    <p>Author: ${article.userId.nickname}</p>
                    <p>Likes: ${article.likeCount}</p>
                    <p>Comments: ${article.commentCount}</p>
                    <p class="${categoryClass}"> # ${article.categoryId.category}</p>
                </div>
                <hr>
            `;

                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 0; i <= 3; i++) {
                var pageButton = document.createElement('button');
                pageButton.textContent = i;
                pageButton.classList.add('pagination-button'); // 添加類名
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

function getCategoryClass(category) {
    switch (category) {
        case 'Company News':
            return 'company-news';
        case 'Broad Market News':
            return 'broad-market-news';
        case 'Company Discussion':
            return 'company-discussion';
        case 'Advice Request':
            return 'advice-request';
        default:
            return 'other';
    }
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