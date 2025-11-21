from locust import HttpUser, task, between
import os

class ApiGatewayUser(HttpUser):
    wait_time = between(1, 3)
    host = os.getenv("TARGET_HOST", "http://localhost:8080")

    @task
    def list_users(self):
        self.client.get("/user-service/api/users", name="list_users")

    @task
    def list_products(self):
        self.client.get("/product-service/api/products", name="list_products")

    @task
    def list_orders(self):
        self.client.get("/order-service/api/orders", name="list_orders")