function publishArticle() {
    // pop up input space
    var title = prompt("請輸入文章標題:");
    var content = prompt("請輸入文章內容:");
    var username = prompt("請輸入您的用戶名:");

    // send to backend
    var article = {
        title: title,
        content: content,
        username: username
    };

    fetch('/postArticle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(article)
    })
        .then(response => {
            if (response.ok) {
                alert("文章發佈成功！");
                // 刷新文章列表
                fetchArticles();
            } else {
                alert("文章發佈失敗！");
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("文章發佈失敗！");
        });
}

function publishClickEvent(categoryId) {

    var timestamp = new Date().getTime();

    // 發送categoryId
    var userClickEvent = {
        categoryId: categoryId,
        timestamp: timestamp
    };



    fetch('/postClickEvent', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(userClickEvent)
    })
        .then(response => {
            if (response.ok) {
                console.log(userClickEvent);
                console.log("Click Event發送成功！");

            } else {
                console.error("Click Event發送失敗！");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}




function fetchArticles() {
    // 渲染到頁面上
    fetch('/getAllArticle')
        .then(response => response.json())
        .then(articles => {
            var articleList = document.getElementById('articleList');
            articleList.innerHTML = ''; // 清空之前的文章列表

            articles.forEach(article => {
                var articleDiv = document.createElement('div');

                //聽取點擊事件
                articleDiv.addEventListener('click', function() {
                    publishClickEvent(article.categoryId, article.timestamp);
                });


                articleDiv.innerHTML = `
                    <h3>${article.title}</h3>
                    <p>${article.content}</p>
                    <p>作者：${article.username}</p>
                    <hr>
                `;
                articleList.appendChild(articleDiv);
            });
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

// 頁面加載時獲取文章列表
window.onload = function() {
    fetchArticles();
};
