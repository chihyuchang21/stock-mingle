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

function redirectToChatroomPage() {
    window.location.href = 'chatroom-sockjs.html';
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

// 設置當前頁面的按鈕樣式
function setActiveButton(pageNumber) {
    var pagination = document.getElementById('pagination');
    var buttons = pagination.getElementsByTagName('button');
    for (let i = 0; i < 20; i++) {
        if (parseInt(buttons[i].textContent) === pageNumber + 1) {
            buttons[i].classList.add('active');
        } else {
            buttons[i].classList.remove('active');
        }
    }
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
        .then(({totalPages, articles}) => { // 解構新數據結構
            // 渲染文章列表
            articles.forEach(article => {
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
<!--                <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="120" height="120">-->

                
<!--                暫時把image寫死-->
                    <div class="image-wrapper">
<!--                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="100" height="100">-->
                    </div>               
                    <div class="article-details">
                        <div class="user-info">
                            <img src="${article.userId.image}" alt="${article.userId.nickname}" style="width: 25px; height: 25px; border-radius: 50%;">
                            <p>${article.userId.nickname}</p>
                        </div>
                           <p>
                              <span class="like-icon" style="display: inline-block; width: 15px; height: 15px; background-image: url('/image/like-black.png'); background-size: cover;"></span> ${article.likeCount}
                           </p> 
                           <p>
                              <span class="comment-icon" style="display: inline-block; width: 14px; height: 14px; background-image: url('/image/comment.png'); background-size: cover;"></span> ${article.commentCount}
                           </p>
                    <p class="${categoryClass}"> # ${article.categoryId.category}</p>
                    </div>
                <hr>
            `;
                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕 (只顯示 1~4 頁)
            for (let i = 1; i <= Math.min(totalPages, 4); i++) {
                var pageButton = document.createElement('button');
                pageButton.textContent = i;
                if (i === pageNumber + 1) { // 添加激活的按鈕類
                    pageButton.classList.add('active');
                }
                pageButton.addEventListener('click', function () {
                    fetchArticles(i - 1); // 注意頁數從 0 開始
                    setActiveButton(i - 1);
                });
                pagination.appendChild(pageButton);
            }

            // 如果總頁數大於 4，顯示 "More" 按鈕
            if (totalPages > 4) {
                var moreButton = document.createElement('button');
                moreButton.textContent = 'More';
                moreButton.addEventListener('click', function () {
                    // 渲染剩餘頁數按鈕
                    for (let i = 4; i < totalPages; i++) {
                        var pageButton = document.createElement('button');
                        pageButton.textContent = i + 1;
                        pageButton.addEventListener('click', function () {
                            fetchArticles(i); // 注意頁數從 0 開始
                            setActiveButton(i - 1);
                        });
                        pagination.appendChild(pageButton);
                    }
                    // 移除 "More" 按鈕
                    pagination.removeChild(moreButton);
                });
                pagination.appendChild(moreButton);
            }

            // 顯示目前頁數
            // var pageInfo = document.createTextNode(`目前是 ${pageNumber + 1}/${totalPages} 頁`);
            // pagination.appendChild(pageInfo);

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

            console.log("fetched algo data: " + data);
            // 檢查返回的數據是否為空數組，代表沒有演算法計算的歷史資料
            if (data.length === 0) {
                // 如果是空數組，則調用 fetchArticles 函數
                fetchArticles(pageNumber);
                return; // 結束函數
            }

            // 渲染 Algo 文章列表
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
<!--                        <img src="https://img.money.com/2022/05/News-Plunging-Stocks-401k.jpg" alt="Stock Market!!" width="400" height="250">-->
                    </div>
                      <div class="article-details">
                         <div class="user-info">
                           <img src="${article.userId.image}" alt="${article.userId.nickname}" style="width: 25px; height: 25px; border-radius: 50%;">
                             <p>${article.userId.nickname}</p>
                            </div>
                            <p>
                            <span class="like-icon" style="display: inline-block; width: 15px; height: 15px; background-image: url('/image/like-black.png'); background-size: cover;"></span> ${article.likeCount}
                            </p>  
                            <p>
                            <span class="comment-icon" style="display: inline-block; width: 14px; height: 14px; background-image: url('/image/comment.png'); background-size: cover;"></span> ${article.commentCount}
                            </p>
                            <p class="${categoryClass}"> # ${article.categoryId.category}</p>
                           </div>
                <hr>
            `;

                articleList.appendChild(articleDiv);
            });

            // 渲染分頁按鈕
            for (let i = 1; i <= 4; i++) {
                var pageButton = document.createElement('button');
                pageButton.textContent = i;
                pageButton.classList.add('pagination-button'); // 添加類名
                if (i === pageNumber + 1) { // 添加激活的按鈕類
                    pageButton.classList.add('active');
                }
                pageButton.addEventListener('click', function () {
                    fetchArticlesByAlgo(i - 1);
                    setActiveButton(i - 1);

                });
                pagination.appendChild(pageButton);
            }
        })

        .catch(error => {
            console.error('Error:', error);
        });
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
                        document.querySelector('.dow-jones').textContent = `Dow Jones ${value}`;
                        break;
                    case 'S&P 500':
                        document.querySelector('.sp-500').textContent = `S&P 500  ${value}`;
                        break;
                    case 'NASDAQ Composite':
                        document.querySelector('.nasdaq').textContent = `NASDAQ  ${value}`;
                        break;
                    case 'Philadelphia Semiconductor Index':
                        document.querySelector('.philadelphia').textContent = `Philadelphia ${value}`;
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

// Function to search articles based on keyword
function searchArticles(keyword) {
    // Check if the keyword is empty
    if (keyword === '') {
        alert('Please enter a keyword to search.');
        return;
    }

    // Construct the search API URL with the keyword
    const apiUrl = `/api/1.0/articles/search?keyword=${encodeURIComponent(keyword)}`;

    // Fetch articles based on the keyword
    fetch(apiUrl)
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to fetch articles.');
            }
        })
        .then(data => {
            // Function to render articles on the page
            function renderArticles(articles) {
                // Get the article list container
                const articleList = document.getElementById('articleList');

                // Clear previous articles
                articleList.innerHTML = '';

                // Check if the data contains the "data" key and it is an array
                if (Array.isArray(articles.data)) {
                    // Iterate through each article
                    articles.data.forEach(article => {
                        // Truncate content if it exceeds maxContentLength
                        var content = article.content;
                        var maxContentLength = 300;
                        var truncatedContent = content.length > maxContentLength ? content.substring(0, maxContentLength) + "..." : content;

                        // 根據文章類別設定class
                        var categoryClass = getCategoryClass(article.categoryId.category);

                        // Create a div element for the article
                        const articleDiv = document.createElement('div');
                        articleDiv.classList.add('article-div'); // Add a class for styling

                        articleDiv.addEventListener('click', function () {
                            fetchArticleDetails(article.id); // 發到後端
                        });

                        // Set the HTML content for the article div
                        articleDiv.innerHTML = `
                            <h3>${article.title}</h3>
                            <div class="content">${truncatedContent}</div>
                            <div class="article-details">
                                 <div class="user-info">
                                    <img src="${article.userId.image}" alt="${article.userId.nickname}" style="width: 25px; height: 25px; border-radius: 50%;">
                                    <p>${article.userId.nickname}</p>
                                </div>
                                <p>
                                <span class="like-icon" style="display: inline-block; width: 15px; height: 15px; background-image: url('/image/like-black.png'); background-size: cover;"></span> ${article.likeCount}
                                </p>  
                                <p>
                                <span class="comment-icon" style="display: inline-block; width: 14px; height: 14px; background-image: url('/image/comment.png'); background-size: cover;"></span> ${article.commentCount}
                                </p>
                                <p class="${categoryClass}"> # ${article.categoryId.category}</p>
                            </div>
                            <hr>
                        `;

                        // Append the article div to the article list container
                        articleList.appendChild(articleDiv);
                    });
                } else {
                    console.error('Data is not in the expected format:', articles);
                }
            }

            // Render articles based on the search result
            renderArticles(data);
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

// Add click event listener to the search button
searchButton.addEventListener('click', function () {
    // Get the keyword from the search input
    const keyword = searchInput.value.trim();

    // Call the searchArticles function with the keyword
    searchArticles(keyword);
});


// Get article list when the page loads
window.onload = function () {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        fetchArticles();
        fetchStockIndex();
    } else {
        fetchArticlesByAlgo();
        fetchStockIndex();
    }
};