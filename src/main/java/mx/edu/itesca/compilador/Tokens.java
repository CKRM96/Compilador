package mx.edu.itesca.compilador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Tokens {
    int[][] matriz;
    File archivo;
    JTable tblTokens, tblErrores, tblContadores;
    String[] expresiones;

    public Tokens(int[][] matriz, File archivo, JTable tblTokens, JTable tblErrores, JTable tblContadores, String[] expresiones) {
        this.matriz = matriz;
        this.archivo = archivo;
        this.tblTokens = tblTokens;
        this.tblErrores = tblErrores;
        this.tblContadores = tblContadores;
        this.expresiones = expresiones;
        compilarMatriz();
    }

    private void compilarMatriz() {
        int estadoActual = 0;
        StringBuilder lexema = new StringBuilder();
        int linea = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            int caracter;
            while ((caracter = br.read()) != -1) {
                char c = (char) caracter;

                // Salto de línea
                if (c == '\n') {
                    linea++;
                    continue;
                }

                // Espacios y tabulaciones: tokenizar si hay algo acumulado
                if (Character.isWhitespace(c)) {
                    if (lexema.length() > 0) {
                        finalizarLexema(estadoActual, lexema.toString(), linea);
                        estadoActual = 0;
                        lexema.setLength(0);
                    }
                    continue;
                }

                int columnaMatriz = getColumna(c);

                if (columnaMatriz == -1) {
                    if (lexema.length() > 0) {
                        agregarError("Token no reconocido", lexema.toString(), linea);
                        lexema.setLength(0);
                    }
                    agregarError("Carácter inválido", Character.toString(c), linea);
                    estadoActual = 0;
                    continue;
                }

                int nuevoEstado = matriz[estadoActual][columnaMatriz];

                if (nuevoEstado >= 500) {
                    // Error léxico detectado
                    agregarError("Error léxico", lexema.toString() + c, linea);
                    estadoActual = 0;
                    lexema.setLength(0);
                    continue;
                }

                if (nuevoEstado < 0) {
                    // Estado final (token reconocido)
                    lexema.append(c);
                    agregarTokenFinal(nuevoEstado, lexema.toString(), linea);
                    estadoActual = 0;
                    lexema.setLength(0);
                    continue;
                }

                // Avance normal
                estadoActual = nuevoEstado;
                lexema.append(c);
            }

            // Revisar lexema pendiente al final
            if (lexema.length() > 0) {
                finalizarLexema(estadoActual, lexema.toString(), linea);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizarLexema(int estadoActual, String lexema, int linea) {
        if (esEstadoFinal(estadoActual)) {
            agregarToken(estadoActual, lexema, linea);
        } else {
            agregarError("Token no reconocido", lexema, linea);
        }
    }

    private boolean esEstadoFinal(int estado) {
        return estado >= 0 && estado < expresiones.length && expresiones[estado] != null && !expresiones[estado].isEmpty();
    }

    
    private void agregarToken(int estado, String lexema, int linea) {
    String tipo = palabrasReservadas.contains(lexema) ? "PR" : expresiones[estado];

    // NUEVO: Identificar tipo más específico
    tipo = clasificarToken(tipo, lexema);

    ((DefaultTableModel) tblTokens.getModel()).addRow(new Object[]{tipo, lexema, linea});
    actualizarContador(tipo);
}
private String clasificarToken(String tipoBase, String lexema) {
    if (tipoBase.equals("Operador")) {
        if (lexema.equals("++") || lexema.equals("--")) {
            return "Operador Postfix";
        } else if (lexema.equals("&&") || lexema.equals("||")) {
            return "Operador Lógico Binario";
        } else if (lexema.equals("**")) {
            return "Operador Exponente";
        } else if (lexema.equals("<<") || lexema.equals(">>") || lexema.equals(">>>")) {
            return "Operador de Turno";
        } else if (lexema.equals("?")) {
            return "Operador Ternario";
        } else if (lexema.equals("=") || lexema.equals("+=") || lexema.equals("-=") || lexema.equals("*=") || lexema.equals("/=") || lexema.equals("%=")) {
            return "Operador de Asignación";
        } else if (lexema.equals("(") || lexema.equals(")") || lexema.equals("{") || lexema.equals("}") || lexema.equals("[") || lexema.equals("]")) {
            return "Operador de Agrupamiento";
        } else if (lexema.equals("===") || lexema.equals("!==")) {
            return "Operador de Conversión sin Igualdad";
        }
    } else if (tipoBase.equals("Constante")) {
        if (lexema.matches("\".*\"")) {
            return "Constante de Cadena";
        } else if (lexema.matches("\\d+")) {
            return "Constante Numérica";
        } else if (lexema.matches("\\d+\\.\\d+")) {
            return "Constante Real";
        } else if (lexema.matches("\\d+(\\.\\d+)?[eE][-+]?\\d+")) {
            return "Constante Exponencial";
        }
    } else if (tipoBase.equals("Identificador")) {
        if (lexema.equals("true") || lexema.equals("false")) {
            return "Constante Booleana";
        } else if (lexema.equals("null")) {
            return "Constante Nula";
        }
    }
    return tipoBase; // No cambiar si no aplica
}
private void agregarTokenFinal(int estadoFinal, String lexema, int linea) {
    String tipo = obtenerNombreToken(estadoFinal);

    // NUEVO: Identificar tipo más específico
    tipo = clasificarToken(tipo, lexema);

    ((DefaultTableModel) tblTokens.getModel()).addRow(new Object[]{tipo, lexema, linea});
    actualizarContador(tipo);
}
    
    private void agregarError(String descripcion, String lexema, int linea) {
        ((DefaultTableModel) tblErrores.getModel()).addRow(new Object[]{
            "ERROR", descripcion, lexema, "Léxico", linea, "-"
        });
        actualizarContador("Errores");
    }

    private void actualizarContador(String tipo) {
        DefaultTableModel modelo = (DefaultTableModel) tblContadores.getModel();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(tipo)) {
                int actual = Integer.parseInt(modelo.getValueAt(i, 1).toString());
                modelo.setValueAt(actual + 1, i, 1);
                return;
            }
        }
    }

    private String obtenerNombreToken(int estadoFinal) {
        int indice = -estadoFinal; // Estado final es negativo
        if (indice >= 0 && indice < expresiones.length) {
            return expresiones[indice] != null ? expresiones[indice] : "Desconocido";
        }
        return "Desconocido";
    }

    private int getColumna(char c) {
        switch (c) {
            case '+': return 1;
            case '-': return 2;
            case '*': return 3;
            case '<': return 4;
            case '>': return 5;
            case '=': return 6;
            case '!': return 7;
            case '&': return 8;
            case '|': return 9;
            case '%': return 10;
            case '^': return 11;
            case '~': return 12;
            case ',': return 13;
            case '.': return 14;
            case ';': return 15;
            case ':': return 16;
            case '?': return 17;
            case '[': return 18;
            case ']': return 19;
            case '{': return 20;
            case '}': return 21;
            case '(': return 22;
            case ')': return 23;
            case '@': return 26;
            case '/': return 27;
            case '_': return 28;
            case '"': return 29;
            case '\'': return 30;
            case '#': return 31;
            case '$': return 32;
            case '\n': return 33;
            default:
                if (Character.isLetter(c)) return 24;  // Letras [a-zA-Z]
                if (Character.isDigit(c)) return 25;   // Dígitos [0-9]
                if (c != '*') return 34;               // [^*] = cualquier cosa que no sea '*'
                return 35;                             // oc = otro carácter
        }
    }

    Set<String> palabrasReservadas = new HashSet<>(Arrays.asList(
        "await", "break", "case", "catch", "class", "const", "continue", "debugger",
        "default", "delete", "do", "else", "export", "extends", "false", "finally",
        "for", "function", "if", "import", "in", "instanceof", "new", "null",
        "return", "super", "switch", "this", "throw", "true", "try", "typeof",
        "var", "void", "while", "with", "yield", "let", "static", "enum",
        "implements", "interface", "package", "private", "protected", "public",
        "abstract", "boolean", "byte", "char", "double", "final", "float", "goto",
        "int", "long", "native", "short", "synchronized", "throws", "transient",
        "volatile", "require", "module", "exports", "global", "__dirname",
        "__filename", "process", "Buffer", "setImmediate", "setTimeout", "setInterval"
    ));
}