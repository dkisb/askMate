package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.UpdatedQuestionDTO;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public List<QuestionDTO> getAllQuestions() {

        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        return questions.stream().map(q -> new QuestionDTO(q.getId(), q.getTitle(), q.getContent(), q.getCreatedAt())).toList();
    }

    public QuestionDTO getQuestionById(int id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        return new QuestionDTO(question.getId(), question.getTitle(), question.getContent(), question.getCreatedAt());
    }

    public void deleteQuestionById(int id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id" + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (question.getAuthor().getId() != currentUser.getId()) {
            throw new NotAllowedOperationException("You can only delete your own questions");
        }
        questionRepository.deleteById(id);
    }

    public int addNewQuestion(NewQuestionDTO newQuestion) {
        UserEntity user = userRepository.findById(newQuestion.userId()).orElseThrow(() -> new NoSuchElementException("User not found with id " + newQuestion.userId()));
        Question question = new Question();
        question.setTitle(newQuestion.title());
        question.setContent(newQuestion.content());
        question.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        question.setAuthor(user);
        Question saved = questionRepository.save(question);
        return saved.getId();
    }

    public void updateQuestion(UpdatedQuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionDTO.id()).orElseThrow(() -> new NoSuchElementException("Question not found with id " + questionDTO.id()));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (question.getAuthor().getId() != currentUser.getId()) {
            throw new NotAllowedOperationException("You can only edit your onw question");
        }
        question.setTitle(questionDTO.title());
        question.setContent(questionDTO.content());
        questionRepository.save(question);
    }

    public void updateAnyQuestion(UpdatedQuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionDTO.id()).orElseThrow(() -> new NoSuchElementException("Question not found with id " + questionDTO.id()));
        question.setTitle(questionDTO.title());
        question.setContent(questionDTO.content());
        questionRepository.save(question);
    }

    public void deleteAnyQuestionById(int id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        questionRepository.delete(question);
    }
}
