package network;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String action;
    private Object data;

}