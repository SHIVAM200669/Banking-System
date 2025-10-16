# **Project Title:** MyBank – Desktop Banking Application

## **Project Overview:**

MyBank is a **Java Swing-based desktop banking application** designed to provide users with a secure and user-friendly interface to manage their bank accounts. The system supports essential banking operations such as deposits, withdrawals, fund transfers, transaction history viewing, and management of login and transaction PINs. It uses a **MySQL database** for storing user credentials, account details, and transaction records.

---

## **Key Features:**

### **1. User Authentication**

* **Sign Up:** Users can create a new account by providing their full name, email, username, and password.
* **Login:** Users log in using their **login PIN** (instead of the password) for added security.
* **Forgot Login PIN:** Allows users to reset their login PIN securely using email and password verification.

### **2. Dashboard**

* Displays **user account details**, including:

  * Name
  * Account Number
  * Account Balance (animated counter effect)
* Shows **recent transactions** in a table with columns: Date, Type, and Amount.
* Features a **scrolling welcome message** at the bottom for UI appeal.
* Buttons for all major banking operations:

  * Deposit
  * Withdraw
  * Transfer
  * Transaction History
  * Refresh Dashboard
  * Reset Transaction PIN
  * Logout

### **3. Transaction PIN Management**

* Each user has a **6-digit transaction PIN** used to authorize deposits, withdrawals, and fund transfers.
* Users can **reset their transaction PIN** through a secure dialog requiring current password verification.
* Transaction PIN input is **masked** (hidden) when entered, ensuring privacy.

### **4. Transactions Management**

* Supports three main transaction types:

  * Deposit
  * Withdraw
  * Fund Transfer
* Stores transaction records in the database with the following fields:

  * Date/Time
  * Type
  * Amount
* Users can view **the last 15 transactions** on the dashboard.

### **5. Security**

* **Login PIN and Transaction PIN** are used instead of passwords for daily operations.
* All sensitive inputs are **masked using JPasswordField**.
* Database queries use **PreparedStatement** to prevent SQL injection.
* Passwords can optionally be hashed (not included in current version).

### **6. User Interface**

* Built using **Java Swing** for desktop GUI.
* Responsive layout using **BorderLayout and GridBagLayout**.
* Visual enhancements:

  * Animated account balance counter
  * Smooth scrolling welcome text
  * Color-coded buttons with hover effects
  * Card-style account info panel

---

## **Database Schema**

### **1. `users` Table**

| Column          | Type         | Description               |
| --------------- | ------------ | ------------------------- |
| id              | INT PK       | User ID                   |
| name            | VARCHAR(100) | Full name                 |
| email           | VARCHAR(100) | Email address             |
| username        | VARCHAR(50)  | Login username            |
| password        | VARCHAR(50)  | Login password (optional) |
| login_pin       | VARCHAR(6)   | 6-digit login PIN         |
| transaction_pin | VARCHAR(6)   | 6-digit transaction PIN   |

### **2. `accounts` Table**

| Column  | Type   | Description             |
| ------- | ------ | ----------------------- |
| acc_no  | INT PK | Account number          |
| user_id | INT FK | Linked user ID          |
| balance | DOUBLE | Current account balance |

### **3. `transactions` Table**

| Column | Type        | Description                    |
| ------ | ----------- | ------------------------------ |
| id     | INT PK      | Transaction ID                 |
| acc_no | INT FK      | Linked account number          |
| date   | TIMESTAMP   | Transaction date & time        |
| type   | VARCHAR(20) | Deposit, Withdraw, or Transfer |
| amount | DOUBLE      | Transaction amount             |

---

## **Workflow**

1. **Sign Up:**

   * User enters details → Validates passwords → Generates account and assigns default PINs.

2. **Login:**

   * User enters login PIN → System authenticates → Redirects to Dashboard.

3. **Dashboard:**

   * Loads user info and last 15 transactions → Balance counter animates → Buttons allow further actions.

4. **Banking Operations:**

   * For deposits/withdrawals/transfers → User must enter 6-digit transaction PIN (masked).
   * Transactions are validated, recorded, and dashboard updated.

5. **Reset PINs:**

   * Users can reset **login PIN** or **transaction PIN** using current password verification.
   * PINs must be exactly 6 digits.

---

## **Technology Stack**

* **Frontend:** Java Swing
* **Backend:** Java (JDBC)
* **Database:** MySQL
* **JDK Version:** 17+
* **OS:** Cross-platform (Windows/Linux)

---

## **Future Enhancements**

* Hash passwords and PINs for improved security.
* Email OTP verification for PIN reset.
* Multi-account support per user.
* Export transaction history to CSV/PDF.
* Desktop notifications for transactions.

---
