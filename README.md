# Xpressaly 🛒

## Contributors
| Name | Email | GitHub |
|-------|-------|--------|
| Remus Dan Andrei Narita | rd.narita.2023@alumnos.urjc.es | [@Ten1O0](https://github.com/Ten1O0) |
| Daniel Navarro Martín | d.navarrom.2023@alumnos.urjc.es | [@daniel-navarro-ls](https://github.com/daniel-navarro-ls) |
| Laura Sánchez Santiago | l.sanchezsan.2023@alumnos.urjc.es | [@Laura-261](https://github.com/Laura-261) |
| Fernando Torres León | f.torres.2023@alumnos.urjc.es | [@fernandotorres-sudo](https://github.com/fernandotorres-sudo) |

## Web Description❔
**Xpressaly** is a modern web-based marketplace that enables users to buy and sell a wide range of products seamlessly. Whether you're searching for the latest tech gadgets, trendy fashion, or everyday essentials, Xpressaly offers a secure and intuitive platform to explore thousands of listings. Built using Spring Boot and following best practices in web development, Xpressaly ensures a smooth shopping experience, complete with user accounts, product reviews, and order tracking. Start discovering new deals or listing your own products today with Xpressaly.

## Application Overview

### Entities and Diagram
![image](https://github.com/user-attachments/assets/45d52273-8d5f-42d3-bfb2-c27e602f6479)

#### **USER**

- Can place **multiple orders** → `1:N` with **ORDER**
- Can write **multiple reviews** → `1:N` with **REVIEW**
- Can have **multiple products in their cart** → `N:M` with **PRODUCT** (via cart)

#### **PRODUCT**

- Can appear in **multiple orders** → `N:M` with **ORDER**
- Can be reviewed by **multiple users** → `1:N` with **REVIEW**
- Can be in the cart of **multiple users** → `N:M` with **USER**

#### **ORDER**

- Is placed by **one user** → `N:1` with **USER**
- Contains **one or more products** → `N:M` with **PRODUCT**

#### **REVIEW**

- Is written by **one user** → `N:1` with **USER**
- Refers to **one product** → `N:1` with **PRODUCT**


### User Permissions🕴️
- **Admin**: full control to create, update, and delete users, products and reviews. Can place and delete their orders.
- **User**: can buy and review products, delete their own reviews and deactivate their account.
- **Guest**: can browse products and reviews, with the option to register for full access.

### Images🖼️
- **PRODUCT**: one main image for each product.

## Collaborative Development

### Dan: top 5 commits - Phase 3

1. Delete reviews with permissions → [54dcb14](https://github.com/DWS-2025/project-grupo-6/commit/54dcb142525f5274d3313028875e6fe848d89a11)
2. Authentication and Registration Validation Driver Improvements → [d6bd98a](https://github.com/DWS-2025/project-grupo-6/commits/main/?author=Ten1O0)
3. Product Management Implementation → [8d83457](https://github.com/DWS-2025/project-grupo-6/commit/8d83457880947f9a5c3139f87ebd11037a6938c8)
4. Review Management Implementation → [173596a](https://github.com/DWS-2025/project-grupo-6/commit/173596af824dab1b0b0d460f1685ec4d4042195a)
5. PDF document upload and viewing functionality → [d16b07a](https://github.com/DWS-2025/project-grupo-6/commit/d16b07a4f5bde564380da0bb0762e9481c437db5)
> *Also responsible for a significant portion of the site’s visual design.*

### Daniel: top 5 commits - Phase 3

1. Tokens CSRF activos → [0692938](https://github.com/DWS-2025/project-grupo-6/commit/06929385343d2a570e57967c2ed2042a7ffd3122)
2. Cambios API → [236f46f](https://github.com/DWS-2025/project-grupo-6/commit/236f46f17659d75054ba3e84fd63d20c5473eeb0)
3. Postman collection actualizada → [62b7465](https://github.com/DWS-2025/project-grupo-6/commit/62b7465f27aeaecd15f331b85cb3ef70e15ac2ac)
4. Actualizacion comprobaciones api → [73fde61](https://github.com/DWS-2025/project-grupo-6/commit/73fde61ce7cd74a0956d00240a8006aeef761039)
5. Api para las reviews → [39d5b5e](https://github.com/DWS-2025/project-grupo-6/commit/39d5b5e26141653c9b62d76e73ec1a0cec0bb54a)

### Laura: top 5 commits - Phase 3

1. Guest mode → [9d315c2](https://github.com/DWS-2025/project-grupo-6/commit/9d315c2abd0573c97d4b6a4a6134d9b9692d1153) & [517fc13](https://github.com/DWS-2025/project-grupo-6/commit/517fc1329e35182ac7f459b26822612022d43649)
2. HTTPs port → [c2a92e7](https://github.com/DWS-2025/project-grupo-6/commit/c2a92e75fef19215cb58af811c8ac6ba02127102)
3. Fix Content-Type vulnerability → [ddb3acd](https://github.com/DWS-2025/project-grupo-6/commit/ddb3acd30d24b621932766b2750cc564172ffba2)
4. Control access in Order entity → [093cc93](https://github.com/DWS-2025/project-grupo-6/commit/093cc93f4ca8dda9f2aabcc2a7d8f59517216df8)
5. Pdf security → [8c7c6f3](https://github.com/DWS-2025/project-grupo-6/commit/8c7c6f3b05dbb856da39fd6f855ea43b6536bc38)

### Fernando: top 5 commits - Phase 3

1. 



-[@Ten1O0](https://github.com/Ten1O0), [@daniel-navarro-ls](https://github.com/daniel-navarro-ls), [@Laura-261](https://github.com/Laura-261), [@fernandotorres-sudo](https://github.com/fernandotorres-sudo)
