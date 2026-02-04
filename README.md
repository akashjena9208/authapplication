# 🔐 Full Stack Authentication System  
**Spring Boot + React Secure JWT Auth with OAuth & RBAC**

A production-style **full-stack authentication and authorization system** demonstrating how modern applications securely manage user identity, sessions, and role-based access.

This project simulates a real-world auth architecture similar to systems used in SaaS platforms.

## 🚀 What This Project Does

This system provides:

- Secure user registration and login  
- Google & GitHub OAuth login  
- JWT-based authentication  
- Refresh token rotation & revocation  
- Role-based authorization (USER / ADMIN)  
- Secure SPA session handling  
- Full frontend–backend token lifecycle integration  

---

# 🚀 Features

### Authentication
- Secure user registration & login  
- Google OAuth2 login  
- GitHub OAuth2 login  
- BCrypt password hashing  

### Token Security
- Short-lived JWT access tokens  
- Stateful refresh tokens  
- Refresh token rotation  
- Token revocation on logout  

### Authorization
- Role-Based Access Control (USER / ADMIN)  
- Backend-enforced route protection  
- Method-level role security  

### Frontend Security
- Access token stored in memory only  
- Refresh token stored in HttpOnly cookie  
- Axios interceptor for automatic token refresh  
- Protected routes  

---

---

## 🧱 System Architecture

| Layer | Responsibility |
|------|----------------|
| **Frontend (React)** | Secure auth client, token handling, role-based UI |
| **Backend (Spring Boot)** | Identity provider, JWT issuance, security enforcement |
| **Database** | Users, roles, refresh tokens |
| **Browser Cookie** | Secure refresh token storage |

---

## 🔄 Authentication Model

This system uses a **hybrid token model**:

| Token | Type | Storage | Purpose |
|------|------|---------|---------|
| Access Token | Stateless JWT | Memory (frontend) | API access |
| Refresh Token | Stateful | HttpOnly Cookie + DB | Session renewal |

---

---

# 🛡 Security Architecture

| Threat | Defense |
|-------|---------|
| XSS token theft | Access token not stored in localStorage |
| CSRF attacks | HttpOnly + SameSite cookie strategy |
| Token replay | Refresh token rotation |
| Session hijacking | Short access token lifespan |
| Privilege escalation | Backend RBAC enforcement |
| Password compromise | BCrypt hashing |

---


# 🔄 Authentication Flow

1. User submits credentials  
2. Backend verifies password  
3. Access token issued (short-lived)  
4. Refresh token stored in database  
5. Refresh token sent via HttpOnly cookie  
6. Access token stored in frontend memory  

---

# 🔁 Token Refresh Flow

1. Access token expires  
2. Frontend calls `/auth/refresh`  
3. Backend validates refresh token  
4. Refresh token rotated  
5. New access token returned  

---

# 🌍 OAuth Flow

1. User authenticates with Google or GitHub  
2. User account created or linked  
3. Roles assigned  
4. JWT tokens generated  

---

# 👥 Role-Based Access

| Role | Access |
|------|-------|
| USER | Dashboard, profile |
| ADMIN | User management, role control |

---

# ⚙️ Tech Stack

### Frontend
React 18 • Vite • TypeScript • Tailwind CSS • shadcn/ui • Zustand • Axios

### Backend
Spring Boot 3 • Spring Security • JPA • MySQL • JWT • OAuth2 Client

---


## 🌐 API Endpoints

### Auth
POST /api/v1/auth/register  
POST /api/v1/auth/login  
POST /api/v1/auth/refresh  
POST /api/v1/auth/logout  

### Users
GET /api/v1/users  
GET /api/v1/users/{id}  
PUT /api/v1/users/{id}  
DELETE /api/v1/users/{id}  
PUT /api/v1/users/{id}/promote  
PUT /api/v1/users/{id}/demote  

---


---

# ▶️ Running the Project

### Backend
```bash
mvn spring-boot:run
```
### Docker (Backend)
```bash
docker run -p 8080:8080 \
-e SPRING_PROFILES_ACTIVE=prod \
-e DB_URL=jdbc:mysql://host.docker.internal:3306/auth_app \
-e DB_USERNAME=your_user \
-e DB_PASSWORD=your_password \
auth-app-backend
```
### Frontend
```bash npm install  
npm run dev
```
D:\Project\auth_app\auth-app-frontend\public
---

## 📸 Application Screenshots

### 🏠 Landing Page
![Landing](/auth-app-frontend/public/img1.png)
![Landing](/auth-app-frontend/public/img2.png)  
![Landing](/auth-app-frontend/public/img3.png)
![Landing](/auth-app-frontend/public/img4.png) 

### 🔑 Login & Registration 
![Registrar](/auth-app-frontend/public/img5.png)
![Login](/auth-app-frontend/public/img6.png)

### 📊 User Dashboard
![Dashboard](/auth-app-frontend/public/img7.png)  
![Dashboard](/auth-app-frontend/public/img8.png)
![Fotter](/auth-app-frontend/public/img9.png)

### 👤 User Profile
![Profile](/auth-app-frontend/public/img10.png)

---
