package in.harshitkumar7525.RapidQuiz.controllers;

import in.harshitkumar7525.RapidQuiz.document.GameSession;
import in.harshitkumar7525.RapidQuiz.document.Participant;
import in.harshitkumar7525.RapidQuiz.dto.CreateGameRequest;
import in.harshitkumar7525.RapidQuiz.dto.CreateGameResponse;
import in.harshitkumar7525.RapidQuiz.dto.GameStatusResponse;
import in.harshitkumar7525.RapidQuiz.dto.JoinGameRequest;
import in.harshitkumar7525.RapidQuiz.dto.JoinGameResponse;
import in.harshitkumar7525.RapidQuiz.dto.UpdateGameStatusRequest;
import in.harshitkumar7525.RapidQuiz.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateGameResponse> create(@Valid @RequestBody CreateGameRequest request) {
        GameSession game = gameService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateGameResponse.from(game));
    }

    @PostMapping("/join")
    public ResponseEntity<JoinGameResponse> join(@Valid @RequestBody JoinGameRequest request) {
        Participant participant = gameService.join(request);
        return ResponseEntity.ok(JoinGameResponse.from(participant));
    }

    @PatchMapping("/{gameId}/status")
    public ResponseEntity<GameStatusResponse> updateStatus(@PathVariable String gameId,
                                                           @Valid @RequestBody UpdateGameStatusRequest request) {
        GameSession game = gameService.updateStatus(gameId, request.getHostId(), request.getStatus());
        return ResponseEntity.ok(GameStatusResponse.from(game));
    }
}