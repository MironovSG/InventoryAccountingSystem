// Общие функции и константы

const API_BASE_URL = '/api';

// Получение токена из localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Получение данных пользователя
function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

// Проверка авторизации
function checkAuth() {
    const token = getToken();
    if (!token) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

// Выход из системы
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

// Выполнение API запроса
async function apiRequest(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(API_BASE_URL + url, {
            ...options,
            headers
        });
        
        if (response.status === 401) {
            logout();
            return;
        }
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Ошибка выполнения запроса');
        }
        
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        
        return await response.text();
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// Отображение сообщения об ошибке
function showError(message, containerId = 'errorMessage') {
    const errorDiv = document.getElementById(containerId);
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 5000);
    }
}

// Отображение сообщения об успехе
function showSuccess(message, containerId = 'successMessage') {
    const successDiv = document.getElementById(containerId);
    if (successDiv) {
        successDiv.textContent = message;
        successDiv.style.display = 'block';
        setTimeout(() => {
            successDiv.style.display = 'none';
        }, 3000);
    }
}

// Форматирование даты
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU') + ' ' + date.toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Форматирование даты (только дата)
function formatDateOnly(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
}

// Форматирование числа с разделителями
function formatNumber(number, decimals = 2) {
    if (number === null || number === undefined) return '0';
    return parseFloat(number).toLocaleString('ru-RU', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
    });
}

// Получение бейджа статуса заявки
function getRequestStatusBadge(status) {
    const statusMap = {
        'ACCEPTED': { class: 'badge-info', text: 'Принята' },
        'IN_PROGRESS': { class: 'badge-warning', text: 'В работе' },
        'AWAITING_ISSUANCE': { class: 'badge-warning', text: 'Ожидает выдачи' },
        'ISSUED': { class: 'badge-success', text: 'Выдана' },
        'CLOSED': { class: 'badge-secondary', text: 'Закрыта' },
        'REJECTED': { class: 'badge-danger', text: 'Отклонена' }
    };
    
    const statusInfo = statusMap[status] || { class: 'badge-secondary', text: status };
    return `<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`;
}

// Получение индикатора уровня запасов
function getStockLevelIndicator(stockLevel) {
    const colorMap = {
        'IN_STOCK': 'green',
        'LOW_STOCK': 'yellow',
        'OUT_OF_STOCK': 'red'
    };
    
    const color = colorMap[stockLevel] || 'red';
    return `<span class="stock-level ${color}"></span>`;
}

// Инициализация шапки: логотип и имя пользователя
function initHeader() {
    var logoImg = document.querySelector('.app-header .app-logo img');
    var logoPh = document.querySelector('.app-header .app-logo-placeholder');
    if (logoImg && logoPh) {
        logoImg.onerror = function() { logoImg.style.display = 'none'; logoPh.style.display = 'block'; };
        if (!logoImg.complete || !logoImg.naturalWidth) logoPh.style.display = 'block';
    }
    var u = getCurrentUser();
    var headerName = document.getElementById('headerUserName');
    if (headerName && u) headerName.textContent = u.fullName || u.username || 'Пользователь';
}

// Инициализация информации о пользователе
function initUserInfo() {
    initHeader();
    const user = getCurrentUser();
    if (user) {
        const userInfo = document.getElementById('userInfo');
        if (userInfo) {
            const nameEl = userInfo.querySelector('.user-name');
            const roleEl = userInfo.querySelector('.user-role');
            
            if (nameEl) nameEl.textContent = user.fullName || user.username;
            if (roleEl) {
                const roleMap = {
                    'ADMIN': 'Администратор',
                    'MOL': 'МОЛ',
                    'ENGINEER': 'Инженер',
                    'MANAGER': 'Руководитель'
                };
                roleEl.textContent = roleMap[user.role] || user.role;
            }
        }
        
        // Скрытие элементов меню в зависимости от роли
        if (user.role === 'ENGINEER') {
            const movementsLink = document.getElementById('movementsLink');
            const usersLink = document.getElementById('usersLink');
            if (movementsLink) movementsLink.style.display = 'none';
            if (usersLink) usersLink.style.display = 'none';
        } else if (user.role === 'MOL' || user.role === 'MANAGER') {
            const usersLink = document.getElementById('usersLink');
            if (usersLink) usersLink.style.display = 'none';
        }
    }
}

// Модальные окна
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('show');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
    }
}

// Закрытие модального окна по клику вне его
window.addEventListener('click', function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
});

// Проверка авторизации при загрузке страницы (страница входа — index.html без пути или с /api/ или /api/index.html)
const path = window.location.pathname || '';
const isLoginPage = path === '/' || path === '/api' || path === '/api/' || path.endsWith('/index.html');
if (!isLoginPage && checkAuth()) {
    initUserInfo();
}
