package com.eval3.frontend;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticPageGenerator {
    
    private static final String OUTPUT_DIR = "output";

    private static String getEnv(String key, String defaultValue) {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            try {
                for (String line : Files.readAllLines(envPath)) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2 && parts[0].trim().equals(key)) {
                        return parts[1].trim();
                    }
                }
            } catch (IOException e) {
                System.err.println("Advertencia: No se pudo leer el archivo .env: " + e.getMessage());
            }
        }
        return defaultValue;
    }
    
    public static void main(String[] args) {
        try {
            // Crear directorio de salida si no existe
            Path outputPath = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            
            // Generar archivos
            generateIndexHtml();
            generateStylesCss();
            generateScriptJs();
            
            System.out.println("Página estática generada exitosamente en el directorio 'output'");
            System.out.println("Abra output/index.html en su navegador para ver la página");
            
        } catch (IOException e) {
            System.err.println("Error generando la página estática: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateIndexHtml() throws IOException {
        String html = """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Productos</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>Gestión de Productos</h1>
            <nav>
                <button class="nav-btn active" onclick="showSection('products')">Productos</button>
                <button class="nav-btn" onclick="showSection('register')">Registro</button>
            </nav>
        </header>
        
        <main>
            <!-- Sección de Productos -->
            <section id="products-section" class="section active">
                <div class="section-header">
                    <h2>Catálogo de Productos</h2>
                    <p>Visualice los productos disponibles (solo lectura)</p>
                </div>
                <div class="products-grid" id="products-grid">
                    <!-- Los productos se cargarán dinámicamente -->
                </div>
            </section>
            
            <!-- Sección de Registro -->
            <section id="register-section" class="section">
                <div class="section-header">
                    <h2>Registro de Usuario</h2>
                    <p>Complete el formulario para registrarse</p>
                </div>
                <form id="register-form" class="register-form">
                    <div class="form-group">
                        <label for="username">Nombre de Usuario:</label>
                        <input type="text" id="username" name="username" required>
                    </div>
                    <div class="form-group">
                        <label for="email">Correo Electrónico:</label>
                        <input type="email" id="email" name="email" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Contraseña:</label>
                        <input type="password" id="password" name="password" required>
                    </div>
                    <div class="form-group">
                        <label for="confirm-password">Confirmar Contraseña:</label>
                        <input type="password" id="confirm-password" name="confirm-password" required>
                    </div>
                    <button type="submit" class="submit-btn">Registrarse</button>
                </form>
                <div id="register-message" class="message"></div>
            </section>
        </main>
        
        <footer>
            <p>&copy; 2026 Sistema de Gestión de Productos</p>
        </footer>
    </div>
    
    <script src="script.js"></script>
</body>
</html>
""";
        
        try (FileWriter writer = new FileWriter(OUTPUT_DIR + "/index.html")) {
            writer.write(html);
        }
    }
    
    private static void generateStylesCss() throws IOException {
        String css = """
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
    padding: 20px;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    background: white;
    border-radius: 15px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    overflow: hidden;
}

header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 30px;
    text-align: center;
}

header h1 {
    font-size: 2.5em;
    margin-bottom: 20px;
}

nav {
    display: flex;
    justify-content: center;
    gap: 15px;
}

.nav-btn {
    background: rgba(255, 255, 255, 0.2);
    border: 2px solid rgba(255, 255, 255, 0.5);
    color: white;
    padding: 12px 30px;
    border-radius: 25px;
    cursor: pointer;
    font-size: 1em;
    font-weight: 600;
    transition: all 0.3s ease;
}

.nav-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-2px);
}

.nav-btn.active {
    background: white;
    color: #667eea;
}

main {
    padding: 40px;
}

.section {
    display: none;
}

.section.active {
    display: block;
    animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.section-header {
    text-align: center;
    margin-bottom: 40px;
}

.section-header h2 {
    color: #333;
    font-size: 2em;
    margin-bottom: 10px;
}

.section-header p {
    color: #666;
    font-size: 1.1em;
}

.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 25px;
}

.product-card {
    background: #f8f9fa;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.product-image {
    width: 100%;
    height: 200px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 3em;
}

.product-name {
    font-size: 1.3em;
    font-weight: 700;
    color: #333;
    margin-bottom: 10px;
}

.product-description {
    color: #666;
    font-size: 0.95em;
    margin-bottom: 15px;
    line-height: 1.5;
}

.product-price {
    font-size: 1.5em;
    font-weight: 700;
    color: #667eea;
    margin-bottom: 10px;
}

.product-stock {
    color: #28a745;
    font-weight: 600;
}

.register-form {
    max-width: 500px;
    margin: 0 auto;
    background: #f8f9fa;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: #333;
    font-weight: 600;
}

.form-group input {
    width: 100%;
    padding: 12px;
    border: 2px solid #ddd;
    border-radius: 8px;
    font-size: 1em;
    transition: border-color 0.3s ease;
}

.form-group input:focus {
    outline: none;
    border-color: #667eea;
}

.submit-btn {
    width: 100%;
    padding: 15px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1.1em;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.message {
    margin-top: 20px;
    padding: 15px;
    border-radius: 8px;
    text-align: center;
    font-weight: 600;
    display: none;
}

.message.success {
    background: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
    display: block;
}

.message.error {
    background: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
    display: block;
}

footer {
    background: #f8f9fa;
    text-align: center;
    padding: 20px;
    color: #666;
    border-top: 1px solid #ddd;
}

@media (max-width: 768px) {
    header h1 {
        font-size: 1.8em;
    }
    
    .nav-btn {
        padding: 10px 20px;
        font-size: 0.9em;
    }
    
    main {
        padding: 20px;
    }
    
    .products-grid {
        grid-template-columns: 1fr;
    }
}
""";
        
        try (FileWriter writer = new FileWriter(OUTPUT_DIR + "/styles.css")) {
            writer.write(css);
        }
    }

    private static void generateScriptJs() throws IOException {
        String usersUrl = getEnv("BACKEND_USERS_URL", "http://localhost:8081");
        String productsUrl = getEnv("BACKEND_PRODUCTS_URL", "http://localhost:8082");
        System.out.println("Configurando frontend: Users API -> " + usersUrl + " | Products API -> " + productsUrl);

        String js = """

// Datos de ejemplo de productos (como respaldo si el backend está desconectado)
const products = [
    {
        id: 1,
        name: "Laptop HP Pavilion",
        description: "Laptop de 15.6 pulgadas, Intel Core i5, 8GB RAM, 256GB SSD",
        price: 899.99,
        stock: 15,
        icon: "💻"
    },
    {
        id: 2,
        name: "Smartphone Samsung Galaxy",
        description: "Samsung Galaxy S23, 128GB, cámara de 50MP, 5G",
        price: 799.99,
        stock: 25,
        icon: "📱"
    },
    {
        id: 3,
        name: "Auriculares Sony WH-1000XM4",
        description: "Auriculares inalámbricos con cancelación de ruido",
        price: 349.99,
        stock: 30,
        icon: "🎧"
    },
    {
        id: 4,
        name: "Tablet iPad Air",
        description: "Apple iPad Air 5ta generación, 64GB, Wi-Fi",
        price: 599.99,
        stock: 20,
        icon: "📲"
    },
    {
        id: 5,
        name: "Smartwatch Apple Watch",
        description: "Apple Watch Series 8, GPS, 45mm",
        price: 449.99,
        stock: 18,
        icon: "⌚"
    },
    {
        id: 6,
        name: "Cámara Canon EOS",
        description: "Cámara DSLR Canon EOS Rebel T7, 24.1MP",
        price: 549.99,
        stock: 12,
        icon: "📷"
    },
    {
        id: 7,
        name: "Consola PlayStation 5",
        description: "Sony PlayStation 5, 825GB SSD, con control DualSense",
        price: 499.99,
        stock: 8,
        icon: "🎮"
    },
    {
        id: 8,
        name: "Monitor Dell UltraSharp",
        description: "Monitor Dell 27 pulgadas 4K, IPS, USB-C",
        price: 429.99,
        stock: 22,
        icon: "🖥️"
    }
];

// Almacenamiento local de usuarios registrados (historial / respaldo local)
let registeredUsers = JSON.parse(localStorage.getItem('registeredUsers')) || [];

// Inicializar la página
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();
    setupRegisterForm();
});

// Cargar productos en la grid (desde el Backend 2)
function loadProducts() {
    const productsGrid = document.getElementById('products-grid');
    productsGrid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #666;">Cargando productos...</p>';
    
    fetch('{{BACKEND_PRODUCTS_URL}}/api/products')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al responder del servidor');
            }
            return response.json();
        })
        .then(data => {
            productsGrid.innerHTML = '';
            
            // Si la base de datos está vacía, mostramos los de ejemplo
            const listToRender = (data && data.length > 0) ? data : products;
            if (data && data.length === 0) {
                console.log("La base de datos de productos está vacía. Mostrando productos de ejemplo.");
                // Agregamos un aviso visual de que son de ejemplo
                const infoMsg = document.createElement('div');
                infoMsg.style.cssText = "grid-column: 1/-1; background: #e2e8f0; color: #4a5568; padding: 10px; border-radius: 8px; text-align: center; font-size: 0.9em; margin-bottom: 15px;";
                infoMsg.textContent = "Base de datos vacía. Mostrando catálogo de ejemplo.";
                productsGrid.appendChild(infoMsg);
            }
            
            listToRender.forEach(product => {
                const productCard = createProductCard(product);
                productsGrid.appendChild(productCard);
            });
        })
        .catch(error => {
            console.warn('Backend 2 (Productos) desconectado. Mostrando datos locales de respaldo:', error);
            productsGrid.innerHTML = '';
            
            const infoMsg = document.createElement('div');
            infoMsg.style.cssText = "grid-column: 1/-1; background: #fee2e2; color: #991b1b; padding: 10px; border-radius: 8px; text-align: center; font-size: 0.9em; margin-bottom: 15px;";
            infoMsg.textContent = "Backend de productos desconectado. Mostrando catálogo local de respaldo.";
            productsGrid.appendChild(infoMsg);
            
            products.forEach(product => {
                const productCard = createProductCard(product);
                productsGrid.appendChild(productCard);
            });
        });
}

// Crear tarjeta de producto
function createProductCard(product) {
    const card = document.createElement('div');
    card.className = 'product-card';
    card.innerHTML = `
        <div class="product-image">${product.icon || '📦'}</div>
        <h3 class="product-name">${product.name}</h3>
        <p class="product-description">${product.description || ''}</p>
        <p class="product-price">$${Number(product.price).toFixed(2)}</p>
        <p class="product-stock">Stock: ${product.stock} unidades</p>
    `;
    return card;
}

// Configurar formulario de registro
function setupRegisterForm() {
    const form = document.getElementById('register-form');
    form.addEventListener('submit', handleRegister);
}

// Manejar registro de usuario (conectado al Backend 1)
function handleRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    
    // Validaciones locales
    if (password !== confirmPassword) {
        showMessage('Las contraseñas no coinciden', 'error');
        return;
    }
    
    if (password.length < 6) {
        showMessage('La contraseña debe tener al menos 6 caracteres', 'error');
        return;
    }
    
    // Enviar petición al Backend 1 (User Service)
    fetch('{{BACKEND_USERS_URL}}/api/users/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            email: email,
            password: password
        })
    })
    .then(async response => {
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || 'Error al registrar usuario en el servidor');
        }
        return data;
    })
    .then(data => {
        showMessage('Usuario registrado exitosamente en la base de datos', 'success');
        document.getElementById('register-form').reset();
        
        // Registrar también en el historial local por compatibilidad
        const newUser = {
            username: username,
            email: email,
            registeredAt: new Date().toISOString()
        };
        registeredUsers.push(newUser);
        localStorage.setItem('registeredUsers', JSON.stringify(registeredUsers));
    })
    .catch(error => {
        console.error('Error al registrar usuario:', error);
        showMessage(error.message, 'error');
    });
}

// Mostrar mensaje
function showMessage(text, type) {
    const messageDiv = document.getElementById('register-message');
    messageDiv.textContent = text;
    messageDiv.className = `message ${type}`;
    
    // Ocultar mensaje después de 5 segundos
    setTimeout(() => {
        messageDiv.className = 'message';
    }, 5000);
}

// Mostrar sección específica
function showSection(sectionName) {
    // Ocultar todas las secciones
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Desactivar todos los botones de navegación
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Mostrar sección seleccionada
    if (sectionName === 'products') {
        document.getElementById('products-section').classList.add('active');
        document.querySelectorAll('.nav-btn')[0].classList.add('active');
        loadProducts(); // Recargar productos al ver la sección
    } else if (sectionName === 'register') {
        document.getElementById('register-section').classList.add('active');
        document.querySelectorAll('.nav-btn')[1].classList.add('active');
    }
}
""";

        js = js.replace("{{BACKEND_USERS_URL}}", usersUrl)
               .replace("{{BACKEND_PRODUCTS_URL}}", productsUrl);
        
        try (FileWriter writer = new FileWriter(OUTPUT_DIR + "/script.js")) {
            writer.write(js);
        }
    }
}

