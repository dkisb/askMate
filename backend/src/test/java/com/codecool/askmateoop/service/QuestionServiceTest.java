package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void getAllQuestionsWhenQuestionRepositoryIsNotEmpty() {
        Question question1 = new Question();
        question1.setTitle("Title1");
        question1.setId(1);
        Question question2 = new Question();
        question2.setTitle("Title2");
        question2.setId(2);
        List<Question> questions = List.of(question1, question2);
        when(questionRepository.findAll()).thenReturn(questions);
        QuestionDTO dto1 = new QuestionDTO(1, "Title1", null, null);
        QuestionDTO dto2 = new QuestionDTO(2, "Title2", null, null);
        List<QuestionDTO> expected = List.of(dto1, dto2);
        assertEquals(expected, questionService.getAllQuestions());
    }

    @Test
    void getAllQuestionsWhenQuestionRepositoryIsEmpty() {
        List<Question> questions = List.of();
        when(questionRepository.findAll()).thenReturn(questions);

        assertEquals(List.of(), questionService.getAllQuestions());
    }

    @Test
    void getQuestionWithValidQuestionId() {
        Question question = new Question();
        question.setId(1);
        question.setTitle("Title");
        question.setContent("Content");
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertEquals("Title", questionService.getQuestionById(question.getId()).title());
        assertEquals("Content", questionService.getQuestionById(question.getId()).content());
    }

    @Test
    void getQuestionWithInvalidQuestionId() {
        when(questionRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> questionService.getQuestionById(1));
    }

    @Test
    void deleteQuestionByIdWhenUserIsAuthorized() {
        int questionId = 1;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setId(5);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(userEntity));
        Question question = new Question();
        question.setId(questionId);
        question.setAuthor(userEntity);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        questionService.deleteQuestionById(questionId);

        verify(questionRepository, times(1)).deleteById(questionId);
    }

    @Test
    void deleteQuestionByIdWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity userEntity1 = new UserEntity();
        userEntity1.setUsername("testUser");
        userEntity1.setId(3);
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setUsername("realAuthor");
        userEntity2.setId(4);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(userEntity1));
        Question question = new Question();
        question.setId(1);
        question.setAuthor(userEntity2);
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
        int questionId = 1;
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Question question = new Question();
        question.setId(questionId);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> questionService.deleteQuestionById(1));
    }

    @Test
    void addNewQuestionWhenUserExistsThenSavesAndReturnsId() {
        int userId = 1;
        NewQuestionDTO dto = new NewQuestionDTO("Title", "Content", userId);
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("testUser");
        Question savedQuestion = new Question();
        savedQuestion.setId(99);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);
        int resultId = questionService.addNewQuestion(dto);

        assertEquals(99, resultId);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        Question captured = questionCaptor.getValue();
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
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UpdatedQuestionDTO dto = new UpdatedQuestionDTO(5, "New Title", "New Content");
        UserEntity user = new UserEntity();
        user.setId(2);
        user.setUsername("testUser");
        when(userRepository.findByUsername(springUser.getUsername()))
                .thenReturn(Optional.of(user));
        Question question = new Question();
        question.setId(5);
        question.setTitle("Old Title");
        question.setContent("Old Content");
        question.setAuthor(user);
        when(questionRepository.findById(question.getId()))
                .thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class)))
                .thenReturn(question);

        questionService.updateQuestion(dto);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question captured = captor.getValue();
        assertEquals("New Title", captured.getTitle());
        assertEquals("New Content", captured.getContent());
        assertEquals(user, captured.getAuthor());
    }
}
