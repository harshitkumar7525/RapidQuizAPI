package in.harshitkumar7525.RapidQuiz.service;

import in.harshitkumar7525.RapidQuiz.document.Question;
import in.harshitkumar7525.RapidQuiz.document.Quizzes;
import in.harshitkumar7525.RapidQuiz.dto.CreateQuizRequest;
import in.harshitkumar7525.RapidQuiz.dto.QuestionRequest;
import in.harshitkumar7525.RapidQuiz.exception.QuizValidationException;
import in.harshitkumar7525.RapidQuiz.exception.ResourceNotFoundException;
import in.harshitkumar7525.RapidQuiz.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public Quizzes create(CreateQuizRequest request) {
        validateQuestions(request.getQuestions());

        Quizzes quiz = new Quizzes();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCreatedBy(request.getCreatedBy());
        quiz.setQuestions(toQuestions(request.getQuestions()));
        return quizRepository.save(quiz);
    }

    public List<Quizzes> findByCreatedBy(String createdBy) {
        return quizRepository.findByCreatedBy(createdBy);
    }

    public Quizzes findById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    public Quizzes update(String id, String requesterId, CreateQuizRequest request) {
        Quizzes existing = findById(id);
        if (!existing.getCreatedBy().equals(requesterId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }
        if (request.getQuestions() != null) {
            validateQuestions(request.getQuestions());
            existing.setQuestions(toQuestions(request.getQuestions()));
        }
        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        return quizRepository.save(existing);
    }

    public void delete(String id, String requesterId) {
        Quizzes existing = findById(id);
        if (!existing.getCreatedBy().equals(requesterId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }
        quizRepository.deleteById(id);
    }

    // correct_answer must exactly match one of options -- same rule as the Go backend.
    private void validateQuestions(List<QuestionRequest> questions) {
        for (QuestionRequest q : questions) {
            if (!q.getOptions().contains(q.getCorrectAnswer())) {
                throw new QuizValidationException(
                        "correct_answer must match one of the provided options for question: " + q.getQuestion());
            }
        }
    }

    private List<Question> toQuestions(List<QuestionRequest> requests) {
        return requests.stream()
                .map(r -> new Question(r.getQuestion(), r.getOptions(), r.getCorrectAnswer(), r.getTimeLimit()))
                .collect(Collectors.toList());
    }
}