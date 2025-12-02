#include <Arduino.h>
#include <WiFi.h>
#include <Firebase.h>
#include <FirebaseJson.h>
#include <Wire.h>
#include <ESP32Servo.h>
#include <SSD1306.h>
#include "secrets.h" 
#include <qrcodeoled.h>

// ===== OLED =====
#define SCREEN_ADDRESS 0x3C
#define OLED_SDA 21
#define OLED_SCL 22
SSD1306 display(SCREEN_ADDRESS, OLED_SDA, OLED_SCL);
QRcodeOled qrcode(&display);

// ===== SLOT PINS =====
#define IR1 32
#define LED_R1 25
#define LED_G1 26
#define IR2 33
#define LED_R2 27
#define LED_G2 14
#define IR3 34
#define LED_R3 12
#define LED_G3 13
#define IR4 35
#define LED_R4 18
#define LED_G4 19
#define IR5 39
#define LED_R5 4
#define LED_G5 5

// ===== PALANG =====
#define IR_MASUK 36
#define SERVO_MASUK 15
#define IR_KELUAR 23
#define SERVO_KELUAR 2

// ===== BUZZER =====
#define BUZZER 16

Servo servoMasuk;
Servo servoKeluar;

Firebase fb(REFERENCE_URL, AUTH_TOKEN);
String mallId = "mall01";

// local status
String slotStatus[5] = {"available","available","available","available","available"};
String slotBookingId[5] = {"","","","",""}; 
int sensorValue[5]   = {0,0,0,0,0};

const int irPins[5] = {IR1, IR2, IR3, IR4, IR5};
const int ledR[5]   = {LED_R1, LED_R2, LED_R3, LED_R4, LED_R5};
const int ledG[5]   = {LED_G1, LED_G2, LED_G3, LED_G4, LED_G5};

String lastState = "";
unsigned long lastFetch = 0;

// ===== Variabel Timer Non-Blocking untuk Palang =====
unsigned long servoMasukOpenTime = 0;
unsigned long servoKeluarOpenTime = 0;
bool servoMasukIsOpen = false;
bool servoKeluarIsOpen = false;
const long servoOpenDuration = 350; // Durasi palang terbuka (ms)

// ===== VARIABEL STATUS =====
bool isWaitingForScan = false;     
bool anySlotBooked = false;        
unsigned long lastBookingCheck = 0; 

void beep(){
  tone(BUZZER, 2000); delay(90);
  noTone(BUZZER);
}

void setup(){
  Serial.begin(115200);
  Wire.begin(OLED_SDA, OLED_SCL);

  display.init();
  display.flipScreenVertically();
  display.clear();
  display.drawString(0,0,"Connecting WiFi...");
  display.display();

  qrcode.init(); 

  pinMode(BUZZER, OUTPUT);

  for(int i=0;i<5;i++){
    pinMode(irPins[i], INPUT);
    pinMode(ledR[i], OUTPUT);
    pinMode(ledG[i], OUTPUT);
    digitalWrite(ledR[i], LOW);
    digitalWrite(ledG[i], HIGH);
  }
  pinMode(IR_MASUK, INPUT);
  pinMode(IR_KELUAR, INPUT);

  servoMasuk.attach(SERVO_MASUK);
  servoKeluar.attach(SERVO_KELUAR);
  servoMasuk.write(0);
  servoKeluar.write(0);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while(WiFi.status()!=WL_CONNECTED){
    delay(300);
    Serial.print(".");
  }

  display.clear();
  display.drawString(0,0,"WiFi Connected!");
  display.display();
  delay(500);

  fetchFirebase();

  // Startup Beep
  Serial.println("System Ready!");
  beep(); delay(100); beep();
}

