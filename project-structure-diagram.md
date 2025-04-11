# Project Structure Diagram

## Overview
This document provides a comprehensive visualization of the project structure for the e-commerce application. The diagram shows the relationships between different components and how they interact with each other.

```
project-grupo-6/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── es/xpressaly/
│   │   │       ├── Controller/           # HTTP request handlers
│   │   │       │   ├── ProductController.java    # Handles product-related requests
│   │   │       │   ├── OrderController.java      # Handles order/cart operations
│   │   │       │   ├── UserController.java       # Handles user authentication
│   │   │       │   ├── ImageController.java      # Handles image uploads/retrieval
│   │   │       │   ├── ReviewController.java     # Handles product reviews
│   │   │       │   ├── AuthController.java       # Handles authentication
│   │   │       │   ├── InitialPageController.java # Handles landing page
│   │   │       │   └── DataBaseUsage.java        # Database utilities
│   │   │       │
│   │   │       ├── api/                  # REST API controllers
│   │   │       │   ├── ProductApiController.java # Product API endpoints
│   │   │       │   ├── OrderApiController.java   # Order API endpoints
│   │   │       │   └── UserApiController.java    # User API endpoints
│   │   │       │
│   │   │       ├── dto/                  # Data Transfer Objects
│   │   │       │   ├── ProductDTO.java          # Product data transfer
│   │   │       │   ├── UserDTO.java             # User data transfer
│   │   │       │   ├── OrderDTO.java            # Order data transfer
│   │   │       │   └── ReviewDTO.java           # Review data transfer
│   │   │       │
│   │   │       ├── mapper/               # Object mappers
│   │   │       │   ├── ProductMapper.java       # Maps Product <-> ProductDTO
│   │   │       │   ├── UserMapper.java          # Maps User <-> UserDTO
│   │   │       │   ├── OrderMapper.java         # Maps Order <-> OrderDTO
│   │   │       │   └── ReviewMapper.java        # Maps Review <-> ReviewDTO
│   │   │       │
│   │   │       ├── Model/                # Data models
│   │   │       │   ├── Product.java             # Product entity
│   │   │       │   ├── User.java                # User entity
│   │   │       │   ├── UserRole.java            # User role enumeration
│   │   │       │   ├── Order.java               # Order entity
│   │   │       │   ├── OrderUpdateResponse.java # Order update response
│   │   │       │   └── Review.java              # Product review entity
│   │   │       │
│   │   │       ├── Repository/          # Database access layer
│   │   │       │   ├── ProductRepository.java   # Product data operations
│   │   │       │   ├── UserRepository.java      # User data operations
│   │   │       │   ├── OrderRepository.java     # Order data operations
│   │   │       │   └── ReviewRepository.java    # Review data operations
│   │   │       │
│   │   │       ├── Service/             # Business logic layer
│   │   │       │   ├── ProductService.java      # Product business logic
│   │   │       │   ├── UserService.java         # User authentication/management
│   │   │       │   ├── OrderService.java        # Order processing logic
│   │   │       │   └── ReviewService.java       # Review management logic
│   │   │       │
│   │   │       └── XpressalyApplication.java # Main application entry point
│   │   │
│   │   └── resources/
│   │       ├── static/                  # Static resources
│   │       │   ├── css/                        # Stylesheets
│   │       │   │   ├── header.css              # Header styling
│   │       │   │   ├── product-cards.css       # Product card styling
│   │       │   │   ├── product-details.css     # Product details page styling
│   │       │   │   ├── wellcome.css            # Home page styling
│   │       │   │   ├── my_order.css            # Order page styling
│   │       │   │   └── profile-modern.css      # User profile styling
│   │       │   │
│   │       │   ├── js/                         # JavaScript files
│   │       │   │   └── product-interactions.js # Product interaction logic
│   │       │   │
│   │       │   └── images/                     # Static images
│   │       │       └── Logos/                  # Logo images
│   │       │
│   │       ├── templates/               # Mustache templates
│   │       │   ├── common/                     # Shared template components
│   │       │   │   ├── header.mustache         # Site header
│   │       │   │   └── footer.mustache         # Site footer
│   │       │   │
│   │       │   ├── Wellcome.mustache          # Home page with product listing
│   │       │   ├── Product.mustache            # Product details page
│   │       │   ├── CreateProduct.mustache      # Admin product creation form
│   │       │   ├── Login.mustache              # User login page
│   │       │   ├── Register.mustache           # User registration page
│   │       │   ├── Profile.mustache            # User profile page
│   │       │   └── Order.mustache              # Shopping cart/order page
│   │       │
│   │       └── application.properties   # Application configuration
│   │
│   └── test/                      # Test code
│       └── java/
│           └── es/codeurjc/webapp/
│               ├── controller/           # Controller tests
│               ├── service/              # Service tests
│               └── repository/           # Repository tests
│
├── target/                        # Compiled output
├── .mvn/                          # Maven wrapper
├── pom.xml                        # Maven project configuration
└── README.md                      # Project documentation
```

## Component Relationships

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Controllers   │────▶│    Services     │────▶│  Repositories   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                      │                        │
         ▼                      │                        │
┌─────────────────┐             │                        │
│  API Controllers│             │                        │
└─────────────────┘             │                        │
         │                      │                        │
         ▼                      ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│      DTOs       │◀───▶│     Models      │◀────│    Database     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                      ▲
         ▼                      │
┌─────────────────┐     ┌─────────────────┐
│     Mappers     │     │    Templates    │
└─────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │  Static Assets  │
                        │  (CSS, JS, img) │
                        └─────────────────┘
```

## Data Flow

1. **User Request Flow**:
   - Browser → Controller → Service → Repository → Database
   - Database → Repository → Service → Controller → Template → Browser

2. **API Request Flow**:
   - Client → APIController → Service → Repository → Database
   - Database → Repository → Service → APIController → JSON → Client

## Key Components

### Frontend
- **Templates**: Mustache templates for rendering HTML
- **CSS**: Styling for different pages and components
- **JavaScript**: Client-side interactivity (product search, cart management)

### Backend
- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic
- **Repositories**: Data access layer for database operations
- **Models**: Data structures representing business entities

### Database
- Stores product, user, order, and rating information

This structure follows the MVC (Model-View-Controller) pattern with additional service and repository layers for better separation of concerns.