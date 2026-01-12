// МОЛ - Каталог ТМЦ

let allMaterials = [];
let categories = [];

document.addEventListener('DOMContentLoaded', async function() {
    await loadCategories();
    await loadMaterials();
});

async function loadCategories() {
    try {
        categories = await apiRequest('/material-categories/active');
        
        // Заполнение фильтра категорий
        const filterSelect = document.getElementById('categoryFilter');
        filterSelect.innerHTML = '<option value="">Все категории</option>';
        categories.forEach(cat => {
            filterSelect.innerHTML += `<option value="${cat.id}">${cat.name}</option>`;
        });
        
        // Заполнение категорий в модальном окне
        const categorySelect = document.getElementById('categorySelect');
        categorySelect.innerHTML = '<option value="">Выберите категорию...</option>';
        categories.forEach(cat => {
            categorySelect.innerHTML += `<option value="${cat.id}">${cat.name}</option>`;
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function loadMaterials() {
    try {
        allMaterials = await apiRequest('/materials/active');
        displayMaterials(allMaterials);
    } catch (error) {
        console.error('Error loading materials:', error);
        document.getElementById('catalogTable').innerHTML = 
            '<tr><td colspan="9" class="text-center">Ошибка загрузки данных</td></tr>';
    }
}

function displayMaterials(materials) {
    const tbody = document.getElementById('catalogTable');
    if (materials.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">Нет материалов</td></tr>';
        return;
    }
    
    tbody.innerHTML = materials.map(material => `
        <tr>
            <td>${material.article}</td>
            <td>${material.name}</td>
            <td>${material.categoryName}</td>
            <td>${getWarehouseName(material.warehouseType || 'CONSUMABLES')}</td>
            <td>${material.storageLocation || '-'}</td>
            <td>${formatNumber(material.currentQuantity, 0)}</td>
            <td>${material.unitOfMeasure}</td>
            <td>${getStockLevelIndicator(material.stockLevel)} ${getStockLevelText(material.stockLevel)}</td>
            <td>
                <button class="btn btn-info btn-sm" onclick="editMaterial(${material.id})">✏️ Редактировать</button>
                <button class="btn btn-danger btn-sm" onclick="deleteMaterial(${material.id})">🗑️ Удалить</button>
            </td>
        </tr>
    `).join('');
}

function getWarehouseName(type) {
    const names = {
        'CONSUMABLES': 'Расходные материалы',
        'SPARE_PARTS': 'Запасные части',
        'COMPONENTS': 'Комплектующие'
    };
    return names[type] || type;
}

function getStockLevelText(level) {
    const texts = {
        'IN_STOCK': 'В наличии',
        'LOW_STOCK': 'Мало',
        'OUT_OF_STOCK': 'Отсутствует'
    };
    return texts[level] || level;
}

function applyFilters() {
    const warehouseFilter = document.getElementById('warehouseFilter').value;
    const categoryFilter = document.getElementById('categoryFilter').value;
    const stockFilter = document.getElementById('stockFilter').value;
    
    let filtered = allMaterials;
    
    if (warehouseFilter) {
        filtered = filtered.filter(m => m.warehouseType === warehouseFilter);
    }
    
    if (categoryFilter) {
        filtered = filtered.filter(m => m.categoryId == categoryFilter);
    }
    
    if (stockFilter) {
        if (stockFilter === 'in-stock') {
            filtered = filtered.filter(m => m.stockLevel === 'IN_STOCK');
        } else if (stockFilter === 'low-stock') {
            filtered = filtered.filter(m => m.stockLevel === 'LOW_STOCK');
        } else if (stockFilter === 'out-of-stock') {
            filtered = filtered.filter(m => m.stockLevel === 'OUT_OF_STOCK');
        }
    }
    
    displayMaterials(filtered);
}

async function searchMaterials() {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) {
        displayMaterials(allMaterials);
        return;
    }
    
    try {
        const materials = await apiRequest(`/materials/search?query=${encodeURIComponent(query)}`);
        displayMaterials(materials);
    } catch (error) {
        console.error('Error searching materials:', error);
    }
}

function openAddMaterialModal() {
    document.getElementById('modalTitle').textContent = 'Добавить материал';
    document.getElementById('materialForm').reset();
    document.getElementById('materialId').value = '';
    openModal('materialModal');
}

async function editMaterial(id) {
    try {
        const material = await apiRequest(`/materials/${id}`);
        
        document.getElementById('modalTitle').textContent = 'Редактировать материал';
        document.getElementById('materialId').value = material.id;
        document.getElementById('materialCode').value = material.article;
        document.getElementById('materialName').value = material.name;
        document.getElementById('warehouseType').value = material.warehouseType || 'CONSUMABLES';
        document.getElementById('categorySelect').value = material.categoryId;
        document.getElementById('storageLocation').value = material.storageLocation || '';
        document.getElementById('unitOfMeasure').value = material.unitOfMeasure;
        document.getElementById('currentQuantity').value = material.currentQuantity;
        document.getElementById('minQuantity').value = material.minQuantity || '';
        document.getElementById('description').value = material.description || '';
        
        openModal('materialModal');
    } catch (error) {
        alert('Ошибка загрузки данных материала');
    }
}

async function saveMaterial() {
    const materialId = document.getElementById('materialId').value;
    
    const data = {
        article: document.getElementById('materialCode').value,
        name: document.getElementById('materialName').value,
        warehouseType: document.getElementById('warehouseType').value,
        categoryId: parseInt(document.getElementById('categorySelect').value),
        storageLocation: document.getElementById('storageLocation').value,
        unitOfMeasure: document.getElementById('unitOfMeasure').value,
        currentQuantity: parseFloat(document.getElementById('currentQuantity').value),
        minQuantity: document.getElementById('minQuantity').value ? 
                      parseFloat(document.getElementById('minQuantity').value) : null,
        description: document.getElementById('description').value,
        active: true
    };
    
    try {
        if (materialId) {
            // Обновление
            await apiRequest(`/materials/${materialId}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
            alert('Материал успешно обновлен');
        } else {
            // Создание
            await apiRequest('/materials', {
                method: 'POST',
                body: JSON.stringify(data)
            });
            alert('Материал успешно добавлен');
        }
        
        closeModal('materialModal');
        await loadMaterials();
    } catch (error) {
        showError(error.message, 'modalError');
    }
}

async function deleteMaterial(id) {
    if (!confirm('Вы уверены, что хотите удалить этот материал?')) {
        return;
    }
    
    try {
        await apiRequest(`/materials/${id}`, {
            method: 'DELETE'
        });
        alert('Материал успешно удален');
        await loadMaterials();
    } catch (error) {
        alert('Ошибка при удалении материала: ' + error.message);
    }
}
