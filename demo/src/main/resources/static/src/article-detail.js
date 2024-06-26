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
                let imageElement = ''; // Initialize an empty string for the image element

                // Check if article.image is not null or 'default'
                if (article.image !== null && article.image !== 'default') {
                    imageElement = `<img src="${article.image}" alt="${article.title}" style="width: 500px; height: 150px;">`;
                }

                articleDetailDiv.innerHTML = `
                <h2>${article.title}</h2>
                <p class="${categoryClass}">  # ${article.categoryId.category}</p>
                <p>${article.content}</p>
                ${imageElement} <!-- Insert the image element -->
                <div class="article-more-details">
                    <img src="${article.userId.image}" alt="${article.userId.nickname}" style="width: 25px; height: 25px; border-radius: 50%;">
                    <p> ${article.userId.nickname}</p>
                                <p>
                                <span class="like-icon" style="display: inline-block; width: 15px; height: 15px; background-image: url('/image/like-black.png'); background-size: cover;"></span> ${article.likeCount}
                                </p>  
                                <p>
                                <span class="comment-icon" style="display: inline-block; width: 14px; height: 14px; background-image: url('/image/comment.png'); background-size: cover;"></span> ${article.commentCount}
                                </p>                    
                                  <button id="likeButton" onclick="toggleLike(${article.id}, true)">
                                   <i class="fa-regular fa-thumbs-up"></i> Like
                                </button>
                </div>
            `;                // Fetch comments for the article
                fetch(`/api/1.0/articles/details/comments?id=${articleId}`)
                    .then(response => response.json())
                    .then(comments => {
                        // Create a div to hold comments
                        const commentsDiv = document.createElement('div');
                        commentsDiv.id = 'commentsDiv';
                        const commentsCount = document.getElementById('commentsCount');
                        // commentsCount.textContent += comments.length; // 設置評論數量為回應中物件的數量


                        // Populate comments in the HTML
                        comments.forEach(comment => {
                            const commentDiv = document.createElement('div');
                            const paragraph = document.createElement('p');

                            // Set the text content of the paragraph // Edit Here
                            // paragraph.innerHTML = `${comment.userId.nickname}:<br>${comment.content}`;
                            paragraph.innerHTML = `<span class="nickname">${comment.userId.nickname}:</span>${comment.content}`;


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
                        <textarea id="commentInput" placeholder="Say something..."></textarea>
                        <button onclick="postComment(${article.id})">Post</button> 
                       <!-- 把article.id送進post func-->
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


function postComment(articleId) {
    const commentInput = document.getElementById('commentInput').value;
    var token = localStorage.getItem('accessToken');


    if (commentInput.trim() === '') {
        alert('Please enter a comment.')
        return;
    }

    const newComment = {
        articleId: articleId,
        content: commentInput
    }


    fetch('/api/1.0/articles/details/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token // 在標頭中包含Bearer Token
        },
        body: JSON.stringify(newComment)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to post comment.');
            }
            document.getElementById('commentInput').value = ''; // 清除評論輸入框
            window.location.reload();
        })
        .catch(error => {
            console.error('Error posting comment:', error);
            alert('Failed to post comment. Please try again later.');
        });
}


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
                // const likeCountElement = document.getElementById('likeCount');
                // const currentLikeCount = parseInt(likeCountElement.innerText);
                // likeCountElement.innerText = currentLikeCount + 1;
                // if (response.status === 201) {
                //     likeCountElement.innerText = currentLikeCount + 1; // 點贊成功，數量 +1
                // } else if (response.status === 204) {
                //     likeCountElement.innerText = currentLikeCount - 1; // 取消點贊成功，數量 -1
                // }

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
            if (data) {
                accountBtn.src = data["data"]["image"];
                // 改圓角
                accountBtn.style.borderRadius = "50%";
                accountBtn.style.width = "32px"; /* 設置按鈕寬度 */
                accountBtn.style.height = "32px"; /* 設置按鈕高度 */

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
        console.log("No Access Tokne")
    } else {
        fetchUserProfile(accessToken);
    }
}
