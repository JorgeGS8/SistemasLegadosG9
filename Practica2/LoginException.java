import java.io.IOException;

public class LoginException extends IOException {

    public enum Tipo {
        USUARIO_NO_AUTORIZADO,
        USUARIO_INCORRECTO,
        CONTRASENA_INCORRECTA,
        USUARIO_EN_USO
    }

    private final Tipo tipo;

    public LoginException(Tipo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
