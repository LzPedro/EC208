/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interpretador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Home
 */
public class Interpretador{

    private static char format;
    private static int rs;
    private static int rt;
    private static int rd;
    private static int shamt;
    private static int funct;
    private static int imm;
    private static int address;
    private static String code;
    private static int[] memoria;
    private static int pc;
    
    private static FileWriter fw;
    private static BufferedWriter bw;
    
    public static void main(String[] args) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("operacao.txt", "r");
        
        memoria = new int[32];
        pc = 0;
        
        type(raf);         
        raf.close();
        
    }

    //Identifica o tipo de codigo (R, I ou J)
    private static void type(RandomAccessFile raf) throws IOException{
        /*
            5 primeiros bits correspondem ao tipo de codigo
            
            0 - Tipo R
            2 ou 3 - Tipo J
            Outros - Tipo I
        */
        
        raf.seek(0);
        while((code = raf.readLine()) != null){            
            pc += code.length()+2;
            raf.seek(pc);

            code = code.replaceAll(" ", "");
            String subCode = code.substring(0, 6);        
            int value = Integer.parseInt(subCode,2);

            switch (value) {
                case 0:
                    format = 'R';
                    break;
                case 2:
                case 3:
                    format = 'J';
                    break;
                default:
                    format = 'I';
                    break;
            }
            breakCode();
            
        }       
    }
    
    //Separa o codigo em partes
    private static void breakCode() throws IOException{
        /*
            R - opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
            I - opcode(6) | rs(5) | rt(5) | imm(16)
            J - opcode(6) | address(26)
        */
        
        switch(format){
            case 'R':
                rs = Integer.parseInt(code.substring(6, 11), 2);
                rt = Integer.parseInt(code.substring(11, 16), 2);
                rd = Integer.parseInt(code.substring(16, 21), 2);
                shamt = Integer.parseInt(code.substring(21, 26), 2);
                funct = Integer.parseInt(code.substring(26, 32), 2);
                executeTypeR();
                break;
            
            case 'J':
                address = Integer.parseInt(code.substring(6, 32), 2);
                break;
            
            default:
                rs = Integer.parseInt(code.substring(6, 11), 2);
                rt = Integer.parseInt(code.substring(11, 16), 2);
                imm = Integer.parseInt(code.substring(16, 32), 2);
                break;
        }
    }
    
    private static void executeTypeR() throws IOException{
        switch(funct)
        {
            case 32:
                add(rd, rs, rt);
                break;
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }
    
    private static void executeTypeI() throws IOException{
        switch(funct)
        {
            case 32:
                add(rd, rs, rt);
                break;
            
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }
    
    private static int loadWord(int registro){
        
        registro = memoria[registro];
        return registro;
    }
    
    private static void storeWord (int registro, int valor) throws IOException{
        //escreve no txt de memoria o registro
       memoria[registro] = valor;
       fw = new FileWriter("saida.txt", false);
       bw = new BufferedWriter(fw);
       
        for (int i = 0; i < memoria.length; i++) {
            bw.write(Integer.toBinaryString(memoria[i])+"\n");
        }
        bw.close();
    }
    
    private static void add(int rd, int rs, int rt) throws IOException{
        //rd = rs + rt;
        rs = loadWord(rs);
        rt = loadWord(rt);
        rd = rs + rt;
    }
    
}