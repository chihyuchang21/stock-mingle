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


document.addEventListener('DOMContentLoaded', function () {
    // Function to fetch article details based on the ID from query parameter
    function fetchArticleDetails() {
        // Get the article ID from the query parameter
        const urlParams = new URLSearchParams(window.location.search);
        const articleId = urlParams.get('id');

        console.log('Article ID:', articleId); // Add this line for debugging

        // Fetch article details from the backend
        fetch(`/api/1.0/articles/details?id=${articleId}`)
            .then(response => response.json())
            .then(article => {
                // Populate the article details in the HTML

                var categoryClass = getCategoryClass(article.categoryId.category);
                const articleDetailDiv = document.getElementById('articleDetail');
                articleDetailDiv.innerHTML = `
                    <h2>${article.title}</h2>
                    <p class="${categoryClass}">  # ${article.categoryId.category}</p>
                    <p>${article.content}</p>
                    <div class="article-more-details">
                        <p>Author: ${article.userId.nickname}</p>
                        <p>Comments: ${article.commentCount}</p>
                        <p>Likes: <span id="likeCount">${article.likeCount}</span></p> <!-- 使用 span 包點讚數字 -->
                        <button id="likeButton" onclick="toggleLike(${article.id}, true)">Like</button>
                        
                    </div>
                `;
                // Fetch comments for the article
                fetch(`/api/1.0/articles/details/comments?id=${articleId}`)
                    .then(response => response.json())
                    .then(comments => {
                        // Create a div to hold comments
                        const commentsDiv = document.createElement('div');
                        commentsDiv.id = 'commentsDiv';

                        // Populate comments in the HTML
                        comments.forEach(comment => {
                            const commentDiv = document.createElement('div');
                            const paragraph = document.createElement('p');

                            // Set the text content of the paragraph
                            paragraph.textContent = `${comment.userId.nickname}: ${comment.content}`;

                            // Add CSS class to the paragraph
                            paragraph.classList.add('commentParagraph');

                            // Append paragraph to commentDiv
                            commentDiv.appendChild(paragraph);

                            // Append commentDiv to commentsDiv
                            commentsDiv.appendChild(commentDiv);
                        });

                        // Create input container
                        const inputContainer = document.createElement('div');
                        inputContainer.classList.add('commentInputContainer');
                        inputContainer.innerHTML = `
                        <input type="text" id="commentInput" placeholder="在此輸入留言...">
                        <button onclick="postComment()">留言</button>
                        `;

                        // Append input container after commentsDiv
                        commentsDiv.appendChild(inputContainer);


                        // Append commentsDiv to articleDetailDiv
                        articleDetailDiv.appendChild(commentsDiv);
                    })
                    .catch(error => {
                        console.error('Error fetching comments:', error);
                    });

            })
            .catch(error => {
                console.error('Error fetching article details:', error);
            });
    }

    // Call the fetchArticleDetails function
    fetchArticleDetails();
});


function toggleLike(articleId) {
    // 禁用 Like 按鈕，避免重複點擊
    document.getElementById('likeButton').disabled = true;

    // 發送 POST 請求給後端的 /api/1.0/articles/like ，傳遞 articleId
    fetch('/api/1.0/articles/like', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({articleId: articleId})
    })
        .then(response => {
            if (response.ok) {
                // 如果請求成功，更新點讚數量
                const likeCountElement = document.getElementById('likeCount');
                const currentLikeCount = parseInt(likeCountElement.innerText);
                if (response.status === 201) {
                    likeCountElement.innerText = currentLikeCount + 1; // 點贊成功，數量 +1
                } else if (response.status === 204) {
                    likeCountElement.innerText = currentLikeCount - 1; // 取消點贊成功，數量 -1
                }
            } else {
                console.error('Failed to like/unlike article');
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
        case 'Broad market news':
            return 'broad-market-news';
        case 'Company Discussion':
            return 'company-discussion';
        case 'Advice Request':
            return 'advice-request';
        default:
            return 'other';
    }
}
