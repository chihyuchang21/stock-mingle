document.addEventListener('DOMContentLoaded', function () {
    var articleForm = document.getElementById('articleForm');
    articleForm.addEventListener('submit', function (event) {
        event.preventDefault();

        var title = document.getElementById('title').value;
        var content = document.getElementById('content').value;
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
