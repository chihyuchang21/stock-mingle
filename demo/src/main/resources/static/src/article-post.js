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
            content: content
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
                    // Refresh article list
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
