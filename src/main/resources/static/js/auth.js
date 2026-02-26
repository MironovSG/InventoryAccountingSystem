// Аутентификация

document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMessage');
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            const error = await response.text();
            errorDiv.textContent = error || 'Ошибка аутентификации';
            errorDiv.style.display = 'block';
            return;
        }
        
        const data = await response.json();
        
        // Сохранение токена и данных пользователя
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
            userId: data.userId,
            username: data.username,
            email: data.email,
            fullName: data.fullName,
            role: data.role,
            departmentId: data.departmentId,
            departmentName: data.departmentName
        }));
        
        // Перенаправление на соответствующую панель управления в зависимости от роли
        if (data.role === 'ADMIN') {
            window.location.href = '/admin-dashboard.html';
        } else if (data.role === 'MOL') {
            window.location.href = '/mol-dashboard.html';
        } else if (data.role === 'MANAGER') {
            window.location.href = '/manager-dashboard.html';
        } else {
            // Для всех остальных ролей (ENGINEER, SPECIALIST_*)
            window.location.href = '/user-dashboard.html';
        }
    } catch (error) {
        console.error('Login error:', error);
        errorDiv.textContent = 'Ошибка подключения к серверу';
        errorDiv.style.display = 'block';
    }
});
