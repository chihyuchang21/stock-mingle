function redirectToArticlePostPage() {
    window.location.href = 'article-post.html';
}

function redirectToLoginPage() {
    window.location.href = 'login.html';
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

function fetchArticles() {
    // Render on the page
    fetch('/api/1.0/articles/guest')
        .then(response => response.json())
        .then(articles => {
            var articleList = document.getElementById('articleList');
            // articleList.innerHTML = ''; // Clear previous article list

            articles.forEach(article => {
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
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

// Get article list when the page loads
window.onload = function () {
    fetchArticles();
};
