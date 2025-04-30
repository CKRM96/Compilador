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
                int columnaMatriz = getColumna(c);

                if (columnaMatriz == -1) {
                    if (lexema.length() > 0) {
                        
                        lexema.setLength(0);
                    }
                    
                    estadoActual = 0;
                    continue;
                }

                int nuevoEstado = matriz[estadoActual][columnaMatriz];

                if (nuevoEstado >= 500) {
                    // Error léxico detectado
                    agregarError(nuevoEstado, lexema.toString() + c, "ERROR DE LEXEMA", linea);

                    lexema.setLength(0);
                    columnaMatriz = getColumna(c);
                    estadoActual = matriz[0][columnaMatriz];
                    if (c == ' ' || c == '\n') {

                    } else {
                        lexema.append(c);
                    }
                    continue;
                }else if (nuevoEstado < 0) {
                    // Estado final (token reconocido)
                    if (lexema.length() > 0) {
                        finalizarLexema(nuevoEstado, lexema.toString(), linea);

                        lexema.setLength(0);
                        columnaMatriz = getColumna(c);
                        estadoActual = matriz[0][columnaMatriz];
                        if (c == ' ' || c == '\n') {

                        } else {
                            lexema.append(c);
                        }

                    }
                    
                } else {
                    estadoActual = nuevoEstado;
                    lexema.append(c);
                }

                if (c == '\n') {
                    linea++;
                }
                
            }
            System.out.println("");
            
            // Revisar lexema pendiente al final
