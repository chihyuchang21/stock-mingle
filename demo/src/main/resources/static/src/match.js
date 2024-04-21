function redirectToMatchPage() {
    window.location.href = 'match.html';
}


const accessToken = localStorage.getItem('accessToken');

if (accessToken) {
    fetchTodayMatch(accessToken);
} else {
    console.log("No Access Token")
}

async function fetchTodayMatch(token) {
    try {
        const response = await fetch(`/api/1.0/user/match-today`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Match fetch failed');
        }

        const {data} = await response.json();

        console.log(data[0].nickname); // 將第一個元素的暱稱印出

        document.getElementById('today-match').innerText = "Today's Match: " + data[0].nickname; // 將第一個元素的暱稱顯示在 id 為 today-match 的元素中

    } catch (error) {
        console.error('Error:', error);
    }
}
