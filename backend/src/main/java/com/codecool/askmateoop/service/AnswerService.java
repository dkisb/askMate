package com.codecool.askmateoop.service;

import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.repository.AnswerRepository;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        UserEntity user = question.get().getAuthor();
        return answers.get().stream().map(a -> new AnswerDTO(a.getContent(), a.getCreatedAt(), user.getId())).toList();
    }

    public int addNewAnswer(NewAnswerDTO answer) {
        Optional<Question> question = questionRepository.findQuestionById(answer.questionId());
        Optional<UserEntity> user = userRepository.findById(answer.userId());
        if (question.isEmpty() || user.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        Answer newAnswer = new Answer();
        newAnswer.setContent(answer.content());
        newAnswer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        newAnswer.setQuestion(question.get());
        newAnswer.setAuthor(user.get());
        Answer savedAnswer = answerRepository.save(newAnswer);
        return savedAnswer.getId();
    }

    public boolean deleteAnswer(int id){
        try {
            answerRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
