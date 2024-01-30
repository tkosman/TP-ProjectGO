<h2 align="center">
  <br>
 <img src="go_client/src/main/resources/com/go_game/client/logo.png" alt="Logo" width="200">
  <br>
  Java GO Game
  <br>
</h2>

<div align="center">
  
  <a href=""> ![example workflow](https://github.com/tkosman/TP-ProjectGO/actions/workflows/CI.yml/badge.svg?branch=main) </a>
  <a href=""> ![GitHub top language](https://img.shields.io/github/languages/top/tkosman/TP-ProjectGO) </a>
  <a href=""> ![GitHub language count](https://img.shields.io/github/languages/count/tkosman/TP-ProjectGO) </a>
  
</div>

<div align="center">

  ![Java](https://img.shields.io/badge/_-Java-B07219.svg?style=for-the-badge)
  ![JavaFX](https://img.shields.io/badge/_-JavaFX-C0724.svg?style=for-the-badge)
  ![Apache Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
  ![JUnit](https://img.shields.io/badge/_-JUnit-C2923.svg?style=for-the-badge)


</div>

# Description

This project is a client-server implementation of the GO Game in Java, developed using JavaFX, Maven, JUnit, and MariaDB. It was created as a part of the 'Technology of Programming' course for the **Algorithmic Computer Science** major at the **Wrocław University of Science and Technology**.

## Game Modes

<p align="center">
  <img src="go_client/src/main/resources/com/go_game/client/logo.png" alt="Bot Mode" width="150"/>
  &nbsp; &nbsp; &nbsp; &nbsp;
  <img src="go_client/src/main/resources/com/go_game/client/logo.png" alt="Multiplayer Mode" width="150"/>
</p>



## Run Locally

Clone the project

```bash
  git clone https://tkosman/TP-ProjectGO
  cd TP-ProjectGO
```

Compile 

```bash
  chmod +x ./install.sh
  ./install.sh
```

Run server

```bash
  cd go_server && mvn exec:java
```

Run client

```bash
  cd go_client && mvn javafx:run
```


## Running Tests

To run tests, run the following command

```bash
  chmod +x ./test.sh
  ./test.sh
```

## Project Team Members

### Backend Development
- <a href="https://github.com/tkosman">@tkosman</a>: Responsible for all backend development aspects of the project. This includes database management, server-side logic, API integration, and ensuring scalability and security of the backend infrastructure.

### Frontend Development
- <a href="https://github.com/wyz3r0">@Wyzero</a> Handles all frontend development tasks. This includes designing user interfaces, implementing user experience designs and integrating with backend services to provide a seamless user experience.
