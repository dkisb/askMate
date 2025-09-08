package com.codecool.askmateoop.service;

import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.payload.dto.question.NewQuestionDTO;
import com.codecool.askmateoop.model.payload.dto.question.QuestionDTO;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.repository.QuestionRepository;
import com.codecool.askmateoop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Optional<Question> questionOpt = questionRepository.findQuestionById(id);
        if (questionOpt.isEmpty()) {
            throw new RuntimeException("Question not found");
        }
        Question question = questionOpt.get();
        return new QuestionDTO(question.getId(), question.getTitle(), question.getContent(), question.getCreatedAt());
    }

    public boolean deleteQuestionById(int id) {
        try {
            questionRepository.deleteQuestionById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Question not found");
        }
    }

    public int addNewQuestion(NewQuestionDTO newQuestion) {
        Optional<UserEntity> user = userRepository.findById(newQuestion.userId());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Question question = new Question();
        question.setTitle(newQuestion.title());
        question.setContent(newQuestion.content());
        question.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        question.setAuthor(user.get());
        Question saved = questionRepository.save(question);
        return saved.getId();
    }

    public void updateQuestion(int id, NewQuestionDTO questionDTO){
        Optional<Question> question = questionRepository.findById(id);

        if(question.isEmpty()){
            throw new RuntimeException("Question not found");
        }
        Question updatedQuestion= question.get();
        updatedQuestion.setTitle(questionDTO.title());
        updatedQuestion.setContent(questionDTO.content());

        questionRepository.save(updatedQuestion);
    }
}
