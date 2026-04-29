package network;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String status;
    private Object data;
    private String message;

}