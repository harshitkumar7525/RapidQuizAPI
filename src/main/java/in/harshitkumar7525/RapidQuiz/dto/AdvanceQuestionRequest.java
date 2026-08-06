package in.harshitkumar7525.RapidQuiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceQuestionRequest {

    @NotBlank(message = "hostId is required")
    private String hostId;
}