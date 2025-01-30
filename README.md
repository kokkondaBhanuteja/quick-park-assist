# Java Quick Park Assist

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [System Architecture](#system-architecture)
5. [Installation Guide](#installation-guide)
6. [Usage](#usage)
7. [Modules](#modules)
8. [Database Design](#database-design)
9. [Future Enhancements](#future-enhancements)
10. [Conclusion](#conclusion)

---

## Introduction
The **Java Quick Park Assist** project is a smart parking system designed to provide users with an easy way to find, book, and manage parking spots with additional services like EV charging and car cleaning. The system allows users to search for available spots, make reservations, and manage their bookings through a seamless and efficient platform.

## Features
- **User Registration:** Allows vehicle owners and parking spot owners to register and manage their profiles.
- **Parking Spot Management:** Enables users to add, view, modify, and remove parking spots.
- **Parking Spot Booking:** Provides an intuitive interface for booking and managing parking spots.
- **Addon Services:** Users can book additional services like cleaning and polishing.
- **EV Charge Reservation:** Facilitates electric vehicle charging slot reservations.

## Technologies Used
### Backend:
- Java (Spring Boot)
- MySQL
- Spring Boot Data JPA
- Spring Boot REST Services

### Frontend:
- Thymeleaf
- HTML, CSS, JavaScript

## System Architecture
+-------------------------------+
|         Frontend UI           |
| (HTML, CSS, JavaScript, Thymeleaf) |
+-------------------------------+
              |
              V
+-------------------------------+
|         Backend (API Layer)   |
|  (Spring Boot, REST APIs)     |
+-------------------------------+
              |
              V
+-------------------------------+
|         Database Layer        |
|       (MySQL, Spring JPA)     |
+-------------------------------+


## Installation Guide
1. **Clone the repository:**
   ```sh
   git clone https://github.com/your-repo/quick-park-assist.git
   cd quick-park-assist
   ```
2. **Set up the database:**
   - Install MySQL and create a database named `quick_park_assist`.
   - Configure database properties in `application.properties`.
3. **Install dependencies:**
   ```sh
   mvn clean install
   ```
4. **Run the application:**
   ```sh
   mvn spring-boot:run
   ```
5. **Access the application:**
   - Open `http://localhost:8080` in a web browser.

## Usage
### User Registration
![User Registration](./images/user_registration.jpeg)
- Register as a vehicle owner or parking spot owner.
- View and update user profile.
- Deactivate or delete account if necessary.

### Parking Spot Management
![Parking Spot Management](./images/parking_spot_management.jpeg)
- Add new parking spots with details like location, pricing, and availability.
- Search and view available spots.
- Modify or remove parking spots.

### Booking Process
![Parking Spot Booking](./images/parking_spot_booking.jpeg)
- Book available spots by selecting a location and time.
- View, modify, or cancel bookings.
- Identify booked spots using a mobile number.

### Addon Services
![Addon Services](./images/addon_service_user.jpeg)
- Book additional services like cleaning and polishing.
- Modify or remove addon services.
    ### For Vehicle Owner 
![Addon Services](./images/addon_service_owner.jpeg)

### EV Charging Reservation
![EV Charge Reservation](./images/ev_charge_reservation.jpeg)
- Reserve EV charging slots.
- Modify or delete reservations.

## Database Design
![Database Schema](./images/database_schema.jpeg)
- **Tables:** Users, ParkingSpots, Bookings, AddonServices, EVReservations.
- **Relationships:** One-to-many mappings between users and bookings, parking spots, and services.

## Future Enhancements
- Implement AI-based dynamic pricing for parking spots.
- Develop a mobile application for better accessibility.
- Integrate real-time parking availability using IoT sensors.

## Conclusion
The Java Quick Park Assist system aims to provide a hassle-free parking experience with value-added services. By leveraging modern web technologies, this platform offers efficiency and convenience for urban commuters and EV owners.

---
For contributions or suggestions, feel free to raise an issue or pull request on GitHub!

