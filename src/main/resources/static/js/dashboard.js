// Панель управления

// Загрузка данных при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        // Загрузка статистики
        await Promise.all([
            loadMaterialStats(),
            loadRequestStats(),
            loadRecentRequests(),
            loadLowStockMaterials()
        ]);
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Загрузка статистики по материалам
async function loadMaterialStats() {
    try {
        const [allMaterials, inStock, lowStock, criticalStock] = await Promise.all([
            apiRequest('/materials/active'),
            apiRequest('/materials/in-stock'),
            apiRequest('/materials/low-stock'),
            apiRequest('/materials/critical-stock')
        ]);
        
        document.getElementById('totalMaterials').textContent = allMaterials.length;
        document.getElementById('inStockMaterials').textContent = inStock.length;
        document.getElementById('lowStockMaterials').textContent = lowStock.length;
        document.getElementById('criticalStockMaterials').textContent = criticalStock.length;
    } catch (error) {
        console.error('Error loading material stats:', error);
    }
}

// Загрузка статистики по заявкам
async function loadRequestStats() {
    try {
        const user = getCurrentUser();
        let requests;
        
        if (user.role === 'ENGINEER') {
            requests = await apiRequest('/requests/my-requests');
        } else {
            requests = await apiRequest('/requests');
        }
        
        const activeRequests = requests.filter(r => 
            ['ACCEPTED', 'IN_PROGRESS', 'AWAITING_ISSUANCE'].includes(r.status)
        );
        const pendingRequests = requests.filter(r => r.status === 'ACCEPTED');
        
        document.getElementById('activeRequests').textContent = activeRequests.length;
        document.getElementById('pendingRequests').textContent = pendingRequests.length;
    } catch (error) {
        console.error('Error loading request stats:', error);
    }
}

// Загрузка последних заявок
async function loadRecentRequests() {
    try {
        const user = getCurrentUser();
        let requests;
        
        if (user.role === 'ENGINEER') {
            requests = await apiRequest('/requests/my-requests');
        } else {
            requests = await apiRequest('/requests');
        }
        
        // Последние 10 заявок
        const recentRequests = requests.slice(0, 10);
        
        const tbody = document.getElementById('recentRequestsTable');
        if (recentRequests.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">Нет заявок</td></tr>';
            return;
        }
        
        tbody.innerHTML = recentRequests.map(request => `
            <tr onclick="window.location.href='/requests.html?id=${request.id}';" style="cursor: pointer;">
                <td>${request.requestNumber}</td>
                <td>${request.requesterName}</td>
                <td>${request.departmentName}</td>
                <td>${request.purpose.substring(0, 50)}${request.purpose.length > 50 ? '...' : ''}</td>
                <td>${getRequestStatusBadge(request.status)}</td>
                <td>${formatDateOnly(request.createdAt)}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading recent requests:', error);
        document.getElementById('recentRequestsTable').innerHTML = 
            '<tr><td colspan="6" class="text-center">Ошибка загрузки данных</td></tr>';
    }
}

// Загрузка материалов с низким запасом
async function loadLowStockMaterials() {
    try {
        const materials = await apiRequest('/materials/low-stock');
        
        const tbody = document.getElementById('lowStockTable');
        if (materials.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">Нет материалов с низким запасом</td></tr>';
            return;
        }
        
        tbody.innerHTML = materials.map(material => `
            <tr onclick="window.location.href='/materials.html?id=${material.id}';" style="cursor: pointer;">
                <td>${material.article}</td>
                <td>${material.name}</td>
                <td>${formatNumber(material.currentQuantity, 0)}</td>
                <td>${formatNumber(material.minQuantity, 0)}</td>
                <td>${material.unitOfMeasure}</td>
                <td>${getStockLevelIndicator(material.stockLevel)} ${material.stockLevel === 'LOW_STOCK' ? 'Мало' : 'Критический'}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading low stock materials:', error);
        document.getElementById('lowStockTable').innerHTML = 
            '<tr><td colspan="6" class="text-center">Ошибка загрузки данных</td></tr>';
    }
}
