# 🎵 loify 🎶

### Currently Live At: [loify.me](https://loify.me)

## 📖 Overview

**Loify** is a web application that lets users access their Spotify playlists and create customized versions of the tracks in various genres like **lofi**, **jazz**, **hardstyle**, and **piano**. Users can then add these unique versions back to their existing playlists on Spotify. 🎧🎶

The project consists of two main parts:

- **loify-frontend**: The front-end built with **Vue.js** to provide the user interface. 🖥️
- **loify-backend**: The back-end built with **Spring Boot** that handles OAuth 2.0 authentication with Spotify and processes the playlists. ⚙️

## 🚀 Features

- **Spotify OAuth 2.0 Authentication**: Users can log in to Spotify and grant permissions to access their playlists. 🔐
- **Playlist Customization**: Users can create different versions of their playlists, such as:
  - 🎧 **Lofi**
  - 🎷 **Jazz**
  - 🎸 **Hardstyle**
  - 🎹 **Piano**
- **Add Custom Playlists**: The generated versions are automatically added to the user’s existing playlists on Spotify. ➕


## 📂 Project Structure
loify/ ├── loify-frontend/ (Vue.js app) 🎨 ├── loify-backend/ (Spring Boot app) ⚙️
### `loify-frontend` 🖥️

- A **Vue.js** application that allows users to:
  - Log in with Spotify 🔑
  - View and manage playlists 📃
  - Create and interact with the customized playlist versions 🎵
  
### `loify-backend` ⚙️

- A **Spring Boot** REST API application that:
  - Handles Spotify OAuth 2.0 authentication 🔐
  - Fetches user playlists from Spotify 🎶
  - Processes and generates the customized playlist versions 🎧
  - Adds them back to the user's playlists on Spotify ➕


## 🛠️ Getting Started

### ⚙️ Prerequisites

- **Node.js** (for `loify-frontend`) 🌐
- **Java 17 or later** (for `loify-backend`) ☕
- **Maven** (for building the Spring Boot app) 🏗️
- **Spotify Developer Account**: Register to get your **Client ID** and **Client Secret** on [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/). 🎧

### 🏗️ Environment Setup

#### 1. **Frontend (loify-frontend)**

1. Clone the frontend repository:
 ```bash
 git clone https://github.com/your-username/loify-frontend.git
 cd loify-frontend
```
Install dependencies:
```
npm install
Create a .env file in the root directory with your Spotify credentials:
```
```
VUE_APP_SPOTIFY_CLIENT_ID=YOUR_CLIENT_ID
VUE_APP_SPOTIFY_CLIENT_SECRET=YOUR_CLIENT_SECRET
Run the frontend development server:
```
```
npm run serve
```
The app should now be running on http://localhost:3000. 🚀


2. Backend (loify-backend)
Clone the backend repository:
```
git clone https://github.com/your-username/loify-backend.git
cd loify-backend
Build the Spring Boot application using Maven:
```

```
./mvnw clean install
Create an application.properties file in the src/main/resources directory:
```

```
spotify.clientId=YOUR_CLIENT_ID
spotify.clientSecret=YOUR_CLIENT_SECRET
spotify.redirectUri=http://localhost:8080/callback
```

Run the Spring Boot application:
```
./mvnw spring-boot:run
```
The backend will now be running on http://localhost:8080. 🚀

### 🔑 OAuth 2.0 Authentication
Loify uses Spotify OAuth 2.0 to authenticate users and access their playlists.

When a user accesses the app, they will be prompted to log in via Spotify. 🎤
After authentication, the backend handles Spotify API calls to fetch the user's playlists and manage the track data.
The backend securely stores the access tokens to interact with Spotify on behalf of the user.

### 🎬 Usage
**Login with Spotify:**
- Go to the frontend app (http://localhost:8080) and log in using your Spotify account. 🎧
**View Playlists:**
- After logging in, you will see a list of your Spotify playlists. 📃
**Generate Custom Playlists:**
- Choose a playlist and pick one of the customized audio versions (Lofi, Jazz, Hardstyle, Piano). 🎶
**Add Custom Playlists to Spotify:**
- The customized versions will be added back to your existing playlists on Spotify. ➕

### Technologies Used
**Frontend**: Vue.js, Axios (for HTTP requests) 🖥️
**Backend**: Spring Boot, Spring Security (for OAuth2.0), Spotify Web API 🎧
**Authentication**: Spotify OAuth 2.0 🔑
**Build Tools:** Maven (for backend), npm (for frontend) 🛠️

### License
This project is licensed under the MIT License - see the LICENSE file for details. 📄
