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

        console.log("accountName" + accountName);
        console.log("password" + password);

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

signUpForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    const accountName = document.getElementById('signup-account').value;
    const password = document.getElementById('signup-password').value;
    const nickname = document.getElementById('signup-nickname').value; // You may add input fields for nickname, genderId, and genderMatch if needed
    const genderId = document.getElementById('signup-genderId').value;
    const genderMatch = document.getElementById('signup-genderMatch').value;

    try {
        const response = await fetch('/api/1.0/user/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({accountName, password, nickname, genderId, genderMatch})
        });

        if (!response.ok) {
            alert('Sign up failed');
            throw new Error('Sign up failed');
        }


        const {data} = await response.json();
        localStorage.setItem('accessToken', data.access_token);
        console.log('Sign up successful. Access token:', data.access_token);
        fetchUserProfile(data.access_token);
    } catch (error) {
        console.error('Error during sign up:', error);
    }
});
