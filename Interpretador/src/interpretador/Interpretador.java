/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interpretador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
    
    private static FileWriter fw;
    private static BufferedWriter bw;
    
    public static void main(String[] args) throws IOException {
        FileReader fr = new FileReader("operacao.txt");
        BufferedReader br = new BufferedReader(fr);
        
        memoria = new int[32];
        
        type(br);         
        br.close();
        bw.close();
    }

    //Identifica o tipo de codigo (R, I ou J)
    private static void type(BufferedReader br) throws IOException{
        /*
            5 primeiros bits correspondem ao tipo de codigo
            
            0 - Tipo R
            2 ou 3 - Tipo J
            Outros - Tipo I
        */
        
        code = br.readLine();
        code = code.replaceAll(" ", "");
        String subCode = code.substring(0, 6);        
        int value = binary(subCode, subCode.length());
        
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
    
    //Separa o codigo em partes
    private static void breakCode() throws IOException{
        /*
            R - opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
            I - opcode(6) | rs(5) | rt(5) | imm(16)
            J - opcode(6) | address(26)
        */
        
        switch(format){
            case 'R':
                rs = binary(code.substring(6, 11), 5);
                rt = binary(code.substring(11, 16), 5);
                rd = binary(code.substring(16, 21), 5);
                shamt = binary(code.substring(21, 26), 5);
                funct = binary(code.substring(26, 32), 6);
                executeTypeR();
                break;
            
            case 'J':
                address = binary(code.substring(6, 32), 26);
                break;
            
            default:
                rs = binary(code.substring(6, 11), 5);
                rt = binary(code.substring(11, 16), 5);
                imm = binary(code.substring(16, 32), 16);
                break;
        }
    }
    
    private static int loadWord(int registro){
        
        registro = memoria[registro];
        return registro;
    }
    
    private static void storeWord (int registro, int posMemory, int valor) throws IOException{
        //escreve no txt de memoria o registro
       memoria[registro] = valor;
       fw = new FileWriter("saida.txt", false);
       bw = new BufferedWriter(fw);
       
        for (int i = 0; i < memoria.length; i++) {
            bw.write(Integer.toBinaryString(memoria[i])+"\n");
        }
                
    }
    
    private static void add(int rd, int rs, int rt) throws IOException{
        //rd = rs + rt;
        rs = loadWord(rs);
        rt = loadWord(rt);
        rd = rs + rt;
        storeWord(rt, 0, rd);
    }
    
    //Converte uma string(em binario) em um valor inteiro
    private static int binary(String subCode, int size) {
        int value = 0;
        int n = (int) Math.pow(2, (size-1));
        for (int i = 0; i < size; i++) {
            if(subCode.charAt(i) == '1'){
                value += n;
            }
            n /= 2;
        }
        return value;
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
}
