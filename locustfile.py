"""
Pruebas de Rendimiento y Estrés con Locust
===========================================

Este archivo contiene pruebas de carga y rendimiento para el User Service
simulando casos de uso reales del sistema.

Instalación:
    pip install locust

Ejecución con interfaz web:
    locust -f locustfile.py
    Luego abre: http://localhost:8089

Ejecución headless (sin interfaz):
    locust -f locustfile.py --headless -u 100 -r 10 -t 60s --host http://localhost:8080

Parámetros:
    -u 100: 100 usuarios concurrentes
    -r 10: Incrementa 10 usuarios por segundo
    -t 60s: Duración total de 60 segundos
"""

from locust import HttpUser, task, between, events
import random
import json

# ============================================
# Clase Principal: Usuario Normal del Sistema
# ============================================
class EcommerceUser(HttpUser):
    """
    Simula un usuario típico del sistema de e-commerce
    que realiza operaciones CRUD sobre credenciales
    """
    
    wait_time = between(1, 3)  # Espera entre 1 y 3 segundos entre peticiones
    host = "http://localhost:8080"  # API Gateway
    
    def on_start(self):
        """Se ejecuta una vez al iniciar cada usuario simulado"""
        self.credential_id = None
        self.username = f"loadtest_user_{random.randint(1000, 9999)}"
        print(f"✅ Usuario iniciado: {self.username}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 1: Health Check
    # Peso: 5 (se ejecuta frecuentemente)
    # ============================================
    @task(5)
    def health_check(self):
        """
        Verifica el estado de salud del servicio.
        Esta es la operación más frecuente para monitoreo.
        """
        with self.client.get(
            "/user-service/actuator/health",
            catch_response=True,
            name="[READ] Health Check"
        ) as response:
            if response.status_code == 200:
                if "UP" in response.text:
                    response.success()
                else:
                    response.failure("Service not UP")
            else:
                response.failure(f"Status code: {response.status_code}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 2: Listar Credenciales (Lectura)
    # Peso: 10 (operación más frecuente)
    # ============================================
    @task(10)
    def list_credentials(self):
        """
        Lista todas las credenciales.
        Simula la operación más común en el sistema.
        """
        with self.client.get(
            "/user-service/api/credentials",
            catch_response=True,
            name="[READ] List All Credentials"
        ) as response:
            if response.status_code == 200:
                try:
                    credentials = response.json()
                    if isinstance(credentials, list):
                        response.success()
                    else:
                        response.failure("Response is not a list")
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Status code: {response.status_code}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 3: Crear Credencial (Escritura)
    # Peso: 2 (menos frecuente que lectura)
    # ============================================
    @task(2)
    def create_credential(self):
        """
        Crea una nueva credencial.
        Simula el registro de nuevos usuarios.
        """
        payload = {
            "username": self.username,
            "password": f"Pass{random.randint(1000, 9999)}@123",
            "isEnabled": True,
            "isAccountNonExpired": True,
            "isAccountNonLocked": True,
            "isCredentialsNonExpired": True
        }
        
        with self.client.post(
            "/user-service/api/credentials",
            json=payload,
            catch_response=True,
            name="[WRITE] Create Credential"
        ) as response:
            if response.status_code == 201:
                try:
                    result = response.json()
                    self.credential_id = result.get("credentialId")
                    response.success()
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Status code: {response.status_code}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 4: Obtener Credencial por ID (Lectura)
    # Peso: 8
    # ============================================
    @task(8)
    def get_credential_by_id(self):
        """
        Obtiene una credencial específica por ID.
        Simula la consulta de perfil de usuario.
        """
        # Usar ID aleatorio entre 1 y 100 o el creado previamente
        credential_id = self.credential_id if self.credential_id else random.randint(1, 100)
        
        with self.client.get(
            f"/user-service/api/credentials/{credential_id}",
            catch_response=True,
            name="[READ] Get Credential by ID"
        ) as response:
            if response.status_code == 200:
                response.success()
            elif response.status_code == 404:
                # 404 es aceptable (el ID puede no existir)
                response.success()
            else:
                response.failure(f"Unexpected status: {response.status_code}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 5: Actualizar Credencial (Escritura)
    # Peso: 1 (operación menos frecuente)
    # ============================================
    @task(1)
    def update_credential(self):
        """
        Actualiza una credencial existente.
        Simula la actualización de perfil de usuario.
        """
        if not self.credential_id:
            return  # Skip si no hay credencial creada
        
        payload = {
            "credentialId": self.credential_id,
            "username": f"{self.username}_updated",
            "password": f"NewPass{random.randint(1000, 9999)}@456",
            "isEnabled": True,
            "isAccountNonExpired": True,
            "isAccountNonLocked": True,
            "isCredentialsNonExpired": True
        }
        
        with self.client.put(
            "/user-service/api/credentials",
            json=payload,
            catch_response=True,
            name="[WRITE] Update Credential"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status code: {response.status_code}")

    # ============================================
    # PRUEBA DE RENDIMIENTO 6 (BONUS): Métricas Prometheus
    # Peso: 3
    # ============================================
    @task(3)
    def prometheus_metrics(self):
        """
        Consulta las métricas de Prometheus.
        Simula el monitoreo del sistema.
        """
        with self.client.get(
            "/user-service/actuator/prometheus",
            catch_response=True,
            name="[MONITORING] Prometheus Metrics"
        ) as response:
            if response.status_code == 200:
                if "jvm_memory_used_bytes" in response.text:
                    response.success()
                else:
                    response.failure("Metrics not found")
            else:
                response.failure(f"Status code: {response.status_code}")


# ============================================
# Clase de Usuarios Agresivos (Pico de Carga)
# ============================================
class SpikingUser(HttpUser):
    """
    Simula usuarios con comportamiento más agresivo
    para probar picos de carga y resistencia del sistema
    """
    
    wait_time = between(0.1, 0.5)  # Espera muy corta
    host = "http://localhost:8080"
    
    @task(1)
    def spike_health_check(self):
        """Hammering del health endpoint"""
        self.client.get(
            "/user-service/actuator/health",
            name="[SPIKE] Health Check"
        )
    
    @task(1)
    def spike_list_credentials(self):
        """Hammering del endpoint de listado"""
        self.client.get(
            "/user-service/api/credentials",
            name="[SPIKE] List Credentials"
        )


# ============================================
# Clase de Usuarios de Lectura Pesada
# ============================================
class ReadHeavyUser(HttpUser):
    """
    Simula usuarios que solo realizan operaciones de lectura.
    Útil para probar el rendimiento de queries y cache.
    """
    
    wait_time = between(0.5, 2)
    host = "http://localhost:8080"
    
    @task(15)
    def read_list(self):
        """Lectura intensiva de listados"""
        self.client.get(
            "/user-service/api/credentials",
            name="[READ-HEAVY] List"
        )
    
    @task(10)
    def read_by_id(self):
        """Lectura intensiva por ID"""
        credential_id = random.randint(1, 100)
        self.client.get(
            f"/user-service/api/credentials/{credential_id}",
            name="[READ-HEAVY] By ID"
        )


# ============================================
# Clase de Usuarios de Escritura Pesada
# ============================================
class WriteHeavyUser(HttpUser):
    """
    Simula usuarios que realizan muchas operaciones de escritura.
    Útil para probar el rendimiento de transacciones y base de datos.
    """
    
    wait_time = between(1, 2)
    host = "http://localhost:8080"
    
    @task(10)
    def write_create(self):
        """Escritura intensiva de creación"""
        payload = {
            "username": f"write_heavy_{random.randint(10000, 99999)}",
            "password": f"Pass{random.randint(1000, 9999)}@123",
            "isEnabled": True,
            "isAccountNonExpired": True,
            "isAccountNonLocked": True,
            "isCredentialsNonExpired": True
        }
        
        self.client.post(
            "/user-service/api/credentials",
            json=payload,
            name="[WRITE-HEAVY] Create"
        )


# ============================================
# Event Listeners para Logging Avanzado
# ============================================
@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Se ejecuta cuando inicia el test de carga"""
    print("\n" + "="*80)
    print("🚀 INICIANDO PRUEBAS DE RENDIMIENTO - USER SERVICE")
    print("="*80)
    print(f"Host: {environment.host}")
    print(f"Usuarios: {environment.runner.target_user_count}")
    print("="*80 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Se ejecuta cuando termina el test de carga"""
    print("\n" + "="*80)
    print("✅ PRUEBAS DE RENDIMIENTO COMPLETADAS")
    print("="*80)
    
    # Obtener estadísticas
    stats = environment.stats
    print(f"\n📊 RESUMEN DE RESULTADOS:")
    print(f"   Total de requests: {stats.total.num_requests}")
    print(f"   Requests fallidos: {stats.total.num_failures}")
    print(f"   Tasa de fallos: {stats.total.fail_ratio:.2%}")
    print(f"   Tiempo promedio de respuesta: {stats.total.avg_response_time:.2f}ms")
    print(f"   Tiempo máximo de respuesta: {stats.total.max_response_time:.2f}ms")
    print(f"   RPS (requests/seg): {stats.total.current_rps:.2f}")
    print("="*80 + "\n")


# ============================================
# Configuración de Escenarios Personalizados
# ============================================
"""
Para ejecutar escenarios específicos:

1. Solo usuarios normales:
   locust -f locustfile.py --user-classes EcommerceUser

2. Solo picos de carga:
   locust -f locustfile.py --user-classes SpikingUser

3. Solo lectura pesada:
   locust -f locustfile.py --user-classes ReadHeavyUser

4. Solo escritura pesada:
   locust -f locustfile.py --user-classes WriteHeavyUser

5. Mix personalizado (usuarios normales + picos):
   locust -f locustfile.py --user-classes EcommerceUser SpikingUser
"""

# ============================================
# Métricas Objetivo
# ============================================
"""
MÉTRICAS OBJETIVO PARA EL USER SERVICE:

✅ Latencia:
   - P50 < 100ms
   - P95 < 500ms
   - P99 < 1000ms

✅ Throughput:
   - > 100 requests/segundo
   - > 1000 requests/minuto

✅ Disponibilidad:
   - Tasa de error < 1%
   - Sin timeouts bajo carga normal

✅ Resistencia:
   - Soportar 100 usuarios concurrentes
   - Degradación gradual bajo estrés
   - Recuperación automática después de picos
"""
