package in.harshitkumar7525.RapidQuiz.document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Question {
    private String question;
    private List<String> options;
    private String correct_answer;
    private Integer time_limit;
}

@Document("quizzes")
public class Quizzes {
    @Id
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @CreatedBy
    private String createdBy;

    @NotBlank
    private List<Question> questions;

    @CreatedDate
    private LocalDateTime created_at;

    @LastModifiedDate
    private LocalDateTime updated_at;
}