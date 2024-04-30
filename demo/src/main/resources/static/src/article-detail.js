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
                        <p>Likes: ${article.likeCount}</p>
                        <p>Comments: ${article.commentCount}</p>
                    </div>
                `;
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }

    // Call the fetchArticleDetails function when the page loads
    fetchArticleDetails();
});

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
