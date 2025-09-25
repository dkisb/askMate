package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewReplyDTO;
import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.repository.AnswerRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private AnswerService answerService;

    // ---------- helpers ----------
    private void setAuthenticatedUser(String username) {
        User principal = new User(username, "password", new HashSet<>());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    // ---------- tests ----------

    @Test
    void getAnswersWithValidQuestionId() {
        int questionId = 1;
        Question question = new Question();
        question.setId(questionId);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        Answer answer1 = new Answer();
        Answer answer2 = new Answer();
        Answer answer3 = new Answer();
        answer1.setId(11);
        answer2.setId(22);
        answer3.setId(33);
        answer1.setContent("Content1");
        answer2.setContent("Content2");
        answer3.setContent("Content3");

        UserEntity user1 = new UserEntity(); user1.setUsername("author1");
        UserEntity user2 = new UserEntity(); user2.setUsername("author2");
        UserEntity user3 = new UserEntity(); user3.setUsername("author3");

        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        answer1.setCreatedAt(ts);
        answer2.setCreatedAt(ts);
        answer3.setCreatedAt(ts);

        answer1.setAuthor(user1);
        answer2.setAuthor(user2);
        answer3.setAuthor(user3);

        answer1.setQuestion(question);
        answer2.setQuestion(question);
        answer3.setQuestion(question);

        Answer parent1 = new Answer(); parent1.setId(1);
        Answer parent2 = new Answer(); parent2.setId(2);
        Answer parent3 = new Answer(); parent3.setId(3);

        answer1.setLikes(2); answer1.setDislikes(1); answer1.setParent(parent1);
        answer2.setLikes(4); answer2.setDislikes(3); answer2.setParent(parent2);
        answer3.setLikes(6); answer3.setDislikes(0); answer3.setParent(parent3);

        when(answerRepository.getAllByQuestionId(questionId))
                .thenReturn(Optional.of(List.of(answer1, answer2, answer3)));

        List<AnswerDTO> expected = List.of(
                new AnswerDTO(11, "Content1", ts, "author1", 1, 1, 2, 1),
                new AnswerDTO(22, "Content2", ts, "author2", 2, 1, 4, 3),
                new AnswerDTO(33, "Content3", ts, "author3", 3, 1, 6, 0)
        );

        assertEquals(expected, answerService.getAnswers(questionId));
    }

    @Test
    void getAnswersWithInvalidQuestionId() {
        int questionId = 1;
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getAnswers(questionId));
    }

    @Test
    void addNewAnswerWithValidQuestionIdAndUserId() {
        // Service ignores dto.userId() and uses the authenticated user
        NewAnswerDTO dto = new NewAnswerDTO("Content", 3, 5);

        Question question = new Question(); question.setId(dto.questionId());
        when(questionRepository.findById(dto.questionId())).thenReturn(Optional.of(question));

        // set authenticated user
        String username = "authorUser";
        setAuthenticatedUser(username);

        // return a current user whose id matches dto.userId() to keep your assertions valid
        UserEntity currentUser = new UserEntity();
        currentUser.setId(dto.userId());
        currentUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));

        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

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
        // The failure must come from user lookup via username
        NewAnswerDTO dto = new NewAnswerDTO("Content", 3, 5);

        Question q = new Question(); q.setId(dto.questionId());
        when(questionRepository.findById(dto.questionId())).thenReturn(Optional.of(q));

        String username = "ghost";
        setAuthenticatedUser(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> answerService.addNewAnswer(dto));
    }

    @Test
    void updateAnswerWithValidAnswerIdThenSaveAnswer() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity author = new UserEntity(); author.setId(10); author.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(author));

        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated comment");
        Answer answer = new Answer();
        answer.setAuthor(author);
        answer.setId(dto.id());
        answer.setContent("Old comment");
        answer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        when(answerRepository.findById(dto.id())).thenReturn(Optional.of(answer));
        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.updateAnswer(dto);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        assertEquals(dto.content(), answerCaptor.getValue().getContent());
    }

    @Test
    void updateAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(3, "Updated comment");
        when(answerRepository.findById(dto.id())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.updateAnswer(dto));
    }

    @Test
    void updateAnswerWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity current = new UserEntity(); current.setId(2); current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        UpdatedAnswerDTO dto = new UpdatedAnswerDTO(5, "New Content");

        UserEntity realAuthor = new UserEntity(); realAuthor.setId(999); realAuthor.setUsername("realAuthor");
        Answer answer = new Answer();
        answer.setId(5);
        answer.setContent("Old Content");
        answer.setAuthor(realAuthor);

        when(answerRepository.findById(answer.getId())).thenReturn(Optional.of(answer));

        assertThrows(NotAllowedOperationException.class, () -> answerService.updateAnswer(dto));
    }

    @Test
    void likeAnswerWithValidAnswerIdThenSaveAnswer() {
        int answerId = 1;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setLikes(4);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.likeAnswer(answerId);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        assertEquals(5, answerCaptor.getValue().getLikes());
    }

    @Test
    void likeAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 1;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.likeAnswer(answerId));
    }

    @Test
    void getLikesWithValidAnswerIdThenGetLikes() {
        int answerId = 1;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setLikes(4);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertEquals(4, answerService.getLikes(answerId));
    }

    @Test
    void getLikesWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 1;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getLikes(answerId));
    }

    @Test
    void dislikeAnswerWithValidAnswerIdThenSaveAnswer() {
        int answerId = 10;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setDislikes(4);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.dislikeAnswer(answerId);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        assertEquals(5, answerCaptor.getValue().getDislikes());
    }

    @Test
    void dislikeAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 10;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.dislikeAnswer(answerId));
    }

    @Test
    void getDislikesWithValidAnswerIdThenGetDislikes() {
        int answerId = 10;
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setDislikes(5);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        assertEquals(5, answerService.getDislikes(answerId));
    }

    @Test
    void getDislikesWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 10;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getDislikes(answerId));
    }

    @Test
    void deleteAnswerWhenUserIsAuthorizedThenDeleteAnswer() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity currentUser = new UserEntity(); currentUser.setId(10); currentUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));

        int answerId = 1;
        Answer answer = new Answer(); answer.setId(answerId); answer.setAuthor(currentUser);

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));

        answerService.deleteAnswer(answerId);

        verify(answerRepository, times(1)).deleteById(answerId);
    }

    @Test
    void deleteAnswerWhenUserIsNotAuthorizedThenThrowsNotAllowedOperationException() {
        String username = "testUser";
        setAuthenticatedUser(username);

        UserEntity currentUser = new UserEntity(); currentUser.setId(10); currentUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));

        UserEntity realAuthor = new UserEntity(); realAuthor.setId(11);

        int answerId = 2;
        Answer answer = new Answer(); answer.setId(answerId); answer.setAuthor(realAuthor);

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
        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.updateAnyAnswer(dto);

        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        assertEquals(dto.content(), answerCaptor.getValue().getContent());
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
        Answer answer = new Answer(); answer.setId(answerId);
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
        UserEntity author = new UserEntity(); author.setUsername("Author");
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setContent("Content");
        answer.setAuthor(author);
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        answer.setCreatedAt(ts);
        Answer parent = new Answer(); parent.setId(2);
        answer.setParent(parent);
        Question question = new Question(); question.setId(1);
        answer.setQuestion(question);
        answer.setLikes(3);
        answer.setDislikes(1);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));

        AnswerDTO expected = new AnswerDTO(4, "Content", ts, "Author", 2, 1, 3, 1);
        assertEquals(expected, answerService.getAnswer(answerId));
    }

    @Test
    void getAnswerWithInvalidAnswerIdThenThrowNoSuchElementException() {
        int answerId = 5;
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> answerService.getAnswer(answerId));
    }

    @Test
    void addCommentOfCommentWithValidParentIdAndUserIdThenAddComment() {
        int parentId = 5;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, parentId);

        // Service uses current user, not dto.userId()
        String username = "replier";
        setAuthenticatedUser(username);
        UserEntity current = new UserEntity(); current.setId(77); current.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(current));

        Answer parentAnswer = new Answer(); parentAnswer.setId(parentId);
        parentAnswer.setQuestion(new Question());
        parentAnswer.setReplies(new ArrayList<>(List.of(new Answer(), new Answer())));
        when(answerRepository.findById(parentId)).thenReturn(Optional.of(parentAnswer));

        when(answerRepository.save(Mockito.any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.addCommentOfComment(parentId, dto);

        ArgumentCaptor<Answer> captor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(captor.capture());
        assertEquals(dto.content(), captor.getValue().getContent());
        assertEquals(parentId, captor.getValue().getParent().getId());
    }

    @Test
    void addCommentOfCommentWithInvalidUserIdThenThrowNoSuchElementException() {
        int parentId = 5;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, parentId);

        String username = "ghost";
        setAuthenticatedUser(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty()); // trigger user-not-found

        assertThrows(NoSuchElementException.class, () -> answerService.addCommentOfComment(parentId, dto));
    }

    @Test
    void addCommentOfCommentWithInvalidParentIdThenThrowNoSuchElementException() {
        int parentId = 11;
        NewReplyDTO dto = new NewReplyDTO("New comment", 3, parentId);

        String username = "replier";
        setAuthenticatedUser(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new UserEntity()));

        when(answerRepository.findById(parentId)).thenReturn(Optional.empty()); // trigger parent-not-found

        assertThrows(NoSuchElementException.class, () -> answerService.addCommentOfComment(parentId, dto));
    }
}
