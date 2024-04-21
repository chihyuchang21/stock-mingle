function redirectToMatchPage() {
    window.location.href = 'match.html';
}


const accessToken = localStorage.getItem('accessToken');

if (accessToken) {
    fetchTodayMatch(accessToken);
} else {
    console.log("No Access Token")
}

let currentIndex = localStorage.getItem('currentIndex') ? parseInt(localStorage.getItem('currentIndex')) : 0; //从 localStorage 中获取 currentIndex，如果不存在则设置为 0

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

        if (currentIndex < data.length) {
            const nickname = data[currentIndex].nickname;
            currentIndex++; // 更新 index
            localStorage.setItem('currentIndex', currentIndex); // 把currentIndex存在localstorage中
            console.log(nickname);
            document.getElementById('today-match').innerText = nickname; // 渲染後端傳回來的index
        } else {
            console.log("No more nicknames available");
        }

    } catch (error) {
        console.error('Error:', error);
    }
}

