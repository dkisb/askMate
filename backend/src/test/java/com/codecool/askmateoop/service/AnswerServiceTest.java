package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewReplyDTO;
import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.repository.AnswerRepository;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnswerServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private AnswerService answerService;

    @Test
    void getAnswersWithValidQuestionId(){
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(new Question()));
        Answer answer1 = new Answer();
        Answer answer2 = new Answer();
        Answer answer3 = new Answer();
        answer1.setId(1);
        answer2.setId(2);
        answer3.setId(3);
        answer1.setContent("Content1");
        answer2.setContent("Content2");
        answer3.setContent("Content3");
        Timestamp timestamp = Mockito.mock(Timestamp.class);
        answer1.setCreatedAt(timestamp);
        answer2.setCreatedAt(timestamp);
        answer3.setCreatedAt(timestamp);
        List<Answer> answers = List.of(answer1, answer2, answer3);
        when(answerRepository.getAllByQuestionId(questionId)).thenReturn(Optional.of(answers));
        AnswerDTO answerDTO1 = new AnswerDTO(1, "Content1", timestamp, "testUser", 0, 0, 0, 0);
        AnswerDTO answerDTO2 = new AnswerDTO(2, "Content2", timestamp, "testUser2", 0, 0, 0, 0);
        AnswerDTO answerDTO3 = new AnswerDTO(3, "Content3", timestamp, "testUser3", 0, 0, 0, 0);
        List<AnswerDTO> expected = List.of(answerDTO1, answerDTO2, answerDTO3);
        assertEquals(expected, answerService.getAnswers(questionId));
    }

    @Test
    void getAnswersWithInvalidQuestionId(){
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getAnswers(questionId));
    }

    @Test
    void addNewAnswerWithValidQuestionIdAndUserId() {
        NewAnswerDTO dto = new NewAnswerDTO("Content", 3, 5);
        Question question = new Question();
        question.setId(dto.questionId());
        when(questionRepository.findById(dto.questionId())).thenReturn(Optional.of(question));
        UserEntity author = new UserEntity();
        author.setId(dto.userId());
        when(userRepository.findById(dto.userId())).thenReturn(Optional.of(author));
        Answer answer = new Answer();
        when(answerRepository.save(Mockito.any(Answer.class))).thenReturn(answer);

        answerService.addNewAnswer(dto);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        Answer capturedAnswer = answerCaptor.getValue();
        assertEquals(dto.content(), capturedAnswer.getContent());
        assertEquals(dto.questionId(), capturedAnswer.getQuestion().getId());
        assertEquals(dto.userId(), capturedAnswer.getAuthor().getId());
    }

    @Test
    void addNewAnswerWithInvalidQuestionId() {
        NewAnswerDTO dto = new NewAnswerDTO("Content", 3, 5);
        when(questionRepository.findById(dto.questionId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.addNewAnswer(dto));
    }

    @Test
    void addNewAnswerWithInvalidUserId() {
        NewAnswerDTO dto = new NewAnswerDTO("Content", 3, 5);
        when(questionRepository.findById(dto.questionId())).thenReturn(Optional.of(new Question()));
        when(userRepository.findById(dto.userId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.addNewAnswer(dto));
    }

    @Test
    void updateAnswerWithValidAnswerIdThenSaveAnswer() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity author = new UserEntity();
        author.setId(10);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(author));
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated comment");
        Answer answer = new Answer();
        answer.setAuthor(author);
        answer.setId(dto.id());
        answer.setContent("Old comment");
        when(answerRepository.findById(dto.id())).thenReturn(Optional.of(answer));
        Timestamp timestamp = Mockito.mock(Timestamp.class);
        answer.setCreatedAt(timestamp);
        when(answerRepository.save(Mockito.any(Answer.class))).thenReturn(answer);

        answerService.updateAnswer(dto);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        Answer capturedAnswer = answerCaptor.getValue();
        assertEquals(dto.content(), capturedAnswer.getContent());
    }

    @Test
    void updateAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated comment");
        when(answerRepository.findById(dto.id())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.updateAnswer(dto));
    }

    @Test
    void updateAnswerWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(5, "New Content");
        UserEntity user = new UserEntity();
        user.setId(2);
        user.setUsername("testUser");
        UserEntity user2 = new UserEntity();
        user2.setUsername("realAuthor");
        when(userRepository.findByUsername(springUser.getUsername()))
                .thenReturn(Optional.of(user));
        Answer answer = new Answer();
        answer.setId(5);
        answer.setContent("Old Content");
        answer.setAuthor(user2);
        when(answerRepository.findById(answer.getId())).thenReturn(Optional.of(answer));
        assertThrows(NotAllowedOperationException.class, () -> answerService.updateAnswer(dto));
    }

    @Test
    void deleteAnswerWhenUserIsAuthorizedThenDeleteAnswer() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity currentUser = new UserEntity();
        currentUser.setId(10);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(currentUser));
        int answerId = 1;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setAuthor(currentUser);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        answerService.deleteAnswer(answerId);
        verify(answerRepository, times(1)).deleteById(answerId);
    }

    @Test
    void deleteAnswerWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        User springUser = new User("testUser", "password", new HashSet<>());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(springUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserEntity currentUser = new UserEntity();
        currentUser.setId(10);
        when(userRepository.findByUsername(springUser.getUsername())).thenReturn(Optional.of(currentUser));
        UserEntity realAuthor = new UserEntity();
        realAuthor.setId(11);
        int answerId = 2;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setAuthor(realAuthor);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertThrows(NotAllowedOperationException.class, () -> answerService.deleteAnswer(answerId));
    }

    @Test
    void deleteAnswerWithInvalidAnswerIdThenThrowsNoSuchElementException() {
        int answerId = 7;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.deleteAnswer(answerId));
    }

    @Test
    void updateAnyAnswerWithValidAnswerIdThenUpdateAnswer() {
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated content");
        Answer answer = new Answer();
        answer.setId(dto.id());
        answer.setContent("Old content");
        when(answerRepository.findById(dto.id())).thenReturn(Optional.of(answer));
        when(answerRepository.save(Mockito.any(Answer.class))).thenReturn(answer);
        answerService.updateAnyAnswer(dto);
        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        Answer capturedAnswer = answerCaptor.getValue();
        assertEquals(dto.content(), capturedAnswer.getContent());
    }

    @Test
    void updateAnyAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated content");
        when(answerRepository.findById(dto.id())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.updateAnyAnswer(dto));
    }

    @Test
    void deleteAnyAnswerWithValidAnswerIdThenDeleteAnswer() {
        int answerId = 8;
        Answer answer = new Answer();
        answer.setId(answerId);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        answerService.deleteAnyAnswer(answerId);
        verify(answerRepository, times(1)).deleteById(answerId);
    }

    @Test
    void deleteAnyAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 9;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.deleteAnyAnswer(answerId));
    }

    @Test
    void getAnswerWithValidAnswerIdThenGetAnswer() {
        int answerId = 4;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setContent("Content");
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertEquals("Content", answerService.getAnswer(answerId).content());
    }

    @Test
    void getAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 5;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getAnswer(answerId));
    }

    @Test
    void addCommentOfCommentWithValidParentIdAndQuestionIdThenAddComment() {
        int parentId = 1;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, 5);
        Question question = new Question();
        question.setId(3);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        Answer parentAnswer = new Answer();
        parentAnswer.setId(parentId);
        when(answerRepository.findById(parentId)).thenReturn(Optional.of(parentAnswer));
        Answer answer = new Answer();
        when(answerRepository.save(Mockito.any(Answer.class))).thenReturn(answer);

        answerService.addCommentOfComment(parentId, dto);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        Answer capturedAnswer = answerCaptor.getValue();
        assertEquals(dto.content(), capturedAnswer.getContent());
    }

    @Test
    void addCommentOfCommentWithInvalidQuestionIdThenThrowNoSuchElementException() {
        int parentId = 11;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, 5);
        when(questionRepository.findById(dto.parentId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.addCommentOfComment(parentId, dto));
    }

    @Test
    void addCommentOfCommentWithInvalidParentIdThenThrowNoSuchElementException() {
        int parentId = 11;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, 5);
        Question question = new Question();
        question.setId(3);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(answerRepository.findById(parentId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.addCommentOfComment(parentId, dto));
    }
}
