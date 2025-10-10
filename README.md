# askMate

<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]

<h3 align="center">askMate - Q&A Platform</h3>

<p align="center">
  A modern Reddit-like Q&A platform built with Spring Boot and React.<br>
  Ask questions, get answers, and engage with the community!
  <br />
  <a href="https://github.com/dkisb/askMate"><strong>Explore the docs »</strong></a>
  <br />
  <br />
  <a href="https://ask-mate-five.vercel.app/">Try the app</a>
  &middot;
  <a href="https://github.com/your-org/askMate/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
  &middot;
  <a href="https://github.com/your-org/askMate/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
</p>

---

## 🚀 What is askMate?

askMate is a full-stack Q&A platform inspired by Reddit and Stack Overflow. Users can register, ask questions, post answers, and engage with the community through likes, dislikes, and comments. It features JWT authentication, role-based access control, and a modern responsive UI.

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#features">Features</a></li>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#configuration">Configuration</a></li>
      </ul>
    </li>
     <li><a href="#usage">Usage</a></li>
     <li><a href="#api-overview">API Overview</a></li>
     <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
    <li><a href="#faq">FAQ</a></li>
  </ol>
</details>

---

## About The Project

askMate is a modern Q&A platform that brings the Stack Overflow and Reddit experience to a Spring Boot + React stack. Users can ask questions, provide answers, vote on content, and build a community around knowledge sharing. The platform features secure authentication, role-based permissions, and a clean, responsive interface.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Features

- **User Authentication**: JWT-based login/registration system
- **Q&A System**: Create, edit, and manage questions and answers
- **Voting System**: Like/dislike questions and answers
- **Comments**: Nested discussion threads
- **User Profiles**: Track activity and manage content
- **Role-based Access**: Admin and moderator capabilities
- **Responsive Design**: Modern UI that works on all devices

### Built With

