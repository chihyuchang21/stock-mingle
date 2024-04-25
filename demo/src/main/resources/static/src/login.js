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

// Get the navbar h1 element
const navbarTitle = document.querySelector('.navbar h1');

// Add click event listener to the navbar h1 element
navbarTitle.addEventListener('click', function () {
    // Redirect to index.html
    window.location.href = 'index.html';
});


const signInForm = document.getElementById('sign-in-form');
const signUpForm = document.getElementById('sign-up-form');
const profileContainer = document.getElementById('profile-container');
const authContainer = document.getElementById('auth-container');

// Check for access token
const accessToken = localStorage.getItem('accessToken');
if (accessToken) {
    fetchUserProfile(accessToken);
} else {
    console.log("No Access Token")
    // authContainer.style.display = 'block';
}

async function fetchUserProfile(token) {
    try {
        const response = await fetch('/api/1.0/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Profile fetch failed');
        }

        const {data} = await response.json();
        // get name and email from data
        document.getElementById('user-name').textContent = `Account Name: ${data.accountName}`;
        document.getElementById('user-account').textContent = `Nick Name: ${data.nickname}`;
        profileContainer.style.display = 'block';
        authContainer.style.display = 'none';
    } catch (error) {
        console.error('Fetch error:', error);
        // localStorage.removeItem('accessToken');
        authContainer.style.display = 'block';
    }
}

signInForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    const accountName = document.getElementById('signin-account').value;
    const password = document.getElementById('signin-password').value;

    try {
        const response = await fetch('/api/1.0/user/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({accountName, password})
        });


        if (!response.ok) {
            alert('Sign in failed');
            throw new Error('Sign in failed');
        }

        // 拿後端傳回來的資料
        const {data} = await response.json();
        localStorage.setItem('accessToken', data.access_token);
        console.log('Sign in successful. Access token:', data.access_token);
        fetchUserProfile(data.access_token);

        // window.history.back();
    } catch (error) {
        console.error('Error during sign in:', error);
    }
});

/*
選興趣
*/
document.addEventListener('DOMContentLoaded', function () {
    const hashtagButtons = document.querySelectorAll('.hashtag-btn');
    const selectedHashtags = [];
    const accountNameInput = document.getElementById('signup-account');

    hashtagButtons.forEach(button => {
        button.addEventListener('click', () => {
            const hashtag = button.value;

            // 判斷是否已經選擇了該興趣
            if (selectedHashtags.includes(hashtag)) {
                // 如果已經選擇了，則取消選擇
                const index = selectedHashtags.indexOf(hashtag);
                selectedHashtags.splice(index, 1);
                button.classList.remove('selected');
            } else {
                // 如果未選擇，則判斷是否已經選擇了3個興趣
                if (selectedHashtags.length < 3) {
                    // 如果未達到3個興趣的上限，則添加到已選擇的興趣列表中
                    selectedHashtags.push(hashtag);
                    button.classList.add('selected');
                } else {
                    // 如果已選擇了3個興趣，則提示用戶
                    alert('You can only select up to 3 hashtags.');
                }
            }

            console.log('Selected hashtags:', selectedHashtags);
        });
    });

    // 監聽提交按鈕的點擊事件
    signUpForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        const accountName = accountNameInput.value;
        const password = document.getElementById('signup-password').value;
        const nickname = document.getElementById('signup-nickname').value;
        const genderId = mapGenderToId(document.getElementById('signup-genderId').value);
        const genderMatch = mapGenderToId(document.getElementById('signup-genderMatch').value);


        try {
            const response = await fetch('/api/1.0/user/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    accountName,
                    password,
                    nickname,
                    genderId,
                    genderMatch,
                })
            });

            if (!response.ok) {
                alert('Sign up failed');
                throw new Error('Sign up failed');
            }

            const {data} = await response.json();
            localStorage.setItem('accessToken', data.access_token);
            console.log('Sign up successful. Access token:', data.access_token);
            fetchUserProfile(data.access_token);

            // 發送hashtags到'/api/1.0/user/signup/hashtag'
            const hashtagsData = {
                accountName: accountName,
                hashtags: selectedHashtags
            };

            const hashtagsResponse = await fetch('/api/1.0/user/signup/hashtag', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(hashtagsData)
            });

            if (!hashtagsResponse.ok) {
                console.error('Failed to send hashtags data');
                // 在這裡處理失敗的情況，例如顯示錯誤消息給用戶
            } else {
                console.log('Hashtags data sent successfully');
                // 在這裡可以進行頁面跳轉或其他操作
            }
        } catch (error) {
            console.error('Error during sign up:', error);
        }
    });

    // 將性別值映射為後端需要的格式
    function mapGenderToId(gender) {
        switch (gender) {
            case 'female':
                return 1;
            case 'male':
                return 2;
            case 'non-binary':
                return 3;
            default:
                return null;
        }
    }
});
