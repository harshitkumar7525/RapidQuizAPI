package in.harshitkumar7525.RapidQuiz.service;

import in.harshitkumar7525.RapidQuiz.document.GameSession;
import in.harshitkumar7525.RapidQuiz.document.Participant;
import in.harshitkumar7525.RapidQuiz.document.Quizzes;
import in.harshitkumar7525.RapidQuiz.dto.CreateGameRequest;
import in.harshitkumar7525.RapidQuiz.dto.JoinGameRequest;
import in.harshitkumar7525.RapidQuiz.exception.ConflictException;
import in.harshitkumar7525.RapidQuiz.exception.ForbiddenException;
import in.harshitkumar7525.RapidQuiz.exception.QuizValidationException;
import in.harshitkumar7525.RapidQuiz.exception.ResourceNotFoundException;
import in.harshitkumar7525.RapidQuiz.repository.GameSessionRepository;
import in.harshitkumar7525.RapidQuiz.repository.ParticipantRepository;
import in.harshitkumar7525.RapidQuiz.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipantRepository participantRepository;
    private final QuizRepository quizRepository;
    private final RoomCodeGenerator roomCodeGenerator;

    public GameService(GameSessionRepository gameSessionRepository,
                       ParticipantRepository participantRepository,
                       QuizRepository quizRepository,
                       RoomCodeGenerator roomCodeGenerator) {
        this.gameSessionRepository = gameSessionRepository;
        this.participantRepository = participantRepository;
        this.quizRepository = quizRepository;
        this.roomCodeGenerator = roomCodeGenerator;
    }

    public GameSession create(CreateGameRequest request) {
        Quizzes quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz does not exist"));

        if (!quiz.getCreatedBy().equals(request.getHostId())) {
            throw new ForbiddenException("Only the quiz creator can start a game session for it");
        }

        GameSession game = new GameSession();
        game.setQuizId(quiz.getId());
        game.setHostId(request.getHostId());
        game.setRoomCode(generateUniqueRoomCode());
        game.setStatus(GameSession.GameStatus.WAITING);
        game.setCurrentQuestion(0);
        return gameSessionRepository.save(game);
    }

    public Participant join(JoinGameRequest request) {
        GameSession game = gameSessionRepository.findByRoomCode(request.getRoomCode())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (game.getStatus() == GameSession.GameStatus.ENDED) {
            throw new ResourceNotFoundException("Room not found or game has ended");
        }

        participantRepository.findByGameIdAndName(game.getId(), request.getName())
                .ifPresent(p -> {
                    throw new ConflictException("Display name already taken in this room");
                });

        Participant participant = new Participant();
        participant.setGameId(game.getId());
        participant.setName(request.getName());
        participant.setJoinedAt(LocalDateTime.now());
        return participantRepository.save(participant);
    }

    private String generateUniqueRoomCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = roomCodeGenerator.generate();
            if (!gameSessionRepository.existsByRoomCode(code)) {
                return code;
            }
        }
        throw new QuizValidationException("Failed to generate a unique room code, please retry");
    }
}