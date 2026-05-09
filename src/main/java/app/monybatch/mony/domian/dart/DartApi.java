package app.monybatch.mony.domian.dart;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dart_api")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DartApi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 255)
    private String category;

    @Column(name = "api_name", nullable = false, length = 255)
    private String apiName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "path", nullable = false, length = 255)
    private String path;

}