//            if (lexema.length() > 0) {
//                finalizarLexema(estadoActual, lexema.toString(), linea);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizarLexema(int estadoFinal, String lexema, int linea) {
        agregarToken(estadoFinal, lexema, linea);
    }

    private void agregarToken(int estadoFinal, String lexema, int linea) {
        String tipo = "";
        if (palabrasReservadas.contains(lexema)) {
            actualizarContador("Palabras reservada");
            tipo = "PR";

        }else if(lexema.equalsIgnoreCase("null")){
            tipo= "null";
            actualizarContador("Constante nula");
        }else if(lexema.equalsIgnoreCase("false")||lexema.equalsIgnoreCase("true")){
            tipo="booleana";
            actualizarContador("Constante booleana");
        }
        else {
            tipo = clasificarToken(estadoFinal);
            actualizarContador(tipo);
        }

        // NUEVO: Identificar tipo más específico
        ((DefaultTableModel) tblTokens.getModel()).addRow(new Object[]{tipo, lexema, linea});
        
    }

    public String clasificarToken(int estadoFinal) {
        String res = "";
        switch (estadoFinal) {
            case -1:
                res = "Operador +";
                actualizarContador("Operador matemático");
                break;
            case -2:
                res = "Operador ++";
                actualizarContador("Operador postfix");
                break;
            case -3:
                res = "Operador +=";
                actualizarContador("Operadores de asignación");
                break;
            case -4:
                res = "Operador -";
                actualizarContador("Operador matemático");
                break;
            case -5:
                res = "Operador --";
                actualizarContador("Operador postfix");
                break;
            case -6:
                res = "Operador -=";
                actualizarContador("Operadores de asignación");
                break;
            case -7:
                res = "Operador *";
                actualizarContador("Operador matemático");
                break;
            case -8:
                res = "Operador **";
                actualizarContador("Operador exponente");
                break;
            case -9:
                res = "Operador *=";
                actualizarContador("Operadores de asignación");
                break;
            case -10:
                res = "Operador <";
                actualizarContador("Operador relancional");
                break;
            case -11:
                res = "Operador <<";
                actualizarContador("Operador de turno");
                break;
            case -12:
                res = "Operador <<=";
                actualizarContador("Operadores de asignación");
                break;
            case -13:
                res = "Operador <=";
                actualizarContador("Operador relancional");
                break;
            case -14:
                res = "Operador <>";
                actualizarContador("Operador relancional");
                break;
            case -15:
                res = "Operador >";
                actualizarContador("Operador relancional");
                break;
            case -16:
                res = "Operador >>";
                actualizarContador("Operador de turno");
                break;
            case -17:
                res = "Operador >>>";
                actualizarContador("Operador de turno");
                break;
            case -18:
                res = "Operador >>>=";
                actualizarContador("Operadores de asignación");
                break;
            case -19:
                res = "Operador >=";
                actualizarContador("Operador relancional");
                break;
            case -20:
                res = "Operador >>=";
                actualizarContador("Operadores de asignación");
                break;
            case -21:
                res = "Operador =";
                actualizarContador("Operadores de asignación");
                break;
            case -22:
                res = "Operador ==";
                actualizarContador("Operador relancional");
                break;
            case -23:
                res = "Operador ===";
                actualizarContador("sin igualdad de conv");
                break;
            case -24:
                res = "Operador =>";
                actualizarContador("Operadores de asignación");
                break;
            case -25:
                res = "Operador !";
                actualizarContador("Operadores logicos");
                break;
            case -26:
                res = "Operador !=";
                actualizarContador("Operador relancional");
                break;
            case -27:
                res = "Operador !==";
                actualizarContador("sin igualdad de conv");
                break;
            case -28:
                res = "Operador /";
                actualizarContador("Operador matemático");
                break;
            case -29:
                res = "Operador /=";
                actualizarContador("Operadores de asignación");
                break;
            case -30:
                res = "Comentario /*";
                actualizarContador("Comentario");
                break;
            case -31:
                res = "Comentario //";
                actualizarContador("Comentario");
                break;
            case -32:
                res = "Operador %";
                actualizarContador("Operador lógico binario");
                break;
            case -33:
                res = "Operador %=";
                actualizarContador("Operadores de asignación");
                break;
            case -34:
                res = "Numero entero";
                actualizarContador("Constante numérica");
                break;
            case -35:
                res = "numero decimal";
                actualizarContador("Constante real");
                break;
            case -36:
                res = "exponencial";
                actualizarContador("Constante exponencial");
                break;
            case -37:
                res = "Operador &";
                actualizarContador("Operador lógico binario");
                break;
            case -38:
                res = "Operador &&";
                actualizarContador("Operadores logicos");
                break;
            case -39:
                res = "Operador &=";
                actualizarContador("Operadores de asignación");
                break;
            case -40:
                res = "Operador |";
                actualizarContador("Operador lógico binario");
                break;
            case -41:
                res = "Operador ||";
                actualizarContador("Operadores logicos");
                break;

            case -42:
                res = "Operador ^";
                actualizarContador("Operador lógico binario");
                break;
            case -43:
                res = "Operador ^=";
                actualizarContador("Operadores de asignación");
                break;
            case -44:
                res = "Identificador";
                actualizarContador("Identificadores");
                break;
            case -45:
                res = "Operador agrupamiento {";
                actualizarContador("De Agrupamiento");                
                break;
            case -46:
                res = "Operador agrupamiento }";
                actualizarContador("De Agrupamiento");
                break;
            case -47:
                res = "Operador agrupamiento (";
                actualizarContador("De Agrupamiento");
                break;
            case -48:
                res = "Operador agrupamiento )";
                actualizarContador("De Agrupamiento");
                break;
            case -49:
                res = "Operador agrupamiento [";
                actualizarContador("De Agrupamiento");
                break;
            case -50:
                res = "Operador agrupamiento ]";
                actualizarContador("De Agrupamiento");
                break;
            case -51:
                res = "Operador de control ,";
                actualizarContador("Operador de control");
                break;
            case -52:
                res = "Operador de control .";
                actualizarContador("Operador de control");
                break;
            case -53:
                res = "Operador de control ;";
                actualizarContador("Operador de control");
                break;
            case -54:
                res = "Operador de control :";
                actualizarContador("Operador de control");
                break;
            case -55:
                res = "Operador ternario ?";
                actualizarContador("Operador ternario");

                break;
            case -56:
                res = "Operador ~";
                actualizarContador("Operador lógico binario");
                break;
            case -57:
                res = "Constante de cadena \" ";
                actualizarContador("Constante de cadena");
                break;
                
            case -58:
                res = "Constante de cadena \' ";
                actualizarContador("Constante de cadena");
                break;
            default:
        }      
        return res;
    }


    private void agregarError(int codigoError, String lexema, String tipoError, int linea) {
    String descripcion;

    switch (codigoError) {
        case 501:
            descripcion = "se esperaba una /";
            break;
        case 502:
            descripcion = "se esperaba un numero entero";
            break;
        case 503:
            descripcion = "se esperaba un +, un - o un numero";
            break;
        case 504:
            descripcion = "se esperaba cualquier cosa menos el salto de linea";
            break;
        case 505:
            descripcion = "se esperaba todo menos _";
            break;
        case 506:
            descripcion = "se esperaba todo menos $";
            break;
        case 507:
            descripcion = "se esperaba todo menos #";
            break;
        // Agrega más errores aquí según tu diseño
        default:
            descripcion = " ";
            break;
    }

    ((DefaultTableModel) tblErrores.getModel()).addRow(new Object[]{
        "E" + codigoError, descripcion, lexema,tipoError, linea
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
            case '+':
                return 0;
            case '-':
                return 1;
            case '*':
                return 2;
            case '<':
                return 3;
            case '>':
                return 4;
            case '=':
                return 5;
            case '!':
                return 6;
            case '&':
                return 7;
            case '|':
                return 8;
            case '%':
                return 9;
            case '^':
                return 10;
            case '~':
                return 11;
            case ',':
                return 12;
            case '.':
                return 13;
            case ';':
                return 14;
            case ':':
                return 15;
            case '?':
                return 16;
            case '[':
                return 17;
            case ']':
                return 18;
            case '{':
                return 19;
            case '}':
                return 20;
            case '(':
                return 21;
            case ')':
                return 22;
            case '@':
                return 23;
            case '/':
                return 26;
            case '_':
                return 27;
            case '"':
                return 28;
            case '\'':
                return 29;
            case '#':
                return 30;
            case '$':
                return 31;
            case '\n':
                return 32;
            default:
                if (Character.isLetter(c)) {
                    return 24;  // Letras [a-zA-Z]
                }
                if (Character.isDigit(c)) {
                    return 25;   // Dígitos [0-9]
                }
                if (c != '*') {
                    return 33;               // [^*] = cualquier cosa que no sea '*'
                }
                return 34;                             // oc = otro carácter
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
