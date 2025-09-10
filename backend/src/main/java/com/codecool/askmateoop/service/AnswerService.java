package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.repository.AnswerRepository;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public List<AnswerDTO> getAnswers(int questionId) {
        questionRepository.findById(questionId).orElseThrow(() -> new NoSuchElementException("Question not found with id: " + questionId));
        List<Answer> answers = answerRepository.getAllByQuestionId(questionId).orElseThrow(() -> new NoSuchElementException("Answers not found with questionId: " + questionId));
        return answers.stream().map(a -> new AnswerDTO(a.getId(), a.getContent(), a.getCreatedAt())).toList();
    }

    public void addNewAnswer(NewAnswerDTO answerDTO) {
        Question question = questionRepository.findById(answerDTO.questionId()).orElseThrow(() -> new NoSuchElementException("Question not found with id: " + answerDTO.questionId()));
        Answer answer = new Answer();
        answer.setContent(answerDTO.content());
        answer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        answer.setQuestion(question);
        answer.setAuthor(question.getAuthor());
        answerRepository.save(answer);
    }

    public void updateAnswer(UpdatedAnswerDTO answerDTO) {
        Answer answer = answerRepository.findById(answerDTO.id()).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + answerDTO.id()));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (answer.getAuthor().getId() != currentUser.getId()) {
            throw new NotAllowedOperationException("You are allowed only to edit your own answer");
        }
        answer.setContent(answerDTO.content());
        answerRepository.save(answer);
    }

    public void deleteAnswer(int id){
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (answer.getAuthor().getId() != currentUser.getId()) {
            throw new NotAllowedOperationException("You are allowed only to delete your own answer");
        }
        answerRepository.deleteById(id);
    }

    public void updateAnyAnswer(UpdatedAnswerDTO answerDTO) {
        Answer answer = answerRepository.findById(answerDTO.id()).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + answerDTO.id()));
        answer.setContent(answerDTO.content());
        answerRepository.save(answer);
    }

    public void deleteAnyAnswer(int id) {
        answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        answerRepository.deleteById(id);
    }
}
