document.addEventListener('DOMContentLoaded', function () {
    var articleForm = document.getElementById('articleForm');
    articleForm.addEventListener('submit', function (event) {
        event.preventDefault();

        var title = document.getElementById('title').value;
        var content = document.getElementById('content').value;
        var username = document.getElementById('username').value;

        // TODO: Authentication

        var article = {
            title: title,
            content: content,
            username: username
        };

        // Send message data to back end
        fetch('/api/1.0/articles', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
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
