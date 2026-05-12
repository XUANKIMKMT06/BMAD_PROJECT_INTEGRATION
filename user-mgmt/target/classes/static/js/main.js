// const login_btn = document.getElementById('btn');
// const signup_btn = document.getElementById('signup');
logout_btn = document.getElementById('logout');
dashboard_btn = document.getElementById('dashboard');
form = document.getElementById('userForm');
registerForm = document.getElementById('registerForm');

if (logout_btn) logout_btn.addEventListener('click', (e) => {
    e.preventDefault();
    fetch('/api/v1/auth/logout', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwt')}`
        }
    }).then(() => {
        window.location.replace('/');
        localStorage.removeItem('jwt');
    })
});

if (registerForm) registerForm.addEventListener('submit', (event) => {
    event.preventDefault();

    fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(form_to_json(registerForm))
    })
        .then(response => {
            if (response.headers.get('Content-Type').startsWith('application/json')) {
                response.json().then(data => {
                        localStorage.setItem('user', JSON.stringify(data.user));
                        localStorage.setItem('jwt', data.token);
                        home();
                    });
            } else response.text().then(response => {
                    setAlertMessage(response);
                });
        });

});

if (form) form.addEventListener('submit', (event) => {
    event.preventDefault();

    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(form_to_json(form)),
        redirect: "follow"
    })
        .then(response => {

            if (response.headers.get('Content-Type').startsWith('application/json')) {
                response.json()
                    .then(data => {
                        localStorage.setItem('user', JSON.stringify(data.user));
                        localStorage.setItem('jwt', data.token);
                        home();
                    });

            } else
                response.text().then(response => {
                    setAlertMessage(response);
                });

        }).catch(err => console.log(err));
});

if (dashboard_btn) dashboard_btn.addEventListener('click', async () => {
    fetch('/dashboard', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwt')}`,
        }
    })
        .then(r => r.text()).then(html => {
            history.pushState(null, '', '/dashboard');
            document.open();
            document.write(html);
            document.close();
        })
        .catch(err => console.log(err));
});

function home() {

    let user = JSON.parse(localStorage.getItem('user'));

    fetch('/home', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwt')}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.headers.get('Content-Type').includes('plain'))
                response.text().then(html => {

                    history.pushState(null, '', `/${user.username}`);
                    document.open();
                    document.write(html);
                    document.getElementById('email').innerText = user.username;
                    document.getElementById('user').innerText = user.name;

                    if (!user.authorities?.some(auth => auth.authority === 'ROLE_ADMIN'))
                        document.getElementById('dashboard').style.visibility = 'hidden';

                    document.close();
                }).catch(() => localStorage.removeItem('jwt'));
            if (response.redirected) localStorage.removeItem('jwt');
        })
}

function form_to_json(formToConvert) {
    const formData = new FormData(formToConvert);
    const data = {};
    formData.forEach((value, key) => {
        data[key] = value;
    });
    return data;
}

function setAlertMessage(response) {
    const message = document.getElementById('state');
    message.innerHTML = `
        <ul class="mb-0">
            ${response.split('\n').map(line => `<li>${line}</li>`).join(``)}
        </ul>
    `;
    message.style.position = 'relative';
    message.style.visibility = 'visible';
}

