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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionLikeRepository questionLikeRepository;
    private final QuestionDislikeRepository questionDislikeRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository, QuestionLikeRepository questionLikeRepository, QuestionDislikeRepository questionDislikeRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.questionLikeRepository = questionLikeRepository;
        this.questionDislikeRepository = questionDislikeRepository;
    }

    public List<QuestionDTO> getAllQuestions() {

        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }
        return questions.stream().map(q -> new QuestionDTO(q.getId(), q.getTitle(), q.getContent(), q.getCreatedAt(), q.getAuthor().getUsername())).toList();
    }

    public QuestionDTO getQuestionById(int id) {
        Optional<Question> questionOpt = questionRepository.findById(id);
        if (questionOpt.isEmpty()) {
            throw new NoSuchElementException("Question not found with id " + id);
        }
        Question question = questionOpt.get();
        return new QuestionDTO(question.getId(), question.getTitle(), question.getContent(),question.getCreatedAt(),question.getAuthor().getUsername());
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
            throw new NotAllowedOperationException("You can only edit your own question");
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
    public void likeQuestion(int id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        question.setLikes(question.getLikes() + 1);
        questionRepository.save(question);
    }
    public int getLikes(int id){
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        return question.getLikes();
    }

    public void dislikeQuestion(int id){
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        question.setDislikes(question.getDislikes() + 1);
        questionRepository.save(question);
    }

    public int getDislikes(int id){
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        return question.getDislikes();
    }

    public void deleteAnyQuestionById(int id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        questionRepository.delete(question);
    }

    public List<QuestionDTO> getMyQuestions() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        List<Question> myQuestions = questionRepository.findAllByAuthor(currentUser).orElseThrow(() -> new NoSuchElementException("Questions not found"));
        return myQuestions.stream().map(question -> new QuestionDTO(question.getId(), question.getTitle(), question.getContent(), question.getCreatedAt(), user.getUsername())).collect(Collectors.toList());

    }

    public void addLikeToQuestion(int id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity reviewer = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        if (questionDislikeRepository.existsByQuestionIdAndReviewer(id, reviewer)) {
            throw new NotAllowedOperationException("You have already disliked this question");
        }
        if (questionLikeRepository.existsByQuestionIdAndReviewer(id, reviewer)) {
            questionLikeRepository.deleteByQuestionIdAndReviewer(id, reviewer);
            question.setLikes(question.getLikes() - 1);
            questionRepository.save(question);
        } else {
            QuestionLike like = new QuestionLike();
            like.setQuestionId(id);
            like.setReviewer(reviewer);
            questionLikeRepository.save(like);
            question.setLikes(question.getLikes() + 1);
            questionRepository.save(question);
        }
    }

    public void addDislikeToQuestion(int id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity reviewer = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Question question = questionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Question not found with id " + id));
        if (questionLikeRepository.existsByQuestionIdAndReviewer(id, reviewer)) {
            throw new NotAllowedOperationException("You have already liked this question");
        }
        if (questionDislikeRepository.existsByQuestionIdAndReviewer(id, reviewer)) {
            questionDislikeRepository.deleteByQuestionIdAndReviewer(id, reviewer);
            question.setDislikes(question.getDislikes() - 1);
        } else {
            QuestionDislike dislike = new QuestionDislike();
            dislike.setQuestionId(id);
            dislike.setReviewer(reviewer);
            questionDislikeRepository.save(dislike);
            question.setDislikes(question.getDislikes() + 1);
            questionRepository.save(question);
        }
    }
}
