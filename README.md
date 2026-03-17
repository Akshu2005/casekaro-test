# 🛒 CaseKaro Playwright Automation Project

## 🚀 Overview

This project is an end-to-end automation framework built using **Java + Playwright + Cucumber (BDD)**.
It automates user interactions on the CaseKaro website and validates product listings based on specific business rules.

---

## 🧰 Tech Stack

* **Language:** Java
* **Automation Tool:** Playwright
* **Framework:** Cucumber (BDD)
* **Build Tool:** Maven

---

## ✨ Features

* 🌐 Navigate to CaseKaro website
* 📱 Click on *Mobile Covers* section
* 🔍 Search for "Apple" products
* ❌ Validate absence of banned brands (Samsung, Vivo, Oppo, etc.)
* 🧾 Extract product details
* 📊 Export data to CSV file

---

## 📂 Project Structure

```
casekaro-test/
│── src/
│   ├── main/java/...        # Core logic
│   ├── test/java/...        # Step definitions & test runner
│   ├── test/resources/...   # Feature files
│── target/                  # Build files (ignored)
│── pom.xml                  # Maven dependencies
│── products.csv             # Output data
```

---

## ▶️ How to Run

1. Clone the repository:

```
git clone https://github.com/Akshu2005/casekaro-test.git
```

2. Navigate to project:

```
cd casekaro-test
```

3. Run tests:

```
mvn clean test
```

---

## 📌 Test Scenario

* Open CaseKaro website
* Navigate to Mobile Covers
* Search for "Apple"
* Validate no banned brands are present
* Extract product details

---

## 💡 Key Learnings

* Hands-on experience with Playwright automation
* BDD implementation using Cucumber
* Handling dynamic web elements
* Data extraction and validation
* Writing clean and maintainable test code

---

## 👩‍💻 Author

**Akanksha Srivastava**
🔗 [GitHub](https://github.com/Akshu2005)

---
