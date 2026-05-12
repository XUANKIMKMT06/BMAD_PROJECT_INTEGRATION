let users = [];
const userList = document.getElementById('userList');
const searchInput = document.getElementById('searchInput');
const userCount = document.getElementById('userCount');
const addUserBtn = document.getElementById('addUserBtn');
const addUserForm = document.getElementById('addUserForm');
const addUserModal = new bootstrap.Modal(document.getElementById('addUserModal'));
const userDataModal = new bootstrap.Modal(document.getElementById('userDataModal'));


searchInput.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    const users = userList.querySelectorAll('.user-list-item');
    users.forEach(user => {
        const email = user.querySelector('span').textContent.toLowerCase();
        if ( email.includes(searchTerm) )
            user.style.setProperty("display", "flex", "important")
        else
            user.style.setProperty("display", "none", "important");
    });
});

userList.addEventListener('click', (e) => {
    const deleteBtn = e.target.closest('.delete-user-button');
    if (deleteBtn) {
        const userItem = deleteBtn.closest('.user-list-item');
        const username = userItem.querySelector('span').textContent;
        fetch(`delete/${username}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            }
        }).catch(err => console.log('error: ', err));
        userItem.remove();
        updateUserCount();
    }
});

addUserBtn.addEventListener('click', () => {
    addUserModal.show();
});

addUserForm.addEventListener('submit', (e) => {
    e.preventDefault();
    let userdata = JSON.stringify(form_to_json(addUserForm));
    fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('jwt')}`
        },
        body: userdata
    })
        .then(response => {
            if (response.ok) addUserToDashboard(JSON.parse(userdata));
            else response.text()
                .then(
                    response => setAlertMessage(response)
                )
        })
        .catch(err => console.log(err));
    addUserModal.hide();
});

function addUserToDashboard(user) {
    document.getElementById('userList').innerHTML +=
    `
        <li class="list-group-item list-group-item-action user-list-item d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center" onclick="showUserData(this)">
                <i class="bi bi-person-circle me-3 text-secondary"></i>
                <span class="w-75">${user.username}</span>
            </div>
            <button class="delete-user-button btn btn-sm btn-outline-danger">
                <i class="bi bi-trash"></i>
            </button>
        </li>
    `;
    updateUserCount();
}

function showUserData(userElement){
    const username = userElement.getElementsByTagName('span')[0].textContent;
    const user = users.filter(user => user.username === username)[0];
    const body = document.querySelector('#userDataModal .modal-body');

    body.children[1].innerText = user.username; // email
    body.children[2].innerText = user.name; // user name
    body.children[4].innerHTML = user.authorities.map(authority =>
        `<span class="badge bg-info-subtle text-info rounded-pill">${authority.authority}</span>`
    ); // roles

    if (user.authorities?.some(auth => auth.authority === 'ROLE_ADMIN')) {
        body.children[5].style.visibility = 'hidden'; // make admin btn
        body.children[5].style.position = 'absolute';
    }
    else {
        body.children[5].style.visibility = 'visible';
        body.children[5].style.position = 'relative';
    }

    userDataModal.show();
}

function makeAdmin(){
    const username = document.getElementById('email').textContent;
    fetch(`/make-admin/${username}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwt')}`
        }
    }).catch(err => console.log(err));
}

function updateUserCount() {
    const users = userList.querySelectorAll('.user-list-item');
    userCount.textContent = users.length.toString();
}

async function fetchUsers() {
    users = await fetch(
        '/dashboard/users', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('jwt')}`
            }
        }).then(r => r.json())
        .catch(err => console.log(err));


    history.pushState(null, '', '/dashboard');
    users.forEach(user => addUserToDashboard(user));
}