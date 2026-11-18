package br.com.cagedapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArquivoCagedNaoEncontradoException extends RuntimeException {

    public ArquivoCagedNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
