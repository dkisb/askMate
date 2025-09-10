package com.codecool.askmateoop.service;

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
import org.springframework.security.access.AccessDeniedException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
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
        Optional<List<Answer>> answers = answerRepository.getAllByQuestionId(questionId);
        Optional<Question> question = questionRepository.findById(questionId);
        if (answers.isEmpty() || question.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        return answers.get().stream().map(a -> new AnswerDTO(a.getId(), a.getContent(), a.getCreatedAt())).toList();
    }

    public void addNewAnswer(NewAnswerDTO answerDTO) {
        Answer answer = new Answer();
        Question question = questionRepository.findById(answerDTO.questionId()).orElseThrow(() -> new RuntimeException("Question not found"));
        answer.setContent(answerDTO.content());
        answer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        answer.setQuestion(question);
        answer.setAuthor(question.getAuthor());
        answerRepository.save(answer);
    }

    public void updateAnswer(UpdatedAnswerDTO answerDTO) {
        Answer answer = answerRepository.findById(answerDTO.id()).orElseThrow(() -> new RuntimeException("Answer not found"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        if (answer.getAuthor().getId() != currentUser.getId()) {
            throw new AccessDeniedException("You do not have permission to update this answer");
        }
        answer.setContent(answerDTO.content());
        answerRepository.save(answer);
    }

    public void deleteAnswer(int id){
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new RuntimeException("Answer not found"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        if (answer.getAuthor().getId() != currentUser.getId()) {
            throw new AccessDeniedException("You are not allowed to delete this answer");
        }
        answerRepository.deleteById(id);
    }

    public void updateAnyAnswer(UpdatedAnswerDTO answerDTO) {
        Answer answer = answerRepository.findById(answerDTO.id()).orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setContent(answerDTO.content());
        answerRepository.save(answer);
    }

    public void deleteAnyAnswer(int id) {
        answerRepository.findById(id).orElseThrow(() -> new RuntimeException("Answer not found"));
        answerRepository.deleteById(id);
    }
}
