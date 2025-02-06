# Quran Memorization Review App 📖📲🤲🌙

Welcome to the **Quran Memorization Review App**! This app is designed to help you improve your Quran memorization by providing a dynamic, interactive, and intelligent platform for reviewing and testing your knowledge. Whether you're a beginner or an advanced memorizer, this app has something for everyone.

---

## 📱 **Screenshots & Demos**

*Coming Soon!*

<div align="center">
  <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/gif1.gif" width="200" alt="Authentication" />
    <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/gif2.gif" width="200" alt="screen" />
      <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/screen.png" width="200" alt="screen" />
    <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/screen2.png" width="200" alt="screen" />
    <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/screen22.png" width="200" alt="screen" />
    <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/screen23.png" width="200" alt="screen" />
    <img src="https://raw.githubusercontent.com/n-jah//QuranTest/master/media/screen3.png" width="200" alt="screen" />

</div>


---

## 🚀 **Features**

- **Dynamic Question Generation** 🔄  
  Randomized questions are generated based on your selected **Juz range**. This ensures a **diverse learning experience** every time you take a test. The app adapts the **number of lines per question dynamically**, based on the Juz range you're testing on, helping you focus on specific portions of the Quran while avoiding repetition.
  
- **Customizable Tests** 📝  
  Tailor your test to suit your study preferences:
  - Select a specific **Juz range** (from Juz 1 to Juz 30).
  - Adjust the **number of questions** in each test to match your preferred difficulty.
  - Choose how many **lines per question** you want to be tested on, allowing you to control the complexity of the test.

- **Real-Time Grade Tracking** 📊  
  As you progress, the app tracks your performance and provides **real-time feedback** on your grade:
  - Your total number of answered questions and **average grade** are displayed to keep you motivated and informed.
  - The grade calculation adjusts dynamically based on your answers, providing accurate performance feedback after every test.

- **No Repetition** 🔄  
  The app ensures that **questions don’t overlap or repeat**. This guarantees you get a fresh set of questions every time, helping you reinforce your memorization without the boredom of reviewing the same material over and over again.

- **Quranic Verses Database**  
  All Quranic data is stored in a local **SQLite database**, ensuring offline functionality and fast access to verses.
  
- **Grades Tracking**  
  Track your progress with a detailed grade system. The app calculates the average grade for each session based on your performance.

- **Customizable Test Ranges**  
  You can define your own range of verses, from **specific Surahs** and **Ayahs** to **page numbers**.

---
## 🧑‍💻 **How It Works**

The app uses a **SQLite database & Room** to store the Quranic verses, allowing you to access and review the content offline. When you select a range of verses or a specific Surah, the app will randomly generate questions within that range.

- **Generate Questions:** The questions are designed to help you focus on a specific range of verses, making it easier to concentrate on your weak areas.
- **Track Grades:** As you progress through the review questions, the app will calculate your **grades** based on your answers and display your overall progress.

### **App Logic Breakdown:**

1. **Database Initialization**: The Quranic verses are stored in a local SQLite database with fields such as Surah, Ayah, Juz, and Page. This ensures quick offline access and allows for easy generation of questions based on specific criteria.
   
2. **Question Generation Logic**:
   - When the user selects a range of verses (either by Juz, Surah, or Page), the app fetches verses from the database that match the selected criteria.
   - It then generates **random questions** from these verses while maintaining **sequential flow** to ensure that questions are posed in a logical, sequential order without repeating verses.

3. **User Progress & Grades**:
   - Each question answered correctly adds to the user’s **overall performance score**.
   - The app tracks **user grades** for each session, providing a **grade history** to reflect the user’s progress over time.
   - Users can review their performance for each Juz, Surah, or specific range of verses.

---

## 📦 **Tech Stack**

- **SQLite & Room** – Database for storing Quranic verses locally.
- **Kotlin** – Programming language used to build the app.
- **MVVM Architecture** – Ensures clean code separation between the UI, ViewModel, and data.
- **LiveData & ViewModel** – For data management and UI updates.
- **Coroutines** – For asynchronous programming to handle long-running tasks like question generation without blocking the UI.

---

## 🚧 **Upcoming Features**

We’re not stopping here! There are several exciting features planned for future updates to make your Quran memorization journey even better:

🔹 **Quiz Mode (Test Mode)** – We’ll be adding a **Quiz Mode** where you can take tests **question by question**, helping you review and evaluate your progress. You’ll be able to track your own performance and see where you need improvement.

🔹 **Memorization by Surah and Verses** – Soon, you’ll be able to **memorize by Surah**, and select a specific range of verses (e.g., from verse X to verse Y). You’ll also be able to test yourself by **pages**, so you can focus on memorizing specific parts with more precision.

🔹 **Difficulty Levels** – For those looking for an extra challenge, we’re planning to introduce different **difficulty levels**. This will allow you to test yourself with **similar-sounding verses** (Mutashabihat) or harder questions that will push your memorization skills.

🔹 **AI-Powered Focus** – With the power of **Artificial Intelligence** 🧠, the app will intelligently detect which parts of the Quran you’re struggling with and focus your review on those weak spots. This feature will make your memorization more efficient by ensuring that you’re not spending time on areas you’ve already mastered.

🔹 **Voice Memorization** 🎤 – We’re also introducing **voice memorization**. This will allow you to recite verses aloud and get immediate feedback on your performance. This feature will help you improve your pronunciation and accuracy while memorizing, making your review sessions more interactive and effective.

---


## 🤝 **Contribute**

We welcome contributions to the project! If you'd like to help improve the app, here's how you can contribute:

1. **Fork the repository** and create your own branch.
2. **Make your changes** and ensure that the app runs smoothly.
3. **Create a pull request**, explaining the changes you've made.
4. **Write tests** to cover your changes, if necessary.

We’d love to see how you can improve this app and make Quran memorization even better for everyone.

---

## 📝 **License**

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.
