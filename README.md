<div align="center">
<hr>
</div>

<div align="center">
<a href="https://youtu.be/0DgMbbMePc4" target="blank">
</a>

<h2>Parkeer - IoT Smart Parking System</h2>

<img src="./assets/bg_parkeer.png" width="80%">

</div>

💡 Overview

Parkeer is a comprehensive smart parking ecosystem that integrates a mobile application with physical hardware (IoT). Designed to solve parking congestion, the system allows users to book slots remotely, while the hardware ensures only valid bookings can access the facility.

The system synchronizes Real-time Database events between the Android App and the ESP32 Microcontroller to control gates, monitor slot occupancy, and manage payments instantly.

🔗 Watch Demo on YouTube

✨ Key Features

📱 Mobile Application

Real-time Slot Monitoring: View slot status (Available 🟢, Booked 🟡, Occupied 🔴) updated instantly from hardware sensors.

Secure Booking System: Reserve specific slots in advance. The app performs atomic transactions to deduct wallet balance safely.

QR Code Access: Scan the QR Code displayed at the parking gate to validate booking and open the barrier.

Auto-Refund Policy: If a user cancels a booking before scanning, the system automatically releases the slot and refunds the balance to the user's wallet.

Vehicle Management: Register multiple vehicles for easy booking.

🤖 IoT Hardware (Smart Gate & Slots)

Automated Gate Control: Servo motors open the gate only upon successful QR validation from the app.

Security Logic: No Booking = No Entry. The buzzer alerts if an unauthorized vehicle attempts to enter.

Sensor Fusion:

Gate Sensors: Detect vehicle presence at entry/exit points.

Slot Sensors: 5 IR sensors monitor individual parking spots.

Visual Feedback: OLED Display shows dynamic QR codes and status messages; LEDs indicate slot status physically.

🛠 Tech Stack

Android (Software)

Language: Java

IDE: Android Studio

Database:

Firebase Firestore: User profiles, Malls data, Vehicle info.

Firebase Realtime Database (RTDB): High-speed sync for Slot Status and Gate Triggers.

Libraries: zxing-android-embedded (QR Scan), Firebase Auth, Glide.

IoT (Hardware/Firmware)

Microcontroller: ESP32

Language: C++ (Arduino IDE)

Communication: WiFi & FirebaseClient Library.

Components:

2x Servo Motors (MG996R/SG90)

7x IR Obstacle Sensors (5 Slots + 2 Gates)

1x OLED Display (SSD1306)

1x Buzzer

LED Indicators

📄 Project Members

Leo Fernandy

Leonardo

Vincent Liawis

Stanley Lim

Erick Budi

🏫 Supervisor:

Mr. Ade Maulana

📅 Getting Started

Prerequisites

Android Studio (Latest Stable)

Arduino IDE (with ESP32 Board Manager installed)

Firebase Project (configured with both Firestore and Realtime Database)

1. Android Setup

Clone the repository:

git clone [https://github.com/Leofernandy/ParkeerIoTA.git](https://github.com/Leofernandy/ParkeerIoTA.git)


Add your google-services.json file to the app/ directory.

Sync Gradle and Build the project.

2. Hardware (Firmware) Setup

Open the ParkingSystem_QR_Gate.ino file in Arduino IDE.

Install required libraries:

Firebase ESP32 Client

ESP32Servo

ESP8266 and ESP32 OLED driver for SSD1306 displays

QRcodeOled

Create a secrets.h file and configure your credentials:

#define WIFI_SSID "YourWiFiName"
#define WIFI_PASSWORD "YourWiFiPass"
#define REFERENCE_URL "[https://your-project.firebaseio.com](https://your-project.firebaseio.com)"
#define AUTH_TOKEN "YourDatabaseSecret"
