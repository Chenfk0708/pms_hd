// Jeez Fitness gateway homepage script

const { createApp } = Vue;

const STATUS = {
    RUNNING: "RUNNING",
    DEGRADED: "DEGRADED",
    OFFLINE: "OFFLINE",
    CHECKING: "CHECKING"
};

function createService(config) {
    return {
        ...config,
        statusCode: STATUS.CHECKING,
        statusLabel: "Checking",
        statusBadge: "bg-secondary",
        statusIcon: "bi bi-hourglass-split"
    };
}

createApp({
    data() {
        return {
            lastUpdateTime: new Date().toLocaleString("zh-CN"),
            buildTime: new Date().toLocaleString("zh-CN"),
            services: [
                createService({
                    name: "Auth Service",
                    description: "Identity and authentication center.",
                    port: "8081",
                    path: "/auth",
                    icon: "bi bi-shield-lock-fill",
                    swaggerUrl: "/auth/swagger-ui/index.html",
                    healthUrl: "/auth/actuator/health",
                    headerClass: "bg-auth"
                }),
                createService({
                    name: "Member Service",
                    description: "Member profiles, cards and reservations.",
                    port: "8082",
                    path: "/member",
                    icon: "bi bi-people-fill",
                    swaggerUrl: "/member/swagger-ui/index.html",
                    healthUrl: "/member/actuator/health",
                    headerClass: "bg-member"
                }),
                createService({
                    name: "Store Service",
                    description: "Store info and facility configuration.",
                    port: "8083",
                    path: "/store",
                    icon: "bi bi-shop",
                    swaggerUrl: "/store/swagger-ui/index.html",
                    healthUrl: "/store/actuator/health",
                    headerClass: "bg-store"
                }),
                createService({
                    name: "Equipment Service",
                    description: "Device management and real-time communication.",
                    port: "8084",
                    path: "/equipment",
                    icon: "bi bi-gear-fill",
                    swaggerUrl: "/equipment/swagger-ui/index.html",
                    healthUrl: "/equipment/actuator/health",
                    headerClass: "bg-equipment"
                }),
                createService({
                    name: "Coach Service",
                    description: "Coach profiles and scheduling.",
                    port: "8085",
                    path: "/coach",
                    icon: "bi bi-person-badge-fill",
                    swaggerUrl: "/coach/swagger-ui/index.html",
                    healthUrl: "/coach/actuator/health",
                    headerClass: "bg-coach"
                }),
                createService({
                    name: "Course Service",
                    description: "Course catalog and timetable.",
                    port: "8086",
                    path: "/course",
                    icon: "bi bi-calendar-check-fill",
                    swaggerUrl: "/course/swagger-ui/index.html",
                    healthUrl: "/course/actuator/health",
                    headerClass: "bg-course"
                }),
                createService({
                    name: "Manager Service",
                    description: "Admin management and system logs.",
                    port: "8087",
                    path: "/manager",
                    icon: "bi bi-speedometer2",
                    swaggerUrl: "/manager/swagger-ui/index.html",
                    healthUrl: "/manager/actuator/health",
                    headerClass: "bg-manager"
                })
            ],
            infrastructures: [
                {
                    name: "MySQL",
                    description: "Relational database",
                    port: "3306",
                    icon: "bi bi-database-fill",
                    color: "#00758F",
                    url: null
                },
                {
                    name: "Redis",
                    description: "Cache and session storage",
                    port: "6379",
                    icon: "bi bi-lightning-charge-fill",
                    color: "#DC382D",
                    url: null
                },
                {
                    name: "MinIO",
                    description: "Object storage service",
                    port: "9000 / 9001",
                    icon: "bi bi-cloud-upload-fill",
                    color: "#C72C48",
                    url: "https://minio.fitness.dualseason.com"
                },
                {
                    name: "Nacos",
                    description: "Configuration and service discovery center",
                    port: "8848",
                    icon: "bi bi-hdd-network-fill",
                    color: "#2563EB",
                    url: "https://nacos.fitness.dualseason.com"
                },
                {
                    name: "RocketMQ",
                    description: "Message queue",
                    port: "9876 / 10911",
                    icon: "bi bi-envelope-fill",
                    color: "#D77310",
                    url: null
                },
                {
                    name: "Gateway",
                    description: "Gateway route monitor",
                    port: "8080",
                    icon: "bi bi-door-open-fill",
                    color: "#6610f2",
                    url: "/actuator/gateway/routes"
                },
                {
                    name: "Swagger",
                    description: "Aggregated API docs",
                    port: "8080",
                    icon: "bi bi-file-earmark-code-fill",
                    color: "#85EA2D",
                    url: "/swagger-gateway.html"
                }
            ],
            techStack: [
                { name: "Java", version: "17", icon: "bi bi-cup-hot-fill", color: "#f89820" },
                { name: "Spring Boot", version: "3.1.5", icon: "bi bi-bootstrap-fill", color: "#6DB33F" },
                { name: "Spring Cloud", version: "2022.0.4", icon: "bi bi-cloud-fill", color: "#6DB33F" },
                { name: "MySQL", version: "8.0", icon: "bi bi-database-fill", color: "#00758F" },
                { name: "Redis", version: "7+", icon: "bi bi-lightning-charge-fill", color: "#DC382D" },
                { name: "RocketMQ", version: "5.1.4", icon: "bi bi-envelope-fill", color: "#D77310" },
                { name: "MyBatis Plus", version: "3.5.9", icon: "bi bi-code-square", color: "#0080FF" },
                { name: "Sa-Token", version: "1.37.0", icon: "bi bi-shield-lock-fill", color: "#F56C6C" },
                { name: "MinIO", version: "Latest", icon: "bi bi-cloud-upload-fill", color: "#C72C48" },
                { name: "Docker", version: "Latest", icon: "bi bi-box-fill", color: "#2496ED" },
                { name: "Vue", version: "3.3", icon: "bi bi-code-slash", color: "#42B883" },
                { name: "Bootstrap", version: "5.3", icon: "bi bi-bootstrap-fill", color: "#7952B3" }
            ]
        };
    },

    computed: {
        totalServices() {
            return this.services.length;
        },
        runningServices() {
            return this.services.filter((service) => service.statusCode === STATUS.RUNNING).length;
        }
    },

    methods: {
        setServiceState(service, status) {
            service.statusCode = status;
            if (status === STATUS.RUNNING) {
                service.statusLabel = "Running";
                service.statusBadge = "bg-success";
                service.statusIcon = "bi bi-check-circle-fill";
                return;
            }
            if (status === STATUS.DEGRADED) {
                service.statusLabel = "Issue";
                service.statusBadge = "bg-warning";
                service.statusIcon = "bi bi-exclamation-triangle-fill";
                return;
            }
            if (status === STATUS.OFFLINE) {
                service.statusLabel = "Offline";
                service.statusBadge = "bg-danger";
                service.statusIcon = "bi bi-x-circle-fill";
                return;
            }
            service.statusLabel = "Checking";
            service.statusBadge = "bg-secondary";
            service.statusIcon = "bi bi-hourglass-split";
        },

        async refreshStatus() {
            this.lastUpdateTime = "Refreshing...";
            const checks = this.services.map((service) => this.checkServiceHealth(service));
            await Promise.all(checks);
            this.lastUpdateTime = new Date().toLocaleString("zh-CN");
        },

        async checkServiceHealth(service) {
            this.setServiceState(service, STATUS.CHECKING);
            try {
                const response = await axios.get(service.healthUrl, { timeout: 5000 });
                const healthStatus = response?.data?.status || response?.data?.data?.status;
                this.setServiceState(service, healthStatus === "UP" ? STATUS.RUNNING : STATUS.DEGRADED);
            } catch (error) {
                this.setServiceState(service, STATUS.OFFLINE);
            }
        }
    },

    mounted() {
        this.refreshStatus();
    }
}).mount("#app");

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function onClick(event) {
        const href = this.getAttribute("href");
        if (href !== "#") {
            event.preventDefault();
            const target = document.querySelector(href);
            if (target) {
                target.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        }
    });
});

// Navbar shadow on scroll
window.addEventListener("scroll", () => {
    const navbar = document.querySelector(".top-nav");
    if (!navbar) return;
    if (window.scrollY > 50) {
        navbar.classList.add("shadow-lg");
    } else {
        navbar.classList.remove("shadow-lg");
    }
});