- [![React][React.js]][React-url]
- [![Vite-url][Vite.js]][Vite-url]
- [![Java Spring Boot][Spring]][Spring-url]
- [![PostgreSQL][PostgreSQL]][Postgres-url]
- [![Tailwind-url][Tailwind.js]][Tailwind-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->

## Getting Started

These instructions will get you a copy of askMate up and running locally for development.

### Prerequisites

- [Java 17+](https://adoptopenjdk.net/)
- [Maven](https://maven.apache.org/)
- [Node.js](https://nodejs.org/) (v18+ recommended)
- [npm](https://www.npmjs.com/)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/) (optional)

### Installation

1. **Clone the repo**
   ```sh
   git clone https://github.com/your-org/askMate.git
   cd askMate
   ```

2. **Install frontend dependencies**
   ```sh
   cd frontend
   npm install
   ```

3. **Set up backend**
   ```sh
   cd ../backend
   mvn clean install
   ```

4. **Configure database**

   - Create a PostgreSQL database named `askmate`
   - Update `backend/src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/askmate
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

5. **Run the backend server**
   ```sh
   cd backend
   mvn spring-boot:run
   ```

6. **Run the frontend development server (in another terminal)**
   ```sh
   cd frontend
   npm run dev
   ```

7. **Open [http://localhost:5173](http://localhost:5173) to view the application**

### Run with Docker

Alternatively, you can run the entire stack with Docker Compose:

```sh
docker compose up --build
```

This will start the backend, frontend, and PostgreSQL database automatically.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->

## Usage

1. **Register/Login**: Create an account or sign in to access all features
2. **Ask Questions**: Post questions about any topic to get help from the community
3. **Answer Questions**: Help others by providing detailed, helpful answers
4. **Vote & Comment**: Like/dislike content and participate in discussions
5. **Manage Content**: Edit your questions and answers, view your activity
6. **Admin Features**: Moderate content and manage users (admin/moderator roles)

### Key Features

- **Question Management**: Create, edit, and delete your questions
- **Answer System**: Post answers with rich text formatting
- **Voting System**: Upvote/downvote questions and answers
- **Comments**: Add comments to questions and answers
- **User Profiles**: Track your activity and reputation
- **Search & Filter**: Find questions by topic or keywords

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## API Overview

The backend provides a RESTful API with the following main endpoints:

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Questions
- `GET /api/questions` - List all questions
- `POST /api/questions` - Create a question (authenticated)
- `GET /api/questions/{id}` - Get question details
- `PUT /api/questions/{id}` - Update question (authenticated)
- `DELETE /api/questions/{id}` - Delete question (authenticated)

### Answers
- `POST /api/questions/{id}/answers` - Add answer to question
- `PUT /api/answers/{id}` - Update answer
- `DELETE /api/answers/{id}` - Delete answer

### Voting
- `POST /api/questions/{id}/like` - Like a question
- `POST /api/questions/{id}/dislike` - Dislike a question
- `POST /api/answers/{id}/like` - Like an answer
- `POST /api/answers/{id}/dislike` - Dislike an answer

### Admin/Moderator
- `GET /api/admin/users` - List all users (admin only)
- `PUT /api/admin/users/{id}/role` - Update user role (admin only)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ROADMAP -->

## Roadmap

- [ ] Rich text editor for questions and answers
- [ ] Image upload support
- [ ] Advanced search and filtering
- [ ] Email notifications
- [ ] Mobile app
- [ ] Real-time notifications
- [ ] Tag system for categorization
- [ ] User reputation system
- [ ] Internationalization

See the [open issues](https://github.com/your-org/askMate/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request, or simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Top contributors:

<a href="https://github.com/dkisb/twenty-one/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=dkisb/twenty-one" alt="contrib.rocks image" />
</a>

<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE` for more information.

---

## Contact

Your Name - [@yourhandle](https://github.com/yourhandle)

Project Link: [https://github.com/your-org/askMate](https://github.com/your-org/askMate)

---

## Acknowledgments

- [React](https://reactjs.org/)
- [Java Spring](https://spring.io/)
- [PostgreSQL](https://www.postgresql.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [contrib.rocks](https://contrib.rocks)
- [Vite](https://vitejs.dev/)
- [JWT](https://jwt.io/)
- [Prettier](https://prettier.io/)
- [ESLint](https://eslint.org/)

---

## FAQ

**Q: How do I get started?**  
A: Register an account, then you can immediately start asking questions or answering others' questions.

**Q: Can I edit my questions and answers?**  
A: Yes! You can edit your own content anytime. Other users' content can only be modified by admins/moderators.

**Q: How does the voting system work?**  
A: Users can upvote or downvote questions and answers. This helps surface the most helpful content to the top.

**Q: What are the different user roles?**  
A: Regular users can ask/answer questions. Moderators can manage content. Admins have full system access.

**Q: Is there a search feature?**  
A: Basic search is available. Advanced search and filtering are planned for future releases.

**Q: Can I upload images?**  
A: Image upload support is on the roadmap. Currently, you can only use text content.

---

<!-- MARKDOWN LINKS & IMAGES -->

[contributors-shield]: https://img.shields.io/github/contributors/dkisb/askMate.svg?style=for-the-badge
[contributors-url]: https://github.com/dkisb/askMate/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/dkisb/askMate.svg?style=for-the-badge
[forks-url]: https://github.com/dkisb/askMate/network/members
[stars-shield]: https://img.shields.io/github/stars/dkisb/askMate.svg?style=for-the-badge
[stars-url]: https://github.com/dkisb/askMate/stargazers
[issues-shield]: https://img.shields.io/github/issues/dkisb/askMate.svg?style=for-the-badge
[issues-url]: https://github.com/dkisb/askMate/issues
[license-shield]: https://img.shields.io/github/license/dkisb/askMate.svg?style=for-the-badge
[license-url]: https://github.com/dkisb/askMate/blob/main/LICENSE
[product-screenshot]: public/screenshot.png
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://react.dev/
[Vite.js]: https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=FFD62E
[Vite-url]: https://vitejs.dev/
[Spring]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[Spring-url]: https://spring.io/projects/spring-boot
[PostgreSQL]: https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[Postgres-url]: https://www.postgresql.org/
[Tailwind.js]: https://img.shields.io/badge/Tailwind_CSS-38BDF8?style=for-the-badge&logo=tailwindcss&logoColor=white
[Tailwind-url]: https://tailwindcss.com/
