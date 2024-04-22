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
                const articleDetailDiv = document.getElementById('articleDetail');
                articleDetailDiv.innerHTML = `
                    <h2>${article.title}</h2>
                    <p>Category: ${article.categoryId.category}</p>
                    <p>Content: ${article.content}</p>
                    <p>Author: ${article.userId}</p>
                    <p>Likes: ${article.likeCount}</p>
                    <p>Comments: ${article.commentCount}</p>
                `;
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }

    // Call the fetchArticleDetails function when the page loads
    fetchArticleDetails();
});
