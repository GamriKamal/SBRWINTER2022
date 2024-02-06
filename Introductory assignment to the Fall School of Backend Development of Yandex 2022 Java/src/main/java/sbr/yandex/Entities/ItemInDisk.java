package sbr.yandex.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class ItemInDisk {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long idOfItems;
    @NonNull
    @Column(unique = true)
    private String id;
    private LocalDateTime date;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "children")
    private List<ItemInDisk> children;
    @NonNull
    private String parentId;
    private long size;
    @Column(nullable = false)
    private String type;
    private String url;

    public ItemInDisk(@NonNull String id, LocalDateTime date, List<ItemInDisk> children, @NonNull String parentId, long size, String type, String url) {
        this.id = id;
        this.date = date;
        this.children = children;
        this.parentId = parentId;
        this.size = size;
        this.type = type;
        this.url = url;
    }

    public static LocalDateTime convertStringToTime(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

        try {
            LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

            return dateTime;
        } catch (DateTimeParseException e) {
            // Handle the exception or rethrow it as needed
            throw new IllegalArgumentException("Invalid datetime format. Please use ISO 8601 format.", e);
        }
    }

}