void fetchFirebase(){
  String raw;
  int code = fb.getString("slots/"+mallId, raw);
  
  if(code != 200 || raw.length() < 5) {
    Serial.println("[FETCH] Gagal.");
    return; 
  }
  Serial.println("[FETCH] Sukses! Parsing...");

  FirebaseJson json;
  json.setJsonData(raw);

  anySlotBooked = false;  // Reset status

  for(int i=0;i<5;i++){
    FirebaseJsonData result;
    String path = "S" + String(i+1);
    
    if(json.get(result, path + "/status")) {  
      slotStatus[i] = result.stringValue; 
      
      if(json.get(result, path + "/bookingId")) {
        slotBookingId[i] = result.stringValue;
        if(slotStatus[i] == "booked" && slotBookingId[i] != ""){
          anySlotBooked = true; // Ada booking!
          Serial.print("[FETCH] Menemukan slot booked: S");
          Serial.println(i+1);
        }
      } else {
        slotBookingId[i] = "";
      }
    } 
    else {
      if(json.get(result, path)) {
        slotStatus[i] = result.stringValue; 
        slotBookingId[i] = "";
      }
    }
  }
}

void updateBookingStatus(String bookingId, String newStatus) {
  if (bookingId == "") return;
  String path = "bookings/" + bookingId + "/status";
  Serial.print("[BOOKING] Update status: "); Serial.println(newStatus);
  fb.setString(path, newStatus);
}

void updateSlots(){
  for(int i=0;i<5;i++){
    bool car = (digitalRead(irPins[i]) == LOW);
    String currentStatus = slotStatus[i]; 

    // 1. Check-in
    if (car && currentStatus == "booked") {
      slotStatus[i] = "occupied"; 
      digitalWrite(ledR[i], HIGH);
      digitalWrite(ledG[i], LOW);  
      Serial.println("[SLOT] Check-in terdeteksi.");
      updateBookingStatus(slotBookingId[i], "checked-in");
    } 
    // 2. Check-out
    else if (!car && currentStatus == "occupied") {
      slotStatus[i] = "available"; 
      digitalWrite(ledR[i], LOW);
      digitalWrite(ledG[i], HIGH);
      Serial.println("[SLOT] Check-out terdeteksi.");
      updateBookingStatus(slotBookingId[i], "done"); 
      slotBookingId[i] = ""; 
    }
    // 3. Occupied
    else if (car && currentStatus == "occupied") {
      digitalWrite(ledR[i], HIGH);
      digitalWrite(ledG[i], LOW);
    }
    // 4. Booked
    else if (!car && currentStatus == "booked") {
      digitalWrite(ledR[i], HIGH);
      digitalWrite(ledG[i], HIGH); 
    }
    // 5. Available
    else if (!car && (currentStatus == "available" || currentStatus == "done")) {
      slotStatus[i] = "available"; 
      digitalWrite(ledR[i], LOW);
      digitalWrite(ledG[i], HIGH); 
    }
    // 6. Manual Park (Non-booking slot usage)
    else if (car && currentStatus == "available") {
       slotStatus[i] = "occupied";
       digitalWrite(ledR[i], HIGH);
       digitalWrite(ledG[i], LOW); 
    }
  }
}

void pushFirebase(){
  String json = "{";
  for(int i=0;i<5;i++){
    json += "\"S"+String(i+1)+"\":{";
    json += "\"status\":\""+slotStatus[i]+"\",";
    json += "\"bookingId\":\""+slotBookingId[i]+"\"";
    json += "}";
    if(i<4) json+=",";
  }
  json+="}";
  fb.setJson("slots/"+mallId, json);
}

void displayQR(String text){
  display.clear(); 
  display.drawString(0, 0, "Scan to Check-In:"); 
  display.display(); 
  qrcode.create(text); 
}

