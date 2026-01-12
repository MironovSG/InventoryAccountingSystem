// Пользовательская панель управления

document.addEventListener('DOMContentLoaded', async function() {
    await loadDashboardData();
    startNotificationPolling();
});

async function loadDashboardData() {
    try {
        await Promise.all([
            loadMyRequestsCount(),
            loadRecentRequests(),
            loadNotifications()
        ]);
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

async function loadMyRequestsCount() {
    try {
        const requests = await apiRequest('/requests/my-requests');
        document.getElementById('myRequestsCount').textContent = requests.length;
    } catch (error) {
        console.error('Error loading requests count:', error);
    }
}

async function loadRecentRequests() {
    try {
        const requests = await apiRequest('/requests/my-requests');
        const recent = requests.slice(0, 5);
        
        const tbody = document.getElementById('recentRequestsTable');
        if (recent.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">У вас пока нет заявок</td></tr>';
            return;
        }
        
        tbody.innerHTML = recent.map(request => `
            <tr onclick="window.location.href='/user-my-requests.html?id=${request.id}';" style="cursor: pointer;">
                <td>${request.requestNumber}</td>
                <td>${formatDateOnly(request.createdAt)}</td>
                <td>${getRequestStatusBadge(request.status)}</td>
                <td>${request.purpose.substring(0, 50)}${request.purpose.length > 50 ? '...' : ''}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading recent requests:', error);
        document.getElementById('recentRequestsTable').innerHTML = 
            '<tr><td colspan="4" class="text-center">Ошибка загрузки данных</td></tr>';
    }
}

async function loadNotifications() {
    try {
        const notifications = await apiRequest('/notifications/unread');
        const count = notifications.length;
        
        if (count > 0) {
            document.getElementById('notificationBadge').style.display = 'block';
            document.getElementById('notificationCount').textContent = count;
        }
        
        const container = document.getElementById('notificationsList');
        if (notifications.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: #999;">Новых уведомлений нет</p>';
            return;
        }
        
        const recent = notifications.slice(0, 5);
        container.innerHTML = recent.map(notif => `
            <div style="border-left: 4px solid ${notif.isRead ? '#ccc' : '#2196F3'}; padding: 10px; margin-bottom: 10px; background: ${notif.isRead ? '#f9f9f9' : '#e3f2fd'}; border-radius: 4px; cursor: pointer;" onclick="markAsReadAndView(${notif.id})">
                <div style="font-weight: 600; margin-bottom: 5px;">${notif.title}</div>
                <div style="font-size: 14px; color: #666;">${notif.message}</div>
                <div style="font-size: 12px; color: #999; margin-top: 5px;">${formatDate(notif.createdAt)}</div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

async function markAsReadAndView(notificationId) {
    try {
        await apiRequest(`/notifications/${notificationId}/read`, { method: 'PUT' });
        await loadNotifications();
    } catch (error) {
        console.error('Error marking notification as read:', error);
    }
}

// Периодическая проверка новых уведомлений
function startNotificationPolling() {
    setInterval(async () => {
        await loadNotifications();
    }, 30000); // Проверка каждые 30 секунд
}

// Открытие модального окна создания заявки
function openCreateRequestModal() {
    document.getElementById('createRequestForm').reset();
    openModal('createRequestModal');
}

// Добавление новой строки в таблицу заявки
function addRequestRow() {
    const tbody = document.getElementById('requestItemsTable');
    const newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td><input type="text" class="item-code" placeholder="Код"></td>
        <td><input type="text" class="item-name" placeholder="Название"></td>
        <td><input type="number" class="item-quantity" step="0.001" min="0.001" placeholder="Кол-во"></td>
        <td><input type="text" class="item-work" placeholder="Для каких работ"></td>
    `;
    tbody.appendChild(newRow);
}

// Отправка заявки
async function submitRequest() {
    const codes = document.querySelectorAll('.item-code');
    const names = document.querySelectorAll('.item-name');
    const quantities = document.querySelectorAll('.item-quantity');
    const works = document.querySelectorAll('.item-work');
    
    const items = [];
    let purpose = '';
    
    for (let i = 0; i < codes.length; i++) {
        const code = codes[i].value.trim();
        const name = names[i].value.trim();
        const quantity = quantities[i].value.trim();
        const work = works[i].value.trim();
        
        if (code && name && quantity) {
            // Найти материал по коду или названию
            try {
                const materials = await apiRequest(`/materials/search?query=${encodeURIComponent(code)}`);
                if (materials.length > 0) {
                    items.push({
                        materialId: materials[0].id,
                        requestedQuantity: parseFloat(quantity),
                        notes: work
                    });
                } else {
                    showError(`Материал с кодом "${code}" не найден в каталоге`, 'modalError');
                    return;
                }
            } catch (error) {
                showError('Ошибка при поиске материала: ' + error.message, 'modalError');
                return;
            }
            
            if (work && !purpose) {
                purpose = work;
            }
        }
    }
    
    if (items.length === 0) {
        showError('Заполните хотя бы одну строку с материалом', 'modalError');
        return;
    }
    
    if (!purpose) {
        purpose = 'Выдача материалов';
    }
    
    const data = {
        purpose: purpose,
        notes: '',
        items: items
    };
    
    try {
        await apiRequest('/requests', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        closeModal('createRequestModal');
        alert('Заявка успешно отправлена! МОЛ получит уведомление.');
        await loadDashboardData();
    } catch (error) {
        showError('Ошибка при создании заявки: ' + error.message, 'modalError');
    }
}
