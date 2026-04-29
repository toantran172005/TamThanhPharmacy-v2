package network;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String action;
    private Object data;

}