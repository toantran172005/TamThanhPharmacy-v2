package entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "CT_PhieuNhapThuoc")
public class CTPhieuNhapThuoc implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private IdClass id = new IdClass();

    @Column(name = "soLo", nullable = false)
    private Integer soLo;

    @Column(name = "soLuongNhap", nullable = false)
    private Integer soLuongNhap;

    @Column(name = "donGiaNhap", nullable = false)
    private Double donGiaNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPNT")
    @JoinColumn(name = "maPNT", referencedColumnName = "maPNT", columnDefinition = "varchar(20)")
    private PhieuNhapThuoc phieuNhapThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maThuoc")
    @JoinColumn(name = "maThuoc", referencedColumnName = "maThuoc", columnDefinition = "varchar(20)")
    private Thuoc thuoc;

    @Embeddable
    public static class IdClass implements Serializable {

        @Column(name = "maPNT", length = 20, columnDefinition = "varchar(20)")
        private String maPNT;

        @Column(name = "maThuoc", length = 20, columnDefinition = "varchar(20)")
        private String maThuoc;

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            IdClass idClass = (IdClass) object;
            return Objects.equals(maPNT, idClass.maPNT) && Objects.equals(maThuoc, idClass.maThuoc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maPNT, maThuoc);
        }
    }
}