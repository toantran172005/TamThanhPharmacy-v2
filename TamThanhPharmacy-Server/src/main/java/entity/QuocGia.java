package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "QuocGia")
public class QuocGia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maQuocGia", length = 100)
    private String maQuocGia;

    @Nationalized
    @Column(name = "tenQuocGia", length = 100, nullable = false)
    private String tenQuocGia;

}
