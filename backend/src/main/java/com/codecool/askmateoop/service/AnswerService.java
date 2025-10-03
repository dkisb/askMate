package com.codecool.askmateoop.service;

import com.codecool.askmateoop.errorhandler.custom_exceptions.NotAllowedOperationException;
import com.codecool.askmateoop.model.entities.ratings.AnswerDislike;
import com.codecool.askmateoop.model.entities.ratings.AnswerLike;
import com.codecool.askmateoop.model.payload.dto.answer.AnswerDTO;
import com.codecool.askmateoop.model.payload.dto.answer.NewAnswerDTO;
import com.codecool.askmateoop.model.entities.Answer;
import com.codecool.askmateoop.model.entities.Question;
import com.codecool.askmateoop.model.entities.UserEntity;
import com.codecool.askmateoop.model.payload.dto.answer.NewReplyDTO;
import com.codecool.askmateoop.model.payload.dto.answer.UpdatedAnswerDTO;
import com.codecool.askmateoop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerLikeRepository answerLikeRepository;
    private final AnswerDislikeRepository answerDislikeRepository;

    @Autowired
    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository, UserRepository userRepository, AnswerLikeRepository answerLikeRepository, AnswerDislikeRepository answerDislikeRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.answerLikeRepository = answerLikeRepository;
        this.answerDislikeRepository = answerDislikeRepository;
    }

    public List<AnswerDTO> getAnswers(int questionId) {
        questionRepository.findById(questionId).orElseThrow(() -> new NoSuchElementException("Question not found with id: " + questionId));
        List<Answer> answers = answerRepository.getAllByQuestionId(questionId).orElseThrow(() -> new NoSuchElementException("Answers not found with questionId: " + questionId));
        return answers.stream().map(a -> new AnswerDTO(a.getId(), a.getContent(), a.getCreatedAt(),
                a.getAuthor().getUsername(),
                a.getParent() != null ? a.getParent().getId() : null,
                a.getQuestion() != null ? a.getQuestion().getId() : null,
                a.getLikes(), a.getDislikes())).toList();
    }


    public void addNewAnswer(NewAnswerDTO answerDTO) {
        Question question = questionRepository.findById(answerDTO.questionId()).orElseThrow(() -> new NoSuchElementException("Question not found with id: " + answerDTO.questionId()));
        //User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        UserEntity currentUser = userRepository.findById(answerDTO.userId()).orElseThrow(() -> new NoSuchElementException("User not found with id: " + answerDTO.userId()));
        Answer answer = new Answer();
        answer.setContent(answerDTO.content());
        answer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        answer.setQuestion(question);
        answer.setAuthor(currentUser);
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

    public int getLikes(int id){
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        return answer.getLikes();
    }

    public int getDislikes(int id){
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        return answer.getDislikes();
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

    public AnswerDTO getAnswer(int id) {
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        return new AnswerDTO(answer.getId(), answer.getContent(), answer.getCreatedAt(),answer.getAuthor().getUsername(),
                answer.getParent() != null ? answer.getParent().getId() : null,
                answer.getQuestion() != null ? answer.getQuestion().getId() : null,
                answer.getLikes(), answer.getDislikes());
    }

    public void addCommentOfComment(int parentId, NewReplyDTO replyDTO) { // unnecessary parent_id: NewReplyDTO includes it
        /*
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
         */
        UserEntity currentUser = userRepository.findById(replyDTO.userId()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Answer parentAnswer = answerRepository.findById(parentId).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + replyDTO.parentId()));
        Answer answer = new Answer();
        answer.setContent(replyDTO.content());
        answer.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        answer.setAuthor(currentUser);
        answer.setQuestion(parentAnswer.getQuestion());
        parentAnswer.getReplies().add(answer);
        answer.setParent(parentAnswer);
        answerRepository.save(answer);
    }

    public boolean addLikeToAnswer(int id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity reviewer = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        if (answerDislikeRepository.existsByAnswerIdAndReviewer(id, reviewer)) {
            throw new NotAllowedOperationException("You have already disliked this answer");
        }
        if (answerLikeRepository.existsByAnswerIdAndReviewer(id, reviewer)) {
            answerLikeRepository.deleteByAnswerIdAndReviewer(id, reviewer);
            answer.setLikes(answer.getLikes() - 1);
            answerRepository.save(answer);
            return false;

        }
        AnswerLike like = new AnswerLike();
        like.setAnswerId(id);
        like.setReviewer(reviewer);
        answerLikeRepository.save(like);
        answer.setLikes(answer.getLikes() + 1);
        answerRepository.save(answer);
        return true;
    }

    public boolean addDislikeToAnswer(int id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity reviewer = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        Answer answer = answerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Answer not found with id: " + id));
        if (answerLikeRepository.existsByAnswerIdAndReviewer(id, reviewer)) {
            throw new NotAllowedOperationException("You have already liked this answer");
        }
        if (answerDislikeRepository.existsByAnswerIdAndReviewer(id, reviewer)) {
            answerDislikeRepository.deleteByAnswerIdAndReviewer(id, reviewer);
            answer.setDislikes(answer.getDislikes() - 1);
            answerRepository.save(answer);
            return false;
        }
        AnswerDislike dislike = new AnswerDislike();
        dislike.setAnswerId(id);
        dislike.setReviewer(reviewer);
        answerDislikeRepository.save(dislike);
        answer.setDislikes(answer.getDislikes() + 1);
        answerRepository.save(answer);
        return true;
    }

    public boolean alreadyLiked(int answerId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        return answerLikeRepository.existsByAnswerIdAndReviewer(answerId, userEntity);
    }

    public boolean alreadyDisliked(int answerId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException("User not found"));
        return answerDislikeRepository.existsByAnswerIdAndReviewer(answerId, userEntity);
    }
}