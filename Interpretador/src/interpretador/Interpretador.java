/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interpretador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JOptionPane;

/**
 *
 * @author Home
 */
public class Interpretador {

    //Registradores
    private static int rs;
    private static int rt;
    private static int rd;
    private static int pc;

    private static char format;
    private static int opcode;
    private static int shamt;
    private static int funct;
    private static int imm;
    private static int address;
    private static int[] general_register;

    //Escritores
    private static FileWriter fw;
    private static BufferedWriter bw;

    //Leitor de memoria
    private static final int BITSDADOS = 8;
    private static RandomAccessFile mem;
    private static int[] memoria;

    public static void main(String[] args) throws IOException {
        RandomAccessFile raf = new RandomAccessFile("operacao.txt", "r");

        general_register = new int[32];
        for (int i = 0; i < general_register.length; i++) {
            general_register[i] = 0;
        }
        memoria = new int[32];
        mem = new RandomAccessFile("memoria.txt", "r");

        pc = 0;

        for (int i = 0; i < memoria.length; i++) {
            memoria[i] = Integer.parseInt(mem.readLine(), 2);
        }
        mem.close();

        type(raf);
        raf.close();
    }

    //Identifica o tipo de codigo (R, I ou J)
    private static void type(RandomAccessFile raf) throws IOException {
        /*
            6 primeiros bits correspondem ao tipo de codigo
            
            0 - Tipo R
            2 ou 3 - Tipo J
            Outros - Tipo I
         */
        String code;

        raf.seek(0);
        while ((code = raf.readLine()) != null) {
            pc += code.length() + 2;
            raf.seek(pc);

            code = code.replaceAll(" ", "");
            String subCode = code.substring(0, 6);
            opcode = Integer.parseInt(subCode, 2);

            switch (opcode) {
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
            breakCode(code);
        }
    }

    //Separa o codigo em partes de acordo com o tipo de instrução
    private static void breakCode(String code) throws IOException {
        /*
            R - opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
            I - opcode(6) | rs(5) | rt(5) | imm(16)
            J - opcode(6) | address(26)
         */

        switch (format) {
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
                executeTypeI();
                break;
        }
    }

    //Executa instruções do tipo R
    private static void executeTypeR() throws IOException {
        switch (funct) {
            case 32:
                add(rd, rs, rt);
                break;
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }

    //Executa instruções do tipo I
    private static void executeTypeI() throws IOException {
        switch (opcode) {
            case 35:
                general_register[rt] = loadWord(rs);
                break;
            case 43:
                storeWord(rs, rd);
                break;
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }

    private static int loadWord(int registro) throws IOException {
        mem = new RandomAccessFile("memoria.txt", "r");

        mem.seek((BITSDADOS + 2) * registro);
        registro = Integer.parseInt(mem.readLine(), 2);

        mem.close();
        return registro;
    }

//escreve no txt de memoria o registro
    private static void storeWord(int registro, int valor) throws IOException {
        if (valor > Math.pow(2, BITSDADOS) - 1) {
            System.out.println("Tamanho do espaço da memória excedido!!!");
            JOptionPane.showMessageDialog(null, "Tamanho da posição de memória excedido!!!");
        } else {
            memoria[registro] = valor;

            fw = new FileWriter("memoria.txt", false);
            bw = new BufferedWriter(fw);

            String info;

            for (int i = 0; i < memoria.length; i++) {
                info = Integer.toBinaryString(memoria[i]);

                //Correção para que a string tenha a quantidade definida de bits
                if (info.length() < BITSDADOS) {
                    for (int j = info.length(); j < BITSDADOS; j++) {
                        info = "0" + info;
                    }
                }
                bw.write(info);
                bw.newLine();
            }
            bw.close();
        }

    }

    private static void add(int rd, int rs, int rt) throws IOException {
        //rd = rs + rt;
        rs = general_register[rs];
        rt = general_register[rt];
        Interpretador.rd = rs + rt;
    }
}
