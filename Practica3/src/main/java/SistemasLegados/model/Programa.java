// Modelado de la clase Programa
package main.java.SistemasLegados.model;

public class Programa {

    enum TipoPrograma {
        UTILIDAD("UTILIDAD"),
        ARCADE("ARCADE"),
        CONVERSACIONAL("CONVERSACIONAL"),
        VIDEOAVENTURA("VIDEOAVENTURA"),
        SIMULADOR("SIMULADOR"),
        JUEGO_DE_MESA("JUEGO DE MESA"),
        S_DEPORTIVO("S. DEPORTIVO"),
        ESTRATEGIA("ESTRATEGIA");

        private final String valorExterno;

        TipoPrograma(String valorExterno) {
            this.valorExterno = valorExterno;
        }

        @Override
        public String toString() {
            return valorExterno;
        }

        public static TipoPrograma fromString(String valorExterno) {
            for (TipoPrograma tipo : TipoPrograma.values()) {
                if (tipo.valorExterno.equalsIgnoreCase(valorExterno)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Tipo de programa no válido: " + valorExterno);
        }
        
    };

    private Integer numeroRegistro;
    private String nombre;
    private TipoPrograma tipo;
    private String cinta;

    

    public Programa(Integer registro, String nombre, String tipo, String cinta) {
        this.numeroRegistro = registro;
        this.nombre = nombre;
        this.tipo = TipoPrograma.fromString(tipo);
        this.cinta = cinta;
    }

    public Integer getNumeroRegistro() {
        return numeroRegistro;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo.toString();
    }

    public String getCinta() {
        return cinta;
    }
}