/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.edu.itesca.compilador;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author diego
 */
public class Matrices {
    private Workbook wb;
    private Sheet s;
    int filas;
    int columnas;
    
    public Matrices(){
        try{
            InputStream fis = new FileInputStream("src/main/java/Excel/Matriz.xlsx");
            wb = WorkbookFactory.create(fis);
            s = wb.getSheet("Hoja1");
            Row r = s.getRow(0);
            filas = s.getPhysicalNumberOfRows() -1;
            columnas = r.getPhysicalNumberOfCells() -1;
        }catch (FileNotFoundException ex){
            System.out.println("Error 1");
        }catch (IOException | EncryptedDocumentException ex){
            System.out.println("Error 2");
        }
    }
    
    public int [][] miMatriz(){
        int [][] matriz = new int [filas][columnas];
        for (int i = 0; i < matriz.length; i++) {
            Row r = s.getRow(i + 1);
            for (int j = 0; j < matriz[i].length; j++) {
                Cell c = r.getCell(j);
                matriz[i][j]= (int)c.getNumericCellValue();
                
            }
            
        }
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                System.out.print(matriz[i][j] + " | ");
            }
            System.out.println("");
        }
        return matriz;
    }
    public String[] miColumna(){
        String[] columna = new String[columnas];
        Row r = s.getRow(0);
        for (int i = 0; i < columna.length; i++) {
            Cell c = r.getCell(i);
            columna[i] = c.toString();
        }
        return columna;
    }
}
