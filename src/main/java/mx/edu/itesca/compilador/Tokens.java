/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.edu.itesca.compilador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author diego
 */
public class Tokens {

    int[][] matriz;
    File archivo;
    JTable tblTokens, tblErrores, tblContadores;
    String expresiones[];

    public Tokens(int[][] matriz, File archivo, JTable tblTokens, JTable tblErrores, JTable tblContadores, String[] expresiones) {
        this.matriz = matriz;
        this.archivo = archivo;
        this.tblTokens = tblTokens;
        this.tblErrores = tblErrores;
        this.tblContadores = tblContadores;
        this.expresiones = expresiones;
        CompilarMatriz();
    }

    public void CompilarMatriz() {
        //aqui se deben crear las funciones para leer los tokens

        //si el dato ya pasa los 500 ya es un error
        //si el caracter es un salto de linea, se aumenta la linea (++) y se reincia la columna
        //el espacio y enter tambien cuentan pero no se tokenizan
        //cambia de estado [q's] y se le añade el caracter al lexema
        //si no es token ni es error el lexema aumenta, se pasa al siguiente caracter
        //si lex == palabra reservada se regresa el valor del token
        int estadoActual = 0;
        StringBuilder lexema = new StringBuilder();
        int linea = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            int caracter;
            while ((caracter = br.read()) != -1) {
                char c = (char) caracter;

                // Detectar saltos de línea
                if (c == '\n') {
                    linea++;
                    continue;
                }

                // Ignorar espacios y tabulaciones si no son parte del léxico
                if (Character.isWhitespace(c)) {
                    // Si hay algo en el lexema, revisar si es estado final antes de limpiar
                    if (lexema.length() > 0 && esEstadoFinal(estadoActual)) {
                        agregarToken(estadoActual, lexema.toString(), linea);
                    } else if (lexema.length() > 0) {
                        agregarError("Token no reconocido", lexema.toString(), linea);
                    }

                    estadoActual = 0;
                    lexema.setLength(0);
                    continue;
                }

                int columnaMatriz = getColumna(c);

                if (columnaMatriz == -1) {
                    // Si no pertenece a ningún símbolo válido
                    if (lexema.length() > 0) {
                        agregarError("Token no reconocido", lexema.toString(), linea);
                        lexema.setLength(0);
                    }
                    agregarError("Carácter inválido", Character.toString(c), linea);
                    estadoActual = 0;
                    continue;
                }

                int nuevoEstado = matriz[estadoActual][columnaMatriz];

                if (nuevoEstado == -1) {
                    if (esEstadoFinal(estadoActual)) {
                        agregarToken(estadoActual, lexema.toString(), linea);
                    } else {
                        agregarError("Token no reconocido", lexema.toString(), linea);
                    }

                    // Reiniciar y reintentar desde el nuevo carácter
                    estadoActual = 0;
                    lexema.setLength(0);

                    // Reintentar este carácter desde estado 0
                    columnaMatriz = getColumna(c);
                    nuevoEstado = matriz[estadoActual][columnaMatriz];

                    if (nuevoEstado != -1) {
                        estadoActual = nuevoEstado;
                        lexema.append(c);
                    } else {
                        agregarError("Carácter inválido", Character.toString(c), linea);
                        estadoActual = 0;
                    }

                } else {
                    estadoActual = nuevoEstado;
                    lexema.append(c);
                }
            }

            // Si hay un lexema pendiente al final del archivo
            if (lexema.length() > 0) {
                if (esEstadoFinal(estadoActual)) {
                    agregarToken(estadoActual, lexema.toString(), linea);
                } else {
                    agregarError("Token no reconocido", lexema.toString(), linea);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean esEstadoFinal(int estado) {
        return expresiones[estado] != null && !expresiones[estado].isEmpty();
    }

    private void agregarToken(int estado, String lexema, int linea) {
        String tipo = expresiones[estado]; // p.ej., "ID", "NUM", "BOOL", etc.
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

    private int getIndiceColumna(char c) {
        // Este método depende de cómo tengas definidas tus columnas en expresiones[]
        for (int i = 0; i < expresiones.length; i++) {
            if (expresiones[i].equals(String.valueOf(c))) {
                return i;
            }
        }
        return -1; // Carácter no válido
    }

    private void registrarToken(int estadoFinal, String lexema, int linea) {
        String tipo = obtenerNombreToken(estadoFinal); // p. ej. "IDENTIFICADOR", "NUM"
        ((DefaultTableModel) tblTokens.getModel()).addRow(new Object[]{tipo, lexema, linea});
        actualizarContador(tipo);
    }

    private void registrarError(String descripcion, String lexema, int linea, int columna) {
        ((DefaultTableModel) tblErrores.getModel()).addRow(new Object[]{
            "ERROR", descripcion, lexema, "Léxico", linea, columna
        });
        actualizarContador("Errores");
    }

    private int getColumna(char c) {
        // Este método depende de cómo tengas configuradas las columnas en tu matriz.
        // A continuación te muestro un ejemplo base, tú debes ajustarlo según tus símbolos.

        if (Character.isLetter(c)) {
            return 24; // Supongamos columna 0 es para letras
        } else if (Character.isDigit(c)) {
            return 25; // Supongamos columna 1 es para dígitos
        } else {
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
                case '#':
                    return 29;
                case '$':
                    return 30;
                case '\n':
                    return 31;

                default:
                    return -1; // Carácter inválido
            }
        }
    }

    private String obtenerNombreToken(int estadoFinal) {
        if (estadoFinal >= 0 && estadoFinal < expresiones.length) {
            return expresiones[estadoFinal] != null ? expresiones[estadoFinal] : "Desconocido";
        }
        return "Desconocido";
    }

}
