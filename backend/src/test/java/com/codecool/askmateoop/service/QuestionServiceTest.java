package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.entities.ratings.QuestionDislike;
import com.codecool.askmateoop.model.entities.ratings.QuestionLike;
import com.codecool.askmateoop.model.payload.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.repository.QuestionDislikeRepository;
import com.codecool.askmateoop.repository.QuestionLikeRepository;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionLikeRepository questionLikeRepository;

    @Mock
    private QuestionDislikeRepository questionDislikeRepository;

    @InjectMocks
    private QuestionService questionService;

    // -------- helpers --------
    private void setAuthenticatedUser(String username) {
        User principal = new User(username, "password", new HashSet<>());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    // -------- tests --------

    @Test
    void getAllQuestionsWhenQuestionRepositoryIsNotEmpty() {
        // Build questions WITH authors to avoid NPE in mapping
        UserEntity author1 = new UserEntity();
        author1.setUsername("testUser");
        UserEntity author2 = new UserEntity();
        author2.setUsername("testUser2");

        Question question1 = new Question();
        question1.setId(1);
        question1.setTitle("Title1");
        question1.setAuthor(author1);
        question1.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        Question question2 = new Question();
        question2.setId(2);
        question2.setTitle("Title2");
        question2.setAuthor(author2);
        question2.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        when(questionRepository.findAll()).thenReturn(List.of(question1, question2));

        // We only assert fields you compare via equals (your DTO likely implements equals on all fields);
        // include the same values the service will populate.
        List<QuestionDTO> expected = List.of(
                new QuestionDTO(1, "Title1", null, question1.getCreatedAt(), "testUser"),
                new QuestionDTO(2, "Title2", null, question2.getCreatedAt(), "testUser2")
        );
        assertEquals(expected, questionService.getAllQuestions());
    }

    @Test
    void getAllQuestionsWhenQuestionRepositoryIsEmpty() {
        when(questionRepository.findAll()).thenReturn(List.of());
        assertEquals(List.of(), questionService.getAllQuestions());
    }

    @Test
    void getQuestionWithValidQuestionId() {
        UserEntity author = new UserEntity();
        author.setUsername("any");
        Question question = new Question();
        question.setId(1);
        question.setTitle("Title");
        question.setContent("Content");
        question.setAuthor(author);
        question.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        QuestionDTO dto = questionService.getQuestionById(question.getId());
        assertEquals("Title", dto.title());
        assertEquals("Content", dto.content());
        assertEquals("any", dto.author());
    }

    @Test
    void getQuestionWithInvalidQuestionId() {
        when(questionRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.getQuestionById(1));
    }

    @Test
    void deleteQuestionByIdWhenUserIsAuthorized() {
        int questionId = 1;

        // set authenticated user
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity current = new UserEntity();
        current.setId(5);
        current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        Question question = new Question();
        question.setId(questionId);
        question.setAuthor(current);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        questionService.deleteQuestionById(questionId);

        verify(questionRepository, times(1)).deleteById(questionId);
    }

    @Test
    void deleteQuestionByIdWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity current = new UserEntity();
        current.setId(3);
        current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        UserEntity realAuthor = new UserEntity();
        realAuthor.setId(4);
        realAuthor.setUsername("realAuthor");

        Question question = new Question();
        question.setId(1);
        question.setAuthor(realAuthor);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertThrows(NotAllowedOperationException.class, () -> questionService.deleteQuestionById(question.getId()));
    }

    @Test
    void deleteQuestionByIdWhenQuestionIsNotFoundThenThrowsNoSuchElementException() {
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.deleteQuestionById(questionId));
    }

    @Test
    void deleteQuestionByIdWhenUserIsNotFoundThenThrowsNoSuchElementException() {
        String username = "testUser";
        setAuthenticatedUser(username);

        int questionId = 1;
        Question question = new Question();
        question.setId(questionId);
        question.setAuthor(new UserEntity()); // author can be anything; service will fail on user lookup
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.deleteQuestionById(questionId));
    }

    @Test
    void addNewQuestionWhenUserExistsThenSavesAndReturnsId() {
        int userId = 1;
        NewQuestionDTO dto = new NewQuestionDTO("Title", "Content", userId);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("testUser");

        Question saved = new Question();
        saved.setId(99);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        int resultId = questionService.addNewQuestion(dto);
        assertEquals(99, resultId);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question captured = captor.getValue();
        assertEquals("Title", captured.getTitle());
        assertEquals("Content", captured.getContent());
        assertEquals(user, captured.getAuthor());
        assertNotNull(captured.getCreatedAt());
    }

    @Test
    void addNewQuestionWhenUserIsNotFoundThenThrowsNoSuchElementException() {
        int userId = 1;
        NewQuestionDTO dto = new NewQuestionDTO("Title", "Content", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.addNewQuestion(dto));
    }

    @Test
    void updateQuestionWhenUserIsAuthorized() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UpdatedQuestionDTO dto = new UpdatedQuestionDTO(5, "New Title", "New Content");

        UserEntity current = new UserEntity();
        current.setId(2);
        current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        Question question = new Question();
        question.setId(5);
        question.setTitle("Old Title");
        question.setContent("Old Content");
        question.setAuthor(current);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        questionService.updateQuestion(dto);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question captured = captor.getValue();
        assertEquals("New Title", captured.getTitle());
        assertEquals("New Content", captured.getContent());
        assertEquals(current, captured.getAuthor());
    }

    @Test
    void updateQuestionWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UpdatedQuestionDTO dto = new UpdatedQuestionDTO(5, "New Title", "New Content");

        UserEntity current = new UserEntity();
        current.setId(2);
        current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        UserEntity realAuthor = new UserEntity();
        realAuthor.setId(999);
        realAuthor.setUsername("realAuthor");

        Question question = new Question();
        question.setId(5);
        question.setTitle("Old Title");
        question.setContent("Old Content");
        question.setAuthor(realAuthor);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertThrows(NotAllowedOperationException.class, () -> questionService.updateQuestion(dto));
    }

    @Test
    void updateAnyQuestionWithValidQuestionId() {
        UpdatedQuestionDTO dto = new UpdatedQuestionDTO(5, "New Title", "New Content");
        Question question = new Question();
        question.setId(5);
        question.setTitle("Old Title");
        question.setContent("Old Content");
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        questionService.updateAnyQuestion(dto);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question captured = captor.getValue();
        assertEquals("New Title", captured.getTitle());
        assertEquals("New Content", captured.getContent());
    }

    @Test
    void updateQuestionWithInvalidQuestionId() {
        UpdatedQuestionDTO dto = new UpdatedQuestionDTO(5, "New Title", "New Content");
        when(questionRepository.findById(dto.id())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.updateAnyQuestion(dto));
    }


    @Test
    void getLikesWithValidQuestionId() {
        int questionId = 1;
        Question question = new Question();
        question.setId(questionId);
        question.setLikes(3);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        assertEquals(3, questionService.getLikes(questionId));
    }

    @Test
    void getLikesWithInvalidQuestionId() {
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.getLikes(questionId));
    }

    @Test
    void getDislikesWithValidQuestionId() {
        int questionId = 1;
        Question question = new Question();
        question.setId(questionId);
        question.setDislikes(4);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        assertEquals(4, questionService.getDislikes(questionId));
    }

    @Test
    void getDislikesWithInvalidQuestionId() {
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.getDislikes(questionId));
    }

    @Test
    void deleteAnyQuestionByIdWithValidQuestionId() {
        int questionId = 5;
        Question question = new Question();
        question.setId(questionId);
        question.setTitle("Title");
        question.setContent("Content");
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        doNothing().when(questionRepository).delete(any(Question.class));

        questionService.deleteAnyQuestionById(question.getId());

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).delete(captor.capture());
        Question captured = captor.getValue();
        assertEquals("Title", captured.getTitle());
        assertEquals("Content", captured.getContent());
    }

    @Test
    void deleteAnyQuestionByIdWithInvalidQuestionId() {
        int questionId = 5;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.deleteAnyQuestionById(questionId));
    }

    @Test
    void getMyQuestionsWithAuthorizedUserThenReturnQuestions() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity currentUser = new UserEntity();
        currentUser.setUsername(springUser.getUsername());
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(currentUser));
        Question question1 = new Question();
        question1.setId(1);
        question1.setTitle("Title1");
        question1.setLikes(2);
        question1.setDislikes(3);
        question1.setContent("Content1");
        Timestamp timestamp = Mockito.mock(Timestamp.class);
        question1.setCreatedAt(timestamp);
        question1.setAuthor(currentUser);
        question1.setAnswers(new HashSet<>());
        Question question2 = new Question();
        question2.setId(2);
        question2.setTitle("Title2");
        question2.setLikes(4);
        question2.setDislikes(5);
        question2.setContent("Content2");
        Timestamp timestamp2 = Mockito.mock(Timestamp.class);
        question2.setCreatedAt(timestamp2);
        question2.setAuthor(currentUser);
        question2.setAnswers(new HashSet<>());
        when(questionRepository.findAllByAuthor(currentUser)).thenReturn(Optional.of(List.of(question1, question2)));
        QuestionDTO dto1 = new QuestionDTO(question1.getId(), question1.getTitle(), question1.getContent(), question1.getCreatedAt(), currentUser.getUsername());
        QuestionDTO dto2 = new QuestionDTO(question2.getId(), question2.getTitle(), question2.getContent(), question2.getCreatedAt(), currentUser.getUsername());
        assertEquals(List.of(dto1, dto2), questionService.getMyQuestions());
    }

    @Test
    void getMyQuestionsWhenUserNotFoundThenReturnNoSuchElementException() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.getMyQuestions());
    }

    @Test
    void getMyQuestionsWhenQuestionsNotFoundThenReturnNoSuchElementException() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity currentUser = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(currentUser));
        when(questionRepository.findAllByAuthor(currentUser)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.getMyQuestions());
    }

    @Test
    void addLikeToQuestionWhenUserIsAuthorizedAndQuestionIdIsValidAndQuestionIsNotLikedAndNotDislikedByUser() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        question.setLikes(10);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);
        when(questionLikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);

        boolean result = questionService.addLikeToQuestion(id);
        assertTrue(result);

        ArgumentCaptor<QuestionLike> questionLikeCaptor = ArgumentCaptor.forClass(QuestionLike.class);
        verify(questionLikeRepository).save(questionLikeCaptor.capture());
        QuestionLike capturedQuestionLike = questionLikeCaptor.getValue();
        assertEquals(5, capturedQuestionLike.getQuestionId());
        assertEquals(reviewer, capturedQuestionLike.getReviewer());

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question capturedQuestion = questionCaptor.getValue();
        assertEquals(id, capturedQuestion.getId());
        assertEquals(11, capturedQuestion.getLikes());
    }

    @Test
    void addLikeToQuestionWhenUserIsAuthorizedButQuestionIsAlreadyDislikedByUserThenThrowsNotAllowedException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(true);

        assertThrows(NotAllowedOperationException.class, () -> questionService.addLikeToQuestion(id));
    }

    @Test
    void addLikeToQuestionWhenUserIsAuthorizedButQuestionIsAlreadyLikedByUser() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        question.setLikes(10);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);
        when(questionLikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(true);
        doNothing().when(questionLikeRepository).deleteByQuestionIdAndReviewer(5, reviewer);

        boolean result = questionService.addLikeToQuestion(id);
        assertFalse(result);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question capturedQuestion = questionCaptor.getValue();
        assertEquals(id, capturedQuestion.getId());
        assertEquals(9, capturedQuestion.getLikes());
    }

    @Test
    void addLikeToQuestionWhenUserIsNotFoundThenThrowNoSuchElementException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.addLikeToQuestion(id));
    }

    @Test
    void addLikeToQuestionWhenUserIsAuthorizedButQuestionIsNotFoundThenThrowNoSuchElementException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.addLikeToQuestion(id));
    }

    @Test
    void addDislikeToQuestionWhenUserIsAuthorizedAndQuestionIdIsValidAndQuestionIsNotDislikedAndNotLikedByUser() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        question.setDislikes(10);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionLikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);

        boolean result = questionService.addDislikeToQuestion(id);
        assertTrue(result);

        ArgumentCaptor<QuestionDislike> questionDislikeCaptor = ArgumentCaptor.forClass(QuestionDislike.class);
        verify(questionDislikeRepository).save(questionDislikeCaptor.capture());
        QuestionDislike capturedQuestionDislike = questionDislikeCaptor.getValue();
        assertEquals(5, capturedQuestionDislike.getQuestionId());
        assertEquals(reviewer, capturedQuestionDislike.getReviewer());

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question capturedQuestion = questionCaptor.getValue();
        assertEquals(id, capturedQuestion.getId());
        assertEquals(11, capturedQuestion.getDislikes());
    }

    @Test
    void addDislikeToQuestionWhenUserIsAuthorizedButQuestionIsAlreadyLikedByUserThenThrowsNotAllowedException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionLikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(true);

        assertThrows(NotAllowedOperationException.class, () -> questionService.addDislikeToQuestion(id));
    }

    @Test
    void addDislikeToQuestionWhenUserIsAuthorizedButQuestionIsAlreadyDislikedByUser() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        Question question = new Question();
        question.setId(5);
        question.setDislikes(10);
        when(questionRepository.findById(5)).thenReturn(Optional.of(question));
        when(questionLikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(false);
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(5, reviewer)).thenReturn(true);
        doNothing().when(questionDislikeRepository).deleteByQuestionIdAndReviewer(5, reviewer);

        boolean result = questionService.addDislikeToQuestion(id);
        assertFalse(result);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question capturedQuestion = questionCaptor.getValue();
        assertEquals(id, capturedQuestion.getId());
        assertEquals(9, capturedQuestion.getDislikes());
    }

    @Test
    void addDislikeToQuestionWhenUserIsNotFoundThenThrowNoSuchElementException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.addDislikeToQuestion(id));
    }

    @Test
    void addDislikeToQuestionWhenUserIsAuthorizedButQuestionIsNotFoundThenThrowNoSuchElementException() {
        int id = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.addDislikeToQuestion(id));
    }

    @Test
    void alreadyLikedWhenUserLikedTheQuestionThenReturnTrue() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionLikeRepository.existsByQuestionIdAndReviewer(questionId, reviewer)).thenReturn(true);
        assertTrue(questionService.alreadyLiked(questionId));
    }

    @Test
    void alreadyLikedWhenUserNotLikedTheQuestionThenReturnFalse() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionLikeRepository.existsByQuestionIdAndReviewer(questionId, reviewer)).thenReturn(false);
        assertFalse(questionService.alreadyLiked(questionId));
    }

    @Test
    void alreadyLikedWhenUserIsNotFoundThenThrowNoSuchElementException() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.alreadyLiked(questionId));
    }

    @Test
    void alreadyDislikedWhenUserDislikedTheQuestionThenReturnTrue() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(questionId, reviewer)).thenReturn(true);
        assertTrue(questionService.alreadyDisliked(questionId));
    }

    @Test
    void alreadyDislikedWhenUserNotDislikedTheQuestionThenReturnFalse() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity reviewer = new UserEntity();
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(reviewer));
        when(questionDislikeRepository.existsByQuestionIdAndReviewer(questionId, reviewer)).thenReturn(false);
        assertFalse(questionService.alreadyDisliked(questionId));
    }

    @Test
    void alreadyDislikedWhenUserIsNotFoundThenThrowNoSuchElementException() {
        int questionId = 5;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.alreadyDisliked(questionId));
    }
}
