// Управление пользователями для руководителя

let allUsers = [];
let departments = [];

document.addEventListener('DOMContentLoaded', async function() {
    await loadDepartments();
    await loadUsers();
});

async function loadDepartments() {
    try {
        departments = await apiRequest('/departments/active');
        
        // Заполнить селекты подразделений
        const addDeptSelect = document.getElementById('addDepartment');
        const editDeptSelect = document.getElementById('editDepartment');
        
        addDeptSelect.innerHTML = '<option value="">Не выбрано</option>';
        editDeptSelect.innerHTML = '<option value="">Не выбрано</option>';
        
        departments.forEach(dept => {
            addDeptSelect.innerHTML += `<option value="${dept.id}">${dept.name}</option>`;
            editDeptSelect.innerHTML += `<option value="${dept.id}">${dept.name}</option>`;
        });
    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

async function loadUsers() {
    try {
        allUsers = await apiRequest('/users');
        displayUsers(allUsers);
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById('usersTable').innerHTML = 
            '<tr><td colspan="9" class="text-center">Ошибка загрузки данных</td></tr>';
    }
}

function displayUsers(users) {
    const tbody = document.getElementById('usersTable');
    document.getElementById('totalCount').textContent = users.length;
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">Нет пользователей</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(user => `
        <tr style="${!user.active ? 'opacity: 0.6;' : ''}">
            <td>${user.id}</td>
            <td><strong>${user.username}</strong></td>
            <td>${user.firstName} ${user.lastName}</td>
            <td>${user.email}</td>
            <td>${getRoleBadge(user.role)}</td>
            <td>${user.departmentName || '-'}</td>
            <td>${user.workGroup ? getWorkGroupBadge(user.workGroup) : '-'}</td>
            <td>${user.active ? '<span style="color: #4CAF50;">✓ Активен</span>' : '<span style="color: #999;">✗ Неактивен</span>'}</td>
            <td>
                <button class="btn btn-primary btn-sm" onclick="openEditUserModal(${user.id})">✏️ Редактировать</button>
                ${user.active ? 
                    `<button class="btn btn-danger btn-sm" onclick="deleteUser(${user.id}, '${user.username}')">🗑️ Удалить</button>` :
                    `<button class="btn btn-success btn-sm" onclick="restoreUser(${user.id})">↻ Восстановить</button>`
                }
            </td>
        </tr>
    `).join('');
}

function getRoleBadge(role) {
    const roleNames = {
        'ADMIN': { name: 'Администратор', color: '#f44336' },
        'MOL': { name: 'МОЛ', color: '#FF9800' },
        'MANAGER': { name: 'Руководитель', color: '#9C27B0' },
        'ENGINEER': { name: 'Инженер', color: '#2196F3' }
    };
    
    const roleInfo = roleNames[role] || { name: role, color: '#999' };
    return `<span style="background: ${roleInfo.color}; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600;">${roleInfo.name}</span>`;
}

function getWorkGroupBadge(group) {
    const colors = {
        'ARM': '#2196F3',
        'GOTO': '#4CAF50',
        'GOKS': '#FF9800'
    };
    
    return `<span style="background: ${colors[group] || '#999'}; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px;">${group}</span>`;
}

function filterUsers() {
    const search = document.getElementById('searchInput').value.toLowerCase();
    const role = document.getElementById('roleFilter').value;
    
    let filtered = allUsers;
    
    if (search) {
        filtered = filtered.filter(u => 
            u.username.toLowerCase().includes(search) ||
            u.firstName.toLowerCase().includes(search) ||
            u.lastName.toLowerCase().includes(search) ||
            u.email.toLowerCase().includes(search)
        );
    }
    
    if (role) {
        filtered = filtered.filter(u => u.role === role);
    }
    
    displayUsers(filtered);
}

// Открытие модального окна добавления пользователя
function openAddUserModal() {
    document.getElementById('addUserForm').reset();
    document.getElementById('addError').style.display = 'none';
    openModal('addUserModal');
}

// Добавление пользователя
async function addUser() {
    const username = document.getElementById('addUsername').value.trim();
    const password = document.getElementById('addPassword').value;
    const email = document.getElementById('addEmail').value.trim();
    const firstName = document.getElementById('addFirstName').value.trim();
    const lastName = document.getElementById('addLastName').value.trim();
    const role = document.getElementById('addRole').value;
    const departmentId = document.getElementById('addDepartment').value;
    const workGroup = document.getElementById('addWorkGroup').value;
    
    if (!username || !password || !email || !firstName || !lastName || !role) {
        showError('Заполните все обязательные поля', 'addError');
        return;
    }
    
    const data = {
        username: username,
        password: password,
        email: email,
        firstName: firstName,
        lastName: lastName,
        role: role,
        departmentId: departmentId ? parseInt(departmentId) : null,
        workGroup: workGroup || null,
        active: true
    };
    
    try {
        await apiRequest('/users', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        closeModal('addUserModal');
        alert('Пользователь успешно добавлен!');
        await loadUsers();
    } catch (error) {
        showError('Ошибка при добавлении пользователя: ' + error.message, 'addError');
    }
}

// Открытие модального окна редактирования
async function openEditUserModal(userId) {
    try {
        const user = await apiRequest(`/users/${userId}`);
        
        document.getElementById('editUserId').value = user.id;
        document.getElementById('editUserIdDisplay').value = user.id;
        document.getElementById('editUsername').value = user.username;
        document.getElementById('editPassword').value = '';
        document.getElementById('editEmail').value = user.email;
        document.getElementById('editFirstName').value = user.firstName;
        document.getElementById('editLastName').value = user.lastName;
        document.getElementById('editRole').value = user.role;
        document.getElementById('editDepartment').value = user.departmentId || '';
        document.getElementById('editWorkGroup').value = user.workGroup || '';
        document.getElementById('editActive').checked = user.active;
        document.getElementById('editError').style.display = 'none';
        
        openModal('editUserModal');
    } catch (error) {
        alert('Ошибка при загрузке данных пользователя: ' + error.message);
    }
}

// Обновление пользователя
async function updateUser() {
    const userId = document.getElementById('editUserId').value;
    const username = document.getElementById('editUsername').value.trim();
    const password = document.getElementById('editPassword').value;
    const email = document.getElementById('editEmail').value.trim();
    const firstName = document.getElementById('editFirstName').value.trim();
    const lastName = document.getElementById('editLastName').value.trim();
    const role = document.getElementById('editRole').value;
    const departmentId = document.getElementById('editDepartment').value;
    const workGroup = document.getElementById('editWorkGroup').value;
    const active = document.getElementById('editActive').checked;
    
    if (!username || !email || !firstName || !lastName || !role) {
        showError('Заполните все обязательные поля', 'editError');
        return;
    }
    
    const data = {
        username: username,
        email: email,
        firstName: firstName,
        lastName: lastName,
        role: role,
        departmentId: departmentId ? parseInt(departmentId) : null,
        workGroup: workGroup || null,
        active: active
    };
    
    // Добавить пароль только если он указан
    if (password && password.trim() !== '') {
        data.password = password;
    }
    
    try {
        await apiRequest(`/users/${userId}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
        
        closeModal('editUserModal');
        alert('Данные пользователя успешно обновлены!');
        await loadUsers();
    } catch (error) {
        showError('Ошибка при обновлении пользователя: ' + error.message, 'editError');
    }
}

// Удаление пользователя
async function deleteUser(userId, username) {
    if (!confirm(`Вы уверены, что хотите удалить пользователя "${username}"?\n\nПользователь будет деактивирован и не сможет войти в систему.`)) {
        return;
    }
    
    try {
        await apiRequest(`/users/${userId}`, {
            method: 'DELETE'
        });
        
        alert('Пользователь успешно удален!');
        await loadUsers();
    } catch (error) {
        alert('Ошибка при удалении пользователя: ' + error.message);
    }
}

// Восстановление пользователя
async function restoreUser(userId) {
    try {
        const user = await apiRequest(`/users/${userId}`);
        user.active = true;
        user.deleted = false;
        
        await apiRequest(`/users/${userId}`, {
            method: 'PUT',
            body: JSON.stringify(user)
        });
        
        alert('Пользователь успешно восстановлен!');
        await loadUsers();
    } catch (error) {
        alert('Ошибка при восстановлении пользователя: ' + error.message);
    }
}