void checkScanStatus() {
  if (millis() - lastBookingCheck < 1000) return; 
  lastBookingCheck = millis();

  bool gateOpened = false;
  for (int i = 0; i < 5; i++) {
    if (slotStatus[i] == "booked" && slotBookingId[i] != "") {
      String path = "bookings/" + slotBookingId[i] + "/qrScanned";
      bool isScanned = false; 
      int code = fb.getBool(path, isScanned); 

      if (code == 200 && isScanned == true) {
        Serial.println("[CHECK SCAN] -> SUKSES! Membuka gerbang.");
        isWaitingForScan = false; 
        
        servoMasuk.write(90); 
        beep(); 
        servoMasukIsOpen = true;
        servoMasukOpenTime = millis();
        
        gateOpened = true; 
        break; 
      }
    }
  }

  if (!gateOpened) {
    Serial.println("[CHECK SCAN] -> Menunggu scan...");
  }
}

// ===== FUNGSI palang() DIPERBARUI =====
void palang(){
  unsigned long now = millis(); 

  int full = 0;
  for(int i=0;i<5;i++) if(slotStatus[i]=="occupied" || slotStatus[i] == "booked") full++;

  // --- Logika Palang Masuk ---
  if(digitalRead(IR_MASUK)==LOW && !servoMasukIsOpen && !isWaitingForScan){
    Serial.println("[PALANG] Mobil di gerbang masuk.");
    
    // 1. Cek apakah ada booking aktif di sistem
    if(anySlotBooked){ 
      Serial.println("[PALANG] Ada booking, tampilkan QR.");
      isWaitingForScan = true;
      lastBookingCheck = millis(); 
      displayQR("PARKEERIOT_GATE_01"); 
    } 
    // 2. Cek jika parkir penuh
    else if (full >= 5) {
      Serial.println("[PALANG] Parkir Penuh.");
      beep(); delay(50); beep(); 
    }
    // 3. === PERUBAHAN LOGIKA DI SINI (NO BOOKING = NO ENTRY) ===
    else {
      // Jika tidak ada booking sama sekali di sistem, dan mobil datang -> TOLAK
      Serial.println("[PALANG] Tidak ada booking. Akses Ditolak.");
      
      display.clear();
      display.drawString(0, 20, "NO BOOKING FOUND");
      display.drawString(0, 40, "ACCESS DENIED!");
      display.display();
      
      // Bunyi Alarm Error (Beep 3x)
      beep(); delay(100); beep(); delay(100); beep();
      
      delay(2000); // Tahan pesan di layar sebentar agar terbaca
      
      // Gerbang TETAP TERTUTUP (Tidak ada servoMasuk.write)
    }
  }
  
  // --- Timer Tutup Palang Masuk ---
  if(servoMasukIsOpen && (now - servoMasukOpenTime > servoOpenDuration)){
    servoMasuk.write(0);
    servoMasukIsOpen = false;
  }

  // --- Logika Palang Keluar ---
  if(digitalRead(IR_KELUAR)==LOW && !servoKeluarIsOpen){
    servoKeluar.write(90); 
    beep(); 
    servoKeluarIsOpen = true;
    servoKeluarOpenTime = now;
  }
  if(servoKeluarIsOpen && (now - servoKeluarOpenTime > servoOpenDuration)){
    servoKeluar.write(0);
    servoKeluarIsOpen = false;
  }
}

void displayInfo(){
  display.clear();
  int available = 0;
  for(int i=0;i<5;i++){
    display.drawString(0, i*10, "S"+String(i+1)+": "+slotStatus[i]);
    if(slotStatus[i] == "available") available++;
  }
  display.drawString(0, 52, "Available: " + String(available));
  display.display();
}

void loop(){
  unsigned long now = millis();

  if (isWaitingForScan) {
    checkScanStatus();
  } 
  else {
    if(now - lastFetch > 300){ 
      fetchFirebase();
      lastFetch = now;
    }
    updateSlots();
    displayInfo();

    String cur = "";
    for(int i=0;i<5;i++) cur += slotStatus[i] + slotBookingId[i] + ",";
    if(cur != lastState){
      pushFirebase();
      lastState = cur;
    }
  }
  palang();
}
