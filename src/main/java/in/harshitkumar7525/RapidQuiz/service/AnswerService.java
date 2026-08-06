package in.harshitkumar7525.RapidQuiz.service;

import in.harshitkumar7525.RapidQuiz.document.Answer;
import in.harshitkumar7525.RapidQuiz.document.GameSession;
import in.harshitkumar7525.RapidQuiz.document.Participant;
import in.harshitkumar7525.RapidQuiz.document.Question;
import in.harshitkumar7525.RapidQuiz.document.Quizzes;
import in.harshitkumar7525.RapidQuiz.dto.SubmitAnswerRequest;
import in.harshitkumar7525.RapidQuiz.exception.ConflictException;
import in.harshitkumar7525.RapidQuiz.exception.QuizValidationException;
import in.harshitkumar7525.RapidQuiz.exception.ResourceNotFoundException;
import in.harshitkumar7525.RapidQuiz.repository.AnswerRepository;
import in.harshitkumar7525.RapidQuiz.repository.GameSessionRepository;
import in.harshitkumar7525.RapidQuiz.repository.ParticipantRepository;
import in.harshitkumar7525.RapidQuiz.repository.QuizRepository;
import in.harshitkumar7525.RapidQuiz.websocket.GameBroadcastService;
import in.harshitkumar7525.RapidQuiz.websocket.WSMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AnswerService {

    private final GameSessionRepository gameSessionRepository;
    private final QuizRepository quizRepository;
    private final ParticipantRepository participantRepository;
    private final AnswerRepository answerRepository;
    private final LeaderboardService leaderboardService;
    private final GameBroadcastService gameBroadcastService;

    public AnswerService(GameSessionRepository gameSessionRepository,
                         QuizRepository quizRepository,
                         ParticipantRepository participantRepository,
                         AnswerRepository answerRepository,
                         LeaderboardService leaderboardService,
                         GameBroadcastService gameBroadcastService) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizRepository = quizRepository;
        this.participantRepository = participantRepository;
        this.answerRepository = answerRepository;
        this.leaderboardService = leaderboardService;
        this.gameBroadcastService = gameBroadcastService;
    }

    public Answer submit(String gameId, SubmitAnswerRequest request) {
        GameSession game = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game or quiz not found"));

        if (game.getStatus() != GameSession.GameStatus.RUNNING) {
            throw new QuizValidationException("Game is not currently running");
        }

        Quizzes quiz = quizRepository.findById(game.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Game or quiz not found"));

        Participant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if (!participant.getGameId().equals(gameId)) {
            throw new ResourceNotFoundException("Participant not found");
        }

        int index = request.getQuestionIndex();
        if (index < 0 || index >= quiz.getQuestions().size()) {
            throw new QuizValidationException("Invalid question index");
        }

        if (answerRepository.existsByGameIdAndParticipantIdAndQuestionIndex(gameId, participant.getId(), index)) {
            throw new ConflictException("Answer already submitted for this question by this participant");
        }

        Question question = quiz.getQuestions().get(index);
        boolean isCorrect = question.getCorrectAnswer().equals(request.getAnswer());
        int timeLimit = (question.getTimeLimit() != null && question.getTimeLimit() > 0) ? question.getTimeLimit() : 30;
        int score = isCorrect ? 100 + timeLimit : 0;

        Answer answer = new Answer();
        answer.setGameId(gameId);
        answer.setParticipantId(participant.getId());
        answer.setQuestionIndex(index);
        answer.setAnswer(request.getAnswer());
        answer.setCorrect(isCorrect);
        answer.setScore(score);
        answer.setAnsweredAt(LocalDateTime.now());
        Answer saved = answerRepository.save(answer);

        if (score > 0) {
            leaderboardService.recordScore(gameId, participant.getId(), score);
        }

        gameBroadcastService.broadcast(game.getRoomCode(), new WSMessage("score_update", Map.of(
                "participantId", participant.getId(),
                "name", participant.getName(),
                "isCorrect", isCorrect,
                "score", score
        )));

        return saved;
    }
}