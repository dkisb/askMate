# ask-mate-backend
<a id="readme-top"></a>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
This is a full-stack web application's backend repository written in Java/Spring Boot using PostgreSQL as a database.
delivering a dynamic question-and-answer platform. Designed to enable users to post and respond to questions across diverse topics.

[Frontend Repository](https://github.com/dkisb/ask-mate-frontend)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

* [![Java][Java]][Java-url]
* [![Spring boot][Spring Boot]][Spring-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

### Installation
1. Clone the repo
   ```sh
   git clone https://github.com/github_username/repo_name.git
   ```
2. Create a [PostgreSQL](https://www.postgresql.org/) database
3. Create the `users`, `questions`, and `answers` tables with the following schema:

```sql
-- users table
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255),
  password_hash VARCHAR(255),
  email VARCHAR(255),
  is_admin BOOLEAN,
  reliability_points INT,
  created_at TIMESTAMP
);

-- questions table
CREATE TABLE questions (
  id SERIAL PRIMARY KEY,
  content VARCHAR(255),
  created_at TIMESTAMP,
  user_id INT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- answers table
CREATE TABLE answers (
  id SERIAL PRIMARY KEY,
  content VARCHAR(255),
  title VARCHAR(255),
  created_at TIMESTAMP,
  user_id INT,
  question_id INT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (question_id) REFERENCES questions(id)
);
```
4. Connect your database to the project.

5. Set the following environment variables to connect your application to the database. These are referenced in the `application.properties` file in the `resources` folder:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```
6. Start the application as a backend server.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Top contributors:

<a href="https://github.com/bencsicszoli">
  <p>Zoltán Bencsics</p>
</a>


## Contact

Döme Marcell Kisbalázs - domekisb@gmail.com

Project Link: [https://github.com/CodecoolGlobal/ask-mate-oop-java-dkisb](https://github.com/CodecoolGlobal/ask-mate-oop-java-dkisb)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Java]: https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white
[Java-url]: https://www.oracle.com/java/
[Spring Boot]: https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[Spring-url]: https://spring.io/projects/spring-boot

